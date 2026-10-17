package org.cyclops.integratedterminals.client.gui.container;

import com.google.common.collect.Maps;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlanFlat;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingPlanStatic;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlan;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanFlat;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanGraph;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanProgressBar;
import org.cyclops.integratedterminals.client.gui.container.component.IGuiCraftingPlanView;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalCraftingJobsPlan;
import org.cyclops.integratedterminals.network.packet.CancelCraftingJobPacket;
import org.cyclops.integratedterminals.network.packet.OpenCraftingJobsGuiPacket;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * A gui for visualizing a live crafting plan.
 * @author rubensworks
 */
public class ContainerScreenTerminalCraftingJobsPlan extends ContainerScreenTerminalCraftingPlanBase<ContainerTerminalCraftingJobsPlan> {

    private static final int PROGRESS_BAR_SPACING = 6;

    @Nullable
    private ButtonText cancelButton;

    // The last view of each type is kept, so that its visualization state can be inherited after an update
    @Nullable
    private GuiCraftingPlan guiCraftingPlan;
    @Nullable
    private GuiCraftingPlanFlat guiCraftingPlanFlat;

    private boolean craftingPlanInitialized = false;
    private boolean craftingPlanFlatInitialized = false;

    @Nullable
    private ITerminalCraftingPlan lastCraftingPlan;
    @Nullable
    private ITerminalCraftingPlan completedCraftingPlan;
    @Nullable
    private ITerminalCraftingPlanFlat completedCraftingPlanFlat;
    // Outputs are only what is left to craft, so the last non-empty outputs per job id are kept for the completed plan
    private final Map<Object, List<IPrototypedIngredient<?, ?>>> knownOutputs = Maps.newHashMap();

