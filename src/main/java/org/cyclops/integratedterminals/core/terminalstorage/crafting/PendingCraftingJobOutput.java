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

    /**
     * Determine how relevant the given status is when multiple crafting jobs produce the same instance.
     *
     * Statuses that require the attention of the player take precedence over statuses that don't.
     *
     * @param status A crafting job status.
     * @return The priority of the given status, where a higher number indicates a higher priority.
     */
    public static int getStatusPriority(TerminalCraftingJobStatus status) {
        return switch (status) {
            case ERROR, INVALID, INVALID_INPUTS -> 5;
            case PENDING_INPUTS -> 4;
            case CRAFTING -> 3;
            case PENDING_DEPENDENCIES, QUEUEING -> 2;
            case UNSTARTED -> 1;
            case FINISHED -> 0;
        };
    }

}
