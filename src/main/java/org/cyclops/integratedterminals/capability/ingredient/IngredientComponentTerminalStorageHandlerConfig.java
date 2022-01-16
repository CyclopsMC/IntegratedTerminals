package org.cyclops.integratedterminals.capability.ingredient;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.commoncapabilities.CommonCapabilities;
import org.cyclops.cyclopscore.config.extendedconfig.CapabilityConfig;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;

/**
 * Config for the ingredient component view capability.
 * @author rubensworks
 *
 */
public class IngredientComponentTerminalStorageHandlerConfig extends CapabilityConfig<IIngredientComponentTerminalStorageHandler> {

    public static Capability<IIngredientComponentTerminalStorageHandler> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public IngredientComponentTerminalStorageHandlerConfig() {
        super(
                CommonCapabilities._instance,
                "ingredientComponentTerminalStorageHandler",
                IIngredientComponentTerminalStorageHandler.class
        );
    }

}
