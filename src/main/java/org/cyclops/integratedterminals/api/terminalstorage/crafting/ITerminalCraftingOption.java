package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;

import java.util.Collection;
import java.util.Iterator;

/**
 * Identifies a crafting job possibility.
 * @param <T> The instance type.
 * @author rubensworks
 */
public interface ITerminalCraftingOption<T> {

    /**
     * @return The outputs of this crafting job option for the configured ingredient component.
     */
    public Iterator<T> getOutputs();

    /**
     * @return All output components.
     */
    public Collection<IngredientComponent<?, ?>> getOutputComponents();

    /**
     * The outputs of this crafting job option for the given ingredient component.
     * @param ingredientComponent An ingredient component,
     * @param <T> The instance type.
     * @param <M> The matching condition parameter, may be Void.
     * @return The outputs
     */
    public <T, M> Collection<T> getOutputs(IngredientComponent<T, M> ingredientComponent);

    /**
     * @return All input components.
     */
    public Collection<IngredientComponent<?, ?>> getInputComponents();

    /**
     * The inputs of this crafting job option for the given ingredient component.
     * @param ingredientComponent An ingredient component,
     * @param <T1> The instance type.
     * @param <M> The matching condition parameter, may be Void.
     * @return The inputs
     */
    public <T1, M> Collection<T1> getInputs(IngredientComponent<T1, M> ingredientComponent);

}
