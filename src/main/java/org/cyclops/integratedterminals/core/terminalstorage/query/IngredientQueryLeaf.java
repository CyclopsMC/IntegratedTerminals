package org.cyclops.integratedterminals.core.terminalstorage.query;

import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;

import java.util.function.Predicate;
import java.util.regex.PatternSyntaxException;

/**
 * @author rubensworks
 */
public class IngredientQueryLeaf<T> implements IIngredientQuery<T> {

    private final Predicate<T> tester;

    public IngredientQueryLeaf(String query, IIngredientComponentTerminalStorageHandler<T, ?> handler) {
        Pair<SearchMode, String> parsed = parseQuery(query);
        this.tester = handler.getInstanceFilterPredicate(parsed.getLeft(), parsed.getRight());
    }

    public static Pair<SearchMode, String> parseQuery(String query) {
        if (!query.isEmpty()) {
            char c = query.charAt(0);
            switch (c) {
                case '@':
                    return Pair.of(SearchMode.MOD, query.substring(1));
                case '#':
                    return Pair.of(SearchMode.TOOLTIP, query.substring(1));
                case '$':
                    return Pair.of(SearchMode.DICT, query.substring(1));
            }
        }
        return Pair.of(SearchMode.DEFAULT, query);
    }

    @Override
    public boolean test(T t) {
        try {
            return this.tester.test(t);
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

}
