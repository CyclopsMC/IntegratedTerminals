package org.cyclops.integratedterminals.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.commoncapabilities.ingredient.storage.IngredientComponentStorageWrapperHandlerItemStack;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;
import org.cyclops.integratedterminals.core.terminalstorage.button.TerminalButtonItemStackCraftingGridAutoRefill;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

/**
 * A crafting slot that will automatically auto-refill from the storage (if enabled).
 * @author rubensworks
 */
public class SlotCraftingAutoRefill extends ResultSlot {

    private final CraftingContainer inventoryCrafting;
    private final TerminalStorageTabIngredientComponentItemStackCraftingCommon tabCommon;
    private final TerminalStorageTabIngredientComponentServer<ItemStack, Integer> tabServer;
    private final ContainerTerminalStorageBase container;

    public SlotCraftingAutoRefill(Player player, CraftingContainer inventoryCrafting,
                                  Container inventoryIn, int slotIndex, int xPosition, int yPosition,
                                  TerminalStorageTabIngredientComponentItemStackCraftingCommon tabCommon,
                                  TerminalStorageTabIngredientComponentServer<ItemStack, Integer> tabServer,
                                  ContainerTerminalStorageBase container) {
        super(player, inventoryCrafting, inventoryIn, slotIndex, xPosition, yPosition);
        this.inventoryCrafting = inventoryCrafting;
        this.tabCommon = tabCommon;
        this.tabServer = tabServer;
        this.container = container;
    }

    @Override
    public void onTake(Player thePlayer, ItemStack stack) {
        TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType autoRefill = tabCommon.getAutoRefill();
        if (!thePlayer.level().isClientSide && autoRefill != TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType.DISABLED) {
            NonNullList<ItemStack> beforeCraft = inventoryToList(inventoryCrafting, true);
            super.onTake(thePlayer, stack);
            NonNullList<ItemStack> afterCraft = inventoryToList(inventoryCrafting, false);

            NonNullList<ItemStack> removed = getRemoved(beforeCraft, afterCraft);
            // Attempt to get and re-add removed stacks from storage
            IIngredientComponentStorage<ItemStack, Integer> storage = tabServer.getIngredientNetwork()
                    .getChannel(this.container.getSelectedChannel());
            IIngredientComponentStorage<ItemStack, Integer> player = new IngredientComponentStorageWrapperHandlerItemStack.ComponentStorageWrapper(IngredientComponent.ITEMSTACK, new PlayerInvWrapper(thePlayer.getInventory()));
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
                    thePlayer.containerMenu.broadcastChanges();

                    ItemStack existingStack = inventoryCrafting.getItem(i);
                    existingStack.grow(extracted.getCount());
                    inventoryCrafting.setItem(i, existingStack);
                    ((ServerPlayer) thePlayer).connection.send(
                            new ClientboundContainerSetSlotPacket(thePlayer.containerMenu.containerId, thePlayer.containerMenu.getStateId(), i + this.index + 1,
                                    inventoryCrafting.getItem(i)));
                }
            }
        } else {
            super.onTake(thePlayer, stack);
        }
    }

    public static NonNullList<ItemStack> inventoryToList(Container inventory, boolean copy) {
        NonNullList<ItemStack> list = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            list.set(i, copy ? inventory.getItem(i).copy() : inventory.getItem(i));
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
