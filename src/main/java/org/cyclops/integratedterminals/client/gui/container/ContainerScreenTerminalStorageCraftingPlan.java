package org.cyclops.integratedterminals.client.gui.container;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlanFlat;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlan;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanFlat;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanToggler;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingPlanBase;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

/**
 * A gui for previewing a crafting plan.
 * @author rubensworks
 */
public class ContainerScreenTerminalStorageCraftingPlan<L, C extends ContainerTerminalStorageCraftingPlanBase<L>> extends ContainerScreenExtended<C> {

    private GuiCraftingPlanToggler guiCraftingPlanToggler;

    @Nullable
    private GuiCraftingPlan guiCraftingPlan;
    @Nullable
    private GuiCraftingPlanFlat guiCraftingPlanFlat;

    private ITerminalCraftingPlan craftingPlan;
    private ITerminalCraftingPlanFlat craftingPlanFlat;
    private ButtonText buttonConfirm;

    public ContainerScreenTerminalStorageCraftingPlan(C container, Inventory inventory, Component title) {
        super(container, inventory, title);

        this.guiCraftingPlanToggler = new GuiCraftingPlanToggler(
                () -> this.craftingPlan,
                () -> this.craftingPlanFlat,
                () -> {
                    this.guiCraftingPlan = new GuiCraftingPlan(this, this.craftingPlan, leftPos, topPos, 9, 18, 10);
                    addRenderableWidget(this.guiCraftingPlan);

                    if (this.craftingPlanFlat != null) {
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
                    this.guiCraftingPlanFlat = new GuiCraftingPlanFlat(this, this.craftingPlanFlat, leftPos, topPos, 9, 18, 10);
                    addRenderableWidget(this.guiCraftingPlanFlat);

                    if (this.craftingPlan != null) {
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

        addRenderableWidget(buttonConfirm = new ButtonText(leftPos + 221 + 10 - 50, topPos + 198, 50, 20,
                Component.translatable("gui.integratedterminals.terminal_storage.step.craft"),
                Component.translatable("gui.integratedterminals.terminal_storage.step.craft").withStyle(ChatFormatting.YELLOW),
                createServerPressable(ContainerTerminalStorageCraftingPlanBase.BUTTON_START, (b) -> {}),
                true));
        buttonConfirm.active = (this.guiCraftingPlan != null && this.guiCraftingPlan.isValid()) || (this.guiCraftingPlanFlat != null && this.guiCraftingPlanFlat.isValid());
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
        if (this.guiCraftingPlan != null && this.guiCraftingPlan.isValid()
                && (typedChar == GLFW.GLFW_KEY_ENTER || typedChar == GLFW.GLFW_KEY_KP_ENTER)) {
            buttonConfirm.onPress();
            return true;
        }
        return super.keyPressed(typedChar, keyCode, modifiers);
    }

    private void returnToTerminalStorage() {
        CraftingOptionGuiData data = getMenu().getCraftingOptionGuiData();
        data.getLocation().openContainerFromClient(data);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
        } else if (this.guiCraftingPlanFlat != null) {
            guiCraftingPlanFlat.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
        } else {
            drawCenteredString(matrixStack, font, L10NHelpers.localize("gui.integratedterminals.terminal_storage.step.crafting_plan_calculating"),
                    leftPos + getBaseXSize() / 2, topPos + 23, 16777215);
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
    protected void drawCurrentScreen(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.drawCurrentScreen(matrixStack, mouseX, mouseY, partialTicks);
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.render(matrixStack, mouseX, mouseY, partialTicks);
        } else if (this.guiCraftingPlanFlat != null) {
            guiCraftingPlanFlat.render(matrixStack, mouseX, mouseY, partialTicks);
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

        if (getMenu().getCraftingPlanNotifierId() == valueId) {
            this.craftingPlan = getMenu().getCraftingOptionGuiData().getCraftingOption().getHandler().deserializeCraftingPlan(value);
            this.guiCraftingPlanToggler.setCraftingPlanDisplayMode(null);
            this.init();
        }
        if (getMenu().getCraftingPlanFlatNotifierId() == valueId) {
            this.craftingPlanFlat = getMenu().getCraftingOptionGuiData().getCraftingOption().getHandler().deserializeCraftingPlanFlat(value);
            this.guiCraftingPlanToggler.setCraftingPlanDisplayMode(null);
            this.init();
        }
    }
}
