package org.cyclops.integratedterminals.client.gui.container;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.cyclopscore.client.gui.component.GuiScrollBar;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonExtended;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonText;
import org.cyclops.cyclopscore.client.gui.component.input.GuiNumberField;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerExtended;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingOption;
import org.cyclops.integratedterminals.capability.ingredient.IngredientComponentTerminalStorageHandlerConfig;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.core.client.gui.ExtendedGuiHandler;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingOptionAmount;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientOpenCraftingPlanGuiPacket;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientOpenPacket;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;

/**
 * A gui for setting the amount for a given crafting option.
 * @author rubensworks
 */
public class GuiTerminalStorageCraftingOptionAmount extends GuiContainerExtended {

    public static int OUTPUT_SLOT_X = 135;
    public static int OUTPUT_SLOT_Y = 15;

    private final CraftingOptionGuiData craftingOptionGuiData;
    private final List<IPrototypedIngredient<?, ?>> outputs;

    private GuiNumberField numberField = null;
    private GuiScrollBar scrollBar;
    private int firstRow;
    private GuiButtonText nextButton;

    public GuiTerminalStorageCraftingOptionAmount(EntityPlayer player, PartTarget target, IPartContainer partContainer,
                                                  IPartType partType, CraftingOptionGuiData craftingOptionGuiData) {
        super(new ContainerTerminalStorageCraftingOptionAmount(player, target, partContainer, partType, craftingOptionGuiData));

        this.craftingOptionGuiData = craftingOptionGuiData;

        this.outputs = Lists.newArrayList();
        ITerminalCraftingOption<?> option = craftingOptionGuiData.getCraftingOption().getCraftingOption();
        for (IngredientComponent<?, ?> outputComponent : option.getOutputComponents()) {
            for (Object output : option.getOutputs(outputComponent)) {
                this.outputs.add(new PrototypedIngredient(outputComponent, output, null));
            }
        }
    }

    @Override
    protected ResourceLocation constructResourceLocation() {
        return new ResourceLocation(Reference.MOD_ID, this.getGuiTexture());
    }

