package org.cyclops.integratedterminals.client.gui.container.component;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integratedterminals.client.gui.image.Images;

import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Buttons that are shared by the crafting plan guis.
 * @author rubensworks
 */
public class GuiCraftingPlanButtons {

    /**
     * Create buttons for switching to each of the available display modes.
     * @param toggler The display mode toggler.
     * @param x The X position of the first button.
     * @param y The Y position of the buttons.
     * @param onChange Called after the display mode was changed.
     * @return The buttons.
     */
    public static List<Button> createViewButtons(GuiCraftingPlanToggler toggler, int x, int y, Runnable onChange) {
        List<Button> buttons = Lists.newArrayList();
        int width = Images.BUTTON_BACKGROUND_ACTIVE.getWidth();
        // The default view comes first
        if (toggler.hasTreePlan()) {
            buttons.add(createViewButton(toggler, x + buttons.size() * width, y, GuiCraftingPlanToggler.CraftingPlanDisplayMode.GRAPH,
                    "gui.integratedterminals.craftingplan.view.graph", Images.BUTTON_MIDDLE_CRAFTING_PLAN_GRAPH, onChange));
        }
        if (toggler.hasFlatPlan()) {
            buttons.add(createViewButton(toggler, x + buttons.size() * width, y, GuiCraftingPlanToggler.CraftingPlanDisplayMode.FLAT,
                    "gui.integratedterminals.craftingplan.view.flat", Images.BUTTON_MIDDLE_CRAFTING_PLAN_FLAT, onChange));
        }
        if (toggler.hasTreePlan() && GuiCraftingPlanToggler.isTreeListEnabled()) {
            buttons.add(createViewButton(toggler, x + buttons.size() * width, y, GuiCraftingPlanToggler.CraftingPlanDisplayMode.TREE,
                    "gui.integratedterminals.craftingplan.view.list", Images.BUTTON_MIDDLE_CRAFTING_PLAN_LIST, onChange));
        }
        // A single view needs no switching
        if (buttons.size() <= 1) {
            buttons.clear();
        }
        return buttons;
    }

    protected static Button createViewButton(GuiCraftingPlanToggler toggler, int x, int y,
                                             GuiCraftingPlanToggler.CraftingPlanDisplayMode mode, String unlocalizedName,
                                             IImage image, Runnable onChange) {
        Component name = Component.translatable(unlocalizedName);
        Button button = new ButtonView(x, y, name, (b) -> {
            if (toggler.selectDisplayMode(mode)) {
                onChange.run();
            }
        }, image, toggler.getCraftingPlanDisplayMode() == mode);
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
        boolean fullscreen = GuiCraftingPlanToggler.isGraphFullscreen();
        return Lists.newArrayList(
                new ButtonGraph(graph, 0, "gui.integratedterminals.craftingplan.graph.zoom_in",
                        Images.BUTTON_GRAPH_ZOOM_IN, (b) -> graph.zoomIn(), graph::canZoomIn),
                new ButtonGraph(graph, 1, "gui.integratedterminals.craftingplan.graph.zoom_out",
                        Images.BUTTON_GRAPH_ZOOM_OUT, (b) -> graph.zoomOut(), graph::canZoomOut),
                new ButtonGraph(graph, 2, "gui.integratedterminals.craftingplan.graph.fit",
                        Images.BUTTON_GRAPH_FIT, (b) -> graph.fit(), () -> true),
                new ButtonGraph(graph, 3, fullscreen
                        ? "gui.integratedterminals.craftingplan.graph.fullscreen.exit" : "gui.integratedterminals.craftingplan.graph.fullscreen",
                        fullscreen ? Images.BUTTON_GRAPH_FULLSCREEN_EXIT : Images.BUTTON_GRAPH_FULLSCREEN, (b) -> {
                            GuiCraftingPlanToggler.setGraphFullscreen(!fullscreen);
                            onFullscreenToggle.run();
                        }, () -> true)
        );
    }

    /**
     * A button for switching to a display mode, which is shown as pressed when its mode is active.
     */
    public static class ButtonView extends ButtonImage {

        private static final int COLOR_SELECTED_OUTLINE = IModHelpers.get().getBaseHelpers().RGBAToInt(255, 255, 255, 255);
        private static final int COLOR_UNSELECTED_FADE = IModHelpers.get().getBaseHelpers().RGBAToInt(139, 139, 139, 144);
        private static final int COLOR_HOVER = IModHelpers.get().getBaseHelpers().RGBAToInt(255, 255, 255, 48);

        private final boolean selected;

        public ButtonView(int x, int y, Component narrationMessage, OnPress pressCallback, IImage image, boolean selected) {
            super(x, y, narrationMessage, pressCallback, image);
            this.selected = selected;
        }

        @Override
        protected void drawButtonInner(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
            int x0 = getX();
            int y0 = getY();
            int x1 = x0 + getWidth();
            int y1 = y0 + getHeight();
            (selected ? Images.BUTTON_BACKGROUND_ACTIVE : Images.BUTTON_BACKGROUND_INACTIVE).draw(guiGraphics, x0, y0);
            super.drawButtonInner(guiGraphics, mouseX, mouseY);
            if (selected) {
                // Outline the active view, so it stands out from the other buttons at a glance
                guiGraphics.fill(x0, y0, x1, y0 + 1, COLOR_SELECTED_OUTLINE);
                guiGraphics.fill(x0, y1 - 1, x1, y1, COLOR_SELECTED_OUTLINE);
                guiGraphics.fill(x0, y0 + 1, x0 + 1, y1 - 1, COLOR_SELECTED_OUTLINE);
                guiGraphics.fill(x1 - 1, y0 + 1, x1, y1 - 1, COLOR_SELECTED_OUTLINE);
            } else if (isHovered()) {
                guiGraphics.fill(x0 + 1, y0 + 1, x1 - 2, y1 - 2, COLOR_HOVER);
            } else {
                // Fade the icons of the other views
                guiGraphics.fill(x0 + 2, y0 + 2, x1 - 2, y1 - 2, COLOR_UNSELECTED_FADE);
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
        public void setFocused(boolean focused) {
            // These buttons are pressed repeatedly, so only keep the focus highlight for keyboard navigation
            super.setFocused(focused && Minecraft.getInstance().getLastInputType().isKeyboard());
        }

        @Override
        public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
            this.active = activeSupplier.getAsBoolean();
            super.extractContents(guiGraphics, mouseX, mouseY, partialTicks);
        }

        @Override
        protected void drawButtonInner(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
            if (this.active) {
                super.drawButtonInner(guiGraphics, mouseX, mouseY);
            } else {
                image.drawWithColor(guiGraphics, getX(), getY(), 0.5F, 0.5F, 0.5F, 1F);
            }
        }
    }
}
