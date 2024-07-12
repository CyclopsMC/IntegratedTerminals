package org.cyclops.integratedterminals.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

/**
 * Packet for telling the server that the crafting grid must be cleared.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientItemStackCraftingGridSetResult extends PacketCodec<TerminalStorageIngredientItemStackCraftingGridSetResult> {

    public static final Type<TerminalStorageIngredientItemStackCraftingGridSetResult> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "terminal_storage_ingredient_itemstack_crafting_grid_set_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalStorageIngredientItemStackCraftingGridSetResult> CODEC = getCodec(TerminalStorageIngredientItemStackCraftingGridSetResult::new);

    @CodecField
    private String tabId;
    @CodecField
    private ItemStack itemStack = ItemStack.EMPTY;

    public TerminalStorageIngredientItemStackCraftingGridSetResult() {
        super(ID);
    }

    public TerminalStorageIngredientItemStackCraftingGridSetResult(String tabId, ItemStack itemStack) {
        super(ID);
        this.tabId = tabId;
        this.itemStack = itemStack;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(Level world, Player player) {
        if(player.containerMenu instanceof ContainerTerminalStorageBase) {
            ContainerTerminalStorageBase container = ((ContainerTerminalStorageBase) player.containerMenu);
            TerminalStorageTabIngredientComponentItemStackCraftingCommon tabCommon =
                    (TerminalStorageTabIngredientComponentItemStackCraftingCommon) container.getTabCommon(tabId);
            tabCommon.getSlotCrafting().set(this.itemStack);
        }
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {

    }

}
