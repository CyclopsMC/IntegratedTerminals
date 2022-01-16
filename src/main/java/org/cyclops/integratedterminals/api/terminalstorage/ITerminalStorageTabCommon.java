package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A common-side terminal storage tab for loading slots.
 * @author rubensworks
 */
public interface ITerminalStorageTabCommon {

    /**
     * @return The unique tab name, as inherited from {@link ITerminalStorageTab#getName()}.
     */
    public ResourceLocation getName();

    public default List<Slot> loadSlots(Container container, int startIndex, PlayerEntity player,
                                        Optional<IVariableInventory> variableInventory) {
        return Collections.emptyList();
    }

    public default void onUpdate(Container container, PlayerEntity player,
                                 Optional<IVariableInventory> variableInventory) {

    }

    public static interface IVariableInventory {
        public default void loadNamedInventory(String name, IInventory inventory) {
            NonNullList<ItemStack> tabItems = this.getNamedInventory(name);
            if (tabItems != null) {
                for (int i = 0; i < tabItems.size(); i++) {
                    inventory.setItem(i, tabItems.get(i));
                }
            }
        }

        public default void saveNamedInventory(String name, IInventory inventory) {
            NonNullList<ItemStack> latestItems = NonNullList.create();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                latestItems.add(inventory.getItem(i));
            }
            this.setNamedInventory(name, latestItems);
        }

        @Nullable
        public NonNullList<ItemStack> getNamedInventory(String name);
        public void setNamedInventory(String name, NonNullList<ItemStack> inventory);
    }

}
