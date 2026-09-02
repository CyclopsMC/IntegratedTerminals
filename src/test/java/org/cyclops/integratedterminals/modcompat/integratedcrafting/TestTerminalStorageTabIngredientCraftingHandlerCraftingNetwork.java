package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.cyclops.commoncapabilities.api.ingredient.MixedIngredients;
import org.cyclops.integratedcrafting.api.crafting.CraftingJob;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingPlanStatic;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author rubensworks
 */
public class TestTerminalStorageTabIngredientCraftingHandlerCraftingNetwork {

    private CraftingJob craftingJob;

    @Before
    public void beforeEach() {
        this.craftingJob = new CraftingJob(1, 0, null, 3, new MixedIngredients(Maps.newIdentityHashMap()));
    }

    protected static ITerminalCraftingPlan<Integer> planWithEstimations(long total, long remaining) {
        return new TerminalCraftingPlanStatic<>(1, Collections.emptyList(), Collections.emptyList(),
                TerminalCraftingJobStatus.CRAFTING, 1, 1, Collections.emptyList(), Collections.emptyList(),
                TerminalCraftingPlanStatic.Label.RUNNING, -1, total, remaining, 0, null);
    }

    @Test
    public void testEstimateWithoutMeasurements() {
        // Without a crafting network, no recipe durations are known
        assertEquals(-1, TerminalStorageTabIngredientCraftingHandlerCraftingNetwork
                .estimateTickDuration(null, craftingJob, 3, Collections.emptyList()));
    }

    @Test
    public void testEstimateFromDependencies() {
        // Dependencies are crafted simultaneously, so only the longest one counts
        List<ITerminalCraftingPlan<Integer>> dependencies = Lists.newArrayList(
                planWithEstimations(100, 40),
                planWithEstimations(250, 90));

        assertEquals(250, TerminalStorageTabIngredientCraftingHandlerCraftingNetwork
                .estimateTickDuration(null, craftingJob, 3, dependencies));
    }

    @Test
    public void testEstimateFromRemainingDependencies() {
        List<ITerminalCraftingPlan<Integer>> dependencies = Lists.newArrayList(
                planWithEstimations(100, 40),
                planWithEstimations(250, 90));

        assertEquals(90, TerminalStorageTabIngredientCraftingHandlerCraftingNetwork
                .estimateTickDuration(null, craftingJob, 3, dependencies,
                        ITerminalCraftingPlan::getEstimatedTickDurationRemaining));
    }

    @Test
    public void testEstimateWithUnknownDependencies() {
        List<ITerminalCraftingPlan<Integer>> dependencies = Lists.newArrayList(
                planWithEstimations(-1, -1),
                planWithEstimations(250, 90));

        assertEquals(250, TerminalStorageTabIngredientCraftingHandlerCraftingNetwork
                .estimateTickDuration(null, craftingJob, 3, dependencies));
    }

}
