package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.resources.ResourceLocation;

/**
 * A server-side terminal storage tab.
 * @author rubensworks
 */
public interface ITerminalStorageTabServer {

    /**
     * @return The unique tab name, as inherited from {@link ITerminalStorageTab#getName()}.
     */
    public ResourceLocation getName();

    /**
     * Initializes the tab when the container was opened.
     */
    public void init();

    /**
     * Deinitialize the tab when the container is closed.
     */
    public void deInit();

    /**
     * Called on each tick this tab is active.
     * @param channel The channel that is being shown in the terminal.
     */
    public void updateActive(int channel);

}
