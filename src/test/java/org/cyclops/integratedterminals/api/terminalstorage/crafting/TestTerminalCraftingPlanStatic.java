package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author rubensworks
 */
public class TestTerminalCraftingPlanStatic {

    protected static TerminalCraftingPlanStatic<Integer> plan(int id, TerminalCraftingJobStatus status,
                                                              long craftingQuantity, long craftingQuantityTotal,
                                                              List<ITerminalCraftingPlan<Integer>> dependencies) {
        return new TerminalCraftingPlanStatic<>(id, dependencies, Collections.emptyList(), status,
                craftingQuantity, craftingQuantityTotal, Collections.emptyList(), Collections.emptyList(),
                TerminalCraftingPlanStatic.Label.RUNNING, -1, -1, -1, 0, null);
    }

    @Test
    public void testCraftingQuantitiesWithoutDependencies() {
        ITerminalCraftingPlanFlat<Integer> flat = plan(1, TerminalCraftingJobStatus.CRAFTING, 3, 10,
                Collections.emptyList()).flatten();

        assertEquals(10, flat.getCraftingQuantityTotal());
        assertEquals(3, flat.getCraftingQuantityRemaining());
    }

    @Test
    public void testCraftingQuantitiesWithDependencies() {
        ITerminalCraftingPlanFlat<Integer> flat = plan(1, TerminalCraftingJobStatus.CRAFTING, 3, 10, Lists.newArrayList(
                plan(2, TerminalCraftingJobStatus.CRAFTING, 1, 2, Collections.emptyList()),
                plan(3, TerminalCraftingJobStatus.FINISHED, 0, 5, Collections.emptyList())
        )).flatten();

        assertEquals(17, flat.getCraftingQuantityTotal());
        assertEquals(4, flat.getCraftingQuantityRemaining());
    }

    @Test
    public void testCraftingQuantitiesWithSharedDependency() {
        // Due to job splitting, the same job can occur multiple times in a plan, but it may only be counted once.
        ITerminalCraftingPlan<Integer> shared = plan(4, TerminalCraftingJobStatus.CRAFTING, 1, 2, Collections.emptyList());
        ITerminalCraftingPlanFlat<Integer> flat = plan(1, TerminalCraftingJobStatus.CRAFTING, 3, 10, Lists.newArrayList(
                plan(2, TerminalCraftingJobStatus.CRAFTING, 1, 1, Lists.newArrayList(shared)),
                plan(3, TerminalCraftingJobStatus.CRAFTING, 1, 1, Lists.newArrayList(shared))
        )).flatten();

        assertEquals(14, flat.getCraftingQuantityTotal());
        assertEquals(6, flat.getCraftingQuantityRemaining());
    }

    @Test
    public void testCraftingQuantitiesWithInvalidDependency() {
        // Invalid jobs express missing ingredients in their crafting quantity, so they are not counted.
        ITerminalCraftingPlanFlat<Integer> flat = plan(1, TerminalCraftingJobStatus.UNSTARTED, 10, 10, Lists.newArrayList(
                plan(0, TerminalCraftingJobStatus.INVALID, 64, 64, Collections.emptyList())
        )).flatten();

        assertEquals(10, flat.getCraftingQuantityTotal());
        assertEquals(10, flat.getCraftingQuantityRemaining());
    }

    @Test
    public void testEstimatedTickDurationsAreInheritedByFlatPlan() {
        ITerminalCraftingPlanFlat<Integer> flat = new TerminalCraftingPlanStatic<>(1, Collections.emptyList(),
                Collections.emptyList(), TerminalCraftingJobStatus.CRAFTING, 3, 10, Collections.emptyList(),
                Collections.emptyList(), TerminalCraftingPlanStatic.Label.RUNNING, 20, 200, 140, 0, null)
                .flatten();

        assertEquals(20, flat.getTickDuration());
        assertEquals(200, flat.getEstimatedTickDurationTotal());
        assertEquals(140, flat.getEstimatedTickDurationRemaining());
    }

    @Test
    public void testUnknownValuesByDefault() {
        TerminalCraftingPlanStatic<Integer> plan = new TerminalCraftingPlanStatic<>(1, Collections.emptyList(),
                Collections.emptyList(), TerminalCraftingJobStatus.UNSTARTED, 10, Collections.emptyList(),
                Collections.emptyList(), TerminalCraftingPlanStatic.Label.VALID, -1, 0, null);

        assertEquals(10, plan.getCraftingQuantityTotal());
        assertEquals(-1, plan.getEstimatedTickDurationTotal());
        assertEquals(-1, plan.getEstimatedTickDurationRemaining());
    }

}
