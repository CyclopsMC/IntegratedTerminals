package org.cyclops.integratedterminals.core.terminalstorage.metrics;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Client-side accounting of what one storage terminal open costs.
 *
 * Debug-only, active only when {@link org.cyclops.integratedterminals.GeneralConfig#debugTerminalOpenMetrics} is set.
 * An open starts when the client container is constructed and is considered complete once no further
 * initial packet has been applied for {@link #QUIET_PERIOD_MS}.
 */
public final class ClientOpenMetrics {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * How long without a new applied packet marks the initial burst as finished.
     */
    private static final long QUIET_PERIOD_MS = 500;

    private static final String PACKETS_HEADER = "openId,tag,wallMs,packetClass,instanceCount,rawBytes,"
            + "compressedBytes,deserializeMs,applyMs,sinceOpenMs";
    private static final String OPENS_HEADER = "openId,tag,openWallMs,packets,rawBytes,compressedBytes,instances,"
            + "firstContentMs,completeMs,fillGapMs,deserializeMsTotal,applyMsTotal,viewRebuildMsTotal,"
            + "longestStallMs,measureOverheadMs";

    private static final AtomicInteger OPEN_COUNTER = new AtomicInteger();

    @Nullable
    private static volatile Open current;

    private ClientOpenMetrics() {
    }

    /**
     * Start a new open, finalising any previous one immediately.
     */
    public static synchronized void beginOpen() {
        Open previous = current;
        if (previous != null) {
            previous.finish();
        }
        current = new Open(OPEN_COUNTER.incrementAndGet());
        startWatchdog();
    }

    @Nullable
    public static Open current() {
        return current;
    }

    private static boolean watchdogStarted;

    /**
     * A daemon thread finalises an open once the quiet period has elapsed, since nothing tells the
     * client that the initial burst is over.
     */
    private static synchronized void startWatchdog() {
        if (watchdogStarted) {
            return;
        }
        watchdogStarted = true;
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                Open open = current;
                // Wait for an applied packet, not just a deserialized one: a render thread busy
                // with a large burst would otherwise let the open finish with nothing recorded.
                if (open != null && open.applies.get() > 0
                        && System.currentTimeMillis() - open.lastActivityWallMs >= QUIET_PERIOD_MS) {
                    open.finish();
                }
            }
        }, "integratedterminals-open-metrics");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * One terminal open on the client.
     */
    public static final class Open {

        private final int openId;
        // Captured when the open starts, for the same reason as on the server.
        private final String tag = MetricsCsv.tag();
        private final long startWallMs = System.currentTimeMillis();
        private final long startNanos = System.nanoTime();

        private final AtomicInteger packets = new AtomicInteger();
        private final AtomicInteger applies = new AtomicInteger();
        private long rawBytes;
        private long compressedBytes;
        private long instances;
        private long deserializeNanos;
        private long applyNanos;
        private long viewRebuildNanos;
        private long measureOverheadNanos;
        private long longestStallNanos;
        private long firstContentNanos = -1;
        private volatile long lastApplyWallMs = System.currentTimeMillis();
        // Touched by both halves, so a slow render thread does not look like a finished open.
        private volatile long lastActivityWallMs = System.currentTimeMillis();
        private volatile boolean written;

        private Open(int openId) {
            this.openId = openId;
        }

        /**
         * Record the deserialization half of one packet, which runs off the render thread.
         * @param measureNanos Time spent re-encoding the packet to size it, excluded from reported costs.
         */
        public synchronized void recordDeserialize(String packetClass, int count, PacketSizeMeasurer.Sizes sizes,
                                                   long deserializeNanos, long measureNanos) {
            this.packets.incrementAndGet();
            this.lastActivityWallMs = System.currentTimeMillis();
            this.rawBytes += sizes.raw();
            this.compressedBytes += sizes.compressed();
            this.instances += count;
            this.deserializeNanos += deserializeNanos;
            this.measureOverheadNanos += measureNanos;
            this.pendingClass = packetClass;
            this.pendingCount = count;
            this.pendingSizes = sizes;
            this.pendingDeserializeNanos = deserializeNanos;
        }

        private String pendingClass = "";
        private int pendingCount;
        private PacketSizeMeasurer.Sizes pendingSizes = new PacketSizeMeasurer.Sizes(0, 0);
        private long pendingDeserializeNanos;

        /**
         * Record the render-thread half of one packet, which applies the change to the client views.
         */
        public synchronized void recordApply(long applyNanos) {
            this.applies.incrementAndGet();
            this.applyNanos += applyNanos;
            this.longestStallNanos = Math.max(this.longestStallNanos, applyNanos);
            if (this.firstContentNanos < 0) {
                this.firstContentNanos = System.nanoTime() - this.startNanos;
            }
            this.lastApplyWallMs = System.currentTimeMillis();
            this.lastActivityWallMs = this.lastApplyWallMs;
            MetricsCsv.append("client_packets.csv", PACKETS_HEADER, String.join(",",
                    String.valueOf(this.openId),
                    this.tag,
                    String.valueOf(System.currentTimeMillis()),
                    this.pendingClass,
                    String.valueOf(this.pendingCount),
                    String.valueOf(this.pendingSizes.raw()),
                    String.valueOf(this.pendingSizes.compressed()),
                    ms(this.pendingDeserializeNanos),
                    ms(applyNanos),
                    ms(System.nanoTime() - this.startNanos)));
        }

        /**
         * Record a rebuild of the sorted and filtered view, which happens lazily on render.
         */
        public synchronized void recordViewRebuild(long nanos) {
            this.viewRebuildNanos += nanos;
        }

        synchronized void finish() {
            if (this.written || this.applies.get() == 0) {
                return;
            }
            this.written = true;
            long completeNanos = (this.lastApplyWallMs - this.startWallMs) * 1_000_000L;
            MetricsCsv.append("client_opens.csv", OPENS_HEADER, String.join(",",
                    String.valueOf(this.openId),
                    this.tag,
                    String.valueOf(this.startWallMs),
                    String.valueOf(this.packets.get()),
                    String.valueOf(this.rawBytes),
                    String.valueOf(this.compressedBytes),
                    String.valueOf(this.instances),
                    ms(this.firstContentNanos),
                    ms(completeNanos),
                    ms(completeNanos - this.firstContentNanos),
                    ms(this.deserializeNanos),
                    ms(this.applyNanos),
                    ms(this.viewRebuildNanos),
                    ms(this.longestStallNanos),
                    ms(this.measureOverheadNanos)));
            LOGGER.info("Client terminal open #{} [{}]: {} packets, {} raw bytes, first content {} ms, complete {} ms",
                    this.openId, this.tag, this.packets.get(), this.rawBytes,
                    ms(this.firstContentNanos), ms(completeNanos));
        }

        private static String ms(long nanos) {
            return String.format(Locale.ROOT, "%.3f", nanos / 1_000_000D);
        }
    }
}
