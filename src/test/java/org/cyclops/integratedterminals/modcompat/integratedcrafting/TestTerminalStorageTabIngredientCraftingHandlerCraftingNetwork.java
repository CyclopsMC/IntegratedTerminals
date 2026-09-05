package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import com.google.common.collect.Lists;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingPlanStatic;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author rubensworks
 */
public class TestTerminalStorageTabIngredientCraftingHandlerCraftingNetwork {

    protected static ITerminalCraftingPlan<Integer> planWithEstimations(long total, long remaining) {
        return new TerminalCraftingPlanStatic<>(1, Collections.emptyList(), Collections.emptyList(),
                TerminalCraftingJobStatus.CRAFTING, 1, 1, Collections.emptyList(), Collections.emptyList(),
                TerminalCraftingPlanStatic.Label.RUNNING, -1, total, remaining, 0, null);
    }

    @Test
    public void testEstimateWithoutDependencies() {
        assertEquals(150, TerminalStorageTabIngredientCraftingHandlerCraftingNetwork
                .estimateTickDuration(50, 3, Collections.emptyList()));
    }

    @Test
    public void testEstimateWithoutMeasurements() {
        // Without a measured recipe duration, there is nothing to base the estimation on
        assertEquals(-1, TerminalStorageTabIngredientCraftingHandlerCraftingNetwork
                .estimateTickDuration(-1, 3, Collections.emptyList()));
    }

    @Test
    public void testEstimateFromDependencies() {
        // Dependencies are crafted simultaneously, so only the longest one counts
        List<ITerminalCraftingPlan<Integer>> dependencies = Lists.newArrayList(
                planWithEstimations(100, 40),
                planWithEstimations(250, 90));

        assertEquals(400, TerminalStorageTabIngredientCraftingHandlerCraftingNetwork
                .estimateTickDuration(50, 3, dependencies));
    }

    @Test
    public void testEstimateFromRemainingDependencies() {
        List<ITerminalCraftingPlan<Integer>> dependencies = Lists.newArrayList(
                planWithEstimations(100, 40),
                planWithEstimations(250, 90));

        assertEquals(240, TerminalStorageTabIngredientCraftingHandlerCraftingNetwork
                .estimateTickDuration(50, 3, dependencies,
                        ITerminalCraftingPlan::getEstimatedTickDurationRemaining));
    }

    @Test
    public void testEstimateWithUnknownDependencies() {
        // An unknown dependency only lowers the maximum, which the other dependency still wins
        List<ITerminalCraftingPlan<Integer>> dependencies = Lists.newArrayList(
                planWithEstimations(-1, -1),
                planWithEstimations(250, 90));

        assertEquals(400, TerminalStorageTabIngredientCraftingHandlerCraftingNetwork
                .estimateTickDuration(50, 3, dependencies));
    }

    @Test
    public void testEstimateWithUnknownRecipeButKnownDependencies() {
        // The operations of the job itself are missing from the estimation, so it can not be given
        List<ITerminalCraftingPlan<Integer>> dependencies = Lists.newArrayList(
                planWithEstimations(250, 90));

        assertEquals(-1, TerminalStorageTabIngredientCraftingHandlerCraftingNetwork
                .estimateTickDuration(-1, 3, dependencies));
    }

    @Test
    public void testEstimateWithUnknownRecipeWithoutOperationsLeft() {
        // Without operations left, the unknown recipe duration does not contribute anything anyway
        List<ITerminalCraftingPlan<Integer>> dependencies = Lists.newArrayList(
                planWithEstimations(250, 90));

        assertEquals(250, TerminalStorageTabIngredientCraftingHandlerCraftingNetwork
                .estimateTickDuration(-1, 0, dependencies));
    }

    @Test
    public void testEstimateWithoutOperationsLeftAndUnknownDependencies() {
        assertEquals(-1, TerminalStorageTabIngredientCraftingHandlerCraftingNetwork
                .estimateTickDuration(-1, 0, Collections.emptyList()));
    }

}
