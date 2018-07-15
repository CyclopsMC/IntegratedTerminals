package org.cyclops.integratedterminals.inventory.container.query;

import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.capability.ingredient.IngredientComponentTerminalStorageHandlerConfig;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author rubensworks
 */
public interface IIngredientQuery<T> extends Predicate<T> {

    public static <T, M> IIngredientQuery<T> parse(IngredientComponent<T, M> ingredientComponent, String query) {
        if (query.contains(" ")) {
            String[] conjunctions = query.split(" ");
            return new IngredientQueryConjunctive<>(Arrays.stream(conjunctions).map(c -> parse(ingredientComponent, c))
                    .collect(Collectors.toList()));
        } else if (query.contains("|")) {
            String[] disjunctions = query.split("\\|");
            return new IngredientQueryDisjunctive<>(Arrays.stream(disjunctions).map(c -> parse(ingredientComponent, c))
                    .collect(Collectors.toList()));
        } else {
            IIngredientComponentTerminalStorageHandler<T, M> handler = ingredientComponent
                    .getCapability(IngredientComponentTerminalStorageHandlerConfig.CAPABILITY);
            return new IngredientQueryLeaf<>(query, handler);
        }
    }

}
