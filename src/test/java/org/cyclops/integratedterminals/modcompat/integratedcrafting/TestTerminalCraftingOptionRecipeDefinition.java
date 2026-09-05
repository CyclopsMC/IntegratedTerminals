package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author rubensworks
 */
public class TestTerminalCraftingOptionRecipeDefinition {

    @Test
    @SuppressWarnings("deprecation")
    public void testDurationIsUnknownByDefault() {
        assertEquals(-1, new TerminalCraftingOptionRecipeDefinition<>(null, null).getEstimatedTickDuration());
    }

    @Test
    public void testDurationIsRemembered() {
        assertEquals(200, new TerminalCraftingOptionRecipeDefinition<>(null, null, 200).getEstimatedTickDuration());
    }

}
