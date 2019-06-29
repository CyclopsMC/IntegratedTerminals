package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import org.cyclops.cyclopscore.helper.Helpers;

/**
 * The status of a crafting job.
 * @author rubensworks
 */
public enum TerminalCraftingJobStatus {
    /**
     * A generic job error state.
     */
    ERROR(Helpers.RGBAToInt(250, 0, 0, 150), false),
    /**
     * If this job, or its dependencies, have missing storage instances.
     */
    INVALID(Helpers.RGBAToInt(250, 10, 13, 150), false),
    /**
     * No outputs have been crafted yet, and they are not scheduled yet for crafting.
     */
    UNSTARTED(Helpers.RGBAToInt(225, 225, 225, 150), true),
    /**
     * The crafting job has been scheduled,
     * but is not processing yet because other jobs are still processing.
     */
    QUEUEING(Helpers.RGBAToInt(243, 245, 150, 150), true),
    /**
     * The crafting job has been scheduled,
     * but is not processing yet because a dependency is still being processed.
     */
    PENDING_DEPENDENCIES(Helpers.RGBAToInt(243, 245, 4, 150), true),
    /**
     * The crafting job has been scheduled,
     * but is not processing yet because input ingredients are missing.
     */
    PENDING_INPUTS(Helpers.RGBAToInt(245, 172, 3, 150), true),
    /**
     * The recipe inputs could not be inserted into the crafting handler.
     */
    INVALID_INPUTS(Helpers.RGBAToInt(250, 10, 13, 150), true),
    /**
     * The output is actively being crafted.
     */
    CRAFTING(Helpers.RGBAToInt(43, 174, 231, 150), true),
    /**
     * All expected outputs are crafted.
     */
    FINISHED(Helpers.RGBAToInt(43, 231, 47, 150), true);

    private final int color;
    private final boolean valid;

    private TerminalCraftingJobStatus(int color, boolean valid) {
        this.color = color;
        this.valid = valid;
    }

    public int getColor() {
        return color;
    }

    public boolean isValid() {
        return valid;
    }
}
