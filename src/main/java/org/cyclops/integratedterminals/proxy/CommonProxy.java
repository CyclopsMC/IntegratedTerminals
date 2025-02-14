package org.cyclops.integratedterminals.proxy;

import org.cyclops.cyclopscore.init.ModBaseNeoForge;
import org.cyclops.cyclopscore.network.IPacketHandler;
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
    public ModBaseNeoForge<IntegratedTerminals> getMod() {
        return IntegratedTerminals._instance;
    }

    @Override
    public void registerPackets(IPacketHandler packetHandler) {
        super.registerPackets(packetHandler);

        // Register packets.
        packetHandler.register(TerminalStorageIngredientPartOpenPacket.class, TerminalStorageIngredientPartOpenPacket.ID, TerminalStorageIngredientPartOpenPacket.CODEC);
        packetHandler.register(TerminalStorageIngredientItemOpenPacket.class, TerminalStorageIngredientItemOpenPacket.ID, TerminalStorageIngredientItemOpenPacket.CODEC);
        packetHandler.register(TerminalStorageIngredientItemOpenGenericPacket.class, TerminalStorageIngredientItemOpenGenericPacket.ID, TerminalStorageIngredientItemOpenGenericPacket.CODEC);
        packetHandler.register(TerminalStorageChangeGuiState.class, TerminalStorageChangeGuiState.ID, TerminalStorageChangeGuiState.CODEC);
        packetHandler.register(TerminalStorageIngredientChangeEventPacket.class, TerminalStorageIngredientChangeEventPacket.ID, TerminalStorageIngredientChangeEventPacket.CODEC);
        packetHandler.register(TerminalStorageIngredientCraftingOptionsPacket.class, TerminalStorageIngredientCraftingOptionsPacket.ID, TerminalStorageIngredientCraftingOptionsPacket.CODEC);
        packetHandler.register(TerminalStorageIngredientMaxQuantityPacket.class, TerminalStorageIngredientMaxQuantityPacket.ID, TerminalStorageIngredientMaxQuantityPacket.CODEC);
        packetHandler.register((Class) TerminalStorageIngredientSlotClickPacket.class, TerminalStorageIngredientSlotClickPacket.ID, TerminalStorageIngredientSlotClickPacket.CODEC);
        packetHandler.register((Class) TerminalStorageIngredientOpenCraftingPlanGuiPacket.class, TerminalStorageIngredientOpenCraftingPlanGuiPacket.ID, TerminalStorageIngredientOpenCraftingPlanGuiPacket.CODEC);
        packetHandler.register((Class) TerminalStorageIngredientOpenCraftingJobAmountGuiPacket.class, TerminalStorageIngredientOpenCraftingJobAmountGuiPacket.ID, TerminalStorageIngredientOpenCraftingJobAmountGuiPacket.CODEC);
        packetHandler.register((Class) TerminalStorageIngredientUpdateActiveStorageIngredientPacket.class, TerminalStorageIngredientUpdateActiveStorageIngredientPacket.ID, TerminalStorageIngredientUpdateActiveStorageIngredientPacket.CODEC);
        packetHandler.register(TerminalStorageIngredientItemStackCraftingGridClear.class, TerminalStorageIngredientItemStackCraftingGridClear.ID, TerminalStorageIngredientItemStackCraftingGridClear.CODEC);
        packetHandler.register(TerminalStorageIngredientItemStackCraftingGridBalance.class, TerminalStorageIngredientItemStackCraftingGridBalance.ID, TerminalStorageIngredientItemStackCraftingGridBalance.CODEC);
        packetHandler.register(TerminalStorageIngredientItemStackCraftingGridSetResult.class, TerminalStorageIngredientItemStackCraftingGridSetResult.ID, TerminalStorageIngredientItemStackCraftingGridSetResult.CODEC);
        packetHandler.register(TerminalStorageIngredientItemStackCraftingGridShiftClickOutput.class, TerminalStorageIngredientItemStackCraftingGridShiftClickOutput.ID, TerminalStorageIngredientItemStackCraftingGridShiftClickOutput.CODEC);
        packetHandler.register(TerminalStorageIngredientItemStackCraftingGridSetAutoRefill.class, TerminalStorageIngredientItemStackCraftingGridSetAutoRefill.ID, TerminalStorageIngredientItemStackCraftingGridSetAutoRefill.CODEC);
        packetHandler.register(OpenCraftingJobsPlanGuiPacket.class, OpenCraftingJobsPlanGuiPacket.ID, OpenCraftingJobsPlanGuiPacket.CODEC);
        packetHandler.register(OpenCraftingJobsGuiPacket.class, OpenCraftingJobsGuiPacket.ID, OpenCraftingJobsGuiPacket.CODEC);
        packetHandler.register(CancelCraftingJobPacket.class, CancelCraftingJobPacket.ID, CancelCraftingJobPacket.CODEC);

        IntegratedDynamics.clog("Registered packet handler.");
    }

}