    @Override
    public String getGuiTexture() {
        return IntegratedTerminals._instance.getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI)
                + "crafting_option_amount.png";
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
    public void initGui() {
        super.initGui();

        numberField = new GuiNumberField(0, Minecraft.getMinecraft().fontRenderer,
                guiLeft + 25, guiTop + 36, 53, 14, true, true);
        numberField.setPositiveOnly(true);
        numberField.setMaxStringLength(5);
        numberField.setMaxValue(10000);
        numberField.setMinValue(1);
        numberField.setVisible(true);
        numberField.setTextColor(16777215);
        numberField.setCanLoseFocus(true);
        numberField.setText("1");

        scrollBar = new GuiScrollBar(guiLeft + 153, guiTop + 15, 54, this::setFirstRow, 3);
        scrollBar.setTotalRows(outputs.size() - 1);

        this.buttonList.addAll(Lists.newArrayList(
                new GuiButtonChangeQuantity(0, guiLeft + 5, guiTop + 10, +10),
                new GuiButtonChangeQuantity(1, guiLeft + 5, guiTop + 55, -10),

                new GuiButtonChangeQuantity(2, guiLeft + 48, guiTop + 10, +100),
                new GuiButtonChangeQuantity(3, guiLeft + 48, guiTop + 55, -100),

                new GuiButtonChangeQuantity(4, guiLeft + 91, guiTop + 10, +1000),
                new GuiButtonChangeQuantity(5, guiLeft + 91, guiTop + 55, -1000),

                nextButton = new GuiButtonText(6, guiLeft + 81, guiTop + 33, 50, 20, "", true)
        ));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!this.checkHotbarKeys(keyCode)) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                returnToTerminalStorage();
            } else if (!this.numberField.textboxKeyTyped(typedChar, keyCode)
                    && !this.numberField.textboxKeyTyped(typedChar, keyCode)) {
                super.keyTyped(typedChar, keyCode);
            }
        }
    }

    private void returnToTerminalStorage() {
        IntegratedTerminals._instance.getGuiHandler().setTemporaryData(ExtendedGuiHandler.TERMINAL_STORAGE,
                Pair.of(craftingOptionGuiData.getSide(), new ContainerTerminalStorage.InitTabData(
                        craftingOptionGuiData.getTabName(), craftingOptionGuiData.getChannel())));
        IntegratedTerminals._instance.getPacketHandler().sendToServer(
                new TerminalStorageIngredientOpenPacket(craftingOptionGuiData.getPos(), craftingOptionGuiData.getSide(),
                        craftingOptionGuiData.getTabName(), craftingOptionGuiData.getChannel()));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.numberField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean requiresAction(int buttonId) {
        return true;
    }

    @Override
    public void onButtonClick(int buttonId) {
        super.onButtonClick(buttonId);
        GuiButton button = buttonList.get(buttonId);
        if (button instanceof GuiButtonChangeQuantity) {
            int diff = ((GuiButtonChangeQuantity) button).getDiff();
            setAmount(getAmount() + diff);
        } else if (button instanceof GuiButtonText) {
            if (MinecraftHelpers.isShifted()) {
                calculateCraftingJobAndStart();
            } else {
                calculateCraftingJob();
            }
        }
    }

    private void calculateCraftingJob() {
        CraftingOptionGuiData craftingOptionData = CraftingOptionGuiData.copyWithAmount(craftingOptionGuiData, getAmount());
        IntegratedTerminals._instance.getGuiHandler().setTemporaryData(ExtendedGuiHandler.CRAFTING_OPTION,
                Pair.of(craftingOptionData.getSide(), craftingOptionData));
        IntegratedTerminals._instance.getPacketHandler().sendToServer(
                new TerminalStorageIngredientOpenCraftingPlanGuiPacket(craftingOptionData));
    }

    private void calculateCraftingJobAndStart() {
        // TODO
        System.out.println("Start"); // TODO
    }

    protected <T, M> void drawInstance(IngredientComponent<T, M> ingredientComponent, T instance, int x, int y, GuiTerminalStorage.DrawLayer layer, float partialTick, int mouseX, int mouseY) {
        long quantity = ingredientComponent.getMatcher().getQuantity(instance) * getAmount();
        ingredientComponent.getCapability(IngredientComponentTerminalStorageHandlerConfig.CAPABILITY)
                .drawInstance(ingredientComponent.getMatcher().withQuantity(instance, quantity), quantity, GuiHelpers.quantityToScaledString(quantity), this, layer, partialTick, x, y, mouseX, mouseY, null);
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

    protected void drawOutputSlots(int x, int y, float partialTicks, int mouseX, int mouseY, GuiTerminalStorage.DrawLayer layer) {
        int offsetY = OUTPUT_SLOT_Y;
        for (IPrototypedIngredient output : this.outputs.subList(firstRow, Math.min(this.outputs.size(), firstRow + scrollBar.getVisibleRows()))) {
            drawInstance(output.getComponent(), output.getPrototype(), x + OUTPUT_SLOT_X, y + offsetY, layer, partialTicks, mouseX, mouseY);
            offsetY += GuiHelpers.SLOT_SIZE;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        numberField.drawTextBox(Minecraft.getMinecraft(), mouseX - guiLeft, mouseY - guiTop);
        scrollBar.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        RenderHelpers.bindTexture(this.texture);
        drawOutputSlots(guiLeft, guiTop, partialTicks, mouseX - guiLeft, mouseY - guiTop, GuiTerminalStorage.DrawLayer.BACKGROUND);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        drawOutputSlots(0, 0, 0, mouseX, mouseY, GuiTerminalStorage.DrawLayer.FOREGROUND);
    }

    @Override
    protected void drawCurrentScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawCurrentScreen(mouseX, mouseY, partialTicks);
        scrollBar.drawCurrentScreen(mouseX, mouseY, partialTicks);

        nextButton.displayString = TextFormatting.BOLD + (MinecraftHelpers.isShifted()
                ? L10NHelpers.localize("gui.integratedterminals.terminal_storage.step.craft")
                : L10NHelpers.localize("gui.integratedterminals.terminal_storage.step.next"));
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        scrollBar.handleMouseInput();
    }

    public void setFirstRow(int firstRow) {
        this.firstRow = firstRow;
    }

    public class GuiButtonChangeQuantity extends GuiButtonExtended {

        private final int diff;

        public GuiButtonChangeQuantity(int id, int x, int y, int diff) {
            super(id, x, y, 40, 20, (diff < 0 ? "- " : "+ ") + Integer.toString(Math.abs(diff)), true);
            this.diff = diff;
        }

        @Override
        protected void drawButtonInner(Minecraft minecraft, int i, int j, boolean mouseOver) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int color = 14737632;
            if (!this.enabled) {
                color = 10526880;
            } else if (this.hovered) {
                color = 16777120;
            }
            this.drawCenteredString(minecraft.fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, color);
        }

        public int getDiff() {
            return diff;
        }
    }

}
