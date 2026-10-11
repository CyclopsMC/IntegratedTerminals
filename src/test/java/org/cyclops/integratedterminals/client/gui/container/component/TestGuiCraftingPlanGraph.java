package org.cyclops.integratedterminals.client.gui.container.component;

import com.google.common.collect.Lists;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

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

}
