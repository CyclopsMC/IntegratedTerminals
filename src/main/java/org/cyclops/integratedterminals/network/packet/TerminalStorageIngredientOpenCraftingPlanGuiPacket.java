package org.cyclops.integratedterminals.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
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
public class TerminalStorageIngredientOpenCraftingPlanGuiPacket<T, M, L> extends TerminalStorageIngredientCraftingOptionDataPacketAbstract<T, M, L, TerminalStorageIngredientOpenCraftingPlanGuiPacket<T, M, L>> {

    public static final Type<TerminalStorageIngredientOpenCraftingPlanGuiPacket<?, ?, ?>> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "terminal_storage_ingredient_open_crafting_plan_gui"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalStorageIngredientOpenCraftingPlanGuiPacket<?, ?, ?>> CODEC = (StreamCodec) getCodec(TerminalStorageIngredientOpenCraftingPlanGuiPacket::new);

    public TerminalStorageIngredientOpenCraftingPlanGuiPacket() {
        super((Type) ID);
    }

    public TerminalStorageIngredientOpenCraftingPlanGuiPacket(CraftingOptionGuiData<T, M, L> craftingOptionData) {
        super((Type) ID, craftingOptionData);
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
        CraftingOptionGuiData<T, M, L> craftingJobGuiData = getCraftingOptionData();
        craftingJobGuiData.getLocation().openContainerCraftingPlan(craftingJobGuiData, world, player);
    }

}
