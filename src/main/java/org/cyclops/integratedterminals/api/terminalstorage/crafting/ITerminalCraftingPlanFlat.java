package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A job for crafting a given instance.
 * This is a flattened representation of {@link ITerminalCraftingPlan}.
 *
 * It is possible that a job requires no actual crafting,
 * but can be fetched from storage completely.
 *
 * @param <I> The type of identifier.
 * @author rubensworks
 */
public interface ITerminalCraftingPlanFlat<I> {

    /**
     * @return The unique id of this plan.
     */
    public I getId();

    /**
     * @return The flattened entries that are crafted as part of this plan.
     */
    public List<? extends IEntry> getEntries();

    /**
     * @return The final output instances of this job.
     */
    public List<IPrototypedIngredient<?, ?>> getOutputs();

    /**
     * @return The job status.
     */
    public TerminalCraftingJobStatus getStatus();

    /**
     * @return A visual label for this plan, such as an error or plan type.
     */
    public String getUnlocalizedLabel();

    /**
     * @return The tick duration for this job. -1 indicates no duration.
     */
    public long getTickDuration();

    /**
     * @return The number of crafting operations in this plan, including the ones that finished already.
     *         0 indicates an unknown quantity.
     */
    public default long getCraftingQuantityTotal() {
        return 0;
    }

    /**
     * @return The number of crafting operations in this plan that still have to be performed.
     *         0 indicates an unknown quantity, or a plan without remaining operations.
     */
    public default long getCraftingQuantityRemaining() {
        return 0;
    }

    /**
     * Contrary to {@link #getTickDuration()}, which indicates how long this job has been running already,
     * this is an estimation of how long this job takes from start to finish.
     *
     * @return The estimated total tick duration for this job. -1 indicates an unknown duration.
     */
    public default long getEstimatedTickDurationTotal() {
        return -1;
    }

    /**
     * @return The estimated tick duration until this job is finished. -1 indicates an unknown duration.
     */
    public default long getEstimatedTickDurationRemaining() {
        return -1;
    }

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

    public static interface IEntry {

        /**
         * @return The entry instance.
         * @deprecated Use {@link #getInstances()} instead. TODO: rm in next major
         */
        @Deprecated
        public IPrototypedIngredient<?, ?> getInstance();

        /**
         * @return The alternative entry instances for this entry. Never empty.
         */
        public List<IPrototypedIngredient<?, ?>> getInstances();

        /**
         * @return The number of instances to craft.
         */
        public long getQuantityToCraft();

        /**
         * @return The number of instances to craft.
         */
        public long getQuantityCrafting();

        /**
         * @return The number of instances in storage.
         */
        public long getQuantityInStorage();

        /**
         * @return The number of instances missing from storage.
         */
        public long getQuantityMissing();

    }

}
