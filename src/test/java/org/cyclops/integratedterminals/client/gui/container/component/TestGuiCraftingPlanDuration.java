package org.cyclops.integratedterminals.client.gui.container.component;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author rubensworks
 */
public class TestGuiCraftingPlanDuration {

    @Test
    public void testSecondsBelowOneSecond() {
        assertEquals("0.05", GuiCraftingPlan.getDurationSeconds(1));
        assertEquals("0.25", GuiCraftingPlan.getDurationSeconds(5));
        assertEquals("0.95", GuiCraftingPlan.getDurationSeconds(19));
    }

    @Test
    public void testSecondsBelowTenSeconds() {
        assertEquals("1.0", GuiCraftingPlan.getDurationSeconds(20));
        assertEquals("4.5", GuiCraftingPlan.getDurationSeconds(90));
    }

    @Test
    public void testSecondsBelowOneMinute() {
        assertEquals("10", GuiCraftingPlan.getDurationSeconds(200));
        assertEquals("59", GuiCraftingPlan.getDurationSeconds(1180));
    }

    @Test
    public void testClockBelowOneHour() {
        assertEquals("1:00", GuiCraftingPlan.getDurationClock(1200));
        assertEquals("4:15", GuiCraftingPlan.getDurationClock(5100));
        assertEquals("59:59", GuiCraftingPlan.getDurationClock(71980));
    }

    @Test
    public void testClockFromOneHour() {
        assertEquals("1:00:00", GuiCraftingPlan.getDurationClock(72000));
        assertEquals("2:03:20", GuiCraftingPlan.getDurationClock(148000));
    }

}
