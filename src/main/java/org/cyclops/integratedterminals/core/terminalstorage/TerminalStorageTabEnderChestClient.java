package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageSlot;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
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
        // Let default container handling take care of clicks
        return false;
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
    public int getSlotOffsetX() {
        return 32;
    }

    @Override
    public int getSlotOffsetY() {
        return 58; // Adjusted to position the Ender Chest slots properly
    }

    @Override
    public int getSlotVisibleRows() {
        return 3; // Ender Chest has 3 rows
    }

    @Override
    public int getSlotRowLength() {
        return 9; // 9 columns
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
