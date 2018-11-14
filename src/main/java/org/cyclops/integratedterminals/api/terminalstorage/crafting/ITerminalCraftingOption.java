package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import java.util.Iterator;

/**
 * Identifies a crafting job possibility.
 * @param <T> The instance type.
 * @author rubensworks
 */
public interface ITerminalCraftingOption<T> {

    /**
     * @return The outputs of this crafting job option.
     */
    public Iterator<T> getOutputs();

}
