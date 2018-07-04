package org.cyclops.integratedterminals.capability.ingredient;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityStorage;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;

/**
 * Config for the ingredient component view capability.
 * @author rubensworks
 *
 */
public class IngredientComponentTerminalStorageHandlerConfig extends CapabilityConfig<IIngredientComponentTerminalStorageHandler> {

    /**
     * The unique instance.
     */
    public static IngredientComponentTerminalStorageHandlerConfig _instance;

    @CapabilityInject(IIngredientComponentTerminalStorageHandler.class)
    public static Capability<IIngredientComponentTerminalStorageHandler> CAPABILITY = null;

    /**
     * Make a new instance.
     */
    public IngredientComponentTerminalStorageHandlerConfig() {
        super(
                CommonCapabilities._instance,
                true,
                "ingredientComponentTerminalStorageHandler",
                "Capability for displaying ingredient components of a certain type",
                IIngredientComponentTerminalStorageHandler.class,
                new DefaultCapabilityStorage<IIngredientComponentTerminalStorageHandler>(),
                IngredientComponentTerminalStorageHandlerItemStack.class
        );
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

}
