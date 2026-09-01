package org.cyclops.integratedterminals.core.terminalstorage.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingOption;

import java.util.List;
import java.util.Map;

/**
 * Helpers for listing the inputs that are required by a {@link ITerminalCraftingOption}.
 * @author rubensworks
 */
public final class TerminalCraftingOptionInputs {

    private TerminalCraftingOptionInputs() {}

    /**
     * Get all inputs that are required by the given crafting option, over all its ingredient components.
     *
     * Every entry holds all alternatives that can fulfill that input,
     * and inputs that can be fulfilled by the same alternatives are grouped into a single entry
     * by summing their quantities.
     *
     * @param craftingOption A crafting option.
     * @return The required inputs, with at least one alternative each.
     */
    public static List<List<IPrototypedIngredient<?, ?>>> getGroupedInputs(ITerminalCraftingOption<?> craftingOption) {
        List<List<IPrototypedIngredient<?, ?>>> inputs = Lists.newArrayList();
        for (IngredientComponent<?, ?> inputComponent : craftingOption.getInputComponents()) {
            addInputs(craftingOption, inputComponent, inputs);
        }
        return group(inputs);
    }

    protected static <T, M> void addInputs(ITerminalCraftingOption<?> craftingOption, IngredientComponent<T, M> inputComponent,
                                           List<List<IPrototypedIngredient<?, ?>>> inputs) {
        IIngredientMatcher<T, M> matcher = inputComponent.getMatcher();
        for (List<IPrototypedIngredient<T, M>> alternatives : craftingOption.getInputAlternatives(inputComponent)) {
            List<IPrototypedIngredient<?, ?>> nonEmptyAlternatives = Lists.newArrayList();
            for (IPrototypedIngredient<T, M> alternative : alternatives) {
                if (!matcher.isEmpty(alternative.getPrototype())) {
                    nonEmptyAlternatives.add(alternative);
                }
            }
            if (!nonEmptyAlternatives.isEmpty()) {
                inputs.add(nonEmptyAlternatives);
            }
        }
    }

    /**
     * Group the inputs that can be fulfilled by the same alternatives, by summing their quantities.
     *
     * Recipes commonly require the same ingredient in multiple slots,
     * which we want to show as a single input with a higher quantity.
     *
     * @param inputs The inputs to group, in the order they should be shown in.
     * @return The grouped inputs, where every group is placed at the position of its first input.
     */
    protected static List<List<IPrototypedIngredient<?, ?>>> group(List<List<IPrototypedIngredient<?, ?>>> inputs) {
        List<List<IPrototypedIngredient<?, ?>>> groupedInputs = Lists.newArrayList();
        // Inputs are keyed on their alternatives without quantities, so that only their quantities may differ.
        Map<List<IPrototypedIngredient<?, ?>>, Integer> groupIndexes = Maps.newHashMap();
        for (List<IPrototypedIngredient<?, ?>> alternatives : inputs) {
            List<IPrototypedIngredient<?, ?>> groupKey = withoutQuantities(alternatives);
            Integer groupIndex = groupIndexes.get(groupKey);
            if (groupIndex == null) {
                groupIndexes.put(groupKey, groupedInputs.size());
                groupedInputs.add(alternatives);
            } else {
                groupedInputs.set(groupIndex, addQuantities(groupedInputs.get(groupIndex), alternatives));
            }
        }
        return groupedInputs;
    }

    protected static List<IPrototypedIngredient<?, ?>> withoutQuantities(List<IPrototypedIngredient<?, ?>> alternatives) {
        List<IPrototypedIngredient<?, ?>> withoutQuantities = Lists.newArrayListWithExpectedSize(alternatives.size());
        for (IPrototypedIngredient<?, ?> alternative : alternatives) {
            withoutQuantities.add(withQuantity(alternative, 1));
        }
        return withoutQuantities;
    }

    /**
     * Sum the quantities of two equal lists of alternatives.
     * These are always of equal length, as they were grouped on their alternatives without quantities.
     */
    protected static List<IPrototypedIngredient<?, ?>> addQuantities(List<IPrototypedIngredient<?, ?>> alternatives,
                                                                    List<IPrototypedIngredient<?, ?>> addedAlternatives) {
        List<IPrototypedIngredient<?, ?>> summedAlternatives = Lists.newArrayListWithExpectedSize(alternatives.size());
        for (int i = 0; i < alternatives.size(); i++) {
            IPrototypedIngredient<?, ?> alternative = alternatives.get(i);
            summedAlternatives.add(withQuantity(alternative,
                    getQuantity(alternative) + getQuantity(addedAlternatives.get(i))));
        }
        return summedAlternatives;
    }

    protected static <T, M> IPrototypedIngredient<T, M> withQuantity(IPrototypedIngredient<T, M> ingredient, long quantity) {
        IngredientComponent<T, M> ingredientComponent = ingredient.getComponent();
        return new PrototypedIngredient<>(ingredientComponent,
                ingredientComponent.getMatcher().withQuantity(ingredient.getPrototype(), quantity),
                ingredient.getCondition());
    }

    protected static <T, M> long getQuantity(IPrototypedIngredient<T, M> ingredient) {
        return ingredient.getComponent().getMatcher().getQuantity(ingredient.getPrototype());
    }

}
