package org.cyclops.integratedterminals.client.gui.container;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
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
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlan;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanFlat;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanGraph;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanToggler;
import org.cyclops.integratedterminals.client.gui.container.component.IGuiCraftingPlanView;
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

    private static final ResourceLocation TEXTURE_PANEL = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/part_terminal_storage.png");

    private final Player player;
    private GuiCraftingPlanToggler guiCraftingPlanToggler;

    @Nullable
    private IGuiCraftingPlanView activeView;
    // The last view of each type is kept, so that its visualization state can be inherited after an update
    @Nullable
    private GuiCraftingPlan guiCraftingPlan;
    @Nullable
    private GuiCraftingPlanGraph guiCraftingPlanGraph;
    @Nullable
    private GuiCraftingPlanFlat guiCraftingPlanFlat;

    private boolean craftingPlanInitialized = false;
    private boolean craftingPlanFlatInitialized = false;

    public ContainerScreenTerminalCraftingJobsPlan(ContainerTerminalCraftingJobsPlan container, Inventory inventory, Component title) {
        super(container, inventory, title);

        this.player = inventory.player;
        this.guiCraftingPlanToggler = new GuiCraftingPlanToggler(
                () -> this.getMenu().getCraftingPlan().orElse(null),
                () -> this.getMenu().getCraftingPlanFlat().orElse(null),
                () -> {
                    GuiCraftingPlan previousGuiCraftingPlan = this.guiCraftingPlan;
                    this.guiCraftingPlan = new GuiCraftingPlan(this, this.getMenu().getCraftingPlan().get(), leftPos, topPos, 9, 18, 10);
                    if (previousGuiCraftingPlan != null) {
                        this.guiCraftingPlan.inheritVisualizationState(previousGuiCraftingPlan);
                    }
                    setActiveView(this.guiCraftingPlan);
                },
                () -> {
                    GuiCraftingPlanGraph previousGuiCraftingPlanGraph = this.guiCraftingPlanGraph;
                    this.guiCraftingPlanGraph = new GuiCraftingPlanGraph(this, this.getMenu().getCraftingPlan().get(), leftPos, topPos, 9, 18,
                            imageWidth - 18, imageHeight - 44, true);
                    if (previousGuiCraftingPlanGraph != null) {
                        this.guiCraftingPlanGraph.inheritVisualizationState(previousGuiCraftingPlanGraph);
                    }
                    // Overlay buttons must be added before the graph, so that they receive clicks first
                    for (Button button : GuiCraftingPlanToggler.createGraphButtons(this.guiCraftingPlanGraph, this::init)) {
                        addRenderableWidget(button);
                    }
                    setActiveView(this.guiCraftingPlanGraph);
                },
                () -> {
                    GuiCraftingPlanFlat previousGuiCraftingPlan = this.guiCraftingPlanFlat;
                    this.guiCraftingPlanFlat = new GuiCraftingPlanFlat(this, this.getMenu().getCraftingPlanFlat().get(), leftPos, topPos, 9, 18, 10);
                    if (previousGuiCraftingPlan != null) {
                        this.guiCraftingPlanFlat.inheritVisualizationState(previousGuiCraftingPlan);
                    }
                    setActiveView(this.guiCraftingPlanFlat);
                },
                () -> this.activeView = null
        );
    }

    protected void setActiveView(IGuiCraftingPlanView view) {
        this.activeView = view;
        addRenderableWidget(view);
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/crafting_plan.png");
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return this.guiCraftingPlanToggler.getCraftingPlanDisplayMode() == GuiCraftingPlanToggler.CraftingPlanDisplayMode.FLAT ? ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/crafting_plan_flat.png") : super.getGuiTexture();
    }

    @Override
    public int getBaseXSize() {
        return this.guiCraftingPlanToggler.getWidth(this.width);
    }

    @Override
    public int getBaseYSize() {
        return this.guiCraftingPlanToggler.getHeight(this.height);
    }

    @Override
    public void init() {
        // The display mode determines the gui size
        this.guiCraftingPlanToggler.resolveDisplayMode();
        super.init();

        // Reset states
        this.renderables.clear();
        this.children().clear();

        for (Button button : this.guiCraftingPlanToggler.createViewButtons(leftPos + 8, topPos + imageHeight - 23, this::init)) {
            addRenderableWidget(button);
        }

        this.guiCraftingPlanToggler.init();

        if (this.activeView != null) {
            addRenderableWidget(new ButtonText(leftPos + imageWidth - 25 - 100, topPos + imageHeight - 24, 100, 20,
                    Component.translatable("gui.integratedterminals.terminal_crafting_job.craftingplan.cancel"),
                    Component.translatable("gui.integratedterminals.terminal_crafting_job.craftingplan.cancel"),
                    (b) -> cancelCraftingJob(),
                    true)
            );
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
        if (this.guiCraftingPlanToggler.getCraftingPlanDisplayMode() == GuiCraftingPlanToggler.CraftingPlanDisplayMode.GRAPH) {
            ContainerScreenTerminalStorage.drawPanel(guiGraphics, TEXTURE_PANEL, leftPos, topPos, imageWidth, imageHeight);
        } else {
            super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        }
        if (this.activeView != null) {
            activeView.drawGuiContainerBackgroundLayer(guiGraphics, partialTicks, mouseX, mouseY);
        } else {
            guiGraphics.drawCenteredString(font, L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.empty"),
                    leftPos + imageWidth / 2, topPos + 23, 16777215);
        }
    }

    @Override
    protected void drawCurrentScreen(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawCurrentScreen(guiGraphics, mouseX, mouseY, partialTicks);
        if (this.activeView != null) {
            activeView.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        if (this.activeView != null) {
            activeView.drawGuiContainerForegroundLayer(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double mouseZ, double delta) {
        if (this.activeView != null) {
            return activeView.mouseScrolled(mouseX, mouseY, mouseZ, delta);
        }
        return super.mouseScrolled(mouseX, mouseY, mouseZ, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double mouseXPrev, double mouseYPrev) {
        if (this.activeView != null) {
            return activeView.mouseDragged(mouseX, mouseY, mouseButton, mouseXPrev, mouseYPrev);
        }
        return super.mouseDragged(mouseX, mouseY, mouseButton, mouseXPrev, mouseYPrev);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        // The views are not hit-tested by the default event routing, so they need to be notified explicitly
        boolean handled = this.activeView != null && activeView.mouseReleased(mouseX, mouseY, mouseButton);
        return super.mouseReleased(mouseX, mouseY, mouseButton) || handled;
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
