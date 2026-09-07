package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

import java.util.List;
import java.util.Optional;

/**
 * A common storage terminal tab that exposes the ender chest inventory of the player.
 * @author rubensworks
 */
public class TerminalStorageTabEnderChestCommon implements ITerminalStorageTabCommon {

    public static final int ROWS = 3;
    public static final int COLUMNS = 9;
    public static final int SIZE = ROWS * COLUMNS;

    private static final int PLAYER_INVENTORY_SIZE = 36;
    private static final int PLAYER_SLOTS_SIZE = PLAYER_INVENTORY_SIZE + 5; // Also includes the armor and offhand slots

    private final ContainerTerminalStorageBase<?> container;
    private final ResourceLocation name;
    private final List<Slot> slots;

    public TerminalStorageTabEnderChestCommon(ContainerTerminalStorageBase<?> container, ResourceLocation name) {
        this.container = container;
        this.name = name;
        this.slots = Lists.newArrayListWithCapacity(SIZE);
    }

    @Override
    public ResourceLocation getName() {
        return this.name;
    }

    /**
     * @return If the ender chest tab can be interacted with in the terminal it is part of.
     */
    public boolean isAvailable() {
        return TerminalStorageTabEnderChestClient.isAvailable(this.container);
    }

    @Override
    public List<Pair<Slot, ISlotPositionCallback>> loadSlots(AbstractContainerMenu container, int startIndex, Player player,
                                                             Optional<IVariableInventory> variableInventory,
                                                             ValueDeseralizationContext valueDeseralizationContext) {
        List<Pair<Slot, ISlotPositionCallback>> slots = Lists.newArrayListWithCapacity(SIZE);

        Container enderChestInventory = player.getEnderChestInventory();
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int finalRow = row;
                int finalColumn = column;
                Slot slot = new Slot(enderChestInventory, column + row * COLUMNS, 0, 0) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return isAvailable() && super.mayPlace(stack);
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return isAvailable() && super.mayPickup(player);
                    }
                };
                this.slots.add(slot);
                slots.add(Pair.of(slot, factors -> Pair.of(
                        factors.offsetX() + ITerminalStorageTabClient.DEFAULT_SLOT_OFFSET_X + finalColumn * GuiHelpers.SLOT_SIZE,
                        factors.offsetY() + ITerminalStorageTabClient.DEFAULT_SLOT_OFFSET_Y + finalRow * GuiHelpers.SLOT_SIZE
                )));
            }
        }

        return slots;
    }

    /**
     * @return The index of the first ender chest slot within the container, or -1 if the slots were not loaded.
     */
    public int getFirstSlotIndex() {
        return this.slots.isEmpty() ? -1 : this.slots.get(0).index;
    }

    @Override
    public Optional<ItemStack> handleQuickMove(ContainerTerminalStorageBase<?> container, Player player, int slotIndex) {
        int firstSlotIndex = getFirstSlotIndex();
        if (!isAvailable() || firstSlotIndex < 0 || slotIndex < 0 || slotIndex >= container.slots.size()) {
            return Optional.empty();
        }

        Slot slot = container.getSlot(slotIndex);
        if (!slot.hasItem()) {
            return Optional.of(ItemStack.EMPTY);
        }
        ItemStack slotStack = slot.getItem();
        ItemStack originalStack = slotStack.copy();

        if (slotIndex >= firstSlotIndex && slotIndex < firstSlotIndex + SIZE) {
            // Move from the ender chest into the player inventory
            if (!container.moveStackTo(slotStack, 0, PLAYER_INVENTORY_SIZE, true)) {
                return Optional.of(ItemStack.EMPTY);
            }
        } else if (slotIndex < PLAYER_SLOTS_SIZE) {
            // Move from the player inventory into the ender chest
            if (!container.moveStackTo(slotStack, firstSlotIndex, firstSlotIndex + SIZE, false)) {
                return Optional.of(ItemStack.EMPTY);
            }
        } else {
            // Slots of other tabs are not visible in this tab
            return Optional.of(ItemStack.EMPTY);
        }

        if (slotStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, slotStack);

        return Optional.of(originalStack);
    }
}
