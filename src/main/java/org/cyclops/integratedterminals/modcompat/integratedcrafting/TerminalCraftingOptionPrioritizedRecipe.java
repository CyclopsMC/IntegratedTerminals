package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integratedcrafting.api.recipe.PrioritizedRecipe;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingOption;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

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

    @Override
    public Collection<IngredientComponent<?, ?>> getOutputComponents() {
        return prioritizedRecipe.getRecipe().getOutput().getComponents();
    }

    @Override
    public <T1, M> Collection<T1> getOutputs(IngredientComponent<T1, M> ingredientComponent) {
        return prioritizedRecipe.getRecipe().getOutput().getInstances(ingredientComponent);
    }

    @Override
    public Collection<IngredientComponent<?, ?>> getInputComponents() {
        return prioritizedRecipe.getRecipe().getInputComponents();
    }

    @Override
    public <T1, M> Collection<T1> getInputs(IngredientComponent<T1, M> ingredientComponent) {
        return prioritizedRecipe.getRecipe().getInputs(ingredientComponent)
                .stream()
                .map(prototype -> prototype.get(0).getPrototype())
                .collect(Collectors.toList());
    }

    public IngredientComponent<T, M> getIngredientComponent() {
        return ingredientComponent;
    }

    public PrioritizedRecipe getPrioritizedRecipe() {
        return prioritizedRecipe;
    }

    @Override
    public int compareTo(ITerminalCraftingOption<T> o) {
        if (!(o instanceof TerminalCraftingOptionPrioritizedRecipe)) {
            throw new IllegalArgumentException("Could not compare TerminalCraftingOptionPrioritizedRecipe to " + o);
        }
        TerminalCraftingOptionPrioritizedRecipe that = (TerminalCraftingOptionPrioritizedRecipe) o;
        int compRecipe = this.getPrioritizedRecipe().getRecipe().compareTo(that.getPrioritizedRecipe().getRecipe());
        if (compRecipe == 0) {
            int[] p1 = this.getPrioritizedRecipe().getPriorities();
            int[] p2 = that.getPrioritizedRecipe().getPriorities();
            int minLength = Math.min(p1.length, p2.length);
            for (int i = 0; i < minLength; i++) {
                int comp = Integer.compare(p1[i], p2[i]);
                if (comp != 0) {
                    return comp;
                }
            }
        }
        return compRecipe;
    }
}
