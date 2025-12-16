package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;

import javax.annotation.Nullable;

/**
 * @author rubensworks
 */
public interface ITerminalButtonClient<C extends ITerminalStorageTabClient,
        O extends ITerminalStorageTabCommon, B extends Button> {

    /**
     * Create a gui button for displaying this button.
     * @param x The button X position.
     * @param y The button Y position.
     * @return The gui button.
     */
    public B createButton(int x, int y);

    /**
     * Callback for when the gui button has been clicked.
     *
     * @param clientTab     The client tab in which the button was clicked.
     * @param commonTab     The common tab in which the button was clicked.
     * @param guiButton     The gui button.
     * @param channel       The active channel.
     * @param mouse         The mouse button that was used to click with.
     * @param isDoubleClick If the mouse was double-clicked.
     */
    public void onClick(C clientTab, @Nullable O commonTab, B guiButton, int channel, MouseButtonEvent mouse, boolean isDoubleClick);

}
