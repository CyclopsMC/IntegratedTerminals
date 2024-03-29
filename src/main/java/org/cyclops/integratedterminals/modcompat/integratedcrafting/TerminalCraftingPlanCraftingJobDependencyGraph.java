package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.integratedcrafting.api.crafting.CraftingJobDependencyGraph;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingPlanStatic;

import javax.annotation.Nullable;
import java.util.List;

/**
 * An extension of {@link TerminalCraftingPlanStatic} that saves a {@link CraftingJobDependencyGraph}.
 * @author rubensworks
 */
public class TerminalCraftingPlanCraftingJobDependencyGraph extends TerminalCraftingPlanStatic<Integer> {

    private final CraftingJobDependencyGraph craftingJobDependencyGraph;

    public TerminalCraftingPlanCraftingJobDependencyGraph(int id,
                                                          List<ITerminalCraftingPlan<Integer>> dependencies,
                                                          List<IPrototypedIngredient<?, ?>> outputs,
                                                          TerminalCraftingJobStatus status,
                                                          long craftingQuantity,
                                                          List<IPrototypedIngredient<?, ?>> storageIngredients,
                                                          List<List<IPrototypedIngredient<?, ?>>> lastMissingIngredients,
                                                          TerminalCraftingPlanStatic.Label label,
                                                          long tickDuration,
                                                          int channel,
                                                          @Nullable String initiatorName,
                                                          CraftingJobDependencyGraph craftingJobDependencyGraph) {
        super(id, dependencies, outputs, status, craftingQuantity, storageIngredients, lastMissingIngredients,
                label, tickDuration, channel, initiatorName);
        this.craftingJobDependencyGraph = craftingJobDependencyGraph;
    }

    public CraftingJobDependencyGraph getCraftingJobDependencyGraph() {
        return craftingJobDependencyGraph;
    }
}
