package org.cyclops.integratedterminals.client.gui.container;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.client.gui.component.WidgetScrollBar;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integratedterminals.Capabilities;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlanFlat;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlan;
import org.cyclops.integratedterminals.core.client.gui.CraftingJobGuiData;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingPlan;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalCraftingJobs;
import org.cyclops.integratedterminals.network.packet.CancelCraftingJobPacket;
import org.cyclops.integratedterminals.network.packet.OpenCraftingJobsPlanGuiPacket;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

/**
 * The crafting jobs overview gui.
 * @author rubensworks
 */
public class ContainerScreenTerminalCraftingJobs extends ContainerScreenExtended<ContainerTerminalCraftingJobs> {

    public static int OUTPUT_SLOT_X = 8;
    public static int OUTPUT_SLOT_Y = 17;

    public static int LINE_WIDTH = 221;

    // Width of the info area at the end of a line, which holds the status line and the progress bar below it.
    private static final int COLUMN_INFO = 132;
    private static final int COLUMN_INFO_SPACING = 6;
    private static final int PROGRESS_BAR_OFFSET_Y = 8;
    private static final int PROGRESS_BAR_HEIGHT = 8;

    private static final int BORDER_COLOR = Helpers.RGBAToInt(0, 0, 0, 180);
    private static final int TRACK_COLOR = Helpers.RGBAToInt(0, 0, 0, 100);
    private static final int FILL_COLOR = TerminalCraftingJobStatus.CRAFTING.getColor() | 0xFF000000;

    private final Player player;

    private WidgetScrollBar scrollBar;
    private int firstRow;

    public ContainerScreenTerminalCraftingJobs(ContainerTerminalCraftingJobs container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.player = inventory.player;
    }

    @Override
    public void init() {
        super.init();

        scrollBar = new WidgetScrollBar(leftPos + 236, topPos + 18, 178,
                Component.translatable("gui.cyclopscore.scrollbar"), this::setFirstRow, 10);
        scrollBar.setTotalRows(getMenu().getCraftingJobs().size() - 1);

        addRenderableWidget(new ButtonText(leftPos + 70, topPos + 198, 120, 20,
                Component.translatable("gui.integratedterminals.terminal_crafting_job.craftingplan.cancel_all"),
                Component.translatable("gui.integratedterminals.terminal_crafting_job.craftingplan.cancel_all"),
                (b) -> cancelCraftingJobs(), true));
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/crafting_plan.png");
    }

    @Override
    public int getBaseXSize() {
        return 256;
    }

    @Override
    public int getBaseYSize() {
        return 222;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        scrollBar.render(guiGraphics, mouseX, mouseY, partialTicks);
        RenderHelpers.bindTexture(this.texture);
        drawCraftingPlans(guiGraphics, leftPos, topPos, partialTicks, mouseX - leftPos, mouseY - topPos, ContainerScreenTerminalStorage.DrawLayer.BACKGROUND);

        // Draw plan label
        guiGraphics.drawString(Minecraft.getInstance().font,
                L10NHelpers.localize("parttype.integratedterminals.terminal_crafting_job"),
                leftPos + 8, topPos + 5, 16777215);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        drawCraftingPlans(guiGraphics, 0, 0, 0, mouseX, mouseY, ContainerScreenTerminalStorage.DrawLayer.FOREGROUND);

        // The progress bar only has room for bare numbers, so the labelled values go in a tooltip.
        // Outputs draw their own tooltip, which already contains these lines.
        HandlerWrappedTerminalCraftingPlan hoveredPlan = getHoveredPlan(mouseX, mouseY);
        if (hoveredPlan != null && !isHoveringOutputs(hoveredPlan, mouseX - leftPos)) {
            guiGraphics.renderComponentTooltip(font, getPlanTooltipLines(hoveredPlan.getCraftingPlanFlat()),
                    mouseX - leftPos, mouseY - topPos);
        }
    }

