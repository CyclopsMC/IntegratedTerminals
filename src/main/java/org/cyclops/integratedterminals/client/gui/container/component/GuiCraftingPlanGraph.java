package org.cyclops.integratedterminals.client.gui.container.component;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integratedterminals.Capabilities;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.client.gui.image.Images;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

import static org.cyclops.integratedterminals.client.gui.container.component.CraftingPlanGraphLayout.NODE_SIZE;

/**
 * A gui component for visualizing a crafting plan as a pannable and zoomable graph,
 * with the requested output on the left and its ingredients to the right.
 *
 * The using gui must call the methods listed in {@link IGuiCraftingPlanView}.
 *
 * @author rubensworks
 */
public class GuiCraftingPlanGraph extends AbstractWidget implements IGuiCraftingPlanView {

    public static final int OVERLAY_BUTTON_SIZE = 15;
    public static final int OVERLAY_BUTTON_COUNT = 4;
    private static final int OVERLAY_MARGIN = 3;
    private static final int LEGEND_MIN_WIDTH = 200;
    private static final int GRID_SIZE = 16;
    private static final int CLICK_DISTANCE = 2;
    private static final long SPINNER_FRAME_DURATION = 100;
    private static final long PULSE_DURATION = 1200;
    private static final long FLOW_STEP_DURATION = 80;
    private static final int FLOW_PERIOD = 6;
    private static final int FLOW_DASH = 2;
    // TODO: temporary switch for comparing the two crafting animations, remove before merging
    public static boolean CRAFTING_ANIMATION_BORDER = false;
    /**
     * The smallest zoom level at which quantities are still shown.
     */
    private static final float MIN_READABLE_ZOOM = 0.75F;

    private static final int COLOR_BACKGROUND = 0xFF2B2B2B;
    private static final int COLOR_GRID = 0x0CFFFFFF;
    private static final int COLOR_BEVEL_DARK = 0xFF373737;
    private static final int COLOR_BEVEL_LIGHT = 0xFFFFFFFF;
    private static final int COLOR_EDGE = 0xFFDADADA;
    private static final int COLOR_NODE_OUTLINE = 0xFF000000;
    private static final int COLOR_NODE_FILL = 0xFFC6C6C6;
    private static final int COLOR_NODE_FILL_FAILED = 0xFFF0B4B4;
    private static final int COLOR_NODE_HIGHLIGHT = 0x80FFFFFF;
    private static final int COLOR_NODE_SHADOW = 0x40000000;
    private static final int COLOR_SLOT_FILL = 0xFF8B8B8B;
    private static final int COLOR_SLOT_DARK = 0xFF373737;
    private static final int COLOR_SLOT_LIGHT = 0xFFFFFFFF;
    private static final int COLOR_STATUS_DONE = 0xFF4CC24C;
    private static final int COLOR_STATUS_CRAFTING = 0xFF2BAEE7;
    private static final int COLOR_STATUS_CRAFTING_LIGHT = 0xFFB4E6FF;
    private static final int COLOR_STATUS_WAITING = 0xFFE8D020;
    private static final int COLOR_STATUS_BLOCKED = 0xFFF08A10;
    private static final int COLOR_STATUS_FAILED = 0xFFE03030;
    private static final int COLOR_HOVER = 0xFFFFFFFF;
    private static final int COLOR_BADGE = 0xFF1E1E1E;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_TEXT_HINT = 0xFFBDBDBD;
    private static final int COLOR_TEXT_BACKGROUND = 0xB02B2B2B;

    // A color of 0 means a stored ingredient, which is drawn as an inventory slot
    private static final int[] LEGEND_COLORS = {0, COLOR_STATUS_WAITING, COLOR_STATUS_BLOCKED, COLOR_STATUS_CRAFTING,
            COLOR_STATUS_DONE, COLOR_STATUS_FAILED};
    private static final String[] LEGEND_LABELS = {"stored", "waiting", "blocked", "crafting", "done", "missing"};

    private final AbstractContainerScreen parentGui;
    private final ITerminalCraftingPlan<?> craftingPlan;
    private final int guiLeft;
    private final int guiTop;
    private final int viewWidth;
    private final int viewHeight;
    private final boolean live;
    private final List<GuiCraftingPlan.Element> elements;
    private final boolean valid;
    private final GuiCraftingPlanHeader header;
    private final CraftingPlanGraphCamera camera;

    private CraftingPlanGraphLayout<GuiCraftingPlan.Element> layout;

    private boolean pressing;
    private double dragDistance;
    @Nullable
    private GuiCraftingPlan.Element pressedElement;

