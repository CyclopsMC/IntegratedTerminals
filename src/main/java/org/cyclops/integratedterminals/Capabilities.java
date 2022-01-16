package org.cyclops.integratedterminals;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.integrateddynamics.api.ingredient.capability.IIngredientComponentValueHandler;

/**
 * Used capabilities for this mod.
 * @author rubensworks
 */
public class Capabilities {
    public static Capability<IIngredientComponentValueHandler> INGREDIENTCOMPONENT_VALUEHANDLER = CapabilityManager.get(new CapabilityToken<>(){});
}
