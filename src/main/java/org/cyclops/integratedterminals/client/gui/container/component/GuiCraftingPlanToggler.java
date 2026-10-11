package org.cyclops.integratedterminals.client.gui.container.component;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlanFlat;
import org.cyclops.integratedterminals.client.gui.image.Images;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BooleanSupplier;
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

    /**
     * Create buttons for switching to each of the available display modes.
     * @param x The X position of the first button.
     * @param y The Y position of the buttons.
     * @param onChange Called after the display mode was changed.
     * @return The buttons.
     */
    public List<Button> createViewButtons(int x, int y, Runnable onChange) {
        List<Button> buttons = Lists.newArrayList();
        if (this.craftingPlanSupplier.get() != null) {
            buttons.add(createViewButton(x + buttons.size() * Images.BUTTON_BACKGROUND_ACTIVE.getWidth(), y, CraftingPlanDisplayMode.TREE,
                    "gui.integratedterminals.craftingplan.view.list", Images.BUTTON_MIDDLE_CRAFTING_PLAN_LIST, onChange));
            buttons.add(createViewButton(x + buttons.size() * Images.BUTTON_BACKGROUND_ACTIVE.getWidth(), y, CraftingPlanDisplayMode.GRAPH,
                    "gui.integratedterminals.craftingplan.view.graph", Images.BUTTON_MIDDLE_CRAFTING_PLAN_GRAPH, onChange));
        }
        if (this.craftingPlanFlatSupplier.get() != null) {
            buttons.add(createViewButton(x + buttons.size() * Images.BUTTON_BACKGROUND_ACTIVE.getWidth(), y, CraftingPlanDisplayMode.FLAT,
                    "gui.integratedterminals.craftingplan.view.flat", Images.BUTTON_MIDDLE_CRAFTING_PLAN_FLAT, onChange));
        }
        // A single view needs no switching
        if (buttons.size() <= 1) {
            buttons.clear();
        }
        return buttons;
    }

    protected Button createViewButton(int x, int y, CraftingPlanDisplayMode mode, String unlocalizedName, IImage image, Runnable onChange) {
        Component name = Component.translatable(unlocalizedName);
        Button button = new ButtonView(x, y, name, (b) -> {
            preferredDisplayMode = mode;
            if (this.craftingPlanDisplayMode != mode) {
                this.craftingPlanDisplayMode = mode;
                onChange.run();
            }
        }, image, this.craftingPlanDisplayMode == mode);
        button.setTooltip(Tooltip.create(name));
        return button;
    }

    /**
     * Create the zoom, fit and fullscreen buttons that are shown on top of a graph.
     * @param graph The graph.
     * @param onFullscreenToggle Called after fullscreen mode was toggled.
     * @return The buttons.
     */
    public static List<Button> createGraphButtons(GuiCraftingPlanGraph graph, Runnable onFullscreenToggle) {
        return Lists.newArrayList(
                new ButtonGraph(graph, 0, "gui.integratedterminals.craftingplan.graph.zoom_in",
                        Images.BUTTON_GRAPH_ZOOM_IN, (b) -> graph.zoomIn(), graph::canZoomIn),
                new ButtonGraph(graph, 1, "gui.integratedterminals.craftingplan.graph.zoom_out",
                        Images.BUTTON_GRAPH_ZOOM_OUT, (b) -> graph.zoomOut(), graph::canZoomOut),
                new ButtonGraph(graph, 2, "gui.integratedterminals.craftingplan.graph.fit",
                        Images.BUTTON_GRAPH_FIT, (b) -> graph.fit(), () -> true),
                new ButtonGraph(graph, 3, graphFullscreen
                        ? "gui.integratedterminals.craftingplan.graph.fullscreen.exit" : "gui.integratedterminals.craftingplan.graph.fullscreen",
                        graphFullscreen ? Images.BUTTON_GRAPH_FULLSCREEN_EXIT : Images.BUTTON_GRAPH_FULLSCREEN, (b) -> {
                            graphFullscreen = !graphFullscreen;
                            onFullscreenToggle.run();
                        }, () -> true)
        );
    }

    public static enum CraftingPlanDisplayMode {
        NONE,
        TREE,
        GRAPH,
        FLAT
    }

    /**
     * A button for switching to a display mode, which is shown as pressed when its mode is active.
     */
    public static class ButtonView extends ButtonImage {

        private final boolean selected;

        public ButtonView(int x, int y, Component narrationMessage, OnPress pressCallback, IImage image, boolean selected) {
            super(x, y, narrationMessage, pressCallback, image);
            this.selected = selected;
        }

        @Override
        protected void drawButtonInner(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            (selected ? Images.BUTTON_BACKGROUND_ACTIVE : Images.BUTTON_BACKGROUND_INACTIVE).draw(guiGraphics, getX(), getY());
            super.drawButtonInner(guiGraphics, mouseX, mouseY);
            if (isHovered() && !selected) {
                guiGraphics.fill(getX() + 1, getY() + 1, getX() + getWidth() - 2, getY() + getHeight() - 2, 0x30FFFFFF);
            }
        }
    }

    /**
     * A button that is shown on top of the graph viewport.
     */
    public static class ButtonGraph extends ButtonImage {

        private final IImage image;
        private final BooleanSupplier activeSupplier;

        public ButtonGraph(GuiCraftingPlanGraph graph, int index, String unlocalizedName, IImage image, OnPress onPress,
                           BooleanSupplier activeSupplier) {
            super(graph.getOverlayButtonX(), graph.getOverlayButtonY(index),
                    GuiCraftingPlanGraph.OVERLAY_BUTTON_SIZE, GuiCraftingPlanGraph.OVERLAY_BUTTON_SIZE,
                    Component.translatable(unlocalizedName), onPress, true, image, 0, 0);
            this.image = image;
            this.activeSupplier = activeSupplier;
            setTooltip(Tooltip.create(Component.translatable(unlocalizedName)));
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            this.active = activeSupplier.getAsBoolean();
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        }

        @Override
        protected void drawButtonInner(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if (this.active) {
                super.drawButtonInner(guiGraphics, mouseX, mouseY);
            } else {
                image.drawWithColor(guiGraphics, getX(), getY(), 0.5F, 0.5F, 0.5F, 1F);
            }
        }
    }
}
