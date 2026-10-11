package org.cyclops.integratedterminals.client.gui.container.component;

import com.google.common.collect.Lists;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
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

}
