package org.cyclops.integratedterminals.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.commoncapabilities.ingredient.storage.IngredientComponentStorageWrapperHandlerItemStack;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;
import org.cyclops.integratedterminals.core.terminalstorage.button.TerminalButtonItemStackCraftingGridAutoRefill;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;

/**
 * A crafting slot that will automatically auto-refill from the storage (if enabled).
 * @author rubensworks
 */
public class SlotCraftingAutoRefill extends CraftingResultSlot {

    private final CraftingInventory inventoryCrafting;
    private final TerminalStorageTabIngredientComponentItemStackCraftingCommon tabCommon;
    private final TerminalStorageTabIngredientComponentServer<ItemStack, Integer> tabServer;
    private final ContainerTerminalStorage container;

    public SlotCraftingAutoRefill(PlayerEntity player, CraftingInventory inventoryCrafting,
                                  IInventory inventoryIn, int slotIndex, int xPosition, int yPosition,
                                  TerminalStorageTabIngredientComponentItemStackCraftingCommon tabCommon,
                                  TerminalStorageTabIngredientComponentServer<ItemStack, Integer> tabServer,
                                  ContainerTerminalStorage container) {
        super(player, inventoryCrafting, inventoryIn, slotIndex, xPosition, yPosition);
        this.inventoryCrafting = inventoryCrafting;
        this.tabCommon = tabCommon;
        this.tabServer = tabServer;
        this.container = container;
    }

    @Override
    public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
        TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType autoRefill = tabCommon.getAutoRefill();
        if (!thePlayer.world.isRemote && autoRefill != TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType.DISABLED) {
            NonNullList<ItemStack> beforeCraft = inventoryToList(inventoryCrafting, true);
            ItemStack taken = super.onTake(thePlayer, stack);
            NonNullList<ItemStack> afterCraft = inventoryToList(inventoryCrafting, false);

            NonNullList<ItemStack> removed = getRemoved(beforeCraft, afterCraft);
            // Attempt to get and re-add removed stacks from storage
            IIngredientComponentStorage<ItemStack, Integer> storage = tabServer.getIngredientNetwork()
                    .getChannel(this.container.getSelectedChannel());
            IIngredientComponentStorage<ItemStack, Integer> player = new IngredientComponentStorageWrapperHandlerItemStack.ComponentStorageWrapper(IngredientComponent.ITEMSTACK, new PlayerInvWrapper(thePlayer.inventory));
            for (int i = 0; i < removed.size(); i++) {
                ItemStack removedStack = removed.get(i);
                if (!removedStack.isEmpty()) {
                    ItemStack extracted;

                    // Different source priorities
                    switch (autoRefill) {
                        case STORAGE:
                            extracted = storage.extract(removedStack, ItemMatch.EXACT, false);
                            break;
                        case PLAYER:
                            extracted = player.extract(removedStack, ItemMatch.EXACT, false);
                            break;
                        case STORAGE_PLAYER:
                            extracted = storage.extract(removedStack, ItemMatch.EXACT, false);
                            if (extracted.isEmpty()) {
                                extracted = player.extract(removedStack, ItemMatch.EXACT, false);
                            }
                            break;
                        case PLAYER_STORAGE:
                            extracted = player.extract(removedStack, ItemMatch.EXACT, false);
                            if (extracted.isEmpty()) {
                                extracted = storage.extract(removedStack, ItemMatch.EXACT, false);
                            }
                            break;
                        default:
                            extracted = ItemStack.EMPTY;
                            break;
                    }
                    thePlayer.openContainer.detectAndSendChanges();

                    ItemStack existingStack = inventoryCrafting.getStackInSlot(i);
                    existingStack.grow(extracted.getCount());
                    inventoryCrafting.setInventorySlotContents(i, existingStack);
                    ((ServerPlayerEntity) thePlayer).connection.sendPacket(
                            new SSetSlotPacket(thePlayer.openContainer.windowId, i + this.slotNumber + 1,
                                    inventoryCrafting.getStackInSlot(i)));
                }
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
