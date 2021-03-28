package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;

/**
 * Packet for opening the crafting plan gui.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientOpenCraftingPlanGuiPacket<T, M, L> extends TerminalStorageIngredientCraftingOptionDataPacketAbstract<T, M, L> {

    public TerminalStorageIngredientOpenCraftingPlanGuiPacket() {

    }

    public TerminalStorageIngredientOpenCraftingPlanGuiPacket(CraftingOptionGuiData<T, M, L> craftingOptionData) {
        super(craftingOptionData);
    }

    @Override
    public void actionServer(World world, ServerPlayerEntity player) {
        CraftingOptionGuiData<T, M, L> craftingJobGuiData = getCraftingOptionData();
        craftingJobGuiData.getLocation().openContainerCraftingPlan(craftingJobGuiData, world, player);
    }

}