package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Lists;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalRowColumnProvider;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageSlot;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A client-side storage terminal tab that exposes the ender chest inventory of the player.
 *
 * The ender chest contents are shown through regular container slots,
 * so this tab does not expose any storage slots of its own.
 *
 * @author rubensworks
 */
public class TerminalStorageTabEnderChestClient implements ITerminalStorageTabClient<ITerminalStorageSlot> {

    private static final ITerminalRowColumnProvider ROW_COLUMN_PROVIDER = () -> new ITerminalRowColumnProvider
            .RowsAndColumns(TerminalStorageTabEnderChestCommon.ROWS, TerminalStorageTabEnderChestCommon.COLUMNS);

    private final ContainerTerminalStorageBase<?> container;
    private final ResourceLocation name;
    private final ItemStack icon;

    public TerminalStorageTabEnderChestClient(ContainerTerminalStorageBase<?> container, ResourceLocation name) {
        this.container = container;
        this.name = name;
        this.icon = new ItemStack(Items.ENDER_CHEST);
    }

    /**
     * @param container A storage terminal container.
     * @return If the ender chest tab is available in the given terminal.
     */
    public static boolean isAvailable(ContainerTerminalStorageBase<?> container) {
        return GeneralConfig.terminalStorageTabEnderChestEnabled && container.isEnderUpgraded();
    }

    @Override
    public void onSelect(int channel) {

    }

    @Override
    public void onDeselect(int channel) {

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

    }

    @Override
    public List<ITerminalStorageSlot> getSlots(int channel, int offset, int limit) {
        return Collections.emptyList();
    }

    @Override
    public ITerminalRowColumnProvider getRowColumnProvider() {
        return ROW_COLUMN_PROVIDER;
    }

    @Override
    public boolean isEnabled() {
        return isAvailable(this.container);
    }

    @Override
    public int getSlotCount(int channel) {
        return TerminalStorageTabEnderChestCommon.SIZE;
    }

    @Override
    public String getStatus(int channel) {
        return "";
    }

    @Override
    public int[] getChannels() {
        return new int[0];
    }

    @Override
    public void resetActiveSlot() {

    }

    @Override
    public boolean handleClick(AbstractContainerMenu container, int channel, int hoveringStorageSlot, int mouseButton,
                               boolean hasClickedOutside, boolean hasClickedInStorage, int hoveredContainerSlot,
                               boolean isQuickMove) {
        // The ender chest slots are regular container slots, so they are handled by the container itself
        return false;
    }

    @Override
    public boolean handleScroll(AbstractContainerMenu container, int channel, int hoveringStorageSlot, double delta,
                                boolean hasClickedOutside, boolean hasClickedInStorage, int hoveredContainerSlot) {
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

    }

    @Override
    public List<ITerminalButton<?, ?, ?>> getButtons() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasSearchField() {
        return false;
    }

    @Override
    public boolean hasChannelField() {
        return false;
    }

    @Override
    public boolean hasVariableFilterSlots() {
        return false;
    }

    @Override
    public boolean hasScrollbar() {
        // All ender chest slots fit on screen at once
        return false;
    }

    @Override
    public boolean isSlotValidForDraggingInto(int channel, Slot slot) {
        return false;
    }

    @Override
    public int computeDraggingQuantity(Set<Slot> dragSlots, int dragMode, ItemStack stack, int quantity) {
        return 0;
    }

    @Override
    public int dragIntoSlot(AbstractContainerMenu container, int channel, Slot slot, int quantity, boolean simulate) {
        return 0;
    }
}
