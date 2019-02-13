package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;

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
    @SideOnly(Side.CLIENT)
    public void actionClient(World world, EntityPlayer player) {
        if(player.openContainer instanceof ContainerTerminalStorage) {
            ContainerTerminalStorage container = ((ContainerTerminalStorage) player.openContainer);
            TerminalStorageTabIngredientComponentItemStackCraftingCommon tabCommon =
                    (TerminalStorageTabIngredientComponentItemStackCraftingCommon) container.getTabCommon(tabId);
            tabCommon.getSlotCrafting().putStack(this.itemStack);
        }
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {

    }

}