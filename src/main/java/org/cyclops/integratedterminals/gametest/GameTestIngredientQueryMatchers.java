package org.cyclops.integratedterminals.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.integratedterminals.core.terminalstorage.query.IngredientQueryMatchers;

import java.util.function.Predicate;

/**
 * Game tests for the storage terminal search query matchers.
 * @author rubensworks
 */
public class GameTestIngredientQueryMatchers {

    @GameTest(template = "cyclopscore:empty")
    public void testSubstringsMatch(GameTestHelper helper) {
        Predicate<String> matcher = IngredientQueryMatchers.containsQuery("ton");

        helper.assertTrue(matcher.test("stone"), "A contained query should match");
        helper.assertTrue(matcher.test("ton"), "An equal query should match");
        helper.assertTrue(!matcher.test("dirt"), "An absent query should not match");

        helper.succeed();
    }

    @GameTest(template = "cyclopscore:empty")
    public void testEmptyQueryMatchesEverything(GameTestHelper helper) {
        Predicate<String> matcher = IngredientQueryMatchers.containsQuery("");

        helper.assertTrue(matcher.test("stone"), "An empty query should match");
        helper.assertTrue(matcher.test(""), "An empty query should match an empty value");

        helper.succeed();
    }

    @GameTest(template = "cyclopscore:empty")
    public void testRegexQueriesMatch(GameTestHelper helper) {
        Predicate<String> matcher = IngredientQueryMatchers.containsQuery("st[a-z]ne");

        helper.assertTrue(matcher.test("stone"), "A matching value should match");
        helper.assertTrue(matcher.test("cobbled stone brick"), "A matching value should match anywhere");
        helper.assertTrue(!matcher.test("stne"), "A non-matching value should not match");
        helper.assertTrue(!matcher.test("dirt"), "Other values should not match");

        helper.succeed();
    }

    @GameTest(template = "cyclopscore:empty")
    public void testInvalidQueriesMatchNothing(GameTestHelper helper) {
        Predicate<String> matcher = IngredientQueryMatchers.containsQuery("[");

        helper.assertTrue(!matcher.test("stone"), "An invalid query should not match");
        helper.assertTrue(!matcher.test("["), "An invalid query should not even match itself");

        helper.succeed();
    }

}
