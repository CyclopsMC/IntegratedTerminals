package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A job for crafting a given instance.
 *
 * It is possible that a job requires no actual crafting,
 * but can be fetched from storage completely.
 *
 * @param <I> The type of identifier.
 * @author rubensworks
 */
public interface ITerminalCraftingPlan<I> {

    /**
     * @return The unique id of this plan.
     */
    public I getId();

    /**
     * @return The dependencies of this job.
     */
    public List<ITerminalCraftingPlan<I>> getDependencies();

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
     * @return The ingredients that were missing for 1 job amount.
     *         This should contain something when the status is {@link TerminalCraftingJobStatus#PENDING_INPUTS}.
     *         The inner list represents alternatives.
     */
    public List<List<IPrototypedIngredient<?, ?>>> getLastMissingIngredients();

    /**
     * @return A visual label for this plan, such as an error or plan type.
     */
    public String getUnlocalizedLabel();

    /**
     * @return The tick duration for this job. -1 indicates no duration.
     */
    public long getTickDuration();

    /**
     * @return The channel id, or -1 for non-applicable.
     */
    public int getChannel();

    /**
     * @return The initiator name of the crafting job.
     */
    @Nullable
    public String getInitiatorName();

    /**
     * Mark this plan as errored.
     * @param unlocalizedError An unlocalized error message.
     */
    public void setError(String unlocalizedError);

}
