package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

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

    public default List<Pair<Slot, ISlotPositionCallback>> loadSlots(AbstractContainerMenu container, int startIndex, Player player,
                                        Optional<IVariableInventory> variableInventory) {
        return Collections.emptyList();
    }

    public default void onUpdate(AbstractContainerMenu container, Player player,
                                 Optional<IVariableInventory> variableInventory) {

    }

    public static interface IVariableInventory {
        public default void loadNamedInventory(String name, Container inventory) {
            NonNullList<ItemStack> tabItems = this.getNamedInventory(name);
            if (tabItems != null) {
                for (int i = 0; i < tabItems.size(); i++) {
                    inventory.setItem(i, tabItems.get(i));
                }
            }
        }

        public default void saveNamedInventory(String name, Container inventory) {
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

    public static interface ISlotPositionCallback {
        public Pair<Integer, Integer> getSlotPosition(SlotPositionFactors factors);
    }

    public static record SlotPositionFactors(int offsetX, int offsetY, int gridXSize, int gridYSize, int playerInventoryOffsetY) {}

}
