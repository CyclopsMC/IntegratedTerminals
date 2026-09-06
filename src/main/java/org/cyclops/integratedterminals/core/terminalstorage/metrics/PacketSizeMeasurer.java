package org.cyclops.integratedterminals.core.terminalstorage.metrics;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.zip.Deflater;

/**
 * Measures the encoded size of a packet payload.
 *
 * Debug-only helper for measuring the cost of opening a storage terminal.
 */
public final class PacketSizeMeasurer {

    private static final ThreadLocal<byte[]> DEFLATE_BUFFER = ThreadLocal.withInitial(() -> new byte[1 << 16]);

    private PacketSizeMeasurer() {
    }

    /**
     * Encode the given packet into a scratch buffer to determine its payload size.
     *
     * This is the size of the custom payload only: the packet id and the vanilla frame add a small
     * constant per packet on top of this.
     *
     * @param codec The packet's stream codec.
     * @param packet The packet.
     * @param registryAccess The registry access to encode against.
     * @return The raw and Deflater-compressed payload sizes, in bytes.
     */
    public static Sizes measure(StreamCodec<RegistryFriendlyByteBuf, ?> codec, Object packet,
                                RegistryAccess registryAccess) {
        ByteBuf backing = Unpooled.buffer();
        try {
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(backing, registryAccess);
            ((StreamCodec<RegistryFriendlyByteBuf, Object>) codec).encode(buffer, packet);
            int raw = buffer.readableBytes();
            byte[] bytes = new byte[raw];
            buffer.getBytes(buffer.readerIndex(), bytes);
            return new Sizes(raw, deflatedSize(bytes));
        } finally {
            backing.release();
        }
    }

    /**
     * Compressed size under the same algorithm dedicated servers apply to packets over
     * network-compression-threshold (256 bytes by default).
     */
    private static int deflatedSize(byte[] input) {
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
        try {
            deflater.setInput(input);
            deflater.finish();
            byte[] scratch = DEFLATE_BUFFER.get();
            int total = 0;
            while (!deflater.finished()) {
                total += deflater.deflate(scratch);
            }
            return total;
        } finally {
            deflater.end();
        }
    }

    /**
     * Raw and compressed payload sizes, in bytes.
     */
    public record Sizes(int raw, int compressed) {
    }
}
