package org.cyclops.integratedterminals.client.gui.container.component;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
import org.cyclops.cyclopscore.client.gui.GuiGraphicsExtended;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integratedterminals.Capabilities;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.lwjgl.glfw.GLFW;

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
    private static final int LEGEND_ROW_HEIGHT = 7;
    private static final int SWATCH_SIZE = 5;
    private static final int GRID_SIZE = 16;
    private static final int CLICK_DISTANCE = 2;
    private static final int STACK_OFFSET = 2;
    private static final int PILL_STUB = 5;
    private static final int PILL_HEIGHT = 9;
    private static final int KEY_PAN_DISTANCE = 24;
    private static final long BORDER_LAP_DURATION = 1200;
    private static final long FLOW_STEP_DURATION = 80;
    private static final int FLOW_PERIOD = 6;
    private static final int FLOW_DASH = 2;
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
    // Done jobs are faded while the rest of the plan is still running, so that the remaining work stands out
    private static final int COLOR_DONE_FADE = 0x58C6C6C6;
    private static final int COLOR_STATUS_CRAFTING = 0xFF2BAEE7;
    private static final int COLOR_STATUS_CRAFTING_LIGHT = 0xFFB4E6FF;
    private static final int COLOR_STATUS_WAITING = 0xFFE8D020;
    private static final int COLOR_STATUS_BLOCKED = 0xFFF08A10;
    private static final int COLOR_STATUS_FAILED = 0xFFE03030;
    private static final int COLOR_HOVER = 0xFFFFFFFF;
    private static final int COLOR_BADGE = 0xFF1E1E1E;
    private static final int COLOR_STACK_NEAR = 0xFFA8A8A8;
    private static final int COLOR_STACK_FAR = 0xFF8B8B8B;
    private static final int COLOR_PILL_TEXT = 0xFF2B2B2B;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_TEXT_HINT = 0xFFBDBDBD;
    private static final int COLOR_TEXT_BACKGROUND = 0xB02B2B2B;

    private static final LegendEntry[] LEGEND_PREVIEW = {
            new LegendEntry("to_craft", LegendSwatch.TILE, 0),
            new LegendEntry("stored", LegendSwatch.SLOT, 0),
            new LegendEntry("missing", LegendSwatch.COLOR, COLOR_STATUS_FAILED)
    };
    private static final LegendEntry[] LEGEND_LIVE = {
            new LegendEntry("stored", LegendSwatch.SLOT, 0),
            new LegendEntry("waiting", LegendSwatch.COLOR, COLOR_STATUS_WAITING),
            new LegendEntry("blocked", LegendSwatch.COLOR, COLOR_STATUS_BLOCKED),
            new LegendEntry("crafting", LegendSwatch.COLOR, COLOR_STATUS_CRAFTING),
            new LegendEntry("done", LegendSwatch.DONE, COLOR_STATUS_DONE),
            new LegendEntry("missing", LegendSwatch.COLOR, COLOR_STATUS_FAILED)
    };

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

    private final Int2ObjectMap<List<List<IPrototypedIngredient<?, ?>>>> knownOutputs;

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
     * @param previous The graph that showed an earlier version of this plan, to continue from.
     */
    public GuiCraftingPlanGraph(AbstractContainerScreen parentGui, ITerminalCraftingPlan<?> craftingPlan, int guiLeft, int guiTop,
                                int x, int y, int viewWidth, int viewHeight, boolean live,
                                @Nullable GuiCraftingPlanGraph previous) {
        super(x, y, 0, 0, Component.literal(""));
        this.parentGui = parentGui;
        this.craftingPlan = craftingPlan;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.live = live;
        this.elements = GuiCraftingPlan.getElements(craftingPlan);
        this.knownOutputs = new Int2ObjectOpenHashMap<>();
        if (previous != null) {
            this.knownOutputs.putAll(previous.knownOutputs);
        }
        restoreOutputs(this.elements, this.knownOutputs);
        if (previous != null) {
            restoreToggleState(previous.elements, this.elements);
            restoreCompletedJobs(previous.elements, this.elements);
        }
        pruneEmptyLeaves(this.elements.get(0));
        this.valid = craftingPlan.getStatus().isValid();
        this.header = GuiCraftingPlanHeader.forPlan(craftingPlan);
        this.camera = new CraftingPlanGraphCamera();
        if (previous == null) {
            collapseToFit();
            this.camera.reset(layout.getBounds(), getInnerWidth(), getInnerHeight(),
                    live ? CraftingPlanGraphCamera.DEFAULT_ZOOM_INDEX : CraftingPlanGraphCamera.INITIAL_MAX_ZOOM_INDEX);
        } else {
            refreshLayout();
            // Keep the camera, with the center of the view in place if the viewport was resized
            this.camera.copyFrom(previous.camera);
            this.camera.pan((getInnerWidth() - previous.getInnerWidth()) / 2D, (getInnerHeight() - previous.getInnerHeight()) / 2D);
            this.camera.clamp(layout.getBounds(), getInnerWidth(), getInnerHeight());

            // Live plans are recreated on each update, so continue an ongoing press or drag
            this.pressing = previous.pressing;
            this.dragDistance = previous.dragDistance;
            if (previous.pressedElement != null) {
                for (GuiCraftingPlan.Element element : this.elements) {
                    if (element.getId() == previous.pressedElement.getId()) {
                        this.pressedElement = element;
                        break;
                    }
                }
            }
        }
    }

    /**
     * Remember the outputs of crafting jobs, and show the last known outputs of jobs that currently have none.
     * The outputs of a running job only include what remains to be crafted, so they are empty once it is done.
     * @param elements The elements of the plan.
     * @param knownOutputs The last known outputs per element id, which will be updated.
     */
    protected static void restoreOutputs(List<GuiCraftingPlan.Element> elements,
                                         Int2ObjectMap<List<List<IPrototypedIngredient<?, ?>>>> knownOutputs) {
        for (GuiCraftingPlan.Element element : elements) {
            if (element.isCraftingJob()) {
                if (hasOutputs(element)) {
                    knownOutputs.put(element.getId(), element.getOutputs());
                } else if (knownOutputs.containsKey(element.getId())) {
                    element.setOutputs(knownOutputs.get(element.getId()));
                }
            }
        }
    }

    protected static boolean hasOutputs(GuiCraftingPlan.Element element) {
        return element.getOutputs().stream()
                .anyMatch(alternatives -> alternatives.stream().anyMatch(ingredient -> !isEmpty(ingredient)));
    }

    /**
     * Collapse the same nodes as before, where nodes that did not exist before are expanded.
     * @param previousElements The elements of the previous version of the plan.
     * @param elements The elements of the plan.
     */
    protected static void restoreToggleState(List<GuiCraftingPlan.Element> previousElements, List<GuiCraftingPlan.Element> elements) {
        Int2BooleanOpenHashMap enabledElements = new Int2BooleanOpenHashMap();
        for (GuiCraftingPlan.Element element : previousElements) {
            enabledElements.put(element.getId(), element.isEnabled());
        }
        for (GuiCraftingPlan.Element element : elements) {
            element.setEnabled(enabledElements.getOrDefault(element.getId(), true));
        }
    }

    /**
     * Crafting jobs are removed from a running plan once they are done.
     * Keep showing them as completed in the place where they were, so that progress stays visible
     * and the layout does not change by itself.
     * @param previousElements The elements of the previous version of the plan.
     * @param elements The elements of the plan, to which completed jobs will be added.
     */
    public static void restoreCompletedJobs(List<GuiCraftingPlan.Element> previousElements, List<GuiCraftingPlan.Element> elements) {
        if (previousElements.isEmpty() || elements.isEmpty() || previousElements.get(0).getId() != elements.get(0).getId()) {
            return;
        }
        Int2ObjectMap<GuiCraftingPlan.Element> currentElements = new Int2ObjectOpenHashMap<>();
        for (GuiCraftingPlan.Element element : elements) {
            currentElements.put(element.getId(), element);
        }
        restoreCompletedJobs(previousElements.get(0), currentElements, elements);
    }

    protected static void restoreCompletedJobs(GuiCraftingPlan.Element previousParent,
                                               Int2ObjectMap<GuiCraftingPlan.Element> currentElements,
                                               List<GuiCraftingPlan.Element> elements) {
        GuiCraftingPlan.Element parent = currentElements.get(previousParent.getId());
        int rank = 0;
        for (GuiCraftingPlan.Element previousChild : previousParent.getChildren()) {
            if (!previousChild.isCraftingJob()) {
                continue;
            }
            if (currentElements.containsKey(previousChild.getId())) {
                restoreCompletedJobs(previousChild, currentElements, elements);
            } else if (parent != null) {
                GuiCraftingPlan.Element completed = createCompletedJob(previousChild);
                insertCraftingJob(parent, completed, rank);
                addRecursively(completed, elements);
            }
            rank++;
        }
    }

    /**
     * @param previous A crafting job that is done.
     * @return A completed copy of the job, with completed copies of its sub-jobs, which keep their collapsed state.
     */
    protected static GuiCraftingPlan.Element createCompletedJob(GuiCraftingPlan.Element previous) {
        long total = Math.max(previous.getCraftQuantityTotal(), previous.getCraftQuantity());
        GuiCraftingPlan.Element completed = new GuiCraftingPlan.Element(previous.getId(), previous.getIndent(),
                (List) previous.getOutputs(), 0, 0, total, 0,
                TerminalCraftingJobStatus.FINISHED.getColor(), TerminalCraftingJobStatus.FINISHED)
                .markCraftingJob()
                .markCompleted();
        completed.setEnabled(previous.isEnabled());
        for (GuiCraftingPlan.Element previousChild : previous.getChildren()) {
            if (previousChild.isCraftingJob()) {
                completed.addChild(createCompletedJob(previousChild));
            }
        }
        return completed;
    }

    /**
     * Insert a crafting job at the given position among the crafting jobs of a parent, which follow its ingredients.
     */
    protected static void insertCraftingJob(GuiCraftingPlan.Element parent, GuiCraftingPlan.Element job, int rank) {
        List<GuiCraftingPlan.Element> children = parent.getChildren();
        int jobs = 0;
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).isCraftingJob()) {
                if (jobs == rank) {
                    children.add(i, job);
                    return;
                }
                jobs++;
            }
        }
        children.add(job);
    }

    protected static void addRecursively(GuiCraftingPlan.Element element, List<GuiCraftingPlan.Element> elements) {
        elements.add(element);
        for (GuiCraftingPlan.Element child : element.getChildren()) {
            addRecursively(child, elements);
        }
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

    public static boolean isEmpty(IPrototypedIngredient<?, ?> ingredient) {
        return ((IngredientComponent) ingredient.getComponent()).getMatcher().isEmpty(ingredient.getPrototype());
    }

    public ITerminalCraftingPlan<?> getCraftingPlan() {
        return craftingPlan;
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
            if (mouseX >= toViewX(placement.getX()) && mouseX < toViewX(placement.getX() + placement.getSize())
                    && mouseY >= toViewY(placement.getY()) && mouseY < toViewY(placement.getY() + placement.getSize())) {
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
        return (int) (Minecraft.getInstance().level.getGameTime() / GuiCraftingPlan.TICK_DELAY);
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
     * @return The status to show for the element.
     */
    protected static TerminalCraftingJobStatus getDisplayStatus(GuiCraftingPlan.Element element) {
        // Crafting interfaces report a job as finished in between its crafting operations, while some remain to be done
        if (element.getStatus() == TerminalCraftingJobStatus.FINISHED && element.isCraftingJob() && !element.isCompleted()
                && element.getCraftQuantity() > 0) {
            return TerminalCraftingJobStatus.CRAFTING;
        }
        return element.getStatus();
    }

    /**
     * @param element An element.
     * @return If the element is a done job in a plan that is still running.
     */
    protected boolean isFaded(GuiCraftingPlan.Element element) {
        return element.isCraftingJob() && getDisplayStatus(element) == TerminalCraftingJobStatus.FINISHED
                && getDisplayStatus(elements.get(0)) != TerminalCraftingJobStatus.FINISHED;
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
        for (int[] dash : getFlowDashes(from, to, offset, FLOW_PERIOD, FLOW_DASH)) {
            if (vertical) {
                guiGraphics.fill(across, dash[0], across + thickness, dash[1], COLOR_STATUS_CRAFTING_LIGHT);
            } else {
                guiGraphics.fill(dash[0], across, dash[1], across + thickness, COLOR_STATUS_CRAFTING_LIGHT);
            }
        }
    }

    /**
     * Determine the dashes of a dashed line, aligned to absolute positions so that adjacent lines move in step.
     * @param from The start of the line, inclusive.
     * @param to The end of the line, exclusive.
     * @param offset The animation offset, where increasing it moves the dashes towards the start.
     * @param period The distance between the starts of two dashes.
     * @param length The dash length.
     * @return Pairs of the start (inclusive) and end (exclusive) of each dash, clipped to the line.
     */
    public static List<int[]> getFlowDashes(int from, int to, int offset, int period, int length) {
        List<int[]> dashes = Lists.newArrayList();
        // The first dash may start before the line and still overlap with it
        for (int start = from - Math.floorMod(from + offset, period); start < to; start += period) {
            int dashFrom = Math.max(from, start);
            int dashTo = Math.min(to, start + length);
            if (dashFrom < dashTo) {
                dashes.add(new int[]{dashFrom, dashTo});
            }
        }
        return dashes;
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
        int head = (int) ((System.currentTimeMillis() % BORDER_LAP_DURATION) * length / BORDER_LAP_DURATION);
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
            if (isFailed(getDisplayStatus(target))) {
                return 2;
            }
            if (getDisplayStatus(target) == TerminalCraftingJobStatus.CRAFTING) {
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
                if (getEdgeSeverity(segment) == severity) {
                    int color = severity == 2 ? COLOR_STATUS_FAILED : (severity == 1 ? COLOR_STATUS_CRAFTING : COLOR_EDGE);
                    if (segment.getY1() == segment.getY2()) {
                        int y = toViewY(segment.getY1());
                        guiGraphics.fill(toViewX(segment.getX1()), y, toViewX(segment.getX2() + 1), y + thickness, color);
                    } else {
                        int x = toViewX(segment.getX1());
                        guiGraphics.fill(x, toViewY(segment.getY1()), x + thickness, toViewY(segment.getY2()) + thickness, color);
                    }
                }
            }
        }

        // Ingredients flowing from crafting jobs to their parents,
        // drawn after all edges so that overlapping edges do not hide the dashes of others
        for (CraftingPlanGraphLayout.Segment<GuiCraftingPlan.Element> segment : layout.getSegments()) {
            if (getEdgeSeverity(segment) == 1) {
                if (segment.getY1() == segment.getY2()) {
                    // The parent is always on the left
                    drawFlow(guiGraphics, toViewX(segment.getX1()), toViewX(segment.getX2() + 1), toViewY(segment.getY1()),
                            thickness, true, false);
                } else {
                    // Flow away from the child, towards the vertical center of the parent
                    CraftingPlanGraphLayout.Placement<GuiCraftingPlan.Element> child = layout.getPlacement(segment.getTargets().get(0));
                    boolean up = child != null && child.getY() + child.getSize() / 2 >= segment.getY2();
                    drawFlow(guiGraphics, toViewY(segment.getY1()), toViewY(segment.getY2()) + thickness, toViewX(segment.getX1()),
                            thickness, up, true);
                }
            }
        }

        // Nodes
        CraftingPlanGraphLayout.Placement<GuiCraftingPlan.Element> hovered = getPlacementAt(mouseX, mouseY);
        for (CraftingPlanGraphLayout.Placement<GuiCraftingPlan.Element> placement : layout.getPlacements()) {
            int x0 = toViewX(placement.getX());
            int y0 = toViewY(placement.getY());
            int x1 = toViewX(placement.getX() + placement.getSize());
            int y1 = toViewY(placement.getY() + placement.getSize());
            // Skip nodes outside the viewport, with some slack for the collapsed badge
            if (x1 + NODE_SIZE < innerLeft || x0 > innerRight || y1 < innerTop || y0 > innerBottom) {
                continue;
            }
            drawNode(guiGraphics, placement, x0, y0, x1, y1, placement == hovered, partialTicks, mouseX, mouseY);
        }

        guiGraphics.flush();
        guiGraphics.disableScissor();

        int legendWidth = drawLegend(guiGraphics, innerLeft, innerBottom, live ? LEGEND_LIVE : LEGEND_PREVIEW);
        drawHint(guiGraphics, innerLeft, innerBottom, legendWidth);
    }

    protected void drawNode(GuiGraphics guiGraphics, CraftingPlanGraphLayout.Placement<GuiCraftingPlan.Element> placement,
                            int x0, int y0, int x1, int y1, boolean hovered, float partialTicks, int mouseX, int mouseY) {
        GuiCraftingPlan.Element element = placement.getNode();
        float zoom = camera.getZoom();

        // Collapsed nodes look like a stack, with one layer for a single hidden node and two for more
        int hidden = placement.getHiddenDescendants();
        if (hidden > 0) {
            int offset = Math.max(2, Math.round(STACK_OFFSET * zoom));
            for (int layer = hidden > 1 ? 2 : 1; layer >= 1; layer--) {
                int d = offset * layer;
                guiGraphics.fill(x0 + d, y0 + d, x1 + d, y1 + d, COLOR_NODE_OUTLINE);
                guiGraphics.fill(x0 + d + 1, y0 + d + 1, x1 + d - 1, y1 + d - 1, layer == 1 ? COLOR_STACK_NEAR : COLOR_STACK_FAR);
            }
        }

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
            int accent = getStatusColor(getDisplayStatus(element));
            guiGraphics.fill(x0, y0, x1, y1, COLOR_NODE_OUTLINE);
            int inset = 1;
            if (accent != 0) {
                inset += zoom >= 1.5F ? 3 : 2;
                guiGraphics.fill(x0 + 1, y0 + 1, x1 - 1, y1 - 1, accent);
                if (getDisplayStatus(element) == TerminalCraftingJobStatus.CRAFTING) {
                    // Active jobs have a light running around their frame
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
            // Larger nodes only enlarge the ingredient, as text would become uneven at fractional scales
            boolean enlarged = placement.getSize() != NODE_SIZE;
            String instanceLabel = enlarged ? "" : label;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate((x0 + x1) / 2F, (y0 + y1) / 2F, 0);
            float scale = zoom * placement.getSize() / NODE_SIZE;
            guiGraphics.pose().scale(scale, scale, 1);
            ingredientComponent.getCapability(Capabilities.IngredientComponentTerminalStorageHandler.INGREDIENT)
                    .ifPresent(h -> h.drawInstance(guiGraphics, output.getPrototype(), quantity, instanceLabel, this.parentGui,
                            ContainerScreenTerminalStorage.DrawLayer.BACKGROUND, partialTicks, -8, -8, mouseX, mouseY, null));
            guiGraphics.pose().popPose();
            if (enlarged && !label.isEmpty()) {
                // Draw the label as in a regular node, in the bottom right corner
                int inset = (NODE_SIZE - GuiHelpers.SLOT_SIZE_INNER) / 2 + GuiHelpers.SLOT_SIZE_INNER;
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(x1, y1, 0);
                guiGraphics.pose().scale(zoom, zoom, 1);
                new GuiGraphicsExtended(guiGraphics).drawSlotText(Minecraft.getInstance().font, label, -inset, -inset);
                guiGraphics.pose().popPose();
            }
        }

        // Done jobs are faded while the plan is running, so that the remaining work stands out
        if (isFaded(element)) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 250);
            guiGraphics.fill(x0 + 1, y0 + 1, x1 - 1, y1 - 1, COLOR_DONE_FADE);
            guiGraphics.pose().popPose();
        }

        // Markers are drawn in node space, in front of the ingredient
        boolean failed = isProblem(element);
        if (failed || hidden > 0) {
            Font font = Minecraft.getInstance().font;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x0, y0, 300);
            guiGraphics.pose().scale(zoom, zoom, 1);
            if (failed) {
                guiGraphics.fill(1, 1, 7, 11, COLOR_BADGE);
                guiGraphics.drawString(font, "!", 3, 2, 0xFFFF5555, false);
            }
            if (hidden > 0) {
                // A short edge into a pill with the number of hidden nodes, where the edges to them would start
                String count = "+" + hidden;
                int size = placement.getSize();
                int center = size / 2;
                int pillX = size + PILL_STUB;
                int pillWidth = font.width(count) + 5;
                int pillTop = center - PILL_HEIGHT / 2;
                guiGraphics.fill(size, center, pillX, center + 1, COLOR_EDGE);
                guiGraphics.fill(pillX + 1, pillTop, pillX + pillWidth - 1, pillTop + PILL_HEIGHT, COLOR_EDGE);
                guiGraphics.fill(pillX, pillTop + 1, pillX + pillWidth, pillTop + PILL_HEIGHT - 1, COLOR_EDGE);
                guiGraphics.drawString(font, count, pillX + 3, pillTop + 1, COLOR_PILL_TEXT, false);
            }
            guiGraphics.pose().popPose();
        }
    }

    /**
     * Draw the controls hint at the bottom right, if it fits next to the legend.
     * The controls are also listed in the tooltips of the overlay buttons.
     */
    protected void drawHint(GuiGraphics guiGraphics, int innerLeft, int innerBottom, int legendWidth) {
        Font font = Minecraft.getInstance().font;
        String hint = L10NHelpers.localize("gui.integratedterminals.craftingplan.graph.hint");
        int width = font.width(hint) / 2 + 4;
        int right = innerLeft + getInnerWidth();
        if (legendWidth < 0 || innerLeft + legendWidth + 4 + width > right) {
            return;
        }
        guiGraphics.fill(right - width, innerBottom - LEGEND_ROW_HEIGHT, right, innerBottom, COLOR_TEXT_BACKGROUND);
        RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), font, hint,
                right - width + 2, innerBottom - 5, 0.5F, COLOR_TEXT_HINT, true, Font.DisplayMode.NORMAL);
    }

    /**
     * Draw the legend at the bottom left, wrapping it over multiple rows if the viewport is too narrow.
     * @return The width of the legend if it takes a single row, or -1 if it takes more.
     */
    protected int drawLegend(GuiGraphics guiGraphics, int innerLeft, int innerBottom, LegendEntry[] entries) {
        Font font = Minecraft.getInstance().font;
        int maxWidth = getInnerWidth() - 4;

        // Divide the entries over rows
        List<List<LegendEntry>> rows = Lists.newArrayList();
        List<Integer> rowWidths = Lists.newArrayList();
        List<LegendEntry> row = Lists.newArrayList();
        int rowWidth = 2;
        for (LegendEntry entry : entries) {
            int width = entry.getWidth(font);
            if (!row.isEmpty() && rowWidth + width > maxWidth) {
                rows.add(row);
                rowWidths.add(rowWidth);
                row = Lists.newArrayList();
                rowWidth = 2;
            }
            row.add(entry);
            rowWidth += width;
        }
        rows.add(row);
        rowWidths.add(rowWidth);

        // Draw the rows from the bottom up, so that the first row is on top
        for (int r = 0; r < rows.size(); r++) {
            int bottom = innerBottom - (rows.size() - 1 - r) * LEGEND_ROW_HEIGHT;
            guiGraphics.fill(innerLeft, bottom - LEGEND_ROW_HEIGHT, innerLeft + rowWidths.get(r), bottom, COLOR_TEXT_BACKGROUND);
            int x = innerLeft + 2;
            for (LegendEntry entry : rows.get(r)) {
                entry.drawSwatch(guiGraphics, x, bottom - 6);
                RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), font, entry.getLabel(),
                        x + SWATCH_SIZE + 2, bottom - 5, 0.5F, COLOR_TEXT_HINT, true, Font.DisplayMode.NORMAL);
                x += entry.getWidth(font);
            }
        }
        return rows.size() == 1 ? rowWidths.get(0) : -1;
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
        TerminalCraftingJobStatus status = getDisplayStatus(element);
        String statusKey = "gui.integratedterminals.craftingplan.status." + status.name().toLowerCase(Locale.ENGLISH);
        lines.add(Component.translatable("gui.integratedterminals.craftingplan.status", Component.translatable(statusKey))
                .withStyle(getStatusFormatting(status)));
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
                if (alternatives.isEmpty()) {
                    continue;
                }
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Arrow keys move the view in their direction, so the graph moves the other way
        int dx = 0;
        int dy = 0;
        switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT -> dx = KEY_PAN_DISTANCE;
            case GLFW.GLFW_KEY_RIGHT -> dx = -KEY_PAN_DISTANCE;
            case GLFW.GLFW_KEY_UP -> dy = KEY_PAN_DISTANCE;
            case GLFW.GLFW_KEY_DOWN -> dy = -KEY_PAN_DISTANCE;
            default -> {
                return false;
            }
        }
        camera.pan(dx, dy);
        camera.clamp(layout.getBounds(), getInnerWidth(), getInnerHeight());
        return true;
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        // Characters instead of key codes, so that these keys work on any keyboard layout
        switch (character) {
            case '+', '=' -> zoomIn();
            case '-', '_' -> zoomOut();
            case 'f', 'F' -> fit();
            default -> {
                return false;
            }
        }
        return true;
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

    protected static enum LegendSwatch {
        COLOR,
        DONE,
        TILE,
        SLOT
    }

    /**
     * An entry in the legend, with a small swatch that looks like the nodes it describes.
     */
    protected static class LegendEntry {
        private final String key;
        private final LegendSwatch swatch;
        private final int color;

        public LegendEntry(String key, LegendSwatch swatch, int color) {
            this.key = key;
            this.swatch = swatch;
            this.color = color;
        }

        public String getLabel() {
            return L10NHelpers.localize("gui.integratedterminals.craftingplan.graph.legend." + key);
        }

        public int getWidth(Font font) {
            return SWATCH_SIZE + 2 + font.width(getLabel()) / 2 + 4;
        }

        public void drawSwatch(GuiGraphics guiGraphics, int x, int y) {
            int size = SWATCH_SIZE;
            switch (swatch) {
                case COLOR -> guiGraphics.fill(x, y, x + size, y + size, color);
                case DONE -> {
                    guiGraphics.fill(x, y, x + size, y + size, color);
                    guiGraphics.fill(x, y, x + size, y + size, COLOR_DONE_FADE);
                }
                case TILE -> {
                    guiGraphics.fill(x, y, x + size, y + size, COLOR_NODE_OUTLINE);
                    guiGraphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, COLOR_NODE_FILL);
                }
                case SLOT -> {
                    guiGraphics.fill(x, y, x + size, y + size, COLOR_SLOT_LIGHT);
                    guiGraphics.fill(x, y, x + size - 1, y + size - 1, COLOR_SLOT_DARK);
                    guiGraphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, COLOR_SLOT_FILL);
                }
            }
        }
    }
}
