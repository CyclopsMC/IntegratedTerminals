package org.cyclops.integratedterminals.client.gui.container.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;

import javax.annotation.Nullable;

/**
 * The header lines above a crafting plan view: label, durations, channel and initiator.
 * It is shared by all crafting plan views.
 * @author rubensworks
 */
public class GuiCraftingPlanHeader {

    private final String label;
    private final long tickDuration;
    private final long estimatedTickDuration;
    private final boolean estimatedTickDurationRemaining;
    private final boolean showEstimatedTickDuration;
    private final int channel;
    @Nullable
    private final String initiatorName;

    public GuiCraftingPlanHeader(String unlocalizedLabel, TerminalCraftingJobStatus status, long tickDuration,
                                 long estimatedTickDurationTotal, long estimatedTickDurationRemaining,
                                 int channel, @Nullable String initiatorName) {
        this.label = L10NHelpers.localize(unlocalizedLabel);
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
     * Draw the header lines above a view.
     * @param guiGraphics The gui graphics.
     * @param x The X position of the view.
     * @param y The Y position of the view.
     * @param width The width of the view, including its scrollbar.
     */
    public void drawBackground(GuiGraphics guiGraphics, int x, int y, int width) {
        Font fontRenderer = Minecraft.getInstance().font;
        int rightX = x + width - 38;

        // Draw plan label
        guiGraphics.drawCenteredString(fontRenderer, this.label, x + width / 2 - 1, y - 13, 16777215);

        // Draw duration
        if (tickDuration >= 0) {
            String durationString = GuiCraftingPlan.getDurationString(tickDuration);
            RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), fontRenderer, durationString, rightX, y - 14, 0.5f, 16777215, true, Font.DisplayMode.NORMAL);
        }

        // Draw channel
        if (channel != -1) {
            String channelString = L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.crafting_channel", channel);
            RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), fontRenderer, channelString, rightX, y - 8, 0.5f, 16777215, true, Font.DisplayMode.NORMAL);
        }

        // Draw initiator
        if (initiatorName != null) {
            String initiatorString = L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.owner", initiatorName);
            RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), fontRenderer, initiatorString, x - 4, y - 8, 0.5f, 16777215, true, Font.DisplayMode.NORMAL);
        }

        // Draw estimated duration
        if (showEstimatedTickDuration) {
            RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), fontRenderer, getEstimatedDurationString(), x - 4, y - 14, 0.5f, 16777215, true, Font.DisplayMode.NORMAL);
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
    public void drawForeground(AbstractContainerScreen parentGui, GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        if (showEstimatedTickDuration) {
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
