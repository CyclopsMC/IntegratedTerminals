package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Lists;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalRowColumnProvider;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageSlot;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A client-side storage terminal tab for Ender Chest.
 * @author rubensworks
 */
public class TerminalStorageTabEnderChestClient implements ITerminalStorageTabClient<ITerminalStorageSlot> {

    private final ResourceLocation name;
    private final ItemStack icon;
    protected final ContainerTerminalStorageBase container;

    public TerminalStorageTabEnderChestClient(ContainerTerminalStorageBase container, ResourceLocation name) {
        this.name = name;
        this.icon = new ItemStack(Blocks.ENDER_CHEST);
        this.container = container;
    }

    @Override
    public void onSelect(int channel) {
        // No action needed on select
    }

    @Override
    public void onDeselect(int channel) {
        // No action needed on deselect
    }

    @Override
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public ItemStack getIcon() {
        return this.icon;
    }

    @Override
    public List<Component> getTooltip() {
        return Lists.newArrayList(Component.translatable("gui.integratedterminals.terminal_storage.ender_chest"));
    }

    @Override
    public String getInstanceFilter(int channel) {
        return "";
    }

    @Override
    public void setInstanceFilter(int channel, String filter) {
        // Ender Chest doesn't support filtering
    }

    @Override
    public List<ITerminalStorageSlot> getSlots(int channel, int offset, int limit) {
        // Ender Chest inventory is handled by regular container slots
        return Collections.emptyList();
    }

    @Override
    public ITerminalRowColumnProvider getRowColumnProvider() {
        return () -> new ITerminalRowColumnProvider.RowsAndColumns(3, 9);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public int getSlotCount(int channel) {
        return 27; // Ender Chest has 27 slots
    }

    @Override
    public String getStatus(int channel) {
        return "";
    }

    @Override
    public int[] getChannels() {
        return new int[]{0}; // Single channel
    }

    @Override
    public void resetActiveSlot() {
        // No active slot for Ender Chest
    }

    @Override
    public boolean handleClick(AbstractContainerMenu container, int channel, int hoveringStorageSlot, int mouseButton,
                               boolean hasClickedOutside, boolean hasClickedInStorage, int hoveredContainerSlot,
                               boolean isQuickMove) {
        // Handle shift-clicking for Ender Chest slots
        if (isQuickMove && hoveredContainerSlot >= 0 && container instanceof ContainerTerminalStorageBase) {
            ContainerTerminalStorageBase<?> terminalContainer = (ContainerTerminalStorageBase<?>) container;

            // Get the Ender Chest slots for this tab
            List<Pair<Slot, ITerminalStorageTabCommon.ISlotPositionCallback>> enderSlots =
                    terminalContainer.getTabSlots(getName().toString());

            if (!enderSlots.isEmpty()) {
                // Find the start and end indices of Ender Chest slots
                int startIndex = Integer.MAX_VALUE;
                int endIndex = Integer.MIN_VALUE;
                for (Pair<Slot, ITerminalStorageTabCommon.ISlotPositionCallback> slotPair : enderSlots) {
                    int index = slotPair.getLeft().index;
                    startIndex = Math.min(startIndex, index);
                    endIndex = Math.max(endIndex, index);
                }

                boolean isEnderSlot = hoveredContainerSlot >= startIndex && hoveredContainerSlot <= endIndex;
                Slot slot = container.getSlot(hoveredContainerSlot);
                ItemStack stackInSlot = slot.getItem().copy();

                if (!stackInSlot.isEmpty()) {
                    // Try to move items
                    if (isEnderSlot) {
                        // Moving from Ender Chest to player inventory (slots 0-35)
                        if (moveItems(container, stackInSlot, 0, 36)) {
                            slot.set(stackInSlot.isEmpty() ? ItemStack.EMPTY : stackInSlot);
                            slot.setChanged();
                            return true;
                        }
                    } else {
                        // Moving from player inventory to Ender Chest
                        if (moveItems(container, stackInSlot, startIndex, endIndex + 1)) {
                            slot.set(stackInSlot.isEmpty() ? ItemStack.EMPTY : stackInSlot);
                            slot.setChanged();
                            return true;
                        }
                    }
                }
            }
        }

        // Let default container handling take care of other clicks
        return false;
    }

    private boolean moveItems(AbstractContainerMenu container, ItemStack stack, int startIndex, int endIndex) {
        boolean moved = false;
        int originalCount = stack.getCount();

        // Try to merge with existing stacks first
        for (int i = startIndex; i < endIndex && !stack.isEmpty(); i++) {
            Slot targetSlot = container.getSlot(i);
            ItemStack targetStack = targetSlot.getItem();

            if (!targetStack.isEmpty() && ItemStack.isSameItemSameTags(stack, targetStack)) {
                int maxSize = Math.min(targetSlot.getMaxStackSize(), targetStack.getMaxStackSize());
                int toTransfer = Math.min(stack.getCount(), maxSize - targetStack.getCount());

                if (toTransfer > 0) {
                    targetStack.grow(toTransfer);
                    stack.shrink(toTransfer);
                    targetSlot.setChanged();
                    moved = true;
                }
            }
        }

        // Then try to put in empty slots
        for (int i = startIndex; i < endIndex && !stack.isEmpty(); i++) {
            Slot targetSlot = container.getSlot(i);

            if (!targetSlot.mayPlace(stack)) {
                continue;
            }

            ItemStack targetStack = targetSlot.getItem();
            if (targetStack.isEmpty()) {
                int toTransfer = Math.min(stack.getCount(), targetSlot.getMaxStackSize());
                targetSlot.set(stack.split(toTransfer));
                targetSlot.setChanged();
                moved = true;
            }
        }

        return moved && stack.getCount() < originalCount;
    }

    @Override
    public boolean handleScroll(AbstractContainerMenu container, int channel, int hoveringStorageSlot, double delta,
                                boolean hasClickedOutside, boolean hasClickedInStorage, int hoveredContainerSlot) {
        // No scroll handling needed
        return false;
    }

    @Override
    public int getActiveSlotId() {
        return -1;
    }

    @Override
    public int getActiveSlotQuantity() {
        return 0;
    }

    @Override
    public void setActiveSlotQuantity(int quantity) {
        // No active slot for Ender Chest
    }

    @Override
    public List<ITerminalButton<?, ?, ?>> getButtons() {
        return Collections.emptyList();
    }

    @Override
    public boolean isSlotValidForDraggingInto(int channel, Slot slot) {
        // Allow dragging into Ender Chest slots
        return true;
    }

    @Override
    public int computeDraggingQuantity(Set<Slot> dragSlots, int dragMode, ItemStack stack, int quantity) {
        // Use default dragging behavior
        return quantity / Math.max(1, dragSlots.size());
    }

    @Override
    public int dragIntoSlot(AbstractContainerMenu container, int channel, Slot slot, int quantity, boolean simulate) {
        // Use default container behavior
        return 0;
    }
}
