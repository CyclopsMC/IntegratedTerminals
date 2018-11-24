package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import com.google.common.collect.Lists;
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
            for (Object instance : ingredients.getInstances(component)) {
                outputs.add(new PrototypedIngredient(component, instance, component.getMatcher().getExactMatchCondition()));
            }
        }
        return outputs;
    }

}