    /**
     * @param parentGui The gui this is drawn in.
     * @param craftingPlan The plan to visualize.
     * @param guiLeft The gui X position.
     * @param guiTop The gui Y position.
     * @param x The viewport X position, relative to the gui.
     * @param y The viewport Y position, relative to the gui.
     * @param viewWidth The viewport width.
     * @param viewHeight The viewport height.
     * @param live If this shows a running job, which changes over time.
     *             It then shows a status legend instead of the controls hint,
     *             and initially leaves room for the plan to grow.
     */
    public GuiCraftingPlanGraph(AbstractContainerScreen parentGui, ITerminalCraftingPlan<?> craftingPlan, int guiLeft, int guiTop,
                                int x, int y, int viewWidth, int viewHeight, boolean live) {
        super(x, y, 0, 0, Component.literal(""));
        this.parentGui = parentGui;
        this.craftingPlan = craftingPlan;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.live = live;
        this.elements = GuiCraftingPlan.getElements(craftingPlan);
        pruneEmptyLeaves(this.elements.get(0));
        this.valid = craftingPlan.getStatus().isValid();
        this.header = new GuiCraftingPlanHeader(craftingPlan.getUnlocalizedLabel(), craftingPlan.getStatus(),
                craftingPlan.getTickDuration(), craftingPlan.getEstimatedTickDurationTotal(),
                craftingPlan.getEstimatedTickDurationRemaining(), craftingPlan.getChannel(), craftingPlan.getInitiatorName());
        this.camera = new CraftingPlanGraphCamera();
        collapseToFit();
        this.camera.reset(layout.getBounds(), getInnerWidth(), getInnerHeight(),
                live ? CraftingPlanGraphCamera.DEFAULT_ZOOM_INDEX : CraftingPlanGraphCamera.INITIAL_MAX_ZOOM_INDEX);
    }

    /**
     * Collapse the deepest levels of plans that are too large to read when fully expanded,
     * so that they initially show as an overview that can be expanded per node.
     */
    protected void collapseToFit() {
        refreshLayout();
        int maxDepth = 0;
        for (GuiCraftingPlan.Element element : elements) {
            maxDepth = Math.max(maxDepth, element.getIndent());
        }
        for (int depth = maxDepth - 1; depth >= 1 && !CraftingPlanGraphCamera.fits(layout.getBounds(),
                getInnerWidth(), getInnerHeight(), MIN_READABLE_ZOOM); depth--) {
            for (GuiCraftingPlan.Element element : elements) {
                element.setEnabled(element.getIndent() <= depth);
            }
            refreshLayout();
        }
    }

    /**
     * Remove leaves that have nothing to show, such as running jobs whose outputs have become empty.
     * @param element The element to prune the children of.
     */
    protected static void pruneEmptyLeaves(GuiCraftingPlan.Element element) {
        element.getChildren().removeIf(child -> {
            pruneEmptyLeaves(child);
            return child.getChildren().isEmpty() && child.getOutputs().stream()
                    .allMatch(alternatives -> alternatives.stream().allMatch(GuiCraftingPlanGraph::isEmpty));
        });
    }

    protected static boolean isEmpty(IPrototypedIngredient<?, ?> ingredient) {
        return ((IngredientComponent) ingredient.getComponent()).getMatcher().isEmpty(ingredient.getPrototype());
    }

    public ITerminalCraftingPlan<?> getCraftingPlan() {
        return craftingPlan;
    }

    public void inheritVisualizationState(GuiCraftingPlanGraph previous) {
        // Inherit toggle state, where elements that did not exist before are expanded
        Int2BooleanOpenHashMap enabledElements = new Int2BooleanOpenHashMap();
        for (GuiCraftingPlan.Element element : previous.elements) {
            enabledElements.put(element.getId(), element.isEnabled());
        }
        for (GuiCraftingPlan.Element element : this.elements) {
            element.setEnabled(enabledElements.getOrDefault(element.getId(), true));
        }
        refreshLayout();

        // Inherit camera, keeping the center of the view in place if the viewport was resized
        this.camera.copyFrom(previous.camera);
        this.camera.pan((getInnerWidth() - previous.getInnerWidth()) / 2D, (getInnerHeight() - previous.getInnerHeight()) / 2D);
        this.camera.clamp(layout.getBounds(), getInnerWidth(), getInnerHeight());
    }

    protected void refreshLayout() {
        this.layout = CraftingPlanGraphLayout.create(elements.get(0));
    }

    protected int getInnerLeft() {
        return guiLeft + getX() + 1;
    }

    protected int getInnerTop() {
        return guiTop + getY() + 1;
    }

    protected int getInnerWidth() {
        return viewWidth - 2;
    }

    protected int getInnerHeight() {
        return viewHeight - 2;
    }

    /**
     * @return The absolute X position of the overlay buttons.
     */
    public int getOverlayButtonX() {
        return getInnerLeft() + getInnerWidth() - OVERLAY_MARGIN - OVERLAY_BUTTON_SIZE;
    }