    protected List<HandlerWrappedTerminalCraftingPlan> getVisiblePlans() {
        return this.getMenu().getCraftingJobs()
                .subList(firstRow, Math.min(this.getMenu().getCraftingJobs().size(), firstRow + scrollBar.getVisibleRows()));
    }

    protected void drawCraftingPlans(GuiGraphics guiGraphics, int x, int y, float partialTicks, int mouseX, int mouseY, ContainerScreenTerminalStorage.DrawLayer layer) {
        int offsetY = OUTPUT_SLOT_Y;
        for (HandlerWrappedTerminalCraftingPlan craftingPlan : getVisiblePlans()) {
            drawCraftingPlan(guiGraphics, craftingPlan, x + OUTPUT_SLOT_X, y + offsetY, layer, partialTicks, mouseX, mouseY);
            offsetY += GuiHelpers.SLOT_SIZE;
        }
    }

    protected void drawCraftingPlan(GuiGraphics guiGraphics, HandlerWrappedTerminalCraftingPlan craftingPlan, int x, int y,
                                    ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int mouseX, int mouseY) {
        int xOriginal = x;
        ITerminalCraftingPlanFlat<?> plan = craftingPlan.getCraftingPlanFlat();

        // Draw background color if hovering
        if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND
                && RenderHelpers.isPointInRegion(x - leftPos, y - topPos, LINE_WIDTH, GuiHelpers.SLOT_SIZE, mouseX, mouseY)) {
            guiGraphics.fill(x + 1, y + 1, x + LINE_WIDTH + 1, y + GuiHelpers.SLOT_SIZE, -2130706433);
        }


        // Draw outputs
        List<Component> tooltipLines = layer == ContainerScreenTerminalStorage.DrawLayer.FOREGROUND
                ? getPlanTooltipLines(plan) : null;
        x += 4;
        for (IPrototypedIngredient<?, ?> output : plan.getOutputs()) {
            IngredientComponent<?, ?> ingredientComponent = output.getComponent();
            long quantity = ((IngredientComponent) ingredientComponent).getMatcher().getQuantity(output.getPrototype());
            int finalX = x;
            ingredientComponent.getCapability(Capabilities.IngredientComponentTerminalStorageHandler.INGREDIENT)
                    .ifPresent(h -> h.drawInstance(guiGraphics, output.getPrototype(), quantity,
                            GuiHelpers.quantityToScaledString(quantity), this, layer, partialTick, finalX, y + 1, mouseX, mouseY, tooltipLines, null));
            x += GuiHelpers.SLOT_SIZE_INNER;
        }

