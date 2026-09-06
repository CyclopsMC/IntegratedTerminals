package org.cyclops.integratedterminals.core.terminalstorage.metrics;

import io.netty.channel.Channel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

/**
 * Reads the outbound Netty channel of a player, to see whether the server is buffering
 * the terminal contents rather than getting them onto the wire.
 *
 * Debug-only. Uses reflection because the connection and its channel are not public API.
 */
public final class ChannelWritability {

    private static Field connectionField;
    private static Field channelField;
    private static boolean resolved;
    private static boolean available;

    private ChannelWritability() {
    }

    @Nullable
    public static Channel channelOf(ServerPlayer player) {
        if (!resolved) {
            resolve(player);
        }
        if (!available) {
            return null;
        }
        try {
            Object connection = connectionField.get(player.connection);
            return (Channel) channelField.get(connection);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static synchronized void resolve(ServerPlayer player) {
        if (resolved) {
            return;
        }
        resolved = true;
        try {
            Class<?> listener = player.connection.getClass();
            while (listener != null && connectionField == null) {
                for (Field field : listener.getDeclaredFields()) {
                    if (field.getType().getName().equals("net.minecraft.network.Connection")) {
                        field.setAccessible(true);
                        connectionField = field;
                        break;
                    }
                }
                listener = listener.getSuperclass();
            }
            if (connectionField != null) {
                Object connection = connectionField.get(player.connection);
                Field field = connection.getClass().getDeclaredField("channel");
                field.setAccessible(true);
                channelField = field;
                available = true;
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            available = false;
        }
    }
}
