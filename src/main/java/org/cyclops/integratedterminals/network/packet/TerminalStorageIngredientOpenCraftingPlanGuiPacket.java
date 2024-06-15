package org.cyclops.integratedterminals.network.packet;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;

/**
 * Packet for opening the crafting plan gui.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientOpenCraftingPlanGuiPacket<T, M, L> extends TerminalStorageIngredientCraftingOptionDataPacketAbstract<T, M, L> {

    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "terminal_storage_ingredient_open_crafting_plan_gui");

    public TerminalStorageIngredientOpenCraftingPlanGuiPacket() {
        super(ID);
    }

    public TerminalStorageIngredientOpenCraftingPlanGuiPacket(CraftingOptionGuiData<T, M, L> craftingOptionData) {
        super(ID, craftingOptionData);
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
        CraftingOptionGuiData<T, M, L> craftingJobGuiData = getCraftingOptionData();
        craftingJobGuiData.getLocation().openContainerCraftingPlan(craftingJobGuiData, world, player);
    }

}
