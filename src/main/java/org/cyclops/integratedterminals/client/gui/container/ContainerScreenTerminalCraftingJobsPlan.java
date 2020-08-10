package org.cyclops.integratedterminals.client.gui.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlan;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalCraftingJobsPlan;
import org.cyclops.integratedterminals.network.packet.CancelCraftingJobPacket;
import org.cyclops.integratedterminals.network.packet.OpenCraftingJobsGuiPacket;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

/**
 * A gui for visualizing a live crafting plan.
 * @author rubensworks
 */
public class ContainerScreenTerminalCraftingJobsPlan extends ContainerScreenExtended<ContainerTerminalCraftingJobsPlan> {

    private final PlayerEntity player;

    @Nullable
    private GuiCraftingPlan guiCraftingPlan;

    public ContainerScreenTerminalCraftingJobsPlan(ContainerTerminalCraftingJobsPlan container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);

        this.player = inventory.player;
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/crafting_plan.png");
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
    public void init() {
        super.init();

        this.buttons.clear();
        this.children.clear();

        ITerminalCraftingPlan craftingPlan = getContainer().getCraftingPlan().orElse(null);
        if (craftingPlan != null) {
            GuiCraftingPlan previousGuiCraftingPlan = this.guiCraftingPlan;
            this.guiCraftingPlan = new GuiCraftingPlan(this, craftingPlan, guiLeft, guiTop, 9, 18, 10);
            if (previousGuiCraftingPlan != null) {
                this.guiCraftingPlan.inheritVisualizationState(previousGuiCraftingPlan);
            }
            this.children.add(this.guiCraftingPlan);

            addButton(new ButtonText(guiLeft + 70, guiTop + 198, 100, 20,
                    L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.cancel"),
                    TextFormatting.BOLD + L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.cancel"),
                    (b) -> cancelCraftingJob(),
                    true)
            );
        } else {
            this.guiCraftingPlan = null;
        }
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
        if (typedChar == GLFW.GLFW_KEY_ESCAPE) {
            returnToOverview();
            return true;
        } else {
            return super.keyPressed(typedChar, keyCode, modifiers);
        }
    }

    private void returnToOverview() {
        PartPos center = getContainer().getTarget().get().getCenter();
        OpenCraftingJobsGuiPacket.send(center.getPos().getBlockPos(), center.getSide());
    }

    private void cancelCraftingJob() {
        // Send packet to cancel crafting job
        IntegratedTerminals._instance.getPacketHandler().sendToServer(
                new CancelCraftingJobPacket(getContainer().getCraftingJobGuiData()));

        // Return to overview
        returnToOverview();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        } else {
            drawCenteredString(font, L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.empty"),
                    guiLeft + getBaseXSize() / 2, guiTop + 23, 16777215);
        }
    }

    @Override
    protected void drawCurrentScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawCurrentScreen(mouseX, mouseY, partialTicks);
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.render(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.drawGuiContainerForegroundLayer(mouseX, mouseY);
        }
    }

    @Override
    public void onUpdate(int valueId, CompoundNBT value) {
        super.onUpdate(valueId, value);

        if (getContainer().getCraftingPlanNotifierId() == valueId) {
            this.init();
        }
    }
}
