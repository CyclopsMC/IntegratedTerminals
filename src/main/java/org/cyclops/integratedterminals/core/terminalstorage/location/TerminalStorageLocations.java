package org.cyclops.integratedterminals.core.terminalstorage.location;

import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.api.terminalstorage.location.ITerminalStorageLocationRegistry;

/**
 * @author rubensworks
 */
public class TerminalStorageLocations {

    public static ITerminalStorageLocationRegistry REGISTRY = IntegratedTerminals._instance.getRegistryManager()
            .getRegistry(ITerminalStorageLocationRegistry.class);

    public static TerminalStorageLocationPart PART;

    public static void load() {
        PART = TerminalStorageLocations.REGISTRY.register(new TerminalStorageLocationPart());
    }

}
