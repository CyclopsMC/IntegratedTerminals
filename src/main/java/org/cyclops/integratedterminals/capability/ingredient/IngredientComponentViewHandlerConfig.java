package org.cyclops.integratedterminals.capability.ingredient;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentViewHandler;

/**
 * Config for the ingredient component view capability.
 * @author rubensworks
 *
 */
public class IngredientComponentViewHandlerConfig extends CapabilityConfig<IIngredientComponentViewHandler> {

    /**
     * The unique instance.
     */
    public static IngredientComponentViewHandlerConfig _instance;

    @CapabilityInject(IIngredientComponentViewHandler.class)
    public static Capability<IIngredientComponentViewHandler> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public IngredientComponentViewHandlerConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "ingredientComponentViewHandler",
                "Capability for displaying ingredient components of a certain type",
                IIngredientComponentViewHandler.class,
                new DefaultCapabilityStorage<IIngredientComponentViewHandler>(),
                IngredientComponentViewHandlerItemStack.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
