package org.cyclops.integratedterminals.client.gui.container;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.client.gui.component.GuiScrollBar;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerExtended;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.capability.ingredient.IngredientComponentTerminalStorageHandlerConfig;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlan;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingPlan;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalCraftingJobs;
import org.cyclops.integratedterminals.network.packet.OpenCraftingJobsPlanGuiPacket;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * The crafting jobs overview gui.
 * @author rubensworks
 */
public class GuiTerminalCraftingJobs extends GuiContainerExtended {

    public static int OUTPUT_SLOT_X = 8;
    public static int OUTPUT_SLOT_Y = 17;

    public static int LINE_WIDTH = 221;

    private GuiScrollBar scrollBar;
    private int firstRow;

    public GuiTerminalCraftingJobs(EntityPlayer player, PartTarget target, IPartContainer partContainer,
                                   IPartType partType) {
        super(new ContainerTerminalCraftingJobs(player, target, partContainer, partType));
    }

    @Override
    public void initGui() {
        super.initGui();

        scrollBar = new GuiScrollBar(guiLeft + 236, guiTop + 18, 178, this::setFirstRow, 10);
        scrollBar.setTotalRows(getContainer().getCraftingJobs().size() - 1);
    }

    @Override
    protected ResourceLocation constructResourceLocation() {
        return new ResourceLocation(Reference.MOD_ID, this.getGuiTexture());
    }

    @Override
    public String getGuiTexture() {
        return IntegratedTerminals._instance.getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI)
                + "crafting_plan.png";
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
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        scrollBar.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        RenderHelpers.bindTexture(this.texture);
        drawCraftingPlans(guiLeft, guiTop, partialTicks, mouseX - guiLeft, mouseY - guiTop, GuiTerminalStorage.DrawLayer.BACKGROUND);

