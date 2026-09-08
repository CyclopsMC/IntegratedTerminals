package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import org.cyclops.cyclopscore.modcompat.ICompatInitializer;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.TerminalStorageTabIngredientCraftingHandlers;

/**
 * @author rubensworks
 */
public class IntegratedCraftingModCompatInitializer implements ICompatInitializer {
    @Override
    public void initialize() {
        TerminalStorageTabIngredientCraftingHandlerCraftingNetwork handler = TerminalStorageTabIngredientCraftingHandlers.REGISTRY.register(
                new TerminalStorageTabIngredientCraftingHandlerCraftingNetwork());
        CraftingJobFinishedToastListener.register();
        CraftingJobFinishedEventForwarder.register(handler);
    }
}
