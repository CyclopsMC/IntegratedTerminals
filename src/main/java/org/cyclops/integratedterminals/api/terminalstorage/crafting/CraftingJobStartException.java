package org.cyclops.integratedterminals.api.terminalstorage.crafting;

/**
 * An exception that is thrown when a crafting job could not be effectively started.
 * @author rubensworks
 */
public class CraftingJobStartException extends Exception {

    private final String unlocalizedError;

    public CraftingJobStartException(String unlocalizedError) {
        this.unlocalizedError = unlocalizedError;
    }

    public String getUnlocalizedError() {
        return unlocalizedError;
    }
}
