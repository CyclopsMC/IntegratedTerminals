package org.cyclops.integratedterminals.client.gui.container.component;

import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlanFlat;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingPlanStatic;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author rubensworks
 */
public class TestGuiCraftingPlanProgress {

    protected static TerminalCraftingPlanStatic<Integer> plan(int id, TerminalCraftingJobStatus status,
                                                              long craftingQuantity, long craftingQuantityTotal,
                                                              List<ITerminalCraftingPlan<Integer>> dependencies) {
        return new TerminalCraftingPlanStatic<>(id, dependencies, Collections.emptyList(), status,
                craftingQuantity, craftingQuantityTotal, Collections.emptyList(), Collections.emptyList(),
                TerminalCraftingPlanStatic.Label.RUNNING, -1, -1, -1, 0, null);
    }

    protected static ITerminalCraftingPlanFlat<Integer> flatPlan(TerminalCraftingJobStatus status,
                                                                 long craftingQuantity, long craftingQuantityTotal,
                                                                 List<ITerminalCraftingPlan<Integer>> dependencies) {
        return plan(1, status, craftingQuantity, craftingQuantityTotal, dependencies).flatten();
    }

    @Test
    public void testProgressIsUnknownWithoutQuantities() {
        assertEquals(-1, GuiCraftingPlan.getProgress(
                flatPlan(TerminalCraftingJobStatus.UNSTARTED, 0, 0, Collections.emptyList())));
    }

    @Test
    public void testProgressAtStart() {
        assertEquals(0, GuiCraftingPlan.getProgress(
                flatPlan(TerminalCraftingJobStatus.CRAFTING, 10, 10, Collections.emptyList())));
    }

    @Test
    public void testProgressHalfway() {
        assertEquals(50, GuiCraftingPlan.getProgress(
                flatPlan(TerminalCraftingJobStatus.CRAFTING, 5, 10, Collections.emptyList())));
    }

    @Test
    public void testProgressWhenFinished() {
        assertEquals(100, GuiCraftingPlan.getProgress(
                flatPlan(TerminalCraftingJobStatus.FINISHED, 0, 10, Collections.emptyList())));
    }

    @Test
    public void testProgressCountsDependencies() {
        // 20 of the 30 operations remain, so a third is done
        assertEquals(33, GuiCraftingPlan.getProgress(flatPlan(TerminalCraftingJobStatus.CRAFTING, 10, 20,
                Collections.singletonList(plan(2, TerminalCraftingJobStatus.CRAFTING, 10, 10,
                        Collections.emptyList())))));
    }

}