        // Draw plan label
        drawString(Minecraft.getMinecraft().fontRenderer,
                L10NHelpers.localize("parttype.parttypes.integratedterminals.terminal_crafting_job.name"),
                guiLeft + 8, guiTop + 5, 16777215);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        drawCraftingPlans(0, 0, 0, mouseX, mouseY, GuiTerminalStorage.DrawLayer.FOREGROUND);
    }

    protected List<HandlerWrappedTerminalCraftingPlan> getVisiblePlans() {
        return this.getContainer().getCraftingJobs().subList(firstRow, Math.min(this.getContainer().getCraftingJobs().size(), firstRow + scrollBar.getVisibleRows()));
    }

    protected void drawCraftingPlans(int x, int y, float partialTicks, int mouseX, int mouseY, GuiTerminalStorage.DrawLayer layer) {
        int offsetY = OUTPUT_SLOT_Y;
        for (HandlerWrappedTerminalCraftingPlan craftingPlan : getVisiblePlans()) {
            drawCraftingPlan(craftingPlan, x + OUTPUT_SLOT_X, y + offsetY, layer, partialTicks, mouseX, mouseY);
            offsetY += GuiHelpers.SLOT_SIZE;
        }
    }

    protected void drawCraftingPlan(HandlerWrappedTerminalCraftingPlan craftingPlan, int x, int y,
                                    GuiTerminalStorage.DrawLayer layer, float partialTick, int mouseX, int mouseY) {
        int xOriginal = x;
        ITerminalCraftingPlan<?> plan = craftingPlan.getCraftingPlan();

        // Draw background color if hovering
        if (layer == GuiTerminalStorage.DrawLayer.BACKGROUND
                && RenderHelpers.isPointInRegion(x - guiLeft, y - guiTop, LINE_WIDTH, GuiHelpers.SLOT_SIZE, mouseX, mouseY)) {
            drawRect(x + 1, y + 1, x + LINE_WIDTH + 1, y + GuiHelpers.SLOT_SIZE, -2130706433);
        }


        // Draw outputs
        x += 4;
        for (IPrototypedIngredient<?, ?> output : plan.getOutputs()) {
            IngredientComponent<?, ?> ingredientComponent = output.getComponent();
            long quantity = ((IngredientComponent) ingredientComponent).getMatcher().getQuantity(output.getPrototype());
            ingredientComponent.getCapability(IngredientComponentTerminalStorageHandlerConfig.CAPABILITY)
                    .drawInstance(output.getPrototype(), quantity, GuiHelpers.quantityToScaledString(quantity),
                            this, layer, partialTick, x, y + 1, mouseX, mouseY, null);
            x += GuiHelpers.SLOT_SIZE_INNER;
        }

        // Draw dependency count
        if (layer == GuiTerminalStorage.DrawLayer.BACKGROUND) {
            String statusString = L10NHelpers.localize("gui.integratedterminals.craftingplan.status",
                    L10NHelpers.localize( "gui.integratedterminals.craftingplan.status." + plan.getStatus().name().toLowerCase(Locale.ENGLISH) + ".name"));
            RenderHelpers.drawScaledString(fontRenderer, statusString, xOriginal + LINE_WIDTH - 80, y + 1, 0.5f, 16777215, true);

            int dependencies = getDependencies(plan);
            String dependenciesString = L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.dependencies", dependencies);
            RenderHelpers.drawScaledString(fontRenderer, dependenciesString, xOriginal + LINE_WIDTH - 80, y + 7, 0.5f, 16777215, true);

            if (plan.getChannel() != -1) {
                String channelString = L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.crafting_channel", plan.getChannel());
                RenderHelpers.drawScaledString(fontRenderer, channelString, xOriginal + LINE_WIDTH - 40, y + 7, 0.5f, 16777215, true);
            }

            long tickDuration = plan.getTickDuration();
            if (tickDuration >= 0) {
                String durationString = GuiCraftingPlan.getDurationString(tickDuration);
                RenderHelpers.drawScaledString(fontRenderer, durationString, xOriginal + LINE_WIDTH - 80, y + 13, 0.5f, 16777215, true);
            }
        }
    }

    protected static int getDependencies(ITerminalCraftingPlan<?> plan) {
        int count = 1;
        for (ITerminalCraftingPlan<?> dependency : plan.getDependencies()) {
            count += getDependencies(dependency);
        }
        return count;
    }

    @Override
    protected void drawCurrentScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawCurrentScreen(mouseX, mouseY, partialTicks);
        scrollBar.drawCurrentScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public ContainerTerminalCraftingJobs getContainer() {
        return (ContainerTerminalCraftingJobs) super.getContainer();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        scrollBar.handleMouseInput();
    }

    @Nullable
    protected HandlerWrappedTerminalCraftingPlan getHoveredPlan(int mouseX, int mouseY) {
        mouseX -= guiLeft;
        mouseY -= guiTop;
        if (mouseX > OUTPUT_SLOT_X && mouseX < OUTPUT_SLOT_X + LINE_WIDTH
                && mouseY > OUTPUT_SLOT_Y && mouseY < OUTPUT_SLOT_Y + GuiHelpers.SLOT_SIZE) {
            int index = (mouseY - OUTPUT_SLOT_Y) / GuiHelpers.SLOT_SIZE;
            List<HandlerWrappedTerminalCraftingPlan> plans = getVisiblePlans();
            if (index >= 0 && index < plans.size()) {
                return plans.get(index);
            }
        }
        return null;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        HandlerWrappedTerminalCraftingPlan plan = getHoveredPlan(mouseX, mouseY);
        if (plan != null) {
            PartPos pos = getContainer().getTarget().getCenter();
            OpenCraftingJobsPlanGuiPacket.send(pos.getPos().getBlockPos(), pos.getSide(), getContainer().getChannel(), plan);
        }
    }

    public void setFirstRow(int firstRow) {
        this.firstRow = firstRow;
    }

    @Override
    public void onUpdate(int valueId, NBTTagCompound value) {
        super.onUpdate(valueId, value);

        if (valueId == this.getContainer().getValueIdCraftingJobs()) {
            this.initGui();
        }
    }
}
