package org.cyclops.integratedterminals.api.terminalstorage;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * A client-side terminal storage tab.
 * @author rubensworks
 */
public interface ITerminalStorageTabClient<S extends ITerminalStorageSlot> {

    public static final int DEFAULT_SLOT_OFFSET_X = 32;
    public static final int DEFAULT_SLOT_OFFSET_Y = 40;
    public static final int DEFAULT_SLOT_VISIBLE_ROWS = 5;
    public static final int DEFAULT_SLOT_ROW_LENGTH = 9;
    Boolean DEFAULT_AUTO_FOCUS_BEHAVIOUR = false;

    /**
     * When this tab is selected by the player.
     * @param channel The channel.
     */
    public void onSelect(int channel);

    /**
     * When this tab is deselected by the player.
     * @param channel The channel.
     */
    public void onDeselect(int channel);

    /**
     * @return The unique tab name, as inherited from {@link ITerminalStorageTab#getName()}.
     */
    public ResourceLocation getName();

    /**
     * @return The tab name that will be used to store {@link TerminalStorageState} settings inside a tab.
     * This can be used to modify in what tab certain settings are stored.
     */
    public default ResourceLocation getTabSettingsName() {
        return getName();
    }

    /**
     * @return An icon for the tab.
     */
    public ItemStack getIcon();

    /**
     * @return A tooltip when hovering over the icon.
     */
    public List<Component> getTooltip();

    /**
     * Get the currently active filter.
     * @param channel The channel to get the filter for.
     * @return The active filter.
     */
    public String getInstanceFilter(int channel);

    /**
     * Set a filter for instances in slots.
     * @param filter A string-based filter, which could be a regex.
     * @param channel The channel to filter in.
     */
    public void setInstanceFilter(int channel, String filter);

    /**
     * Get a subset of slots.
     * @param channel A channel id.
     * @param offset A slot offset.
     * @param limit A slot limit.
     * @return A list of slots, can be empty.
     */
    @OnlyIn(Dist.CLIENT)
    public List<S> getSlots(int channel, int offset, int limit);

    /**
     * @return The current provider of row and column count.
     */
    public ITerminalRowColumnProvider getRowColumnProvider();

    /**
     * @return If this tab is enabled.
     */
    public boolean isEnabled();

    /**
     * Get the total number of slots in the given channel
     * @param channel A channel id.
     * @return A slot count.
     */
    public int getSlotCount(int channel);

    /**
     * @param channel A channel id.
     * @return A status string.
     */
    public String getStatus(int channel);

    /**
     * @return All available channels.
     */
    public int[] getChannels();

    /**
     * Unselect the active storage slot.
     */
    public void resetActiveSlot();

    /**
     * Called when a mouse click happens in a gui.
     * @param container The active container.
     * @param channel The active channel.
     * @param hoveringStorageSlot The storage slot id that is being hovered. -1 if none.
     * @param mouseButton The mouse button.
     * @param hasClickedOutside If the player has clicked outside the gui.
     * @param hasClickedInStorage If the player has clicked inside the storage space.
     *                            This can be true even if the storage slot is -1.
     * @param hoveredContainerSlot The container slot id that is being hovered. -1 if none.
     * @return If further click processing should stop.
     */
    public boolean handleClick(AbstractContainerMenu container, int channel, int hoveringStorageSlot, int mouseButton,
                               boolean hasClickedOutside, boolean hasClickedInStorage, int hoveredContainerSlot);

    /**
     * Called when a mouse scroll happens in a gui.
     * @param container The active container.
     * @param channel The active channel.
     * @param hoveringStorageSlot The storage slot id that is being hovered. -1 if none.
     * @param delta The scroll delta.
     * @param hasClickedOutside If the player has clicked outside the gui.
     * @param hasClickedInStorage If the player has clicked inside the storage space.
     *                            This can be true even if the storage slot is -1.
     * @param hoveredContainerSlot The container slot id that is being hovered. -1 if none.
     * @return If further click processing should stop.
     */
    public boolean handleScroll(AbstractContainerMenu container, int channel, int hoveringStorageSlot, double delta,
                               boolean hasClickedOutside, boolean hasClickedInStorage, int hoveredContainerSlot);

    /**
     * @return The active storage slot id.
     */
    public int getActiveSlotId();

    /**
     * @return The active storage slot quantity.
     */
    public int getActiveSlotQuantity();

    /**
     * Set the active quantity.
     * @param quantity A quantity to set.
     */
    void setActiveSlotQuantity(int quantity);

    /**
     * @return Buttons that are available for this tab.
     */
    public List<ITerminalButton<?, ?, ?>> getButtons();

    public default int getSlotOffsetX() {
        return DEFAULT_SLOT_OFFSET_X;
    }

    public default int getSlotOffsetY() {
        return DEFAULT_SLOT_OFFSET_Y;
    }

    public default int getSlotVisibleRows() {
        return getRowColumnProvider().getRowsAndColumns().rows();
    }

    public default int getSlotRowLength() {
        return getRowColumnProvider().getRowsAndColumns().columns();
    }

    public default int getPlayerInventoryOffsetX() {
        return 0;
    }

    public default int getPlayerInventoryOffsetY() {
        return 0;
    }

    /**
     * @return An optional alternative background texture that should be used when rendering the gui of the tab.
     */
    @Nullable
    public default ResourceLocation getBackgroundTexture() {
        return null;
    }

    public default void onTabBackgroundRender(ContainerScreenTerminalStorage<?, ?> screen, PoseStack matrixStack, float f, int mouseX, int mouseY) {

    }

    public default void onCommonSlotRender(AbstractContainerScreen gui, PoseStack matrixStack, ContainerScreenTerminalStorage.DrawLayer layer,
                                           float partialTick, int x, int y, int mouseX, int mouseY,
                                           int slot, ITerminalStorageTabCommon tabCommon) {

    }

    /**
     * Check if we can drag over the current slot with an active instance.
     * @param channel The active channel.
     * @param slot The slot to drag over.
     * @return If we can drag over the slot.
     */
    boolean isSlotValidForDraggingInto(int channel, Slot slot);

    /**
     * Calculate the quantity that will be added to the given stack based on the drag mode and the given list of slots.
     * @param dragSlots The list of slots to drag over.
     * @param dragMode The drag mode.
     * @param stack The stack to calculate the quantity for.
     * @param quantity The available quantity to drag.
     * @return The quantity that will be added.
     */
    int computeDraggingQuantity(Set<Slot> dragSlots, int dragMode, ItemStack stack, int quantity);

    /**
     * Drag the given quantity into the given slot with the currently active instance.
     * @param container The active container.
     * @param channel The active channel.
     * @param slot The slot to drag into.
     * @param quantity The quantity to insert.
     * @param simulate If insertion should only be simulated.
     * @return The quantity that was inserted.
     */
    int dragIntoSlot(AbstractContainerMenu container, int channel, Slot slot, int quantity, boolean simulate);

    /**
     * Check if the search bar should be autofocused
     * @return If we should autofocus the search bar upon display
     */
    boolean getSearchBarAutoFocus();

}