        if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND) {
            int infoX = xOriginal + LINE_WIDTH - COLUMN_INFO;

            // The size and channel are right-aligned, so that the status gets whatever room is left
            int infoRight = xOriginal + LINE_WIDTH - 2;
            if (plan.getChannel() != -1) {
                String channelString = L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.crafting_channel", plan.getChannel());
                infoRight -= scaledWidth(channelString);
                RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), font, channelString, infoRight, y + 1, 0.5f, 16777215, true, Font.DisplayMode.NORMAL);
                infoRight -= COLUMN_INFO_SPACING;
            }

            String dependenciesString = L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.dependencies", plan.getEntries().size());
            infoRight -= scaledWidth(dependenciesString);
            RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), font, dependenciesString, infoRight, y + 1, 0.5f, 16777215, true, Font.DisplayMode.NORMAL);

            String statusString = L10NHelpers.localize("gui.integratedterminals.craftingplan.status",
                    L10NHelpers.localize( "gui.integratedterminals.craftingplan.status." + plan.getStatus().name().toLowerCase(Locale.ENGLISH)));
            RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), font, statusString, infoX, y + 1, 0.5f, 16777215, true, Font.DisplayMode.NORMAL);

            drawProgressBar(guiGraphics, plan, infoX, y + PROGRESS_BAR_OFFSET_Y, COLUMN_INFO);
        }
    }

    /**
     * @return If the mouse is over one of the output instances of the given plan, which draw their own tooltip.
     */
    protected boolean isHoveringOutputs(HandlerWrappedTerminalCraftingPlan plan, double mouseX) {
        int outputs = plan.getCraftingPlanFlat().getOutputs().size();
        return mouseX >= OUTPUT_SLOT_X + 4
                && mouseX < OUTPUT_SLOT_X + 4 + GuiHelpers.SLOT_SIZE_INNER * outputs;
    }

    /**
     * Draw how far a job has come as a bar, with the time it has been running on the left,
     * the completed percentage in the middle, and the estimated time until it is done on the right.
     */
    protected void drawProgressBar(GuiGraphics guiGraphics, ITerminalCraftingPlanFlat<?> plan, int x, int y, int width) {
        int progress = GuiCraftingPlan.getProgress(plan);
        int filled = progress > 0 ? width * progress / 100 : 0;

        // A sunken track, so that the numbers inside the bar stay readable over both the track and the fill
        guiGraphics.fill(x, y, x + width, y + PROGRESS_BAR_HEIGHT, BORDER_COLOR);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + PROGRESS_BAR_HEIGHT - 1, TRACK_COLOR);
        if (filled > 1) {
            guiGraphics.fill(x + 1, y + 1, x + filled - 1, y + PROGRESS_BAR_HEIGHT - 1, FILL_COLOR);
        }

        long tickDuration = plan.getTickDuration();
        if (tickDuration >= 0) {
            drawProgressBarString(guiGraphics, GuiCraftingPlan.getDurationValue(tickDuration), x + 2, y);
        }
        if (progress >= 0) {
            String progressString = L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.progress.short", progress);
            drawProgressBarString(guiGraphics, progressString, x + (width - scaledWidth(progressString)) / 2, y);
        }
        if (plan.getStatus().isValid()) {
            String remainingString = GuiCraftingPlan.getDurationValue(plan.getEstimatedTickDurationRemaining());
            drawProgressBarString(guiGraphics, remainingString, x + width - 2 - scaledWidth(remainingString), y);
        }
    }

    protected int scaledWidth(String string) {
        return font.width(string) / 2;
    }

    protected void drawProgressBarString(GuiGraphics guiGraphics, String string, int x, int y) {
        RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), font, string,
                x, y + 2, 0.5f, 16777215, true, Font.DisplayMode.NORMAL);
    }

    protected List<Component> getPlanTooltipLines(ITerminalCraftingPlanFlat<?> plan) {
        List<Component> lines = Lists.newArrayList();

        // The status is shown in the same color that the plan guis use for it
        String status = plan.getStatus().name().toLowerCase(Locale.ENGLISH);
        lines.add(Component.translatable("gui.integratedterminals.craftingplan.status",
                        Component.translatable("gui.integratedterminals.craftingplan.status." + status)
                                .withStyle(Style.EMPTY.withColor(plan.getStatus().getColor() & 0xFFFFFF)))
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("gui.integratedterminals.craftingplan.status." + status + ".desc")
                .withStyle(ChatFormatting.DARK_GRAY));

        // Durations are what this tooltip is here for, so they get the brightest colors
        int progress = GuiCraftingPlan.getProgress(plan);
        if (progress >= 0) {
            lines.add(tooltipLine("gui.integratedterminals.terminal_crafting_job.craftingplan.progress",
                    String.valueOf(progress), ChatFormatting.WHITE));
        }
        long tickDuration = plan.getTickDuration();
        if (tickDuration >= 0) {
            lines.add(tooltipLine("gui.integratedterminals.terminal_crafting_job.craftingplan.duration",
                    GuiCraftingPlan.getDurationValue(tickDuration), ChatFormatting.WHITE));
        }
        // An unknown duration says nothing here, so it is left out rather than shown as a placeholder
        if (plan.getStatus().isValid()) {
            if (plan.getEstimatedTickDurationRemaining() >= 0) {
                lines.add(tooltipLine("gui.integratedterminals.terminal_crafting_job.craftingplan.duration.remaining",
                        GuiCraftingPlan.getDurationValue(plan.getEstimatedTickDurationRemaining()), ChatFormatting.AQUA));
            }
            if (plan.getEstimatedTickDurationTotal() >= 0) {
                lines.add(tooltipLine("gui.integratedterminals.terminal_crafting_job.craftingplan.duration.estimate",
                        GuiCraftingPlan.getDurationValue(plan.getEstimatedTickDurationTotal()), ChatFormatting.WHITE));
            }
        }

        // Everything that is only occasionally interesting stays in the background
        lines.add(Component.translatable("gui.integratedterminals.terminal_crafting_job.craftingplan.dependencies",
                plan.getEntries().size()).withStyle(ChatFormatting.DARK_GRAY));
        if (plan.getChannel() != -1) {
            lines.add(Component.translatable("gui.integratedterminals.terminal_crafting_job.craftingplan.crafting_channel",
                    plan.getChannel()).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (plan.getInitiatorName() != null) {
            lines.add(Component.translatable("gui.integratedterminals.terminal_crafting_job.craftingplan.owner",
                    plan.getInitiatorName()).withStyle(ChatFormatting.DARK_GRAY));
        }

        return lines;
    }

    /**
     * @return A tooltip line with a dimmed label, so that its value stands out.
     */
    protected static Component tooltipLine(String unlocalizedName, String value, ChatFormatting valueColor) {
        return Component.translatable(unlocalizedName, Component.literal(value).withStyle(valueColor))
                .withStyle(ChatFormatting.GRAY);
    }

    private void cancelCraftingJobs() {
        // Send packets to cancel crafting jobs
        for (HandlerWrappedTerminalCraftingPlan craftingJob : getMenu().getCraftingJobs()) {
            PartPos center = getMenu().getTarget().get().getCenter();
            CraftingJobGuiData data = new CraftingJobGuiData(center.getPos().getBlockPos(), center.getSide(),
                    getMenu().getChannel(), craftingJob.getHandler(),
                    craftingJob.getCraftingPlanFlat().getId());
            IntegratedTerminals._instance.getPacketHandler().sendToServer(new CancelCraftingJobPacket(data));
        }

        // Close the gui
        this.player.closeContainer();
    }

    @Nullable
    protected HandlerWrappedTerminalCraftingPlan getHoveredPlan(double mouseX, double mouseY) {
        mouseX -= leftPos;
        mouseY -= topPos;
        if (mouseX > OUTPUT_SLOT_X && mouseX < OUTPUT_SLOT_X + LINE_WIDTH
                && mouseY > OUTPUT_SLOT_Y && mouseY < OUTPUT_SLOT_Y + GuiHelpers.SLOT_SIZE * scrollBar.getVisibleRows()) {
            int index = (((int) mouseY) - OUTPUT_SLOT_Y) / GuiHelpers.SLOT_SIZE;
            List<HandlerWrappedTerminalCraftingPlan> plans = getVisiblePlans();
            if (index >= 0 && index < plans.size()) {
                return plans.get(index);
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        HandlerWrappedTerminalCraftingPlan plan = getHoveredPlan(mouseX, mouseY);
        if (plan != null) {
            PartPos pos = getMenu().getTarget().get().getCenter();
            OpenCraftingJobsPlanGuiPacket.send(pos.getPos().getBlockPos(), pos.getSide(), getMenu().getChannel(), plan);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double mouseXPrev, double mouseYPrev) {
        return this.getFocused() != null && this.isDragging() && mouseButton == 0 && this.getFocused().mouseDragged(mouseX, mouseY, mouseButton, mouseXPrev, mouseYPrev) ? true : super.mouseDragged(mouseX, mouseY, mouseButton, mouseXPrev, mouseYPrev);
    }

    public void setFirstRow(int firstRow) {
        this.firstRow = firstRow;
    }

    @Override
    public void onUpdate(int valueId, CompoundTag value) {
        super.onUpdate(valueId, value);

        if (valueId == this.getMenu().getValueIdCraftingJobs()) {
            this.init();
        }
    }
}
