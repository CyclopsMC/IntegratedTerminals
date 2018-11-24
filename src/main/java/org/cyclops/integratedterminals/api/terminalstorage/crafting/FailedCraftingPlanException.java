package org.cyclops.integratedterminals.api.terminalstorage.crafting;

/**
 * An exception for significantly failing crafting plans.
 * Things such as missing ingredients should not use this exceptions,
 * but things like infinite recursive recipes should.
 * @author rubensworks
 */
public class FailedCraftingPlanException extends Exception {

    public FailedCraftingPlanException(String message) {
        super(message);
    }

}
