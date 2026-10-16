package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import com.google.common.collect.Lists;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;

import java.util.List;

/**
 * @author rubensworks
 */
public class IntegratedCraftingHelpers {

    public static List<IPrototypedIngredient<?, ?>> getPrototypesFromIngredients(IMixedIngredients ingredients) {
        List<IPrototypedIngredient<?, ?>> outputs = Lists.newArrayList();
        for (IngredientComponent<?, ?> component : ingredients.getComponents()) {
            IIngredientMatcher matcher = component.getMatcher();
            for (Object instance : ingredients.getInstances(component)) {
                // Crafting job buffers keep an empty instance around for inserting into, which is not an ingredient
                if (!matcher.isEmpty(instance)) {
                    outputs.add(new PrototypedIngredient(component, instance, matcher.getExactMatchCondition()));
                }
            }
        }
        return outputs;
    }

}
