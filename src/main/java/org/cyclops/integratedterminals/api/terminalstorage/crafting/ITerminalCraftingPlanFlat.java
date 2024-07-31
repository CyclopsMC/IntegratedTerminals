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
         */
        public IPrototypedIngredient<?, ?> getInstance();

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