    /**
     * @param index The button index, from top to bottom.
     * @return The absolute Y position of an overlay button.
     */
    public int getOverlayButtonY(int index) {
        return getInnerTop() + OVERLAY_MARGIN + index * (OVERLAY_BUTTON_SIZE + 1);
    }

    protected boolean isInViewport(double mouseX, double mouseY) {
        double viewX = mouseX - getInnerLeft();
        double viewY = mouseY - getInnerTop();
        return viewX >= 0 && viewY >= 0 && viewX < getInnerWidth() && viewY < getInnerHeight()
                && !(mouseX >= getOverlayButtonX() && mouseX < getOverlayButtonX() + OVERLAY_BUTTON_SIZE
                    && mouseY >= getOverlayButtonY(0) && mouseY < getOverlayButtonY(OVERLAY_BUTTON_COUNT));
    }

    @Nullable
    protected CraftingPlanGraphLayout.Placement<GuiCraftingPlan.Element> getPlacementAt(double mouseX, double mouseY) {
        if (!isInViewport(mouseX, mouseY)) {
            return null;
        }
        // Node rectangles are drawn from rounded view positions, so hit testing uses the same rounding
        for (CraftingPlanGraphLayout.Placement<GuiCraftingPlan.Element> placement : layout.getPlacements()) {
            if (mouseX >= toViewX(placement.getX()) && mouseX < toViewX(placement.getX() + NODE_SIZE)
                    && mouseY >= toViewY(placement.getY()) && mouseY < toViewY(placement.getY() + NODE_SIZE)) {
                return placement;
            }
        }
        return null;
    }

    /**
     * @param graphX An X position in graph space.
     * @return The absolute X position on screen, in whole pixels.
     */
    protected int toViewX(double graphX) {
        return getInnerLeft() + (int) Math.round(camera.getOriginX()) + (int) Math.floor(graphX * camera.getZoom());
    }

    /**
     * @param graphY A Y position in graph space.
     * @return The absolute Y position on screen, in whole pixels.
     */
    protected int toViewY(double graphY) {
        return getInnerTop() + (int) Math.round(camera.getOriginY()) + (int) Math.floor(graphY * camera.getZoom());
    }

    protected int getTick() {
        return (int) Minecraft.getInstance().level.getGameTime() / GuiCraftingPlan.TICK_DELAY;
    }

    @Nullable
    protected IPrototypedIngredient<?, ?> getShownOutput(GuiCraftingPlan.Element element) {
        if (element.getOutputs().isEmpty()) {
            return null;
        }
        List<IPrototypedIngredient<?, ?>> alternatives = element.getOutputs().get(0);
        return alternatives.isEmpty() ? null : alternatives.get(getTick() % alternatives.size());
    }

    protected static long getQuantity(IPrototypedIngredient<?, ?> ingredient) {
        return ((IngredientComponent) ingredient.getComponent()).getMatcher().getQuantity(ingredient.getPrototype());
    }

    protected static boolean isFailed(TerminalCraftingJobStatus status) {
        return status == TerminalCraftingJobStatus.INVALID || status == TerminalCraftingJobStatus.ERROR
                || status == TerminalCraftingJobStatus.INVALID_INPUTS;
    }

    /**
     * @param element An element.
     * @return If the element itself causes its plan to fail,
     *         as opposed to an ancestor that only fails because of it.
     */
    protected static boolean isProblem(GuiCraftingPlan.Element element) {
        return isFailed(element.getStatus()) && (element.getMissingQuantity() > 0 || element.getChildren().isEmpty());
    }

    /**
     * @param status A job status.
     * @return The color that groups this status for the player, or 0 for jobs that have not started.
     */
    protected static int getStatusColor(TerminalCraftingJobStatus status) {
        if (isFailed(status)) {
            return COLOR_STATUS_FAILED;
        }
        return switch (status) {
            case FINISHED -> COLOR_STATUS_DONE;
            case CRAFTING -> COLOR_STATUS_CRAFTING;
            case QUEUEING, PENDING_DEPENDENCIES -> COLOR_STATUS_WAITING;
            case PENDING_INPUTS, PENDING_OUTPUT_STORAGE -> COLOR_STATUS_BLOCKED;
            default -> 0;
        };
    }

