package org.cyclops.integratedterminals.core.terminalstorage.metrics;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Server-side accounting of what one storage terminal open costs.
 *
 * Debug-only, active only when {@link org.cyclops.integratedterminals.GeneralConfig#debugTerminalOpenMetrics} is set.
 * One open spans all tabs of one container, from the first
 * {@link org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabServer#init()} call
 * until the last chunk has been encoded on the packet serializer thread.
 */
public final class ServerOpenMetrics {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String PACKETS_HEADER = "openId,tag,wallMs,player,tabId,channel,packetClass,"
            + "instanceCount,rawBytes,compressedBytes,thread,sinceOpenMs";
    private static final String OPENS_HEADER = "openId,tag,wallMs,player,channels,totalPackets,totalRawBytes,"
            + "totalCompressedBytes,ingredientPackets,ingredientRawBytes,ingredientCompressedBytes,"
            + "craftingPackets,craftingRawBytes,craftingCompressedBytes,ingredientInstances,craftingRecipes,"
            + "mainThreadMs,totalMs,measureOverheadMs,unwritableTransitions,minBytesBeforeUnwritable,maxOutboundBuffered";

    private static final AtomicInteger OPEN_COUNTER = new AtomicInteger();
    private static final ConcurrentHashMap<UUID, Open> ACTIVE = new ConcurrentHashMap<>();

    private ServerOpenMetrics() {
    }

    /**
     * Start a new open for the given player, replacing any open still in flight.
     */
    public static void begin(ServerPlayer player) {
        Open open = new Open(OPEN_COUNTER.incrementAndGet(), player.getGameProfile().name());
        open.mainThread = Thread.currentThread();
        ACTIVE.put(player.getUUID(), open);
    }

    @Nullable
    public static Open current(ServerPlayer player) {
        return ACTIVE.get(player.getUUID());
    }

    /**
     * Record that all tabs have finished their synchronous init on the main thread.
     * @param player The opening player.
     * @param mainThreadNanos Time spent on the main thread across all tabs.
     */
    public static void endMainThread(ServerPlayer player, long mainThreadNanos) {
        Open open = ACTIVE.get(player.getUUID());
        if (open != null) {
            open.mainThreadNanos = mainThreadNanos;
            open.mainThreadDone = true;
            open.finishIfIdle();
        }
    }

    /**
     * Wrap a packet serializer task so the open is only considered complete once it has run.
     */
    public static Runnable wrapAsync(@Nullable Open open, Runnable task) {
        if (open == null) {
            return task;
        }
        open.pending.incrementAndGet();
        return () -> {
            try {
                task.run();
            } finally {
                open.pending.decrementAndGet();
                open.finishIfIdle();
            }
        };
    }

    /**
     * One terminal open, aggregating every packet sent for it.
     */
    public static final class Open {

        private final int openId;
        private final String player;
        // Captured when the open starts: a row written later must not pick up the next scenario's tag.
        private final String tag = MetricsCsv.tag();
        private final long startNanos = System.nanoTime();
        private final long startWallMs = System.currentTimeMillis();
        private final Set<Integer> channels = ConcurrentHashMap.newKeySet();
        private final AtomicInteger pending = new AtomicInteger();
        private final AtomicLong measureOverheadNanos = new AtomicLong();
        private final AtomicLong mainThreadMeasureNanos = new AtomicLong();
        private volatile Thread mainThread;

        private final AtomicInteger ingredientPackets = new AtomicInteger();
        private final AtomicLong ingredientRaw = new AtomicLong();
        private final AtomicLong ingredientCompressed = new AtomicLong();
        private final AtomicLong ingredientInstances = new AtomicLong();
        private final AtomicInteger craftingPackets = new AtomicInteger();
        private final AtomicLong craftingRaw = new AtomicLong();
        private final AtomicLong craftingCompressed = new AtomicLong();
        private final AtomicLong craftingRecipes = new AtomicLong();

        private volatile boolean mainThreadDone;
        private volatile long mainThreadNanos;
        private volatile long lastPacketNanos;
        private volatile boolean written;
        private final AtomicInteger unwritableTransitions = new AtomicInteger();
        private final AtomicLong minBytesBeforeUnwritable = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxOutboundBuffered = new AtomicLong();
        private volatile boolean lastWritable = true;

        /**
         * Sample the outbound channel, so back-pressure during an open is visible.
         */
        public void sampleChannel(io.netty.channel.Channel channel) {
            if (channel == null) {
                return;
            }
            boolean writable = channel.isWritable();
            if (this.lastWritable && !writable) {
                this.unwritableTransitions.incrementAndGet();
            }
            this.lastWritable = writable;
            long before = channel.bytesBeforeUnwritable();
            this.minBytesBeforeUnwritable.accumulateAndGet(before, Math::min);
            io.netty.channel.ChannelOutboundBuffer buffer = channel.unsafe() == null
                    ? null : channel.unsafe().outboundBuffer();
            if (buffer != null) {
                this.maxOutboundBuffered.accumulateAndGet(buffer.totalPendingWriteBytes(), Math::max);
            }
        }

        private Open(int openId, String player) {
            this.openId = openId;
            this.player = player;
        }

        /**
         * Record one sent packet.
         * @param crafting True for crafting option packets, false for ingredient change packets.
         */
        public void recordPacket(String tabId, int channel, String packetClass, int count,
                                 PacketSizeMeasurer.Sizes sizes, boolean crafting) {
            this.channels.add(channel);
            this.lastPacketNanos = System.nanoTime();
            if (crafting) {
                this.craftingPackets.incrementAndGet();
                this.craftingRaw.addAndGet(sizes.raw());
                this.craftingCompressed.addAndGet(sizes.compressed());
                this.craftingRecipes.addAndGet(count);
            } else {
                this.ingredientPackets.incrementAndGet();
                this.ingredientRaw.addAndGet(sizes.raw());
                this.ingredientCompressed.addAndGet(sizes.compressed());
                this.ingredientInstances.addAndGet(count);
            }
            MetricsCsv.append("server_packets.csv", PACKETS_HEADER, String.join(",",
                    String.valueOf(this.openId),
                    this.tag,
                    String.valueOf(System.currentTimeMillis()),
                    this.player,
                    tabId,
                    String.valueOf(channel),
                    packetClass,
                    String.valueOf(count),
                    String.valueOf(sizes.raw()),
                    String.valueOf(sizes.compressed()),
                    Thread.currentThread().getName().replace(',', ';'),
                    ms(System.nanoTime() - this.startNanos)));
        }

        /**
         * Add time spent measuring packet sizes, so it can be subtracted from the reported cost.
         */
        public void addMeasureOverhead(long nanos) {
            this.measureOverheadNanos.addAndGet(nanos);
            if (Thread.currentThread() == this.mainThread) {
                this.mainThreadMeasureNanos.addAndGet(nanos);
            }
        }

        void finishIfIdle() {
            if (!this.mainThreadDone || this.pending.get() > 0 || this.written) {
                return;
            }
            synchronized (this) {
                if (this.written) {
                    return;
                }
                this.written = true;
            }
            long totalNanos = Math.max(this.mainThreadNanos - this.mainThreadMeasureNanos.get() + this.measureOverheadNanos.get(),
                    (this.lastPacketNanos == 0 ? System.nanoTime() : this.lastPacketNanos) - this.startNanos);
            int totalPackets = this.ingredientPackets.get() + this.craftingPackets.get();
            long totalRaw = this.ingredientRaw.get() + this.craftingRaw.get();
            long totalCompressed = this.ingredientCompressed.get() + this.craftingCompressed.get();
            MetricsCsv.append("server_opens.csv", OPENS_HEADER, String.join(",",
                    String.valueOf(this.openId),
                    this.tag,
                    String.valueOf(this.startWallMs),
                    this.player,
                    String.valueOf(this.channels.size()),
                    String.valueOf(totalPackets),
                    String.valueOf(totalRaw),
                    String.valueOf(totalCompressed),
                    String.valueOf(this.ingredientPackets.get()),
                    String.valueOf(this.ingredientRaw.get()),
                    String.valueOf(this.ingredientCompressed.get()),
                    String.valueOf(this.craftingPackets.get()),
                    String.valueOf(this.craftingRaw.get()),
                    String.valueOf(this.craftingCompressed.get()),
                    String.valueOf(this.ingredientInstances.get()),
                    String.valueOf(this.craftingRecipes.get()),
                    ms(this.mainThreadNanos - this.mainThreadMeasureNanos.get()),
                    ms(totalNanos - this.measureOverheadNanos.get()),
                    ms(this.measureOverheadNanos.get()),
                    String.valueOf(this.unwritableTransitions.get()),
                    String.valueOf(this.minBytesBeforeUnwritable.get() == Long.MAX_VALUE
                            ? -1 : this.minBytesBeforeUnwritable.get()),
                    String.valueOf(this.maxOutboundBuffered.get())));
            LOGGER.info("Terminal open #{} [{}]: {} packets, {} raw bytes, {} compressed bytes, {} ms main thread, {} ms total",
                    this.openId, this.tag, totalPackets, totalRaw, totalCompressed,
                    ms(this.mainThreadNanos - this.mainThreadMeasureNanos.get()),
                    ms(totalNanos - this.measureOverheadNanos.get()));
        }

        private static String ms(long nanos) {
            return String.format(java.util.Locale.ROOT, "%.3f", nanos / 1_000_000D);
        }
    }
}
