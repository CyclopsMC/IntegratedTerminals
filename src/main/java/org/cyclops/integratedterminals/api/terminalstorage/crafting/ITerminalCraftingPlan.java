package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;

import java.util.List;

/**
 * A job for crafting a given instance.
 *
 * It is possible that a job requires no actual crafting,
 * but can be fetched from storage completely.
 *
 * @author rubensworks
 */
public interface ITerminalCraftingPlan {

    /**
     * @return The dependencies of this job.
     */
    public List<ITerminalCraftingPlan> getDependencies();

    /**
     * @return The output instances of this job.
     */
    public List<IPrototypedIngredient<?, ?>> getOutputs();

    /**
     * @return The job status.
     */
    public TerminalCraftingJobStatus getStatus();

    /**
     * @return The number of instances that will be crafted..
     * (These are expected to become available later on because of a dependency job)
     */
    public long getCraftingQuantity();

    /**
     * @return The ingredients that will be used from storage.
     */
    public List<IPrototypedIngredient<?, ?>> getStorageIngredients();

    /**
     * @return A visual label for this plan, such as an error or plan type.
     */
    public String getUnlocalizedLabel();

}
