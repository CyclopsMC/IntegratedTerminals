package org.cyclops.integratedterminals.client.gui.container;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.client.gui.component.WidgetScrollBar;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integratedterminals.Capabilities;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlanFlat;
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
        x += 4;
        for (IPrototypedIngredient<?, ?> output : plan.getOutputs()) {
            IngredientComponent<?, ?> ingredientComponent = output.getComponent();
            long quantity = ((IngredientComponent) ingredientComponent).getMatcher().getQuantity(output.getPrototype());
            int finalX = x;
            ingredientComponent.getCapability(Capabilities.IngredientComponentTerminalStorageHandler.INGREDIENT)
                    .ifPresent(h -> h.drawInstance(guiGraphics, output.getPrototype(), quantity,
                            GuiHelpers.quantityToScaledString(quantity), this, layer, partialTick, finalX, y + 1, mouseX, mouseY, null));
            x += GuiHelpers.SLOT_SIZE_INNER;
        }

        // Draw dependency count
        if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND) {
            String statusString = L10NHelpers.localize("gui.integratedterminals.craftingplan.status",
                    L10NHelpers.localize( "gui.integratedterminals.craftingplan.status." + plan.getStatus().name().toLowerCase(Locale.ENGLISH)));
            RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), font, statusString, xOriginal + LINE_WIDTH - 80, y + 1, 0.5f, 16777215, true, Font.DisplayMode.NORMAL);

            int dependencies = plan.getEntries().size();
            String dependenciesString = L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.dependencies", dependencies);
            RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), font, dependenciesString, xOriginal + LINE_WIDTH - 80, y + 7, 0.5f, 16777215, true, Font.DisplayMode.NORMAL);

            if (plan.getChannel() != -1) {
                String channelString = L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.crafting_channel", plan.getChannel());
                RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), font, channelString, xOriginal + LINE_WIDTH - 40, y + 7, 0.5f, 16777215, true, Font.DisplayMode.NORMAL);
            }

            long tickDuration = plan.getTickDuration();
            if (tickDuration >= 0) {
                String durationString = GuiCraftingPlan.getDurationString(tickDuration);
                RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), font, durationString, xOriginal + LINE_WIDTH - 80, y + 13, 0.5f, 16777215, true, Font.DisplayMode.NORMAL);
            }
        }
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
