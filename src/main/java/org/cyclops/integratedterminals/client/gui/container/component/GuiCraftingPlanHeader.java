package org.cyclops.integratedterminals.client.gui.container.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlanFlat;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingPlanStatic;

import javax.annotation.Nullable;

/**
 * The header lines above a crafting plan view: label, durations, channel and initiator.
 * It is shared by all crafting plan views.
 * @author rubensworks
 */
public class GuiCraftingPlanHeader {

    private static final String LABEL_COMPLETED = "gui.integratedterminals.terminal_storage.craftingplan.label.completed";

    private final String label;
    private final long tickDuration;
    private final long estimatedTickDuration;
    private final boolean estimatedTickDurationRemaining;
    private final boolean showEstimatedTickDuration;
    private final int channel;
    @Nullable
    private final String initiatorName;
    // Running jobs show their durations in a progress bar instead
    private final boolean showDurations;

    /**
     * @param unlocalizedLabel The plan label.
     * @param status The plan status.
     * @param completed If the plan has nothing left to craft.
     * @param tickDuration The duration so far in ticks.
     * @param estimatedTickDurationTotal The estimated total duration in ticks.
     * @param estimatedTickDurationRemaining The estimated remaining duration in ticks.
     * @param channel The crafting channel.
     * @param initiatorName The name of the player that started the job.
     */
    public GuiCraftingPlanHeader(String unlocalizedLabel, TerminalCraftingJobStatus status, boolean completed, long tickDuration,
                                 long estimatedTickDurationTotal, long estimatedTickDurationRemaining,
                                 int channel, @Nullable String initiatorName) {
        // A running job that has nothing left to craft has completed
        boolean running = TerminalCraftingPlanStatic.Label.RUNNING.getUnlocalizedMessage().equals(unlocalizedLabel);
        this.label = IModHelpers.get().getL10NHelpers().localize(completed && running ? LABEL_COMPLETED : unlocalizedLabel);
        this.showDurations = !running;
        this.tickDuration = tickDuration;
        // Jobs that have not started yet can only show a total estimation, running jobs show what is left of it
        this.estimatedTickDurationRemaining = status != TerminalCraftingJobStatus.UNSTARTED;
        this.estimatedTickDuration = this.estimatedTickDurationRemaining
                ? estimatedTickDurationRemaining : estimatedTickDurationTotal;
        // Jobs that can not be crafted have nothing to estimate, other jobs show a placeholder until they are measured
        this.showEstimatedTickDuration = status.isValid();
        this.channel = channel;
        this.initiatorName = initiatorName;
    }

    /**
     * @param craftingPlan A crafting plan.
     * @return The header for the given plan.
     */
    public static GuiCraftingPlanHeader forPlan(ITerminalCraftingPlan<?> craftingPlan) {
        return new GuiCraftingPlanHeader(craftingPlan.getUnlocalizedLabel(), craftingPlan.getStatus(),
                craftingPlan.getStatus() == TerminalCraftingJobStatus.FINISHED && craftingPlan.getCraftingQuantity() == 0,
                craftingPlan.getTickDuration(), craftingPlan.getEstimatedTickDurationTotal(),
                craftingPlan.getEstimatedTickDurationRemaining(), craftingPlan.getChannel(), craftingPlan.getInitiatorName());
    }

    /**
     * @param craftingPlan A flattened crafting plan.
     * @return The header for the given plan.
     */
    public static GuiCraftingPlanHeader forPlan(ITerminalCraftingPlanFlat<?> craftingPlan) {
        return new GuiCraftingPlanHeader(craftingPlan.getUnlocalizedLabel(), craftingPlan.getStatus(),
                craftingPlan.getStatus() == TerminalCraftingJobStatus.FINISHED && craftingPlan.getCraftingQuantityRemaining() == 0,
                craftingPlan.getTickDuration(), craftingPlan.getEstimatedTickDurationTotal(),
                craftingPlan.getEstimatedTickDurationRemaining(), craftingPlan.getChannel(), craftingPlan.getInitiatorName());
    }

    /**
     * Draw the header lines above a view.
     * @param guiGraphics The gui graphics.
     * @param x The X position of the view.
     * @param y The Y position of the view.
     * @param width The width of the view, including its scrollbar.
     */
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int x, int y, int width) {
        Font fontRenderer = Minecraft.getInstance().font;
        int rightX = x + width - 38;

        // Draw plan label
        guiGraphics.centeredText(fontRenderer, this.label, x + width / 2 - 1, y - 13, ARGB.opaque(16777215));

        // Without durations, the other lines take their place at the top
        int secondLineY = showDurations ? y - 8 : y - 14;

        // Draw duration
        if (showDurations && tickDuration >= 0) {
            String durationString = GuiCraftingPlan.getDurationString(tickDuration);
            IModHelpers.get().getRenderHelpers().drawScaledString(guiGraphics, fontRenderer, durationString, rightX, y - 14, 0.5f, ARGB.opaque(16777215), true, Font.DisplayMode.NORMAL);
        }

        // Draw channel
        if (channel != -1) {
            String channelString = IModHelpers.get().getL10NHelpers().localize("gui.integratedterminals.terminal_crafting_job.craftingplan.crafting_channel", channel);
            IModHelpers.get().getRenderHelpers().drawScaledString(guiGraphics, fontRenderer, channelString, rightX, secondLineY, 0.5f, ARGB.opaque(16777215), true, Font.DisplayMode.NORMAL);
        }

        // Draw initiator
        if (initiatorName != null) {
            String initiatorString = IModHelpers.get().getL10NHelpers().localize("gui.integratedterminals.terminal_crafting_job.craftingplan.owner", initiatorName);
            IModHelpers.get().getRenderHelpers().drawScaledString(guiGraphics, fontRenderer, initiatorString, x - 4, secondLineY, 0.5f, ARGB.opaque(16777215), true, Font.DisplayMode.NORMAL);
        }

        // Draw estimated duration
        if (showDurations && showEstimatedTickDuration) {
            IModHelpers.get().getRenderHelpers().drawScaledString(guiGraphics, fontRenderer, getEstimatedDurationString(), x - 4, y - 14, 0.5f, ARGB.opaque(16777215), true, Font.DisplayMode.NORMAL);
        }
    }

    /**
     * Draw the header tooltips.
     * @param parentGui The gui that the header is drawn in.
     * @param guiGraphics The gui graphics.
     * @param x The X position of the view, relative to the gui.
     * @param y The Y position of the view, relative to the gui.
     * @param mouseX The mouse X position.
     * @param mouseY The mouse Y position.
     */
    public void drawForeground(AbstractContainerScreen parentGui, GuiGraphicsExtractor guiGraphics, int x, int y, int mouseX, int mouseY) {
        if (showDurations && showEstimatedTickDuration) {
            GuiCraftingPlan.drawUnknownDurationTooltip(parentGui, guiGraphics, getEstimatedDurationString(),
                    estimatedTickDuration, x - 4, y - 14, mouseX, mouseY);
        }
    }

    protected String getEstimatedDurationString() {
        return GuiCraftingPlan.getDurationString(estimatedTickDurationRemaining
                ? "gui.integratedterminals.terminal_crafting_job.craftingplan.duration.remaining"
                : "gui.integratedterminals.terminal_crafting_job.craftingplan.duration.estimate", estimatedTickDuration);
    }

}
