package org.cyclops.integratedterminals.part;

import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.part.IPartTypeRegistry;

/**
 * @author rubensworks
 */
public class PartTypes {

    public static final IPartTypeRegistry REGISTRY = IntegratedDynamics._instance.getRegistryManager().getRegistry(IPartTypeRegistry.class);

    public static void load() {}

    public static final PartTypeTerminalStorage TERMINAL_STORAGE = REGISTRY.register(new PartTypeTerminalStorage("terminal_storage"));

}
