package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;

/**
 * Packet for opening the crafting job amount gui.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientOpenCraftingJobAmountGuiPacket<T, M, L> extends TerminalStorageIngredientCraftingOptionDataPacketAbstract<T, M, L> {

    public TerminalStorageIngredientOpenCraftingJobAmountGuiPacket() {

    }

    public TerminalStorageIngredientOpenCraftingJobAmountGuiPacket(CraftingOptionGuiData<T, M, L> craftingOptionData) {
        super(craftingOptionData);
    }

    @Override
    public void actionServer(World world, ServerPlayerEntity player) {
        CraftingOptionGuiData<T, M, L> craftingJobGuiData = getCraftingOptionData();
        craftingJobGuiData.getLocation().openContainerCraftingOptionAmount(craftingJobGuiData, world, player);
    }

}