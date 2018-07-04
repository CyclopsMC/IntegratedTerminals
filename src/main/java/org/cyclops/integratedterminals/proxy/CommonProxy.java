package org.cyclops.integratedterminals.proxy;

import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.network.PacketHandler;
import org.cyclops.cyclopscore.proxy.CommonProxyComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientChangeEventPacket;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientMaxQuantityPacket;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientSlotClickPacket;

/**
 * Proxy for server and client side.
 * @author rubensworks
 *
 */
public class CommonProxy extends CommonProxyComponent {

    @Override
    public ModBase getMod() {
        return IntegratedTerminals._instance;
    }

    @Override
    public void registerPacketHandlers(PacketHandler packetHandler) {
        super.registerPacketHandlers(packetHandler);

        // Register packets.
        packetHandler.register(TerminalStorageIngredientChangeEventPacket.class);
        packetHandler.register(TerminalStorageIngredientMaxQuantityPacket.class);
        packetHandler.register(TerminalStorageIngredientSlotClickPacket.class);

        IntegratedDynamics.clog("Registered packet handler.");
    }

}