    /**
     * Draw light dashes that move along an edge, to show that ingredients are flowing through it.
     * @param guiGraphics The gui graphics.
     * @param from The start of the edge along its direction, inclusive.
     * @param to The end of the edge along its direction, exclusive.
     * @param across The position of the edge across its direction.
     * @param thickness The edge thickness.
     * @param towardsStart If the dashes move towards the start of the edge.
     * @param vertical If the edge is vertical.
     */
    protected void drawFlow(GuiGraphics guiGraphics, int from, int to, int across, int thickness,
                            boolean towardsStart, boolean vertical) {
        int step = (int) ((System.currentTimeMillis() / FLOW_STEP_DURATION) % FLOW_PERIOD);
        int offset = towardsStart ? step : FLOW_PERIOD - step;
        for (int p = from - FLOW_PERIOD; p < to; p += FLOW_PERIOD) {
            // Align dashes to absolute positions, so that adjacent edges move in step
            int start = p - Math.floorMod(p + offset, FLOW_PERIOD);
            int dashFrom = Math.max(from, start);
            int dashTo = Math.min(to, start + FLOW_DASH);
            if (dashFrom < dashTo) {
                if (vertical) {
                    guiGraphics.fill(across, dashFrom, across + thickness, dashTo, COLOR_STATUS_CRAFTING_LIGHT);
                } else {
                    guiGraphics.fill(dashFrom, across, dashTo, across + thickness, COLOR_STATUS_CRAFTING_LIGHT);
                }
            }
        }
    }

    /**
     * Draw a light that runs around a frame, with a fading tail.
     * @param guiGraphics The gui graphics.
     * @param x0 The outer left of the frame.
     * @param y0 The outer top of the frame.
     * @param x1 The outer right of the frame, exclusive.
     * @param y1 The outer bottom of the frame, exclusive.
     * @param thickness The frame thickness.
     */
    protected void drawBorderComet(GuiGraphics guiGraphics, int x0, int y0, int x1, int y1, int thickness) {
        int w = x1 - x0 - thickness;
        int h = y1 - y0 - thickness;
        int length = 2 * (w + h);
        if (length <= 0) {
            return;
        }
        int head = (int) ((System.currentTimeMillis() % PULSE_DURATION) * length / PULSE_DURATION);
        int tail = Math.max(4, length / 3);
        for (int d = tail - 1; d >= 0; d--) {
            int s = Math.floorMod(head - d, length);
            int x;
            int y;
            if (s < w) {
                x = x0 + s;
                y = y0;
            } else if (s < w + h) {
                x = x1 - thickness;
                y = y0 + s - w;
            } else if (s < 2 * w + h) {
                x = x1 - thickness - (s - w - h);
                y = y1 - thickness;
            } else {
                x = x0;
                y = y1 - thickness - (s - 2 * w - h);
            }
            int color = lerpColor(COLOR_STATUS_CRAFTING, 0xFFFFFFFF, 1F - (float) d / tail);
            guiGraphics.fill(x, y, x + thickness, y + thickness, color);
        }
    }

