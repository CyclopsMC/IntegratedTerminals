package org.cyclops.integratedterminals.inventory.container.query;

import java.util.List;

/**
 * @author rubensworks
 */
public class IngredientQueryConjunctive<T> implements IIngredientQuery<T> {

    private final List<IIngredientQuery> patterns;

    public IngredientQueryConjunctive(List<IIngredientQuery> patterns) {
        this.patterns = patterns;
    }

    @Override
    public boolean test(T t) {
        for (IIngredientQuery pattern : patterns) {
            if (!pattern.test(t)) {
                return false;
            }
        }
        return true;
    }
}
