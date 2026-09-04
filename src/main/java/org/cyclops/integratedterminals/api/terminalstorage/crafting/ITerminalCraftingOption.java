package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import net.minecraft.world.item.ItemStack;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Identifies a crafting job possibility.
 * @param <T> The instance type.
 * @author rubensworks
 */
public interface ITerminalCraftingOption<T> extends Comparable<ITerminalCraftingOption<T>> {

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
     * An estimation of how long a single crafting operation of this option takes,
     * based on how long the crafting interfaces of the network needed for it before.
     *
     * @return The estimated tick duration of one crafting operation. -1 indicates an unknown duration.
     */
    public long getEstimatedTickDuration();

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

    /**
     * Item representations of the machines that this crafting job option is crafted in.
     *
     * @return The crafting machines, can be empty if they are unknown.
     */
    public default List<ItemStack> getCraftingMachines() {
        return Collections.emptyList();
    }

    /**
     * The inputs of this crafting job option for the given ingredient component,
     * where each input can be fulfilled by any of its alternatives.
     *
     * By default, this only exposes the single input instances from {@link #getInputs(IngredientComponent)}.
     *
     * @param ingredientComponent An ingredient component,
     * @param <T1> The instance type.
     * @param <M> The matching condition parameter, may be Void.
     * @return The inputs, where each entry holds at least one alternative.
     */
    public default <T1, M> List<List<IPrototypedIngredient<T1, M>>> getInputAlternatives(IngredientComponent<T1, M> ingredientComponent) {
        IIngredientMatcher<T1, M> matcher = ingredientComponent.getMatcher();
        return getInputs(ingredientComponent)
                .stream()
                .<List<IPrototypedIngredient<T1, M>>>map(input -> Collections.singletonList(
                        new PrototypedIngredient<>(ingredientComponent, input, matcher.getExactMatchNoQuantityCondition())))
                .collect(Collectors.toList());
    }

}
