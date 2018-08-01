package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.util.ResourceLocation;

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

}
