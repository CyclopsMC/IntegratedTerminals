package org.cyclops.integratedterminals.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.NonNullList;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageTabIngredientComponentCommontemStackCrafting;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageTabIngredientComponentServer;

/**
 * A crafting slot that will automatically auto-refill from the storage (if enabled).
 * @author rubensworks
 */
public class SlotCraftingAutoRefill extends SlotCrafting {

    private final InventoryCrafting inventoryCrafting;
    private final TerminalStorageTabIngredientComponentCommontemStackCrafting tabCommon;
    private final TerminalStorageTabIngredientComponentServer<ItemStack, Integer> tabServer;
    private final int channel;

    public SlotCraftingAutoRefill(EntityPlayer player, InventoryCrafting inventoryCrafting,
                                  IInventory inventoryIn, int slotIndex, int xPosition, int yPosition,
                                  TerminalStorageTabIngredientComponentCommontemStackCrafting tabCommon,
                                  TerminalStorageTabIngredientComponentServer<ItemStack, Integer> tabServer,
                                  int channel) {
        super(player, inventoryCrafting, inventoryIn, slotIndex, xPosition, yPosition);
        this.inventoryCrafting = inventoryCrafting;
        this.tabCommon = tabCommon;
        this.tabServer = tabServer;
        this.channel = channel;
    }

    @Override
    public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
        if (!thePlayer.world.isRemote && tabCommon.isAutoRefill()) {
            NonNullList<ItemStack> beforeCraft = inventoryToList(inventoryCrafting, true);
            ItemStack taken = super.onTake(thePlayer, stack);
            NonNullList<ItemStack> afterCraft = inventoryToList(inventoryCrafting, false);

            NonNullList<ItemStack> removed = getRemoved(beforeCraft, afterCraft);
            // Attempt to get and re-add removed stacks from storage
            IIngredientComponentStorage<ItemStack, Integer> storage = tabServer.getIngredientNetwork()
                    .getChannel(this.channel);
            for (int i = 0; i < removed.size(); i++) {
                ItemStack removedStack = removed.get(i);
                ItemStack extracted = storage.extract(removedStack,
                        storage.getComponent().getMatcher().getExactMatchCondition(), false);
                ItemStack existingStack = inventoryCrafting.getStackInSlot(i);
                existingStack.grow(extracted.getCount());
                inventoryCrafting.setInventorySlotContents(i, existingStack);
                ((EntityPlayerMP) thePlayer).connection.sendPacket(
                        new SPacketSetSlot(thePlayer.openContainer.windowId, i + this.slotNumber + 1,
                                inventoryCrafting.getStackInSlot(i)));
            }
            return taken;
        }
        return super.onTake(thePlayer, stack);
    }

    public static NonNullList<ItemStack> inventoryToList(IInventory inventory, boolean copy) {
        NonNullList<ItemStack> list = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            list.set(i, copy ? inventory.getStackInSlot(i).copy() : inventory.getStackInSlot(i));
        }
        return list;
    }

    public static NonNullList<ItemStack> getRemoved(NonNullList<ItemStack> before, NonNullList<ItemStack> after) {
        NonNullList<ItemStack> removed = NonNullList.withSize(before.size(), ItemStack.EMPTY);
        for (int i = 0; i < before.size(); i++) {
            ItemStack beforeStack = before.get(i);
            ItemStack afterStack = after.get(i);
            if (beforeStack.getCount() > afterStack.getCount()) {
                beforeStack.setCount(beforeStack.getCount() - afterStack.getCount());
                removed.set(i, beforeStack);
            }
        }
        return removed;
    }
}
