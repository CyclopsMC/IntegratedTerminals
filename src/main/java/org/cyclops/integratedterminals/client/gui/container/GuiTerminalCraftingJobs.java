package org.cyclops.integratedterminals.client.gui.container;

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
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.capability.ingredient.IngredientComponentTerminalStorageHandlerConfig;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingPlan;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalCraftingJobs;

import java.io.IOException;

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
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        drawCraftingPlans(0, 0, 0, mouseX, mouseY, GuiTerminalStorage.DrawLayer.FOREGROUND);
    }

    protected void drawCraftingPlans(int x, int y, float partialTicks, int mouseX, int mouseY, GuiTerminalStorage.DrawLayer layer) {
        int offsetY = OUTPUT_SLOT_Y;
        for (HandlerWrappedTerminalCraftingPlan craftingPlan : this.getContainer().getCraftingJobs().subList(firstRow, Math.min(this.getContainer().getCraftingJobs().size(), firstRow + scrollBar.getVisibleRows()))) {
            drawCraftingPlan(craftingPlan, x + OUTPUT_SLOT_X, y + offsetY, layer, partialTicks, mouseX, mouseY);
            offsetY += GuiHelpers.SLOT_SIZE;
        }
    }

    protected void drawCraftingPlan(HandlerWrappedTerminalCraftingPlan craftingPlan, int x, int y,
                                    GuiTerminalStorage.DrawLayer layer, float partialTick, int mouseX, int mouseY) {
        int xOriginal = x;
        ITerminalCraftingPlan plan = craftingPlan.getCraftingPlan();

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
                            this, layer, partialTick, x, y, mouseX, mouseY, null);
            x += GuiHelpers.SLOT_SIZE_INNER;
        }

        // Draw dependency count
        if (layer == GuiTerminalStorage.DrawLayer.BACKGROUND) {
            int dependencies = getDependencies(plan);
            String dependenciesString = L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.dependencies", dependencies);
            RenderHelpers.drawScaledString(fontRenderer, dependenciesString, xOriginal + LINE_WIDTH - 50, y + 6, 0.5f, 16777215, true);
        }
    }

    protected static int getDependencies(ITerminalCraftingPlan plan) {
        int count = 1;
        for (ITerminalCraftingPlan dependency : plan.getDependencies()) {
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

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        // TODO: go to new gui when clicking on a plan
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
