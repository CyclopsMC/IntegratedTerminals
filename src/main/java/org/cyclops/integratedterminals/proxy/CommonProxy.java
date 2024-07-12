package org.cyclops.integratedterminals.proxy;

import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.network.PacketHandler;
import org.cyclops.cyclopscore.proxy.CommonProxyComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.network.packet.*;

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
        packetHandler.register(TerminalStorageIngredientPartOpenPacket.ID, TerminalStorageIngredientPartOpenPacket.CODEC);
        packetHandler.register(TerminalStorageIngredientItemOpenPacket.ID, TerminalStorageIngredientItemOpenPacket.CODEC);
        packetHandler.register(TerminalStorageIngredientItemOpenGenericPacket.ID, TerminalStorageIngredientItemOpenGenericPacket.CODEC);
        packetHandler.register(TerminalStorageChangeGuiState.ID, TerminalStorageChangeGuiState.CODEC);
        packetHandler.register(TerminalStorageIngredientChangeEventPacket.ID, TerminalStorageIngredientChangeEventPacket.CODEC);
        packetHandler.register(TerminalStorageIngredientCraftingOptionsPacket.ID, TerminalStorageIngredientCraftingOptionsPacket.CODEC);
        packetHandler.register(TerminalStorageIngredientMaxQuantityPacket.ID, TerminalStorageIngredientMaxQuantityPacket.CODEC);
        packetHandler.register(TerminalStorageIngredientSlotClickPacket.ID, TerminalStorageIngredientSlotClickPacket.CODEC);
        packetHandler.register(TerminalStorageIngredientOpenCraftingPlanGuiPacket.ID, TerminalStorageIngredientOpenCraftingPlanGuiPacket.CODEC);
        packetHandler.register(TerminalStorageIngredientOpenCraftingJobAmountGuiPacket.ID, TerminalStorageIngredientOpenCraftingJobAmountGuiPacket.CODEC);
        packetHandler.register(TerminalStorageIngredientUpdateActiveStorageIngredientPacket.ID, TerminalStorageIngredientUpdateActiveStorageIngredientPacket.CODEC);
        packetHandler.register(TerminalStorageIngredientItemStackCraftingGridClear.ID, TerminalStorageIngredientItemStackCraftingGridClear.CODEC);
        packetHandler.register(TerminalStorageIngredientItemStackCraftingGridBalance.ID, TerminalStorageIngredientItemStackCraftingGridBalance.CODEC);
        packetHandler.register(TerminalStorageIngredientItemStackCraftingGridSetResult.ID, TerminalStorageIngredientItemStackCraftingGridSetResult.CODEC);
        packetHandler.register(TerminalStorageIngredientItemStackCraftingGridShiftClickOutput.ID, TerminalStorageIngredientItemStackCraftingGridShiftClickOutput.CODEC);
        packetHandler.register(TerminalStorageIngredientItemStackCraftingGridSetAutoRefill.ID, TerminalStorageIngredientItemStackCraftingGridSetAutoRefill.CODEC);
        packetHandler.register(OpenCraftingJobsPlanGuiPacket.ID, OpenCraftingJobsPlanGuiPacket.CODEC);
        packetHandler.register(OpenCraftingJobsGuiPacket.ID, OpenCraftingJobsGuiPacket.CODEC);
        packetHandler.register(CancelCraftingJobPacket.ID, CancelCraftingJobPacket.CODEC);

        IntegratedDynamics.clog("Registered packet handler.");
    }

}
