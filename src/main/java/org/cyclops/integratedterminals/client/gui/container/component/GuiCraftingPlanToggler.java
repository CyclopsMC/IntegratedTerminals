package org.cyclops.integratedterminals.client.gui.container.component;

import net.minecraft.util.Mth;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlanFlat;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * A helper class that allows toggling between a tree-based, graph-based and flattened crafting plan view.
 * @author rubensworks
 */
public class GuiCraftingPlanToggler {

    public static final int DEFAULT_WIDTH = 256;
    public static final int DEFAULT_HEIGHT = 222;
    private static final int GRAPH_MAX_WIDTH = 400;
    private static final int GRAPH_MAX_HEIGHT = 300;
    private static final int SCREEN_MARGIN = 16;

    /**
     * If the graph view is shown fullscreen, shared by all crafting plan guis for the session.
     */
    private static boolean graphFullscreen = false;
    /**
     * The display mode the player last chose, shared by all crafting plan guis for the session.
     */
    @Nullable
    private static CraftingPlanDisplayMode preferredDisplayMode = null;

    private final Supplier<ITerminalCraftingPlan> craftingPlanSupplier;
    private final Supplier<ITerminalCraftingPlanFlat> craftingPlanFlatSupplier;
    private final Runnable initPlanTree;
    private final Runnable initPlanGraph;
    private final Runnable initPlanFlat;
    private final Runnable deinit;

    private CraftingPlanDisplayMode craftingPlanDisplayMode;

    public GuiCraftingPlanToggler(Supplier<ITerminalCraftingPlan> craftingPlanSupplier, Supplier<ITerminalCraftingPlanFlat> craftingPlanFlatSupplier,
                                  Runnable initPlanTree, Runnable initPlanGraph, Runnable initPlanFlat, Runnable deinit) {
        this.craftingPlanSupplier = craftingPlanSupplier;
        this.craftingPlanFlatSupplier = craftingPlanFlatSupplier;
        this.initPlanTree = initPlanTree;
        this.initPlanGraph = initPlanGraph;
        this.initPlanFlat = initPlanFlat;
        this.deinit = deinit;
    }

    /**
     * Determine the active display mode, without initializing it.
     * This can be called before the gui size is determined.
     */
    public void resolveDisplayMode() {
        // Determine available display modes
        if (this.craftingPlanDisplayMode == null) {
            this.craftingPlanDisplayMode = getDefaultDisplayMode(this.craftingPlanSupplier.get() != null,
                    this.craftingPlanFlatSupplier.get() != null, preferredDisplayMode,
                    GeneralConfig.terminalStorageDefaultToCraftingPlanTree);
        }

        // Reset display mode if both plan views became empty
        if (this.craftingPlanFlatSupplier.get() == null && this.craftingPlanSupplier.get() == null) {
            this.craftingPlanDisplayMode = CraftingPlanDisplayMode.NONE;
        }
    }

    /**
     * @param hasTree If a tree-based plan is available.
     * @param hasFlat If a flattened plan is available.
     * @param preferred The mode the player last chose, or null.
     * @param defaultToTree If a tree-based view should be preferred over the flattened view.
     * @return The display mode to show when a plan is opened.
     */
    public static CraftingPlanDisplayMode getDefaultDisplayMode(boolean hasTree, boolean hasFlat,
                                                                @Nullable CraftingPlanDisplayMode preferred,
                                                                boolean defaultToTree) {
        if (preferred != null && (preferred == CraftingPlanDisplayMode.FLAT ? hasFlat : hasTree)) {
            return preferred;
        }
        if (hasTree && (defaultToTree || !hasFlat)) {
            return CraftingPlanDisplayMode.GRAPH;
        }
        return hasFlat ? CraftingPlanDisplayMode.FLAT : CraftingPlanDisplayMode.NONE;
    }

    public void init() {
        resolveDisplayMode();

        // Prepare gui element for active display mode
        switch (this.craftingPlanDisplayMode) {
            case TREE -> this.initPlanTree.run();
            case GRAPH -> this.initPlanGraph.run();
            case FLAT -> this.initPlanFlat.run();
            case NONE -> this.deinit.run();
        }
    }

    public void setCraftingPlanDisplayMode(CraftingPlanDisplayMode craftingPlanDisplayMode) {
        this.craftingPlanDisplayMode = craftingPlanDisplayMode;
    }

    public CraftingPlanDisplayMode getCraftingPlanDisplayMode() {
        return craftingPlanDisplayMode;
    }

    public static boolean isGraphFullscreen() {
        return graphFullscreen;
    }

    public static void setGraphFullscreen(boolean graphFullscreen) {
        GuiCraftingPlanToggler.graphFullscreen = graphFullscreen;
    }

    /**
     * @param screenWidth The scaled screen width.
     * @return The gui width for the active display mode.
     */
    public int getWidth(int screenWidth) {
        if (this.craftingPlanDisplayMode != CraftingPlanDisplayMode.GRAPH) {
            return DEFAULT_WIDTH;
        }
        if (graphFullscreen) {
            return Math.max(DEFAULT_WIDTH, screenWidth - SCREEN_MARGIN);
        }
        return clamp(Math.round(screenWidth * 0.6F), DEFAULT_WIDTH, Math.min(GRAPH_MAX_WIDTH, screenWidth - SCREEN_MARGIN));
    }

    /**
     * @param screenHeight The scaled screen height.
     * @return The gui height for the active display mode.
     */
    public int getHeight(int screenHeight) {
        if (this.craftingPlanDisplayMode != CraftingPlanDisplayMode.GRAPH) {
            return DEFAULT_HEIGHT;
        }
        if (graphFullscreen) {
            return Math.max(DEFAULT_HEIGHT, screenHeight - SCREEN_MARGIN);
        }
        return clamp(Math.round(screenHeight * 0.8F), DEFAULT_HEIGHT, Math.min(GRAPH_MAX_HEIGHT, screenHeight - SCREEN_MARGIN));
    }

    protected static int clamp(int value, int min, int max) {
        // The minimum wins on screens that are too small
        return Math.max(min, Mth.clamp(value, min, max));
    }

    public boolean hasTreePlan() {
        return this.craftingPlanSupplier.get() != null;
    }

    public boolean hasFlatPlan() {
        return this.craftingPlanFlatSupplier.get() != null;
    }

    /**
     * Switch to a display mode that the player picked, and remember it for the next plans.
     * @param mode The display mode.
     * @return If the display mode changed.
     */
    public boolean selectDisplayMode(CraftingPlanDisplayMode mode) {
        preferredDisplayMode = mode;
        if (this.craftingPlanDisplayMode != mode) {
            this.craftingPlanDisplayMode = mode;
            return true;
        }
        return false;
    }

    public static enum CraftingPlanDisplayMode {
        NONE,
        TREE,
        GRAPH,
        FLAT
    }
}
