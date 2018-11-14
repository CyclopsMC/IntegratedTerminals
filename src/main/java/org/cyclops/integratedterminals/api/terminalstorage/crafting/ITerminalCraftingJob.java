package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import java.util.List;

/**
 * A job for crafting a given instance.
 *
 * It is possible that a job requires no actual crafting,
 * but can be fetched from storage completely.
 *
 * @param <T> The instance type.
 * @author rubensworks
 */
public interface ITerminalCraftingJob<T> {

    /**
     * @return The dependencies of this job.
     */
    public List<ITerminalCraftingJob<T>> getDependencies();

    /**
     * @return The output instance of this job.
     */
    public T getOutput();

    /**
     * @return The job status.
     */
    public TerminalCraftingJobStatus getStatus();

    /**
     * @return The number of craftable instances that are still missing.
     * (These are expected to become available later on because of a dependency job)
     */
    public long getCraftingMissing();

    /**
     * @return The number of non-craftable instances that are missing.
     */
    public long getStorageMissing();

}
