package org.cyclops.integratedterminals.client.gui.container;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
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
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanToggler;
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
    private GuiCraftingPlanToggler guiCraftingPlanToggler;

    @Nullable
    private GuiCraftingPlan guiCraftingPlan;
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
                    addRenderableWidget(this.guiCraftingPlan);

                    if (this.getMenu().getCraftingPlanFlat().isPresent()) {
                        addRenderableWidget(new ButtonText(leftPos + 8, topPos + 198, 80, 20,
                                Component.translatable("gui.integratedterminals.craftingplan.view.flat"),
                                Component.translatable("gui.integratedterminals.craftingplan.view.flat").withStyle(ChatFormatting.ITALIC),
                                (b) -> {
                                    this.guiCraftingPlanToggler.setCraftingPlanDisplayMode(GuiCraftingPlanToggler.CraftingPlanDisplayMode.FLAT);
                                    this.init();
                                },
                                true));
                    }
                },
                () -> {
                    GuiCraftingPlanFlat previousGuiCraftingPlan = this.guiCraftingPlanFlat;
                    this.guiCraftingPlanFlat = new GuiCraftingPlanFlat(this, this.getMenu().getCraftingPlanFlat().get(), leftPos, topPos, 9, 18, 10);
                    if (previousGuiCraftingPlan != null) {
                        this.guiCraftingPlanFlat.inheritVisualizationState(previousGuiCraftingPlan);
                    }
                    addRenderableWidget(this.guiCraftingPlanFlat);

                    if (this.getMenu().getCraftingPlan().isPresent()) {
                        addRenderableWidget(new ButtonText(leftPos + 8, topPos + 198, 80, 20,
                                Component.translatable("gui.integratedterminals.craftingplan.view.tree"),
                                Component.translatable("gui.integratedterminals.craftingplan.view.tree").withStyle(ChatFormatting.ITALIC),
                                (b) -> {
                                    this.guiCraftingPlanToggler.setCraftingPlanDisplayMode(GuiCraftingPlanToggler.CraftingPlanDisplayMode.TREE);
                                    this.init();
                                },
                                true));
                    }
                }
        );
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/crafting_plan.png");
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return this.guiCraftingPlanToggler.getCraftingPlanDisplayMode() == GuiCraftingPlanToggler.CraftingPlanDisplayMode.FLAT ? new ResourceLocation(Reference.MOD_ID, "textures/gui/crafting_plan_flat.png") : super.getGuiTexture();
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

        // Reset states
        this.renderables.clear();
        this.children().clear();
        this.guiCraftingPlan = null;
        this.guiCraftingPlanFlat = null;

        this.guiCraftingPlanToggler.init();

        if (this.guiCraftingPlan != null || this.guiCraftingPlanFlat != null) {
            addRenderableWidget(new ButtonText(leftPos + 221 + 10 - 100, topPos + 198, 100, 20,
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
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
        } else if (this.guiCraftingPlanFlat != null) {
            guiCraftingPlanFlat.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
        } else {
            drawCenteredString(matrixStack, font, L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.empty"),
                    leftPos + getBaseXSize() / 2, topPos + 23, 16777215);
        }
    }

    @Override
    protected void drawCurrentScreen(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.drawCurrentScreen(matrixStack, mouseX, mouseY, partialTicks);
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.render(matrixStack, mouseX, mouseY, partialTicks);
        } else if (this.guiCraftingPlanFlat != null) {
            guiCraftingPlanFlat.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        } else if (this.guiCraftingPlanFlat != null) {
            guiCraftingPlanFlat.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.guiCraftingPlan != null) {
            return guiCraftingPlan.mouseScrolled(mouseX, mouseY, delta);
        } else if (this.guiCraftingPlanFlat != null) {
            return guiCraftingPlanFlat.mouseScrolled(mouseX, mouseY, delta);
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double mouseXPrev, double mouseYPrev) {
        if (this.guiCraftingPlan != null) {
            return guiCraftingPlan.mouseDragged(mouseX, mouseY, mouseButton, mouseXPrev, mouseYPrev);
        } else if (this.guiCraftingPlanFlat != null) {
            return guiCraftingPlanFlat.mouseDragged(mouseX, mouseY, mouseButton, mouseXPrev, mouseYPrev);
        }
        return super.mouseDragged(mouseX, mouseY, mouseButton, mouseXPrev, mouseYPrev);
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
