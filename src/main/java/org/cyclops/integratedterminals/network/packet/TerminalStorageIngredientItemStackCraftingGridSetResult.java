package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

/**
 * Packet for telling the server that the crafting grid must be cleared.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientItemStackCraftingGridSetResult extends PacketCodec {

    @CodecField
    private String tabId;
    @CodecField
    private ItemStack itemStack = ItemStack.EMPTY;

    public TerminalStorageIngredientItemStackCraftingGridSetResult() {

    }

    public TerminalStorageIngredientItemStackCraftingGridSetResult(String tabId, ItemStack itemStack) {
        this.tabId = tabId;
        this.itemStack = itemStack;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(World world, PlayerEntity player) {
        if(player.containerMenu instanceof ContainerTerminalStorageBase) {
            ContainerTerminalStorageBase container = ((ContainerTerminalStorageBase) player.containerMenu);
            TerminalStorageTabIngredientComponentItemStackCraftingCommon tabCommon =
                    (TerminalStorageTabIngredientComponentItemStackCraftingCommon) container.getTabCommon(tabId);
            tabCommon.getSlotCrafting().set(this.itemStack);
        }
    }

    @Override
    public void actionServer(World world, ServerPlayerEntity player) {

    }

}