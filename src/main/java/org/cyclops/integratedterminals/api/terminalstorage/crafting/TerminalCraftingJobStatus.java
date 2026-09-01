package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import org.cyclops.cyclopscore.helper.IModHelpers;

/**
 * The status of a crafting job.
 * @author rubensworks
 */
public enum TerminalCraftingJobStatus {
    /**
     * A generic job error state.
     */
    ERROR(IModHelpers.get().getBaseHelpers().RGBAToInt(250, 0, 0, 150), false, 5),
    /**
     * If this job, or its dependencies, have missing storage instances.
     */
    INVALID(IModHelpers.get().getBaseHelpers().RGBAToInt(250, 10, 13, 150), false, 5),
    /**
     * No outputs have been crafted yet, and they are not scheduled yet for crafting.
     */
    UNSTARTED(IModHelpers.get().getBaseHelpers().RGBAToInt(225, 225, 225, 150), true, 1),
    /**
     * The crafting job has been scheduled,
     * but is not processing yet because other jobs are still processing.
     */
    QUEUEING(IModHelpers.get().getBaseHelpers().RGBAToInt(243, 245, 150, 150), true, 2),
    /**
     * The crafting job has been scheduled,
     * but is not processing yet because a dependency is still being processed.
     */
    PENDING_DEPENDENCIES(IModHelpers.get().getBaseHelpers().RGBAToInt(243, 245, 4, 150), true, 2),
    /**
     * The crafting job has been scheduled,
     * but is not processing yet because input ingredients are missing.
     */
    PENDING_INPUTS(IModHelpers.get().getBaseHelpers().RGBAToInt(245, 172, 3, 150), true, 4),
    /**
     * The recipe inputs could not be inserted into the crafting handler.
     */
    INVALID_INPUTS(IModHelpers.get().getBaseHelpers().RGBAToInt(250, 10, 13, 150), true, 5),
    /**
     * The output is actively being crafted.
     */
    CRAFTING(IModHelpers.get().getBaseHelpers().RGBAToInt(43, 174, 231, 150), true, 3),
    /**
     * All expected outputs are crafted.
     */
    FINISHED(IModHelpers.get().getBaseHelpers().RGBAToInt(43, 231, 47, 150), true, 0);

    private final int color;
    private final boolean valid;
    private final int priority;

    private TerminalCraftingJobStatus(int color, boolean valid, int priority) {
        this.color = color;
        this.valid = valid;
        this.priority = priority;
    }

    public int getColor() {
        return color;
    }

    public boolean isValid() {
        return valid;
    }

    /**
     * The relevance of this status when a single ingredient is produced by multiple crafting jobs,
     * in which case only the status with the highest priority is shown.
     *
     * Statuses that require the attention of the player take precedence over statuses that don't.
     *
     * @return The priority, where a higher number indicates a higher priority.
     */
    public int getPriority() {
        return priority;
    }
}
