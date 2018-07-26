package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A client-side terminal storage tab.
 * @author rubensworks
 */
public interface ITerminalStorageTabClient<S extends ITerminalStorageSlot> {

    public static final int DEFAULT_SLOT_OFFSET_X = 31;
    public static final int DEFAULT_SLOT_OFFSET_Y = 39;
    public static final int DEFAULT_SLOT_VISIBLE_ROWS = 5;
    public static final int DEFAULT_SLOT_ROW_LENGTH = 9;

    /**
     * @return The unique tab id, must be equal to its server-side variant.
     */
    public String getId();

    /**
     * @return An icon for the tab.
     */
    public ItemStack getIcon();

    /**
     * @return A tooltip when hovering over the icon.
     */
    public List<String> getTooltip();

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
    @SideOnly(Side.CLIENT)
    public List<S> getSlots(int channel, int offset, int limit);

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
     * @param hoveredContainerSlot The container slot id that is being hovered. -1 if none.
     * @return If further click processing should stop.
     */
    public boolean handleClick(Container container, int channel, int hoveringStorageSlot, int mouseButton,
                               boolean hasClickedOutside, int hoveredContainerSlot);

    /**
     * @return The active storage slot id.
     */
    public int getActiveSlotId();

    /**
     * @return The active storage slot quantity.
     */
    public int getActiveSlotQuantity();

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
        return DEFAULT_SLOT_VISIBLE_ROWS;
    }

    public default int getSlotRowLength() {
        return DEFAULT_SLOT_ROW_LENGTH;
    }

    /**
     * @return An optional alternative background texture that should be used when rendering the gui of the tab.
     */
    @Nullable
    public default ResourceLocation getBackgroundTexture() {
        return null;
    }
}
