package org.cyclops.integratedterminals.client.gui.container.component;

import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlanFlat;

import java.util.function.Supplier;

/**
 * A helper class that allows toggling between a tree-based and flattened crafting plan view.
 * @author rubensworks
 */
public class GuiCraftingPlanToggler {

    private final Supplier<ITerminalCraftingPlan> craftingPlanSupplier;
    private final Supplier<ITerminalCraftingPlanFlat> craftingPlanFlatSupplier;
    private final Runnable initPlanTree;
    private final Runnable initPlanFlat;

    private CraftingPlanDisplayMode craftingPlanDisplayMode;

    public GuiCraftingPlanToggler(Supplier<ITerminalCraftingPlan> craftingPlanSupplier, Supplier<ITerminalCraftingPlanFlat> craftingPlanFlatSupplier, Runnable initPlanTree, Runnable initPlanFlat) {
        this.craftingPlanSupplier = craftingPlanSupplier;
        this.craftingPlanFlatSupplier = craftingPlanFlatSupplier;
        this.initPlanTree = initPlanTree;
        this.initPlanFlat = initPlanFlat;
    }

    public void init() {
        // Determine available display modes
        if (this.craftingPlanDisplayMode == null) {
            if (this.craftingPlanFlatSupplier.get() != null) {
                if (this.craftingPlanSupplier.get() == null) {
                    this.craftingPlanDisplayMode = CraftingPlanDisplayMode.FLAT;
                } else {
                    this.craftingPlanDisplayMode = GeneralConfig.terminalStorageDefaultToCraftingPlanTree ? CraftingPlanDisplayMode.TREE : CraftingPlanDisplayMode.FLAT;
                }
            } else {
                if (this.craftingPlanSupplier.get() == null) {
                    this.craftingPlanDisplayMode = CraftingPlanDisplayMode.NONE;
                } else {
                    this.craftingPlanDisplayMode = CraftingPlanDisplayMode.TREE;
                }
            }
        }

        // Prepare gui element for active display mode
        switch (this.craftingPlanDisplayMode) {
            case TREE -> this.initPlanTree.run();
            case FLAT -> this.initPlanFlat.run();
        }
    }

    public void setCraftingPlanDisplayMode(CraftingPlanDisplayMode craftingPlanDisplayMode) {
        this.craftingPlanDisplayMode = craftingPlanDisplayMode;
    }

    public CraftingPlanDisplayMode getCraftingPlanDisplayMode() {
        return craftingPlanDisplayMode;
    }

    public static enum CraftingPlanDisplayMode {
        NONE,
        TREE,
        FLAT
    }
}
