package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import com.google.common.collect.Iterables;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingOption;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * Identifies a crafting job possibility based on a {@link IRecipeDefinition}.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter, may be Void.
 * @author rubensworks
 */
public class TerminalCraftingOptionRecipeDefinition<T, M> implements ITerminalCraftingOption<T> {

    private final IngredientComponent<T, M> ingredientComponent;
    private final IRecipeDefinition prioritizedRecipe;

    public TerminalCraftingOptionRecipeDefinition(IngredientComponent<T, M> ingredientComponent,
                                                  IRecipeDefinition prioritizedRecipe) {
        this.ingredientComponent = ingredientComponent;
        this.prioritizedRecipe = prioritizedRecipe;
    }

    @Override
    public Iterator<T> getOutputs() {
        return prioritizedRecipe.getOutput().getInstances(ingredientComponent).iterator();
    }

    @Override
    public Collection<IngredientComponent<?, ?>> getOutputComponents() {
        return prioritizedRecipe.getOutput().getComponents();
    }

    @Override
    public <T1, M> Collection<T1> getOutputs(IngredientComponent<T1, M> ingredientComponent) {
        return prioritizedRecipe.getOutput().getInstances(ingredientComponent);
    }

    @Override
    public Collection<IngredientComponent<?, ?>> getInputComponents() {
        return prioritizedRecipe.getInputComponents();
    }

    @Override
    public <T1, M> Collection<T1> getInputs(IngredientComponent<T1, M> ingredientComponent) {
        return prioritizedRecipe.getInputs(ingredientComponent)
                .stream()
                .filter(prototype -> !prototype.getAlternatives().isEmpty())
                .map(prototype -> Iterables.getFirst(prototype.getAlternatives(), null).getPrototype())
                .collect(Collectors.toList());
    }

    public IngredientComponent<T, M> getIngredientComponent() {
        return ingredientComponent;
    }

    public IRecipeDefinition getRecipe() {
        return prioritizedRecipe;
    }

    @Override
    public int compareTo(ITerminalCraftingOption<T> o) {
        if (!(o instanceof TerminalCraftingOptionRecipeDefinition)) {
            throw new IllegalArgumentException("Could not compare TerminalCraftingOptionPrioritizedRecipe to " + o);
        }
        return this.getRecipe().compareTo(((TerminalCraftingOptionRecipeDefinition) o).getRecipe());
    }
}