    public ContainerScreenTerminalCraftingJobsPlan(ContainerTerminalCraftingJobsPlan container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected IGuiCraftingPlanView createTreeView() {
        GuiCraftingPlan previous = this.guiCraftingPlan;
        this.guiCraftingPlan = new GuiCraftingPlan(this, getCraftingPlan(), leftPos, topPos, 9, 18, 10);
        if (previous != null) {
            this.guiCraftingPlan.inheritVisualizationState(previous);
        }
        return this.guiCraftingPlan;
    }

    @Override
    protected IGuiCraftingPlanView createFlatView() {
        GuiCraftingPlanFlat previous = this.guiCraftingPlanFlat;
        this.guiCraftingPlanFlat = new GuiCraftingPlanFlat(this, getCraftingPlanFlat(), leftPos, topPos, 9, 18, 10);
        if (previous != null) {
            this.guiCraftingPlanFlat.inheritVisualizationState(previous);
        }
        return this.guiCraftingPlanFlat;
    }

    @Override
    protected boolean isLive() {
        return true;
    }

    @Override
    protected String getEmptyMessage() {
        return "gui.integratedterminals.terminal_crafting_job.craftingplan.empty";
    }

    @Override
    protected void onEscape() {
        returnToOverview();
    }

    @Override
    public void init() {
        super.init();
        this.cancelButton = null;

        // Plans that are too large are only sent in their flattened form
        if (this.activeView != null && (getMenu().getCraftingPlan().isPresent() || getMenu().getCraftingPlanFlat().isPresent())) {
            addRenderableWidget(this.cancelButton = new ButtonText(leftPos + imageWidth - 25 - 100, topPos + imageHeight - 24, 100, 20,
                    Component.translatable("gui.integratedterminals.terminal_crafting_job.craftingplan.cancel"),
                    Component.translatable("gui.integratedterminals.terminal_crafting_job.craftingplan.cancel"),
                    (b) -> cancelCraftingJob(),
                    true)
            );
        }
    }

    /**
     * @return The plan of the running job, or the completed version of its last plan once it has finished.
     */
    @Nullable
    @Override
    protected ITerminalCraftingPlan getCraftingPlan() {
        if (getMenu().getCraftingPlan().isPresent()) {
            ITerminalCraftingPlan craftingPlan = getMenu().getCraftingPlan().get();
            if (craftingPlan != this.lastCraftingPlan) {
                this.lastCraftingPlan = craftingPlan;
                rememberOutputs(craftingPlan);
            }
            this.completedCraftingPlan = null;
            this.completedCraftingPlanFlat = null;
            return this.lastCraftingPlan;
        }
        // Finished jobs are removed from the network, so keep showing the result instead of an empty job.
        // Only do this if everything was crafted, as the job may also have been cancelled elsewhere.
        // The server does not always send a last plan with nothing left to craft before removing the job.
        if (this.lastCraftingPlan != null && this.lastCraftingPlan.getCraftingQuantity() == 0) {
            if (this.completedCraftingPlan == null) {
                this.completedCraftingPlan = TerminalCraftingPlanStatic.asCompleted(this.lastCraftingPlan, this.knownOutputs);
            }
            return this.completedCraftingPlan;
        }
        return null;
    }

    protected void rememberOutputs(ITerminalCraftingPlan<?> craftingPlan) {
        List<IPrototypedIngredient<?, ?>> outputs = craftingPlan.getOutputs();
        long remaining = craftingPlan.getCraftingQuantity();
        if (remaining > 0 && outputs.stream().anyMatch(output -> !GuiCraftingPlanGraph.isEmpty(output))) {
            // Outputs are for the remaining crafts, so scale them to all crafts of the job
            long total = Math.max(craftingPlan.getCraftingQuantityTotal(), remaining);
            this.knownOutputs.put(craftingPlan.getId(), outputs.stream()
                    .<IPrototypedIngredient<?, ?>>map(output -> scaleQuantity(output, total, remaining))
                    .toList());
        }
        for (ITerminalCraftingPlan<?> dependency : craftingPlan.getDependencies()) {
            rememberOutputs(dependency);
        }
    }

    protected static IPrototypedIngredient<?, ?> scaleQuantity(IPrototypedIngredient ingredient, long multiplier, long divisor) {
        IIngredientMatcher matcher = ingredient.getComponent().getMatcher();
        long quantity = matcher.getQuantity(ingredient.getPrototype()) * multiplier / divisor;
        return new PrototypedIngredient(ingredient.getComponent(),
                matcher.withQuantity(ingredient.getPrototype(), quantity), ingredient.getCondition());
    }

    @Nullable
    @Override
    protected ITerminalCraftingPlanFlat getCraftingPlanFlat() {
        if (getMenu().getCraftingPlanFlat().isPresent()) {
            return getMenu().getCraftingPlanFlat().get();
        }
        ITerminalCraftingPlan craftingPlan = getCraftingPlan();
        if (craftingPlan == null || craftingPlan != this.completedCraftingPlan) {
            return null;
        }
        // Flatten only once, as this is called multiple times on each init
        if (this.completedCraftingPlanFlat == null) {
            this.completedCraftingPlanFlat = craftingPlan.flatten();
        }
        return this.completedCraftingPlanFlat;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        ITerminalCraftingPlanFlat craftingPlanFlat = getCraftingPlanFlat();
        if (this.activeView != null && craftingPlanFlat != null) {
            // The graph also counts the jobs that are already done, which the flattened plan no longer contains
            int progress = this.activeView instanceof GuiCraftingPlanGraph graph
                    ? graph.getProgress() : GuiCraftingPlan.getProgress(craftingPlanFlat);
            int left = getProgressBarLeft();
            GuiCraftingPlanProgressBar.draw(guiGraphics, progress, craftingPlanFlat.getTickDuration(),
                    craftingPlanFlat.getEstimatedTickDurationRemaining(), craftingPlanFlat.getStatus().isValid(),
                    left, getProgressBarTop(), getProgressBarRight() - left);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractLabels(guiGraphics, mouseX, mouseY);
        ITerminalCraftingPlanFlat craftingPlanFlat = getCraftingPlanFlat();
        if (this.activeView != null && craftingPlanFlat != null && craftingPlanFlat.getStatus().isValid()) {
            // Explain an unknown remaining duration when hovering it, as the plan header no longer shows it for running jobs
            long remaining = craftingPlanFlat.getEstimatedTickDurationRemaining();
            String remainingString = GuiCraftingPlan.getDurationValue(remaining);
            int width = font.width(remainingString) / 2;
            GuiCraftingPlan.drawUnknownDurationTooltip(this, guiGraphics, remainingString, remaining,
                    getProgressBarRight() - 2 - width - leftPos, getProgressBarTop() + 2 - topPos, mouseX, mouseY);
        }
    }

    protected int getProgressBarLeft() {
        return this.viewButtonsRight + PROGRESS_BAR_SPACING;
    }

    protected int getProgressBarRight() {
        return this.cancelButton != null ? this.cancelButton.getX() - PROGRESS_BAR_SPACING : leftPos + imageWidth - 25;
    }

    protected int getProgressBarTop() {
        return topPos + imageHeight - 23 + (18 - GuiCraftingPlanProgressBar.HEIGHT) / 2;
    }

    private void returnToOverview() {
        PartPos center = getMenu().getTarget().get().getCenter();
        OpenCraftingJobsGuiPacket.send(center.getPos().getBlockPos(), center.getSide());
    }

    private void cancelCraftingJob() {
        // Send packet to cancel crafting job
        IntegratedTerminals._instance.getPacketHandler().sendToServer(
                new CancelCraftingJobPacket(getMenu().getCraftingJobGuiData()));

        // Return to overview
        returnToOverview();
    }

    @Override
    public void onUpdate(int valueId, CompoundTag value) {
        super.onUpdate(valueId, value);

        if (getMenu().getCraftingPlanNotifierId() == valueId || getMenu().getCraftingPlanFlatNotifierId() == valueId) {
            if (!craftingPlanInitialized || !craftingPlanFlatInitialized) {
                this.guiCraftingPlanToggler.setCraftingPlanDisplayMode(null);
            }
            this.init();
        }

        if (getMenu().getCraftingPlanNotifierId() == valueId) {
            craftingPlanInitialized = true;
        }
        if (getMenu().getCraftingPlanFlatNotifierId() == valueId) {
            craftingPlanFlatInitialized = true;
        }
    }
}
