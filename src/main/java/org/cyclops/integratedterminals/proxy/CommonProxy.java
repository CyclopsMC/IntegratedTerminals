package org.cyclops.integratedterminals.proxy;

import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.network.PacketHandler;
import org.cyclops.cyclopscore.proxy.CommonProxyComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.network.packet.*;
import org.cyclops.integratedterminals.proxy.guiprovider.GuiProviders;

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
        packetHandler.register(TerminalStorageIngredientOpenPacket.class);
        packetHandler.register(TerminalStorageIngredientChangeEventPacket.class);
        packetHandler.register(TerminalStorageIngredientCraftingOptionsPacket.class);
        packetHandler.register(TerminalStorageIngredientMaxQuantityPacket.class);
        packetHandler.register(TerminalStorageIngredientSlotClickPacket.class);
        packetHandler.register(TerminalStorageIngredientOpenCraftingPlanGuiPacket.class);
        packetHandler.register(TerminalStorageIngredientOpenCraftingJobAmountGuiPacket.class);
        packetHandler.register(TerminalStorageIngredientOpenCraftingPlanGuiPacket.class);
        packetHandler.register(TerminalStorageIngredientUpdateActiveStorageIngredientPacket.class);
        packetHandler.register(TerminalStorageIngredientItemStackCraftingGridClear.class);
        packetHandler.register(TerminalStorageIngredientItemStackCraftingGridBalance.class);
        packetHandler.register(TerminalStorageIngredientItemStackCraftingGridShiftClickOutput.class);
        packetHandler.register(TerminalStorageIngredientItemStackCraftingGridSetAutoRefill.class);
        packetHandler.register(OpenCraftingJobsPlanGuiPacket.class);
        packetHandler.register(OpenCraftingJobsGuiPacket.class);
        packetHandler.register(CancelCraftingJobPacket.class);

        IntegratedDynamics.clog("Registered packet handler.");
    }

    @Override
    public void registerRenderers() {
        super.registerRenderers();

        GuiProviders.register();
    }
}
