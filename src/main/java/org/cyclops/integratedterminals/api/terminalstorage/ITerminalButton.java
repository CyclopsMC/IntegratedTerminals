package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * @author rubensworks
 */
public interface ITerminalButton<C extends ITerminalStorageTabClient, B extends GuiButton> {

    /**
     * Create a gui button for displaying this button.
     * @param x The button X position.
     * @param y The button Y position.
     * @return The gui button.
     */
    @SideOnly(Side.CLIENT)
    public B createButton(int x, int y);

    /**
     * Callback for when the gui button has been clicked.
     * @param clientTab The client tab in which the button was clicked.
     * @param guiButton The gui button.
     * @param channel The active channel.
     * @param mouseButton The mouse button that was used to click with.
     */
    @SideOnly(Side.CLIENT)
    public void onClick(C clientTab, B guiButton, int channel, int mouseButton);

    /**
     * @return The unlocalized name
     */
    public String getUnlocalizedName();

    /**
     * Get the tooltip of this sorter.
     * @param player The player that is requesting the tooltip.
     * @param tooltipFlag The tooltip flag.
     * @param lines The tooltip lines.
     */
    public void getTooltip(EntityPlayer player, ITooltipFlag tooltipFlag, List<String> lines);

}