    protected static int lerpColor(int from, int to, float t) {
        int result = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int a = (from >> shift) & 0xFF;
            int b = (to >> shift) & 0xFF;
            result |= Math.round(a + (b - a) * t) << shift;
        }
        return result;
    }

    /**
     * @param segment An edge segment.
     * @return 2 if it leads to a failed node, 1 if it leads to a node that is crafting, and 0 otherwise.
     */
    protected static int getEdgeSeverity(CraftingPlanGraphLayout.Segment<GuiCraftingPlan.Element> segment) {
        int severity = 0;
        for (GuiCraftingPlan.Element target : segment.getTargets()) {
            if (isFailed(target.getStatus())) {
                return 2;
            }
            if (target.getStatus() == TerminalCraftingJobStatus.CRAFTING) {
                severity = 1;
            }
        }
        return severity;
    }

    protected static boolean isCollapsed(GuiCraftingPlan.Element element) {
        for (GuiCraftingPlan.Element child : element.getChildren()) {
            if (!child.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    protected String getLabel(GuiCraftingPlan.Element element, IPrototypedIngredient<?, ?> output) {
        if (camera.getZoom() < MIN_READABLE_ZOOM) {
            return "";
        }
        long total = element.getCraftQuantityTotal();
        long remaining = element.getCraftQuantity();
        if (total > remaining) {
            return GuiHelpers.quantityToScaledString(total - remaining) + "/" + GuiHelpers.quantityToScaledString(total);
        }
        return GuiHelpers.quantityToScaledString(getQuantity(output));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public void drawGuiContainerBackgroundLayer(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        int left = guiLeft + getX();
        int top = guiTop + getY();
        int innerLeft = getInnerLeft();
        int innerTop = getInnerTop();
        int innerRight = innerLeft + getInnerWidth();
        int innerBottom = innerTop + getInnerHeight();

        header.drawBackground(guiGraphics, left, top, viewWidth);

        // Inset viewport
        guiGraphics.fill(left, top, left + viewWidth, top + viewHeight, COLOR_BEVEL_LIGHT);
        guiGraphics.fill(left, top, left + viewWidth - 1, top + viewHeight - 1, COLOR_BEVEL_DARK);
        guiGraphics.fill(innerLeft, innerTop, innerRight, innerBottom, COLOR_BACKGROUND);

        guiGraphics.enableScissor(innerLeft, innerTop, innerRight, innerBottom);

        // Grid that moves along with the graph
        int gridSize = Math.round(GRID_SIZE * camera.getZoom());
        int gridOffsetX = Math.floorMod((int) Math.round(camera.getOriginX()), gridSize);
        int gridOffsetY = Math.floorMod((int) Math.round(camera.getOriginY()), gridSize);
        for (int x = innerLeft + gridOffsetX; x < innerRight; x += gridSize) {
            guiGraphics.fill(x, innerTop, x + 1, innerBottom, COLOR_GRID);
        }
        for (int y = innerTop + gridOffsetY; y < innerBottom; y += gridSize) {
            guiGraphics.fill(innerLeft, y, innerRight, y + 1, COLOR_GRID);
        }

        // Edges, where the paths to problems are drawn last so that they are not hidden by shared parts of the trunk
        int thickness = camera.getZoom() >= 1.5F ? 2 : 1;
        for (int severity = 0; severity <= 2; severity++) {
            for (CraftingPlanGraphLayout.Segment<GuiCraftingPlan.Element> segment : layout.getSegments()) {
                if (getEdgeSeverity(segment) != severity) {
                    continue;
                }
                int color = severity == 2 ? COLOR_STATUS_FAILED : (severity == 1 ? COLOR_STATUS_CRAFTING : COLOR_EDGE);
                if (segment.getY1() == segment.getY2()) {
                    int y = toViewY(segment.getY1());
                    int x1 = toViewX(segment.getX1());
                    int x2 = toViewX(segment.getX2() + 1);
                    guiGraphics.fill(x1, y, x2, y + thickness, color);
                    if (severity == 1) {
                        // Outputs flow back to the parent, which is always on the left
                        drawFlow(guiGraphics, x1, x2, y, thickness, true, false);
                    }
                } else {
                    int x = toViewX(segment.getX1());
                    int y1 = toViewY(segment.getY1());
                    int y2 = toViewY(segment.getY2()) + thickness;
                    guiGraphics.fill(x, y1, x + thickness, y2, color);
                    if (severity == 1) {
                        // Outputs flow away from the child, towards the vertical center of the parent
                        CraftingPlanGraphLayout.Placement<GuiCraftingPlan.Element> child = layout.getPlacement(segment.getTargets().get(0));
                        boolean up = child != null && child.getY() + NODE_SIZE / 2 >= segment.getY2();
                        drawFlow(guiGraphics, y1, y2, x, thickness, up, true);
                    }
                }
            }
        }

        // Nodes
        CraftingPlanGraphLayout.Placement<GuiCraftingPlan.Element> hovered = getPlacementAt(mouseX, mouseY);
        for (CraftingPlanGraphLayout.Placement<GuiCraftingPlan.Element> placement : layout.getPlacements()) {
            int x0 = toViewX(placement.getX());
            int y0 = toViewY(placement.getY());
            int x1 = toViewX(placement.getX() + NODE_SIZE);
            int y1 = toViewY(placement.getY() + NODE_SIZE);
            // Skip nodes outside the viewport, with some slack for the collapsed badge
            if (x1 + NODE_SIZE < innerLeft || x0 > innerRight || y1 < innerTop || y0 > innerBottom) {
                continue;
            }
            drawNode(guiGraphics, placement, x0, y0, x1, y1, placement == hovered, partialTicks, mouseX, mouseY);
        }

        guiGraphics.flush();
        guiGraphics.disableScissor();

        if (live) {
            drawLegend(guiGraphics, innerLeft, innerBottom);
        } else {
            drawHint(guiGraphics, innerLeft, innerBottom);
        }
    }

    protected void drawNode(GuiGraphics guiGraphics, CraftingPlanGraphLayout.Placement<GuiCraftingPlan.Element> placement,
                            int x0, int y0, int x1, int y1, boolean hovered, float partialTicks, int mouseX, int mouseY) {
        GuiCraftingPlan.Element element = placement.getNode();
        float zoom = camera.getZoom();

        // Frame
        if (hovered) {
            guiGraphics.fill(x0 - 1, y0 - 1, x1 + 1, y1 + 1, COLOR_HOVER);
        }
        if (element.isStoredIngredient()) {
            // Ingredients from storage look like an inventory slot
            guiGraphics.fill(x0, y0, x1, y1, COLOR_SLOT_LIGHT);
            guiGraphics.fill(x0, y0, x1 - 1, y1 - 1, COLOR_SLOT_DARK);
            guiGraphics.fill(x0 + 1, y0 + 1, x1 - 1, y1 - 1, COLOR_SLOT_FILL);
        } else {
            // Crafting jobs look like a raised tile, framed in the color of their status
            int accent = getStatusColor(element.getStatus());
            if (element.getStatus() == TerminalCraftingJobStatus.CRAFTING && !CRAFTING_ANIMATION_BORDER) {
                // Active jobs pulse, so they stand out from the ones that are waiting
                double phase = (System.currentTimeMillis() % PULSE_DURATION) / (double) PULSE_DURATION;
                accent = lerpColor(COLOR_STATUS_CRAFTING, COLOR_STATUS_CRAFTING_LIGHT, (float) (0.5 - 0.5 * Math.cos(phase * 2 * Math.PI)));
            }
            guiGraphics.fill(x0, y0, x1, y1, COLOR_NODE_OUTLINE);
            int inset = 1;
            if (accent != 0) {
                inset += zoom >= 1.5F ? 3 : 2;
                guiGraphics.fill(x0 + 1, y0 + 1, x1 - 1, y1 - 1, accent);
                if (element.getStatus() == TerminalCraftingJobStatus.CRAFTING && CRAFTING_ANIMATION_BORDER) {
                    drawBorderComet(guiGraphics, x0 + 1, y0 + 1, x1 - 1, y1 - 1, inset - 1);
                }
            }
            guiGraphics.fill(x0 + inset, y0 + inset, x1 - inset, y1 - inset,
                    isProblem(element) ? COLOR_NODE_FILL_FAILED : COLOR_NODE_FILL);
            guiGraphics.fill(x0 + inset, y0 + inset, x1 - inset, y0 + inset + 1, COLOR_NODE_HIGHLIGHT);
            guiGraphics.fill(x0 + inset, y0 + inset + 1, x0 + inset + 1, y1 - inset, COLOR_NODE_HIGHLIGHT);
            guiGraphics.fill(x0 + inset + 1, y1 - inset - 1, x1 - inset, y1 - inset, COLOR_NODE_SHADOW);
            guiGraphics.fill(x1 - inset - 1, y0 + inset + 1, x1 - inset, y1 - inset - 1, COLOR_NODE_SHADOW);
        }

        // Ingredient
        IPrototypedIngredient<?, ?> output = getShownOutput(element);
        if (output != null) {
            IngredientComponent<?, ?> ingredientComponent = output.getComponent();
            long quantity = getQuantity(output);
            String label = getLabel(element, output);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate((x0 + x1) / 2F, (y0 + y1) / 2F, 0);
            guiGraphics.pose().scale(zoom, zoom, 1);
            ingredientComponent.getCapability(Capabilities.IngredientComponentTerminalStorageHandler.INGREDIENT)
                    .ifPresent(h -> h.drawInstance(guiGraphics, output.getPrototype(), quantity, label, this.parentGui,
                            ContainerScreenTerminalStorage.DrawLayer.BACKGROUND, partialTicks, -8, -8, mouseX, mouseY, null));
            guiGraphics.pose().popPose();
        }

        // Markers are drawn in node space, in front of the ingredient
        boolean failed = isProblem(element);
        boolean crafting = element.getStatus() == TerminalCraftingJobStatus.CRAFTING;
        int hidden = placement.getHiddenDescendants();
        if (failed || crafting || hidden > 0) {
            Font font = Minecraft.getInstance().font;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x0, y0, 300);
            guiGraphics.pose().scale(zoom, zoom, 1);
            if (failed) {
                guiGraphics.fill(1, 1, 7, 11, COLOR_BADGE);
                guiGraphics.drawString(font, "!", 3, 2, 0xFFFF5555, false);
            }
            if (crafting && !CRAFTING_ANIMATION_BORDER) {
                // The same spinner as on crafting ingredients in the storage terminal
                int frame = (int) ((System.currentTimeMillis() / SPINNER_FRAME_DURATION) % Images.SPINNER.length);
                GlStateManager._enableBlend();
                Images.SPINNER[frame].drawWithColor(guiGraphics, NODE_SIZE - 10, 2, 0.17F, 0.68F, 0.9F, 1F);
            }
            if (hidden > 0) {
                String badge = "+" + hidden;
                int badgeWidth = font.width(badge) + 3;
                int badgeX = NODE_SIZE - 2;
                int badgeY = NODE_SIZE / 2 - 5;
                guiGraphics.fill(badgeX, badgeY, badgeX + badgeWidth, badgeY + 10, COLOR_TEXT_HINT);
                guiGraphics.fill(badgeX + 1, badgeY + 1, badgeX + badgeWidth - 1, badgeY + 9, COLOR_BADGE);
                guiGraphics.drawString(font, badge, badgeX + 2, badgeY + 1, COLOR_TEXT, false);
            }
            guiGraphics.pose().popPose();
        }
    }

    protected void drawHint(GuiGraphics guiGraphics, int innerLeft, int innerBottom) {
        String hint = L10NHelpers.localize("gui.integratedterminals.craftingplan.graph.hint");
        int width = Minecraft.getInstance().font.width(hint) / 2;
        guiGraphics.fill(innerLeft, innerBottom - 7, innerLeft + width + 4, innerBottom, COLOR_TEXT_BACKGROUND);
        RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), Minecraft.getInstance().font, hint,
                innerLeft + 2, innerBottom - 5, 0.5F, COLOR_TEXT_HINT, true, Font.DisplayMode.NORMAL);
    }

    protected void drawLegend(GuiGraphics guiGraphics, int innerLeft, int innerBottom) {
        if (viewWidth < LEGEND_MIN_WIDTH) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        int width = 2;
        for (String label : LEGEND_LABELS) {
            width += 5 + font.width(L10NHelpers.localize("gui.integratedterminals.craftingplan.graph.legend." + label)) / 2 + 4;
        }
        guiGraphics.fill(innerLeft, innerBottom - 7, innerLeft + width, innerBottom, COLOR_TEXT_BACKGROUND);
        int x = innerLeft + 2;
        for (int i = 0; i < LEGEND_LABELS.length; i++) {
            String label = L10NHelpers.localize("gui.integratedterminals.craftingplan.graph.legend." + LEGEND_LABELS[i]);
            if (LEGEND_COLORS[i] == 0) {
                guiGraphics.fill(x, innerBottom - 5, x + 3, innerBottom - 2, COLOR_SLOT_FILL);
            } else {
                guiGraphics.fill(x, innerBottom - 5, x + 3, innerBottom - 2, LEGEND_COLORS[i]);
            }
            x += 5;
            RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), font, label,
                    x, innerBottom - 5, 0.5F, COLOR_TEXT_HINT, true, Font.DisplayMode.NORMAL);
            x += font.width(label) / 2 + 4;
        }
    }

    @Override
    public void drawGuiContainerForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        header.drawForeground(this.parentGui, guiGraphics, getX(), getY(), mouseX, mouseY);

        CraftingPlanGraphLayout.Placement<GuiCraftingPlan.Element> hovered = getPlacementAt(mouseX, mouseY);
        if (hovered != null) {
            GuiCraftingPlan.Element element = hovered.getNode();
            IPrototypedIngredient<?, ?> output = getShownOutput(element);
            if (output != null) {
                // The handler only shows a tooltip when the mouse is inside a slot-sized region,
                // so center that region on the mouse, as nodes can be any size depending on the zoom level.
                int x = mouseX - parentGui.getGuiLeft() - GuiHelpers.SLOT_SIZE_INNER / 2;
                int y = mouseY - parentGui.getGuiTop() - GuiHelpers.SLOT_SIZE_INNER / 2;
                List<Component> lines = getTooltipLines(hovered);
                output.getComponent().getCapability(Capabilities.IngredientComponentTerminalStorageHandler.INGREDIENT)
                        .ifPresent(h -> h.drawInstance(guiGraphics, output.getPrototype(), getQuantity(output), "", this.parentGui,
                                ContainerScreenTerminalStorage.DrawLayer.FOREGROUND, 0, x, y, mouseX, mouseY, lines));
            }
        }
    }

    protected List<Component> getTooltipLines(CraftingPlanGraphLayout.Placement<GuiCraftingPlan.Element> placement) {
        GuiCraftingPlan.Element element = placement.getNode();
        List<Component> lines = Lists.newArrayList();

        // Status
        if (element.isStoredIngredient()) {
            lines.add(Component.translatable("gui.integratedterminals.craftingplan.graph.stored").withStyle(ChatFormatting.GRAY));
            return lines;
        }
        String statusKey = "gui.integratedterminals.craftingplan.status." + element.getStatus().name().toLowerCase(Locale.ENGLISH);
        lines.add(Component.translatable("gui.integratedterminals.craftingplan.status", Component.translatable(statusKey))
                .withStyle(getStatusFormatting(element.getStatus())));
        lines.add(Component.translatable(statusKey + ".desc").withStyle(ChatFormatting.GRAY));

        // Quantities
        if (element.getStorageQuantity() > 0) {
            lines.add(Component.translatable("gui.integratedterminals.terminal_storage.stored", element.getStorageQuantity())
                    .withStyle(ChatFormatting.GRAY));
        }
        if (element.getCraftQuantity() > 0) {
            lines.add(Component.translatable("gui.integratedterminals.terminal_storage.crafting", element.getCraftQuantity())
                    .withStyle(ChatFormatting.GRAY));
        }
        if (element.getMissingQuantity() > 0) {
            lines.add(Component.translatable("gui.integratedterminals.terminal_storage.missing", element.getMissingQuantity())
                    .withStyle(ChatFormatting.RED));
        }

        // Other outputs
        if (element.getOutputs().size() > 1) {
            lines.add(Component.translatable("gui.integratedterminals.craftingplan.graph.outputs").withStyle(ChatFormatting.GRAY));
            int tick = getTick();
            for (List<IPrototypedIngredient<?, ?>> alternatives : element.getOutputs()) {
                IPrototypedIngredient output = alternatives.get(tick % alternatives.size());
                String name = output.getComponent().getMatcher().localize(output.getPrototype());
                lines.add(Component.literal(" " + GuiHelpers.quantityToScaledString(getQuantity(output)) + " " + name)
                        .withStyle(ChatFormatting.GRAY));
            }
        }

        // Collapsing
        if (!element.getChildren().isEmpty()) {
            if (isCollapsed(element)) {
                lines.add(Component.translatable("gui.integratedterminals.craftingplan.graph.collapsed", placement.getHiddenDescendants())
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                lines.add(Component.translatable("gui.integratedterminals.craftingplan.graph.expand")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            } else {
                lines.add(Component.translatable("gui.integratedterminals.craftingplan.graph.collapse")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
        }
        return lines;
    }

    protected static ChatFormatting getStatusFormatting(TerminalCraftingJobStatus status) {
        if (isFailed(status)) {
            return ChatFormatting.RED;
        }
        return switch (status) {
            case FINISHED -> ChatFormatting.GREEN;
            case CRAFTING -> ChatFormatting.AQUA;
            case UNSTARTED -> ChatFormatting.WHITE;
            default -> ChatFormatting.YELLOW;
        };
    }

    public void zoomIn() {
        zoom(1);
    }

    public void zoomOut() {
        zoom(-1);
    }

    protected void zoom(int direction) {
        camera.zoomAt(getInnerWidth() / 2D, getInnerHeight() / 2D, direction);
        camera.clamp(layout.getBounds(), getInnerWidth(), getInnerHeight());
    }

    public void fit() {
        camera.fit(layout.getBounds(), getInnerWidth(), getInnerHeight());
    }

    public boolean canZoomIn() {
        return camera.canZoomIn();
    }

    public boolean canZoomOut() {
        return camera.canZoomOut();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton != 0 || !isInViewport(mouseX, mouseY)) {
            return false;
        }
        CraftingPlanGraphLayout.Placement<GuiCraftingPlan.Element> placement = getPlacementAt(mouseX, mouseY);
        this.pressing = true;
        this.dragDistance = 0;
        this.pressedElement = placement != null ? placement.getNode() : null;
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double offsetX, double offsetY) {
        if (!pressing) {
            return false;
        }
        camera.pan(offsetX, offsetY);
        camera.clamp(layout.getBounds(), getInnerWidth(), getInnerHeight());
        dragDistance += Math.abs(offsetX) + Math.abs(offsetY);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (!pressing) {
            return false;
        }
        this.pressing = false;
        GuiCraftingPlan.Element element = this.pressedElement;
        this.pressedElement = null;
        if (dragDistance < CLICK_DISTANCE && element != null && !element.getChildren().isEmpty()) {
            CraftingPlanGraphLayout.Placement<GuiCraftingPlan.Element> released = getPlacementAt(mouseX, mouseY);
            if (released != null && released.getNode() == element) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                toggleChildren(released);
            }
        }
        return true;
    }

    protected void toggleChildren(CraftingPlanGraphLayout.Placement<GuiCraftingPlan.Element> placement) {
        GuiCraftingPlan.Element element = placement.getNode();
        boolean expand = isCollapsed(element);
        for (GuiCraftingPlan.Element child : element.getChildren()) {
            child.setEnabled(expand);
        }
        int oldY = placement.getY();
        refreshLayout();

        // Keep the toggled node under the mouse
        for (CraftingPlanGraphLayout.Placement<GuiCraftingPlan.Element> newPlacement : layout.getPlacements()) {
            if (newPlacement.getNode() == element) {
                camera.pan(0, (oldY - newPlacement.getY()) * camera.getZoom());
            }
        }
        camera.clamp(layout.getBounds(), getInnerWidth(), getInnerHeight());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isInViewport(mouseX, mouseY) || scrollY == 0) {
            return false;
        }
        camera.zoomAt(mouseX - getInnerLeft(), mouseY - getInnerTop(), (int) Math.signum(scrollY));
        camera.clamp(layout.getBounds(), getInnerWidth(), getInnerHeight());
        return true;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
