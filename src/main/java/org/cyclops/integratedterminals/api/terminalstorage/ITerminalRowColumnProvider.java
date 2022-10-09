package org.cyclops.integratedterminals.api.terminalstorage;

/**
 * Can define a certain number of rows and columns for the terminal grid.
 * @author rubensworks
 */
public interface ITerminalRowColumnProvider {
    public RowsAndColumns getRowsAndColumns();

    public static record RowsAndColumns(int rows, int columns) { }
}
