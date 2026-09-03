package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author rubensworks
 */
public class TestTerminalCraftingOptionRecipeDefinition {

    @Test
    public void testDurationIsUnknownByDefault() {
        assertEquals(-1, new TerminalCraftingOptionRecipeDefinition<>(null, null).getEstimatedTickDuration());
    }

    @Test
    public void testDurationIsRemembered() {
        assertEquals(200, new TerminalCraftingOptionRecipeDefinition<>(null, null, 200).getEstimatedTickDuration());
    }

}
