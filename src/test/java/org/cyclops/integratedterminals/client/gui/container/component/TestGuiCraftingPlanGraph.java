package org.cyclops.integratedterminals.client.gui.container.component;

import com.google.common.collect.Lists;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author rubensworks
 */
public class TestGuiCraftingPlanGraph {

    @Test
    public void testPruneLeavesWithoutOutputs() {
        // Plans without outputs have nothing to show, unless they lead to something that does
        ITerminalCraftingPlan<Integer> plan = TestGuiCraftingPlanProgress.plan(1, TerminalCraftingJobStatus.CRAFTING, 1, 1,
                Lists.newArrayList(
                        TestGuiCraftingPlanProgress.plan(2, TerminalCraftingJobStatus.FINISHED, 0, 2, Collections.emptyList()),
                        TestGuiCraftingPlanProgress.plan(3, TerminalCraftingJobStatus.FINISHED, 0, 3,
                                Lists.newArrayList(TestGuiCraftingPlanProgress.plan(4, TerminalCraftingJobStatus.FINISHED, 0, 3,
                                        Collections.emptyList())))
                ));
        List<GuiCraftingPlan.Element> elements = GuiCraftingPlan.getElements(plan);
        GuiCraftingPlan.Element root = elements.get(0);
        assertEquals(2, root.getChildren().size());

        GuiCraftingPlanGraph.pruneEmptyLeaves(root);
        assertEquals(0, root.getChildren().size());
    }

    @Test
    public void testFlowDashesCoverTheWholeLine() {
        // For every animation phase, the dashes must follow the same pattern from the start up to the very end of a line,
        // without a gap at either end
        int period = 6;
        int length = 2;
        for (int from : new int[]{-7, 0, 3, 100}) {
            for (int size : new int[]{1, 2, 5, 6, 7, 22, 23}) {
                int to = from + size;
                for (int offset = 0; offset < period; offset++) {
                    boolean[] drawn = new boolean[size];
                    for (int[] dash : GuiCraftingPlanGraph.getFlowDashes(from, to, offset, period, length)) {
                        assertTrue(dash[0] >= from && dash[1] <= to && dash[0] < dash[1]);
                        for (int x = dash[0]; x < dash[1]; x++) {
                            drawn[x - from] = true;
                        }
                    }
                    for (int x = from; x < to; x++) {
                        boolean expected = Math.floorMod(x + offset, period) < length;
                        assertEquals("from " + from + " size " + size + " offset " + offset + " at " + x,
                                expected, drawn[x - from]);
                    }
                }
            }
        }
    }

    @Test
    public void testFlowDashesMoveTowardsStart() {
        // Increasing the offset moves every dash one position towards the start of the line
        int[] first = GuiCraftingPlanGraph.getFlowDashes(10, 40, 0, 6, 2).get(1);
        int[] next = GuiCraftingPlanGraph.getFlowDashes(10, 40, 1, 6, 2).get(1);
        assertEquals(first[0] - 1, next[0]);
    }

    protected static ITerminalCraftingPlan<Integer> job(int id, TerminalCraftingJobStatus status, long quantity, long total,
                                                        ITerminalCraftingPlan<Integer>... dependencies) {
        return TestGuiCraftingPlanProgress.plan(id, status, quantity, total, Lists.newArrayList(dependencies));
    }

    protected static List<GuiCraftingPlan.Element> update(List<GuiCraftingPlan.Element> previous, ITerminalCraftingPlan<Integer> plan) {
        List<GuiCraftingPlan.Element> elements = GuiCraftingPlan.getElements(plan);
        GuiCraftingPlanGraph.restoreToggleState(previous, elements);
        GuiCraftingPlanGraph.restoreCompletedJobs(previous, elements);
        return elements;
    }

    protected static int idOf(int planId) {
        return GuiCraftingPlan.getElements(job(planId, TerminalCraftingJobStatus.UNSTARTED, 1, 1)).get(0).getId();
    }

    @Test
    public void testCompletedJobsAreKept() {
        List<GuiCraftingPlan.Element> first = GuiCraftingPlan.getElements(job(1, TerminalCraftingJobStatus.PENDING_DEPENDENCIES, 1, 1,
                job(2, TerminalCraftingJobStatus.CRAFTING, 2, 4,
                        job(3, TerminalCraftingJobStatus.FINISHED, 0, 8)),
                job(4, TerminalCraftingJobStatus.QUEUEING, 3, 3)));

        // Job 2 is done, so it is no longer part of the plan
        List<GuiCraftingPlan.Element> second = update(first, job(1, TerminalCraftingJobStatus.PENDING_DEPENDENCIES, 1, 1,
                job(4, TerminalCraftingJobStatus.CRAFTING, 3, 3)));
        GuiCraftingPlan.Element root = second.get(0);
        assertEquals(2, root.getChildren().size());
        GuiCraftingPlan.Element completed = root.getChildren().get(0);
        assertEquals(idOf(2), completed.getId());
        assertTrue(completed.isCompleted());
        assertTrue(completed.isCraftingJob());
        assertEquals(TerminalCraftingJobStatus.FINISHED, completed.getStatus());
        assertEquals(0, completed.getCraftQuantity());
        assertEquals(4, completed.getCraftQuantityTotal());
        // It stays in its place before the job that is still running, which is unchanged
        assertEquals(idOf(4), root.getChildren().get(1).getId());
        assertEquals(TerminalCraftingJobStatus.CRAFTING, root.getChildren().get(1).getStatus());
        // Its completed sub-job is kept, and stays expanded so that the layout does not change by itself
        assertEquals(1, completed.getChildren().size());
        assertEquals(idOf(3), completed.getChildren().get(0).getId());
        assertTrue(completed.getChildren().get(0).isCompleted());
        assertTrue(completed.getChildren().get(0).isEnabled());
        assertTrue(second.contains(completed.getChildren().get(0)));

        // The player collapses the completed job, and then job 4 is done as well
        completed.getChildren().get(0).setEnabled(false);
        List<GuiCraftingPlan.Element> third = update(second, job(1, TerminalCraftingJobStatus.PENDING_DEPENDENCIES, 1, 1));
        root = third.get(0);
        assertEquals(2, root.getChildren().size());
        assertEquals(idOf(2), root.getChildren().get(0).getId());
        assertEquals(idOf(4), root.getChildren().get(1).getId());
        assertTrue(root.getChildren().get(1).isCompleted());
        // Jobs that were already completed keep their collapsed state
        assertFalse(root.getChildren().get(0).getChildren().get(0).isEnabled());
    }

