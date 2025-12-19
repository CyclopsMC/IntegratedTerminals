package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.resources.Identifier;

/**
 * A server-side terminal storage tab.
 * @author rubensworks
 */
public interface ITerminalStorageTabServer {

    /**
     * @return The unique tab name, as inherited from {@link ITerminalStorageTab#getName()}.
     */
    public Identifier getName();

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
     */
    public void updateActive();

}
