package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

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
    public Identifier getName();

    public default List<Pair<Slot, ISlotPositionCallback>> loadSlots(AbstractContainerMenu container, int startIndex, Player player,
                                                                     Optional<IVariableInventory> variableInventory, ValueDeseralizationContext valueDeseralizationContext) {
        return Collections.emptyList();
    }

    public default void onUpdate(AbstractContainerMenu container, Player player,
                                 Optional<IVariableInventory> variableInventory) {

    }

    /**
     * Handle a quick move (shift-click) on the given container slot.
     *
     * This is only called for the tab that is currently selected,
     * and allows tabs with regular container slots to apply regular quick move semantics.
     *
     * @param container The active container.
     * @param player The player that is quick moving.
     * @param slotIndex The index of the slot that is being quick moved.
     * @return The moved stack following {@link AbstractContainerMenu#quickMoveStack} semantics,
     *         or {@link Optional#empty()} if this tab does not handle quick moves.
     */
    public default Optional<ItemStack> handleQuickMove(ContainerTerminalStorageBase<?> container, Player player, int slotIndex) {
        return Optional.empty();
    }

    public static interface IVariableInventory {
        public default void loadNamedInventory(String name, Container inventory, HolderLookup.Provider holderLookupProvider) {
            NonNullList<ItemStack> tabItems = this.getNamedInventory(name, holderLookupProvider);
            if (tabItems != null) {
                for (int i = 0; i < tabItems.size(); i++) {
                    inventory.setItem(i, tabItems.get(i));
                }
            }
        }

        public default void saveNamedInventory(String name, Container inventory, HolderLookup.Provider holderLookupProvider) {
            NonNullList<ItemStack> latestItems = NonNullList.create();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                latestItems.add(inventory.getItem(i));
            }
            this.setNamedInventory(name, latestItems, holderLookupProvider);
        }

        @Nullable
        public NonNullList<ItemStack> getNamedInventory(String name, HolderLookup.Provider holderLookupProvider);
        public void setNamedInventory(String name, NonNullList<ItemStack> inventory, HolderLookup.Provider holderLookupProvider);
    }

    public static interface ISlotPositionCallback {
        public Pair<Integer, Integer> getSlotPosition(SlotPositionFactors factors);
    }

    public static record SlotPositionFactors(int offsetX, int offsetY, int gridXSize, int gridYSize, int playerInventoryOffsetX, int playerInventoryOffsetY) {}

}
