package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Lists;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.cyclops.cyclopscore.network.PacketBase;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.IntegratedTerminals;

import java.util.Iterator;
import java.util.List;

/**
 * Holds back storage terminal packets for a while, to imitate a server that is far away.
 *
 * This exists to be able to see the effect of the client-side prediction of terminal interactions
 * without needing a remote server, as everything is instant on an integrated server.
 * It does nothing unless {@link GeneralConfig#guiStorageSimulatedLatency} is set.
 *
 * Only the terminal's own click packet is held back, which is enough:
 * the server can not react before it arrives, so its answers, both the container slots
 * and the storage contents, are all delayed by the same amount.
 * Quick-moving *into* the storage is the exception, as that also sends a vanilla click packet
 * that this can not hold back, so that direction keeps answering immediately.
 *
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public final class TerminalStorageLatencySimulation {

    private static final List<DelayedPacket> QUEUE = Lists.newArrayList();

    static {
        NeoForge.EVENT_BUS.register(TerminalStorageLatencySimulation.class);
    }

    private TerminalStorageLatencySimulation() {
    }

    /**
     * Send the given packet to the server, after the simulated latency has passed.
     * @param packet A packet.
     */
    public static void sendToServer(PacketBase packet) {
        int latency = GeneralConfig.guiStorageSimulatedLatency;
        if (latency <= 0) {
            IntegratedTerminals._instance.getPacketHandler().sendToServer(packet);
        } else {
            synchronized (QUEUE) {
                QUEUE.add(new DelayedPacket(packet, System.currentTimeMillis() + latency));
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (QUEUE.isEmpty()) {
            return;
        }
        List<PacketBase> due = Lists.newArrayList();
        synchronized (QUEUE) {
            long time = System.currentTimeMillis();
            Iterator<DelayedPacket> it = QUEUE.iterator();
            while (it.hasNext()) {
                DelayedPacket delayedPacket = it.next();
                if (delayedPacket.sendTime() <= time) {
                    due.add(delayedPacket.packet());
                    it.remove();
                }
            }
        }
        for (PacketBase packet : due) {
            IntegratedTerminals._instance.getPacketHandler().sendToServer(packet);
        }
    }

    private record DelayedPacket(PacketBase packet, long sendTime) {
    }

}
