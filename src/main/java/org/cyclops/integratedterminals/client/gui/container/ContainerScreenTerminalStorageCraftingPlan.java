package org.cyclops.integratedterminals.client.gui.container;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlan;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingPlanBase;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

/**
 * A gui for previewing a crafting plan.
 * @author rubensworks
 */
public class ContainerScreenTerminalStorageCraftingPlan<L, C extends ContainerTerminalStorageCraftingPlanBase<L>> extends ContainerScreenExtended<C> {

    @Nullable
    private GuiCraftingPlan guiCraftingPlan;

    private ITerminalCraftingPlan craftingPlan;

    public ContainerScreenTerminalStorageCraftingPlan(C container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
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

        if (this.craftingPlan != null) {
            this.children.clear();
            this.guiCraftingPlan = new GuiCraftingPlan(this, this.craftingPlan, guiLeft, guiTop, 9, 18, 10);
            this.children.add(this.guiCraftingPlan);
        } else {
            this.guiCraftingPlan = null;
        }

        ButtonText button;
        this.buttons.clear();
        addButton(button = new ButtonText(guiLeft + 95, guiTop + 198, 50, 20,
                        new TranslationTextComponent("gui.integratedterminals.terminal_storage.step.craft"),
                        new TranslationTextComponent("gui.integratedterminals.terminal_storage.step.craft").mergeStyle(TextFormatting.YELLOW),
                        createServerPressable(ContainerTerminalStorageCraftingPlanBase.BUTTON_START, (b) -> {}),
                        true));
        button.active = this.guiCraftingPlan != null && this.guiCraftingPlan.isValid();
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
        if (this.guiCraftingPlan != null && this.guiCraftingPlan.isValid()
                && (typedChar == GLFW.GLFW_KEY_ENTER || typedChar == GLFW.GLFW_KEY_KP_ENTER)) {
            ((Button) this.buttons.get(0)).onPress();
            return true;
        }
        return super.keyPressed(typedChar, keyCode, modifiers);
    }

    private void returnToTerminalStorage() {
        CraftingOptionGuiData data = getContainer().getCraftingOptionGuiData();
        data.getLocation().openContainerFromClient(data);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
        } else {
            drawCenteredString(matrixStack, font, L10NHelpers.localize("gui.integratedterminals.terminal_storage.step.crafting_plan_calculating"),
                    guiLeft + getBaseXSize() / 2, guiTop + 23, 16777215);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        }
    }

    @Override
    protected void drawCurrentScreen(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.drawCurrentScreen(matrixStack, mouseX, mouseY, partialTicks);
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.render(matrixStack, mouseX, mouseY, partialTicks);
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
    public void onUpdate(int valueId, CompoundNBT value) {
        super.onUpdate(valueId, value);

        if (getContainer().getCraftingPlanNotifierId() == valueId) {
            this.craftingPlan = getContainer().getCraftingOptionGuiData().getCraftingOption().getHandler().deserializeCraftingPlan(value);
            this.init();
        }
    }
}
