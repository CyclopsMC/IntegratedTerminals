package org.cyclops.integratedterminals.api.terminalstorage;

/**
 * A server-side terminal storage tab.
 * @author rubensworks
 */
public interface ITerminalStorageTabServer {

    /**
     * @return The unique tab id, must be equal to its client-side variant.
     */
    public String getId();

    /**
     * Initializes the tab when the container was opened.
     */
    public void init();

    /**
     * Deinitialize the tab when the container is closed.
     */
    public void deInit();

}
