package org.cyclops.integratedterminals.client.gui.container.component;

import org.junit.Test;

import static org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanToggler.CraftingPlanDisplayMode.FLAT;
import static org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanToggler.CraftingPlanDisplayMode.GRAPH;
import static org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanToggler.CraftingPlanDisplayMode.NONE;
import static org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanToggler.CraftingPlanDisplayMode.TREE;
import static org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanToggler.getDefaultDisplayMode;
import static org.junit.Assert.assertEquals;

/**
 * @author rubensworks
 */
public class TestGuiCraftingPlanToggler {

    @Test
    public void testDefaultsToGraph() {
        assertEquals(GRAPH, getDefaultDisplayMode(true, true, null, true, true));
        assertEquals(GRAPH, getDefaultDisplayMode(true, false, null, true, true));
    }

    @Test
    public void testDefaultsToFlatWhenConfigured() {
        assertEquals(FLAT, getDefaultDisplayMode(true, true, null, false, true));
        // Without a flat plan, the graph is the only option
        assertEquals(GRAPH, getDefaultDisplayMode(true, false, null, false, true));
    }

    @Test
    public void testFallsBackToFlatForLargePlans() {
        // Plans that are too large are not sent as a tree
        assertEquals(FLAT, getDefaultDisplayMode(false, true, null, true, true));
        assertEquals(FLAT, getDefaultDisplayMode(false, true, GRAPH, true, true));
    }

    @Test
    public void testNothingAvailable() {
        assertEquals(NONE, getDefaultDisplayMode(false, false, null, true, true));
        assertEquals(NONE, getDefaultDisplayMode(false, false, FLAT, true, true));
    }

    @Test
    public void testPrefersLastChosenMode() {
        assertEquals(FLAT, getDefaultDisplayMode(true, true, FLAT, true, true));
        assertEquals(TREE, getDefaultDisplayMode(true, true, TREE, false, true));
        assertEquals(GRAPH, getDefaultDisplayMode(true, true, GRAPH, false, true));
        // An unavailable preference is ignored
        assertEquals(GRAPH, getDefaultDisplayMode(true, false, FLAT, false, true));
    }

    @Test
    public void testIgnoresTreeListWhenDisabled() {
        assertEquals(GRAPH, getDefaultDisplayMode(true, true, TREE, true, false));
        assertEquals(FLAT, getDefaultDisplayMode(true, true, TREE, false, false));
        assertEquals(TREE, getDefaultDisplayMode(true, true, TREE, true, true));
    }

}
