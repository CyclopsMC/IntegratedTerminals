package org.cyclops.integratedterminals.client.gui.container;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.cyclopscore.client.gui.component.WidgetScrollBar;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonExtended;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetNumberField;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingOption;
import org.cyclops.integratedterminals.capability.ingredient.IngredientComponentTerminalStorageHandlerConfig;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingOptionAmountBase;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientOpenCraftingPlanGuiPacket;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * A gui for setting the amount for a given crafting option.
 * @author rubensworks
 */
public class ContainerScreenTerminalStorageCraftingOptionAmount<L, C extends ContainerTerminalStorageCraftingOptionAmountBase<L>> extends ContainerScreenExtended<C> {

    public static int OUTPUT_SLOT_X = 135;
    public static int OUTPUT_SLOT_Y = 15;

    private final List<IPrototypedIngredient<?, ?>> outputs;

    private WidgetNumberField numberField = null;
    private WidgetScrollBar scrollBar;
    private int firstRow;
    private ButtonText nextButton;

    public ContainerScreenTerminalStorageCraftingOptionAmount(C container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);

        this.outputs = Lists.newArrayList();
        ITerminalCraftingOption<?> option = getContainer().getCraftingOptionGuiData().getCraftingOption().getCraftingOption();
        for (IngredientComponent<?, ?> outputComponent : option.getOutputComponents()) {
            for (Object output : option.getOutputs(outputComponent)) {
                this.outputs.add(new PrototypedIngredient(outputComponent, output, null));
            }
        }
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/crafting_option_amount.png");
    }

    @Override
    public int getBaseXSize() {
        return 178;
    }

    @Override
    public int getBaseYSize() {
        return 162;
    }

    @Override
    public void init() {
        super.init();

        numberField = new WidgetNumberField(Minecraft.getInstance().fontRenderer,
                guiLeft + 25, guiTop + 36, 53, 14, true,
                new TranslationTextComponent("gui.integratedterminals.amount"), true);
        numberField.setPositiveOnly(true);
        numberField.setMaxStringLength(5);
        numberField.setMaxValue(10000);
        numberField.setMinValue(1);
        numberField.setVisible(true);
        numberField.setTextColor(16777215);
        numberField.setCanLoseFocus(true);
        numberField.setText("1");
        children.add(numberField);

        scrollBar = new WidgetScrollBar(guiLeft + 153, guiTop + 15, 54,
                new TranslationTextComponent("gui.cyclopscore.scrollbar"), this::setFirstRow, 3);
        scrollBar.setTotalRows(outputs.size() - 1);
        children.add(scrollBar);

        addButton(new ButtonChangeQuantity(guiLeft + 5, guiTop + 10, +10, this::buttonChangeQuantity));
        addButton(new ButtonChangeQuantity(guiLeft + 5, guiTop + 55, -10, this::buttonChangeQuantity));

        addButton(new ButtonChangeQuantity(guiLeft + 48, guiTop + 10, +100, this::buttonChangeQuantity));
        addButton(new ButtonChangeQuantity(guiLeft + 48, guiTop + 55, -100, this::buttonChangeQuantity));

        addButton(new ButtonChangeQuantity(guiLeft + 91, guiTop + 10, +1000, this::buttonChangeQuantity));
        addButton(new ButtonChangeQuantity(guiLeft + 91, guiTop + 55, -1000, this::buttonChangeQuantity));

        addButton(nextButton = new ButtonText(guiLeft + 81, guiTop + 33, 50, 20,
                new TranslationTextComponent("gui.integratedterminals.terminal_storage.step.next"),
                new TranslationTextComponent("gui.integratedterminals.terminal_storage.step.next").mergeStyle(TextFormatting.YELLOW),
                (bb) -> calculateCraftingJob(),
                true));
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        return this.numberField.charTyped(typedChar, keyCode) || super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
        if (typedChar == GLFW.GLFW_KEY_ESCAPE) {
            returnToTerminalStorage();
            return true;
        } else if (typedChar == GLFW.GLFW_KEY_ENTER || typedChar == GLFW.GLFW_KEY_KP_ENTER) {
            calculateCraftingJob();
            return true;
        }
        return this.numberField.keyPressed(typedChar, keyCode, modifiers) || super.keyPressed(typedChar, keyCode, modifiers);
    }

    private void returnToTerminalStorage() {
        CraftingOptionGuiData data = getContainer().getCraftingOptionGuiData();
        data.getLocation().openContainerFromClient(data);
    }

    public void buttonChangeQuantity(Button button) {
        if (button instanceof ContainerScreenTerminalStorageCraftingOptionAmount.ButtonChangeQuantity) {
            int diff = ((ButtonChangeQuantity) button).getDiff();
            setAmount(getAmount() + diff);
        }
    }

    private void calculateCraftingJob() {
        CraftingOptionGuiData craftingOptionData = getContainer().getCraftingOptionGuiData().copyWithAmount(getAmount());
        IntegratedTerminals._instance.getPacketHandler().sendToServer(
                new TerminalStorageIngredientOpenCraftingPlanGuiPacket(craftingOptionData));
    }

    protected <T, M> void drawInstance(MatrixStack matrixStack, IngredientComponent<T, M> ingredientComponent, T instance, int x, int y, ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int mouseX, int mouseY) {
        long quantity = ingredientComponent.getMatcher().getQuantity(instance) * getAmount();
        ingredientComponent.getCapability(IngredientComponentTerminalStorageHandlerConfig.CAPABILITY)
                .orElseThrow(() -> new IllegalStateException("Could not find ingredient terminal storage handler"))
                .drawInstance(matrixStack, ingredientComponent.getMatcher().withQuantity(instance, quantity), quantity, GuiHelpers.quantityToScaledString(quantity), this, layer, partialTick, x, y, mouseX, mouseY, null);
    }

    private int getAmount() {
        try {
            return this.numberField.getInt();
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void setAmount(int amount) {
        this.numberField.setText(Integer.toString(this.numberField.validateNumber(amount)));
    }

    protected void drawOutputSlots(MatrixStack matrixStack, int x, int y, float partialTicks, int mouseX, int mouseY, ContainerScreenTerminalStorage.DrawLayer layer) {
        int offsetY = OUTPUT_SLOT_Y;
        for (IPrototypedIngredient output : this.outputs.subList(firstRow, Math.min(this.outputs.size(), firstRow + scrollBar.getVisibleRows()))) {
            drawInstance(matrixStack, output.getComponent(), output.getPrototype(), x + OUTPUT_SLOT_X, y + offsetY, layer, partialTicks, mouseX, mouseY);
            offsetY += GuiHelpers.SLOT_SIZE;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
        numberField.renderButton(matrixStack, mouseX - guiLeft, mouseY - guiTop, partialTicks);
        scrollBar.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);

        RenderHelpers.bindTexture(this.texture);
        drawOutputSlots(matrixStack, guiLeft, guiTop, partialTicks, mouseX - guiLeft, mouseY - guiTop, ContainerScreenTerminalStorage.DrawLayer.BACKGROUND);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);

        drawOutputSlots(matrixStack, 0, 0, 0, mouseX, mouseY, ContainerScreenTerminalStorage.DrawLayer.FOREGROUND);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double mouseXPrev, double mouseYPrev) {
        return this.getListener() != null && this.isDragging() && mouseButton == 0 && this.getListener().mouseDragged(mouseX, mouseY, mouseButton, mouseXPrev, mouseYPrev) ? true : super.mouseDragged(mouseX, mouseY, mouseButton, mouseXPrev, mouseYPrev);
    }

    public void setFirstRow(int firstRow) {
        this.firstRow = firstRow;
    }

    public class ButtonChangeQuantity extends ButtonExtended {

        private final int diff;

        public ButtonChangeQuantity(int x, int y, int diff, IPressable pressCallback) {
            super(x, y, 40, 20, new StringTextComponent((diff < 0 ? "- " : "+ ") + Integer.toString(Math.abs(diff))), pressCallback, true);
            this.diff = diff;
        }

        @Override
        protected void drawButtonInner(MatrixStack matrixStack, int i, int j) {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            int color = 14737632;
            if (!this.active) {
                color = 10526880;
            } else if (this.isHovered()) {
                color = 16777120;
            }
            this.drawCenteredString(matrixStack, minecraft.fontRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, color);
        }

        public int getDiff() {
            return diff;
        }
    }

}
