package org.cyclops.integratedterminals.core.terminalstorage.crafting;

import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandlerRegistry;

/**
 * @author rubensworks
 */
public class TerminalStorageTabIngredientCraftingHandlers {

    public static ITerminalStorageTabIngredientCraftingHandlerRegistry REGISTRY = IntegratedTerminals._instance.getRegistryManager()
            .getRegistry(ITerminalStorageTabIngredientCraftingHandlerRegistry.class);

    public static void load() {}

}
