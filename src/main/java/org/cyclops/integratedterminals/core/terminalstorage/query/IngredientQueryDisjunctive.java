package org.cyclops.integratedterminals.core.terminalstorage.query;

import java.util.List;

/**
 * @author rubensworks
 */
public class IngredientQueryDisjunctive<T> implements IIngredientQuery<T> {

    private final List<IIngredientQuery> patterns;

    public IngredientQueryDisjunctive(List<IIngredientQuery> patterns) {
        this.patterns = patterns;
    }

    @Override
    public boolean test(T t) {
        for (IIngredientQuery pattern : patterns) {
            if (pattern.test(t)) {
                return true;
            }
        }
        return false;
    }
}
