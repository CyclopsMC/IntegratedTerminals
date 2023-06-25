package org.cyclops.integratedterminals.client.gui.container;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
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

    private final Player player;

    @Nullable
    private GuiCraftingPlan guiCraftingPlan;

    public ContainerScreenTerminalCraftingJobsPlan(ContainerTerminalCraftingJobsPlan container, Inventory inventory, Component title) {
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

        this.renderables.clear();
        this.children().clear();

        ITerminalCraftingPlan craftingPlan = getMenu().getCraftingPlan().orElse(null);
        if (craftingPlan != null) {
            GuiCraftingPlan previousGuiCraftingPlan = this.guiCraftingPlan;
            this.guiCraftingPlan = new GuiCraftingPlan(this, craftingPlan, leftPos, topPos, 9, 18, 10);
            if (previousGuiCraftingPlan != null) {
                this.guiCraftingPlan.inheritVisualizationState(previousGuiCraftingPlan);
            }
            addRenderableWidget(this.guiCraftingPlan);

            addRenderableWidget(new ButtonText(leftPos + 70, topPos + 198, 100, 20,
                    Component.translatable("gui.integratedterminals.terminal_crafting_job.craftingplan.cancel"),
                    Component.translatable("gui.integratedterminals.terminal_crafting_job.craftingplan.cancel"),
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
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.drawGuiContainerBackgroundLayer(guiGraphics, partialTicks, mouseX, mouseY);
        } else {
            guiGraphics.drawCenteredString(font, L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.empty"),
                    leftPos + getBaseXSize() / 2, topPos + 23, 16777215);
        }
    }

    @Override
    protected void drawCurrentScreen(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawCurrentScreen(guiGraphics, mouseX, mouseY, partialTicks);
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.drawGuiContainerForegroundLayer(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.guiCraftingPlan != null) {
            return guiCraftingPlan.mouseScrolled(mouseX, mouseY, delta);
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double mouseXPrev, double mouseYPrev) {
        if (this.guiCraftingPlan != null) {
            return guiCraftingPlan.mouseDragged(mouseX, mouseY, mouseButton, mouseXPrev, mouseYPrev);
        }
        return super.mouseDragged(mouseX, mouseY, mouseButton, mouseXPrev, mouseYPrev);
    }

    @Override
    public void onUpdate(int valueId, CompoundTag value) {
        super.onUpdate(valueId, value);

        if (getMenu().getCraftingPlanNotifierId() == valueId) {
            this.init();
        }
    }
}
