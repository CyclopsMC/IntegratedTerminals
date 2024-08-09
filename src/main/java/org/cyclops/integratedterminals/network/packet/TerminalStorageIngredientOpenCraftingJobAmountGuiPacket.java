package org.cyclops.integratedterminals.network.packet;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;

/**
 * Packet for opening the crafting job amount gui.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientOpenCraftingJobAmountGuiPacket<T, M, L> extends TerminalStorageIngredientCraftingOptionDataPacketAbstract<T, M, L, TerminalStorageIngredientOpenCraftingJobAmountGuiPacket<T, M, L>> {

    public static final Type<TerminalStorageIngredientOpenCraftingJobAmountGuiPacket<?, ?, ?>> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "terminal_storage_ingredient_open_crafting_job_amount_gui"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalStorageIngredientOpenCraftingJobAmountGuiPacket<?, ?, ?>> CODEC = (StreamCodec) getCodec(TerminalStorageIngredientOpenCraftingJobAmountGuiPacket::new);

    public TerminalStorageIngredientOpenCraftingJobAmountGuiPacket() {
        super((Type) ID);
    }

    public TerminalStorageIngredientOpenCraftingJobAmountGuiPacket(HolderLookup.Provider lookupProvider, CraftingOptionGuiData<T, M, L> craftingOptionData) {
        super((Type) ID, lookupProvider, craftingOptionData);
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
        CraftingOptionGuiData<T, M, L> craftingJobGuiData = getCraftingOptionData(world.registryAccess());
        craftingJobGuiData.getLocation().openContainerCraftingOptionAmount(craftingJobGuiData, world, player);
    }

}
