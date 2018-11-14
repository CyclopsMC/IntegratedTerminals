package org.cyclops.integratedterminals.api.terminalstorage.crafting;

/**
 * The status of a crafting job.
 * @author rubensworks
 */
public enum TerminalCraftingJobStatus {
    /**
     * No outputs have been crafted yet, and they are not scheduled yet for crafting.
     */
    UNSTARTED,
    /**
     * The job is scheduled for crafting.
     */
    SCHEDULED,
    /**
     * The output is actively being crafted.
     */
    CRAFTING,
    /**
     * All expected outputs are crafted.
     */
    FINISHED
}
