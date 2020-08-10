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

    @CapabilityInject(IIngredientComponentTerminalStorageHandler.class)
    public static Capability<IIngredientComponentTerminalStorageHandler> CAPABILITY = null;

    public IngredientComponentTerminalStorageHandlerConfig() {
        super(
                CommonCapabilities._instance,
                "ingredientComponentTerminalStorageHandler",
                IIngredientComponentTerminalStorageHandler.class,
                new DefaultCapabilityStorage<IIngredientComponentTerminalStorageHandler>(),
                () -> new IngredientComponentTerminalStorageHandlerItemStack(null)
        );
    }

}
