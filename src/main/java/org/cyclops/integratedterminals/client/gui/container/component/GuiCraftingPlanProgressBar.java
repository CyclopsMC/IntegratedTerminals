package org.cyclops.integratedterminals.client.gui.container.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;

/**
 * A bar that shows how far a crafting job has come,
 * with the time it has been running on the left, the completed percentage in the middle,
 * and the estimated time until it is done on the right.
 * @author rubensworks
 */
public class GuiCraftingPlanProgressBar {

    public static final int HEIGHT = 8;

    private static final int BORDER_COLOR = IModHelpers.get().getBaseHelpers().RGBAToInt(0, 0, 0, 180);
    private static final int TRACK_COLOR = IModHelpers.get().getBaseHelpers().RGBAToInt(0, 0, 0, 100);
    private static final int FILL_COLOR = TerminalCraftingJobStatus.CRAFTING.getColor() | 0xFF000000;

    /**
     * @param guiGraphics The gui graphics.
     * @param progress The completed percentage, or -1 if unknown.
     * @param tickDuration The ticks the job has been running, or -1 if unknown.
     * @param estimatedTickDurationRemaining The estimated remaining ticks, or -1 if unknown.
     * @param showRemaining If the remaining duration should be shown.
     * @param x The X position.
     * @param y The Y position.
     * @param width The width.
     */
    public static void draw(GuiGraphicsExtractor guiGraphics, int progress, long tickDuration, long estimatedTickDurationRemaining,
                            boolean showRemaining, int x, int y, int width) {
        int filled = progress > 0 ? width * progress / 100 : 0;

        // A sunken track, so that the numbers inside the bar stay readable over both the track and the fill
        guiGraphics.fill(x, y, x + width, y + HEIGHT, BORDER_COLOR);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + HEIGHT - 1, TRACK_COLOR);
        if (filled > 1) {
            guiGraphics.fill(x + 1, y + 1, x + filled - 1, y + HEIGHT - 1, FILL_COLOR);
        }

        if (tickDuration >= 0) {
            drawString(guiGraphics, GuiCraftingPlan.getDurationValue(tickDuration), x + 2, y);
        }
        if (progress >= 0) {
            String progressString = IModHelpers.get().getL10NHelpers().localize("gui.integratedterminals.terminal_crafting_job.craftingplan.progress.short", progress);
            drawString(guiGraphics, progressString, x + (width - scaledWidth(progressString)) / 2, y);
        }
        if (showRemaining) {
            String remainingString = GuiCraftingPlan.getDurationValue(estimatedTickDurationRemaining);
            drawString(guiGraphics, remainingString, x + width - 2 - scaledWidth(remainingString), y);
        }
    }

    protected static int scaledWidth(String string) {
        return Minecraft.getInstance().font.width(string) / 2;
    }

    protected static void drawString(GuiGraphicsExtractor guiGraphics, String string, int x, int y) {
        IModHelpers.get().getRenderHelpers().drawScaledString(guiGraphics, Minecraft.getInstance().font, string,
                x, y + 2, 0.5f, ARGB.opaque(16777215), true, Font.DisplayMode.NORMAL);
    }
}
