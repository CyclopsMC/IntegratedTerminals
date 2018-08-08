package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;

/**
 * Packet for telling the server that the crafting grid must be cleared.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientItemStackCraftingGridClear extends PacketCodec {

    @CodecField
    private String tabId;
    @CodecField
    private int channel;
    @CodecField
    private boolean toStorage;

    public TerminalStorageIngredientItemStackCraftingGridClear() {

    }

    public TerminalStorageIngredientItemStackCraftingGridClear(String tabId, int channel, boolean toStorage) {
        this.tabId = tabId;
        this.channel = channel;
        this.toStorage = toStorage;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void actionClient(World world, EntityPlayer player) {

    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        if(player.openContainer instanceof ContainerTerminalStorage) {
            ContainerTerminalStorage container = ((ContainerTerminalStorage) player.openContainer);
            if (container.getTabServer(tabId) instanceof TerminalStorageTabIngredientComponentServer) {
                TerminalStorageTabIngredientComponentServer<ItemStack, Integer> tabServer =
                        (TerminalStorageTabIngredientComponentServer<ItemStack, Integer>) container.getTabServer(tabId);
                TerminalStorageTabIngredientComponentItemStackCraftingCommon tabCommon =
                        (TerminalStorageTabIngredientComponentItemStackCraftingCommon) container.getTabCommon(tabId);
                clearGrid(tabCommon, tabServer, channel, toStorage, player);
            }
        }
    }

    public static void clearGrid(TerminalStorageTabIngredientComponentItemStackCraftingCommon tabCommon,
                                 TerminalStorageTabIngredientComponentServer<ItemStack, Integer> tabServer,
                                 int channel, boolean toStorage, EntityPlayer player) {
        tabCommon.getInventoryCraftResult().setInventorySlotContents(0, ItemStack.EMPTY);
        InventoryCrafting inventoryCrafting = tabCommon.getInventoryCrafting();
        for (int i = 0; i < inventoryCrafting.getSizeInventory(); i++) {
            ItemStack itemStack = inventoryCrafting.removeStackFromSlot(i);
            if (!itemStack.isEmpty()) {
                if (toStorage) {
                    // To storage
                    ItemStack remainder = tabServer.getIngredientNetwork().getChannel(channel).insert(itemStack, false);
                    // Place the remainder back into the grid, so we don't loose it.
                    inventoryCrafting.setInventorySlotContents(i, remainder);
                } else {
                    // To player inventory
                    player.inventory.placeItemBackInInventory(player.world, itemStack);
                }
            }
        }
    }

}