package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * A client-side terminal storage tab.
 * @author rubensworks
 */
public interface ITerminalStorageTabClient {

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
     * Get a subset of slots.
     * @param channel A channel id.
     * @param offset A slot offset.
     * @param limit A slot limit.
     * @return A list of slots, can be empty.
     */
    @SideOnly(Side.CLIENT)
    public List<ITerminalStorageSlot> getSlots(int channel, int offset, int limit);

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
}
