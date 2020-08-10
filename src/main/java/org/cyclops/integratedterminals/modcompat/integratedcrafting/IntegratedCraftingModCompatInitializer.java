package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import org.cyclops.cyclopscore.modcompat.ICompatInitializer;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.TerminalStorageTabIngredientCraftingHandlers;

/**
 * @author rubensworks
 */
public class IntegratedCraftingModCompatInitializer implements ICompatInitializer {
    @Override
    public void initialize() {
        TerminalStorageTabIngredientCraftingHandlers.REGISTRY.register(
                new TerminalStorageTabIngredientCraftingHandlerCraftingNetwork());
    }
}
