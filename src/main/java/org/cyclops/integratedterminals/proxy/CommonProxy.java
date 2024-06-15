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
        packetHandler.register(TerminalStorageIngredientPartOpenPacket.ID, TerminalStorageIngredientPartOpenPacket::new);
        packetHandler.register(TerminalStorageIngredientItemOpenPacket.ID, TerminalStorageIngredientItemOpenPacket::new);
        packetHandler.register(TerminalStorageIngredientItemOpenGenericPacket.ID, TerminalStorageIngredientItemOpenGenericPacket::new);
        packetHandler.register(TerminalStorageChangeGuiState.ID, TerminalStorageChangeGuiState::new);
        packetHandler.register(TerminalStorageIngredientChangeEventPacket.ID, TerminalStorageIngredientChangeEventPacket::new);
        packetHandler.register(TerminalStorageIngredientCraftingOptionsPacket.ID, TerminalStorageIngredientCraftingOptionsPacket::new);
        packetHandler.register(TerminalStorageIngredientMaxQuantityPacket.ID, TerminalStorageIngredientMaxQuantityPacket::new);
        packetHandler.register(TerminalStorageIngredientSlotClickPacket.ID, TerminalStorageIngredientSlotClickPacket::new);
        packetHandler.register(TerminalStorageIngredientOpenCraftingPlanGuiPacket.ID, TerminalStorageIngredientOpenCraftingPlanGuiPacket::new);
        packetHandler.register(TerminalStorageIngredientOpenCraftingJobAmountGuiPacket.ID, TerminalStorageIngredientOpenCraftingJobAmountGuiPacket::new);
        packetHandler.register(TerminalStorageIngredientUpdateActiveStorageIngredientPacket.ID, TerminalStorageIngredientUpdateActiveStorageIngredientPacket::new);
        packetHandler.register(TerminalStorageIngredientItemStackCraftingGridClear.ID, TerminalStorageIngredientItemStackCraftingGridClear::new);
        packetHandler.register(TerminalStorageIngredientItemStackCraftingGridBalance.ID, TerminalStorageIngredientItemStackCraftingGridBalance::new);
        packetHandler.register(TerminalStorageIngredientItemStackCraftingGridSetResult.ID, TerminalStorageIngredientItemStackCraftingGridSetResult::new);
        packetHandler.register(TerminalStorageIngredientItemStackCraftingGridShiftClickOutput.ID, TerminalStorageIngredientItemStackCraftingGridShiftClickOutput::new);
        packetHandler.register(TerminalStorageIngredientItemStackCraftingGridSetAutoRefill.ID, TerminalStorageIngredientItemStackCraftingGridSetAutoRefill::new);
        packetHandler.register(OpenCraftingJobsPlanGuiPacket.ID, OpenCraftingJobsPlanGuiPacket::new);
        packetHandler.register(OpenCraftingJobsGuiPacket.ID, OpenCraftingJobsGuiPacket::new);
        packetHandler.register(CancelCraftingJobPacket.ID, CancelCraftingJobPacket::new);

        IntegratedDynamics.clog("Registered packet handler.");
    }

}
