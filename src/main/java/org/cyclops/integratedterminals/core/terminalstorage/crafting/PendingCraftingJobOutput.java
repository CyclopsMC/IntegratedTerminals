package org.cyclops.integratedterminals.core.terminalstorage.crafting;

import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;

/**
 * The aggregated pending output of all running crafting jobs for a single ingredient instance.
 * @param <T> The instance type.
 * @author rubensworks
 */
public class PendingCraftingJobOutput<T> {

    private final T instance;
    private final TerminalCraftingJobStatus status;

    public PendingCraftingJobOutput(T instance, TerminalCraftingJobStatus status) {
        this.instance = instance;
        this.status = status;
    }

    /**
     * @return The pending instance, where the quantity indicates how much is still expected to be crafted.
     */
    public T getInstance() {
        return instance;
    }

    /**
     * @return The most relevant status over all crafting jobs that will produce this instance.
     */
    public TerminalCraftingJobStatus getStatus() {
        return status;
    }

}