    @Test
    public void testCompletedJobsOfAnotherPlanAreIgnored() {
        List<GuiCraftingPlan.Element> first = GuiCraftingPlan.getElements(job(1, TerminalCraftingJobStatus.CRAFTING, 1, 1,
                job(2, TerminalCraftingJobStatus.CRAFTING, 1, 1)));
        List<GuiCraftingPlan.Element> second = update(first, job(5, TerminalCraftingJobStatus.CRAFTING, 1, 1));
        assertTrue(second.get(0).getChildren().isEmpty());
        assertEquals(1, second.size());
    }

    @Test
    public void testDisplayStatus() {
        // Jobs that are reported as finished in between their crafting operations are still in progress
        GuiCraftingPlan.Element running = GuiCraftingPlan.getElements(job(1, TerminalCraftingJobStatus.FINISHED, 3, 12)).get(0);
        assertEquals(TerminalCraftingJobStatus.CRAFTING, GuiCraftingPlanGraph.getDisplayStatus(running));
        GuiCraftingPlan.Element done = GuiCraftingPlan.getElements(job(1, TerminalCraftingJobStatus.FINISHED, 0, 12)).get(0);
        assertEquals(TerminalCraftingJobStatus.FINISHED, GuiCraftingPlanGraph.getDisplayStatus(done));
        GuiCraftingPlan.Element waiting = GuiCraftingPlan.getElements(job(1, TerminalCraftingJobStatus.QUEUEING, 3, 12)).get(0);
        assertEquals(TerminalCraftingJobStatus.QUEUEING, GuiCraftingPlanGraph.getDisplayStatus(waiting));
    }

    @Test
    public void testProgressCountsCompletedJobs() {
        // 20 smelts and 4 crafts, of which 12 smelts and 1 craft are done
        ITerminalCraftingPlan<Integer> running = job(1, TerminalCraftingJobStatus.PENDING_DEPENDENCIES, 3, 4,
                job(2, TerminalCraftingJobStatus.CRAFTING, 8, 20));
        List<GuiCraftingPlan.Element> elements = GuiCraftingPlan.getElements(running);
        assertEquals(54, GuiCraftingPlanGraph.getProgress(elements));

        // A finished job leaves the plan, but still counts as done
        elements = update(elements, job(1, TerminalCraftingJobStatus.CRAFTING, 3, 4));
        assertEquals(87, GuiCraftingPlanGraph.getProgress(elements));
    }

    @Test
    public void testProgressCountsSharedJobsOnce() {
        ITerminalCraftingPlan<Integer> shared = job(3, TerminalCraftingJobStatus.CRAFTING, 5, 10);
        ITerminalCraftingPlan<Integer> plan = job(1, TerminalCraftingJobStatus.PENDING_DEPENDENCIES, 10, 10,
                job(2, TerminalCraftingJobStatus.PENDING_DEPENDENCIES, 0, 0, shared), shared);
        assertEquals(25, GuiCraftingPlanGraph.getProgress(GuiCraftingPlan.getElements(plan)));
    }

    @Test
    public void testProgressUnknownWithoutCrafts() {
        assertEquals(-1, GuiCraftingPlanGraph.getProgress(GuiCraftingPlan.getElements(job(1, TerminalCraftingJobStatus.UNSTARTED, 0, 0))));
    }

    @Test
    public void testRefusedInputsAreBlockedNotFailed() {
        // Targets refuse inputs while they are busy, so this is a temporary state
        GuiCraftingPlan.Element refused = GuiCraftingPlan.getElements(job(1, TerminalCraftingJobStatus.INVALID_INPUTS, 3, 3)).get(0);
        assertFalse(GuiCraftingPlanGraph.isFailed(refused.getStatus()));
        assertFalse(GuiCraftingPlanGraph.isProblem(refused));
        assertEquals(GuiCraftingPlanGraph.getStatusColor(TerminalCraftingJobStatus.PENDING_INPUTS),
                GuiCraftingPlanGraph.getStatusColor(TerminalCraftingJobStatus.INVALID_INPUTS));
        assertTrue(GuiCraftingPlanGraph.isFailed(TerminalCraftingJobStatus.INVALID));
    }

}
