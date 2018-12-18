package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.integratedcrafting.api.crafting.CraftingJobDependencyGraph;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingPlanStatic;

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
                                                          String unlocalizedLabel,
                                                          CraftingJobDependencyGraph craftingJobDependencyGraph) {
        super(id, dependencies, outputs, status, craftingQuantity, storageIngredients, unlocalizedLabel);
        this.craftingJobDependencyGraph = craftingJobDependencyGraph;
    }

    public CraftingJobDependencyGraph getCraftingJobDependencyGraph() {
        return craftingJobDependencyGraph;
    }
}
