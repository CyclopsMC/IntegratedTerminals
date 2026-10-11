package org.cyclops.integratedterminals.client.gui.container;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlanFlat;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlan;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanButtons;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanFlat;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanGraph;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanToggler;
import org.cyclops.integratedterminals.client.gui.container.component.IGuiCraftingPlanView;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingPlanBase;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientOpenCraftingJobAmountGuiPacket;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

/**
 * A gui for previewing a crafting plan.
 * @author rubensworks
 */
public class ContainerScreenTerminalStorageCraftingPlan<L, C extends ContainerTerminalStorageCraftingPlanBase<L>> extends ContainerScreenExtended<C> {

    private static final ResourceLocation TEXTURE_PANEL = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/part_terminal_storage.png");

    private GuiCraftingPlanToggler guiCraftingPlanToggler;

    @Nullable
    private IGuiCraftingPlanView activeView;
    @Nullable
    private GuiCraftingPlanGraph guiCraftingPlanGraph;

    private ITerminalCraftingPlan craftingPlan;
    private ITerminalCraftingPlanFlat craftingPlanFlat;
    private ButtonText buttonConfirm;

    public ContainerScreenTerminalStorageCraftingPlan(C container, Inventory inventory, Component title) {
        super(container, inventory, title);

        this.guiCraftingPlanToggler = new GuiCraftingPlanToggler(
                () -> this.craftingPlan,
                () -> this.craftingPlanFlat,
                () -> setActiveView(new GuiCraftingPlan(this, this.craftingPlan, leftPos, topPos, 9, 18, 10)),
                () -> {
                    GuiCraftingPlanGraph previousGraph = this.guiCraftingPlanGraph;
                    this.guiCraftingPlanGraph = new GuiCraftingPlanGraph(this, this.craftingPlan, leftPos, topPos, 9, 18,
                            imageWidth - 18, imageHeight - 44, false,
                            previousGraph != null && previousGraph.getCraftingPlan() == this.craftingPlan ? previousGraph : null);
                    // Overlay buttons must be added before the graph, so that they receive clicks first
                    for (Button button : GuiCraftingPlanButtons.createGraphButtons(this.guiCraftingPlanGraph, this::init)) {
                        addRenderableWidget(button);
                    }
                    setActiveView(this.guiCraftingPlanGraph);
                },
                () -> setActiveView(new GuiCraftingPlanFlat(this, this.craftingPlanFlat, leftPos, topPos, 9, 18, 10)),
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
        this.activeView = null;

        for (Button button : GuiCraftingPlanButtons.createViewButtons(this.guiCraftingPlanToggler, leftPos + 8, topPos + imageHeight - 23, this::init)) {
            addRenderableWidget(button);
        }

        this.guiCraftingPlanToggler.init();
        if (this.guiCraftingPlanToggler.getCraftingPlanDisplayMode() != GuiCraftingPlanToggler.CraftingPlanDisplayMode.GRAPH) {
            this.guiCraftingPlanGraph = null;
        }

        addRenderableWidget(new ButtonText(leftPos + imageWidth - 25 - 50 - 55, topPos + imageHeight - 24, 50, 20,
                Component.translatable("gui.integratedterminals.terminal_storage.step.back"),
                Component.translatable("gui.integratedterminals.terminal_storage.step.back"),
                (b) -> returnToCraftingOptionAmount(),
                true));

        addRenderableWidget(Checkbox.builder(Component.translatable("gui.integratedterminals.terminal_storage.step.craft.notify"), font)
                .pos(leftPos + 72, topPos + imageHeight - 22)
                .selected(getMenu().isNotifyOnCompletion())
                .tooltip(Tooltip.create(Component.translatable("gui.integratedterminals.terminal_storage.step.craft.notify.info")))
                .onValueChange((widget, selected) -> getMenu().setNotifyOnCompletion(selected))
                .build());

        addRenderableWidget(buttonConfirm = new ButtonText(leftPos + imageWidth - 25 - 50, topPos + imageHeight - 24, 50, 20,
                Component.translatable("gui.integratedterminals.terminal_storage.step.craft"),
                Component.translatable("gui.integratedterminals.terminal_storage.step.craft").withStyle(ChatFormatting.YELLOW),
                createServerPressable(ContainerTerminalStorageCraftingPlanBase.BUTTON_START, (b) -> {}),
                true));
        buttonConfirm.active = this.activeView != null && this.activeView.isValid();
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
        if (typedChar == GLFW.GLFW_KEY_ESCAPE) {
            returnToCraftingOptionAmount();
            return true;
        }
        if (this.activeView != null && this.activeView.keyPressed(typedChar, keyCode, modifiers)) {
            return true;
        }
        if (this.activeView != null && this.activeView.isValid()
                && (typedChar == GLFW.GLFW_KEY_ENTER || typedChar == GLFW.GLFW_KEY_KP_ENTER)) {
            buttonConfirm.onPress();
            return true;
        }
        return super.keyPressed(typedChar, keyCode, modifiers);
    }

    /**
     * Go back to the gui in which the crafting amount can be set.
     * If no crafting option is known, the terminal itself is opened again.
     */
    private void returnToCraftingOptionAmount() {
        CraftingOptionGuiData data = getMenu().getCraftingOptionGuiData();
        if (data.getCraftingOption() == null) {
            returnToTerminalStorage();
        } else {
            IntegratedTerminals._instance.getPacketHandler().sendToServer(
                    new TerminalStorageIngredientOpenCraftingJobAmountGuiPacket(getMenu().getPlayerIInventory().player.registryAccess(), data));
        }
    }

    private void returnToTerminalStorage() {
        CraftingOptionGuiData data = getMenu().getCraftingOptionGuiData();
        data.getLocation().openContainerFromClient(data);
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
            guiGraphics.drawCenteredString(font, L10NHelpers.localize("gui.integratedterminals.terminal_storage.step.crafting_plan_calculating"),
                    leftPos + imageWidth / 2, topPos + 23, 16777215);
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
    protected void drawCurrentScreen(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawCurrentScreen(guiGraphics, mouseX, mouseY, partialTicks);
        if (this.activeView != null) {
            activeView.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        if (this.activeView != null && this.activeView.charTyped(character, modifiers)) {
            return true;
        }
        return super.charTyped(character, modifiers);
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

        if (getMenu().getCraftingPlanNotifierId() == valueId) {
            this.craftingPlan = getMenu().getCraftingOptionGuiData().getCraftingOption().getHandler().deserializeCraftingPlan(getMenu().getPlayerIInventory().player.registryAccess(), value);
            this.guiCraftingPlanToggler.setCraftingPlanDisplayMode(null);
            this.init();
        }
        if (getMenu().getCraftingPlanFlatNotifierId() == valueId) {
            this.craftingPlanFlat = getMenu().getCraftingOptionGuiData().getCraftingOption().getHandler().deserializeCraftingPlanFlat(getMenu().getPlayerIInventory().player.registryAccess(), value);
            this.guiCraftingPlanToggler.setCraftingPlanDisplayMode(null);
            this.init();
        }
    }
}
