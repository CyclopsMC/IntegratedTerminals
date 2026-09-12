package org.cyclops.integratedterminals.core.terminalstorage.query;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Helpers for matching strings against a search query.
 * @author rubensworks
 */
public final class IngredientQueryMatchers {

    private IngredientQueryMatchers() {
    }

    /**
     * Create a matcher for the given query.
     *
     * The query is compiled only once, as the returned matcher is called
     * for every shown ingredient, every time the view is rebuilt.
     *
     * @param query A query string, which may be a regex.
     * @return A matcher that tests if a string contains the given query.
     *         Invalid queries match nothing.
     */
    public static Predicate<String> containsQuery(String query) {
        Pattern pattern;
        try {
            pattern = Pattern.compile(".*" + query + ".*");
        } catch (PatternSyntaxException e) {
            return value -> false;
        }
        return value -> pattern.matcher(value).matches();
    }

}
