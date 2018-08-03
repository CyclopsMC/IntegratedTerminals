package org.cyclops.integratedterminals;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.integrateddynamics.api.ingredient.capability.IIngredientComponentValueHandler;

/**
 * Used capabilities for this mod.
 * @author rubensworks
 */
public class Capabilities {
    @CapabilityInject(IIngredientComponentValueHandler.class)
    public static Capability<IIngredientComponentValueHandler> INGREDIENTCOMPONENT_VALUEHANDLER = null;
}
