package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integratedcrafting.api.recipe.PrioritizedRecipe;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingOption;

import java.util.Iterator;

/**
 * Identifies a crafting job possibility based on a {@link PrioritizedRecipe}.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter, may be Void.
 * @author rubensworks
 */
public class TerminalCraftingOptionPrioritizedRecipe<T, M> implements ITerminalCraftingOption<T> {

    private final IngredientComponent<T, M> ingredientComponent;
    private final PrioritizedRecipe prioritizedRecipe;

    public TerminalCraftingOptionPrioritizedRecipe(IngredientComponent<T, M> ingredientComponent,
                                                   PrioritizedRecipe prioritizedRecipe) {
        this.ingredientComponent = ingredientComponent;
        this.prioritizedRecipe = prioritizedRecipe;
    }

    @Override
    public Iterator<T> getOutputs() {
        return prioritizedRecipe.getRecipe().getOutput().getInstances(ingredientComponent).iterator();
    }

    public IngredientComponent<T, M> getIngredientComponent() {
        return ingredientComponent;
    }

    public PrioritizedRecipe getPrioritizedRecipe() {
        return prioritizedRecipe;
    }
}
