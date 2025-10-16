package org.cyclops.integratedterminals.core.terminalstorage.crafting;

import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentStorageObservable;

/**
 * @author rubensworks
 */
public record CraftingOptionDelta<T>(HandlerWrappedTerminalCraftingOption<T> craftingOption,
                                     IIngredientComponentStorageObservable.Change changeType) {
}
