package org.cyclops.integratedterminals;

import net.minecraft.resources.ResourceLocation;
import org.cyclops.commoncapabilities.api.ingredient.capability.IngredientComponentCapability;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;

/**
 * Used capabilities for this mod.
 * @author rubensworks
 */
public class Capabilities {
    public static final class IngredientComponentTerminalStorageHandler {
        public static final IngredientComponentCapability<IIngredientComponentTerminalStorageHandler, Void> INGREDIENT = IngredientComponentCapability.createVoid(new ResourceLocation(Reference.MOD_ID, "terminal_storage_handler"), IIngredientComponentTerminalStorageHandler.class);
    }
}
