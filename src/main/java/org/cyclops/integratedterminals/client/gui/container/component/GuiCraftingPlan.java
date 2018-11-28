package org.cyclops.integratedterminals.client.gui.container.component;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
import org.cyclops.cyclopscore.client.gui.component.GuiScrollBar;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.capability.ingredient.IngredientComponentTerminalStorageHandlerConfig;
import org.cyclops.integratedterminals.client.gui.container.GuiTerminalStorage;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * A gui component for visualizing {@link CraftingOptionGuiData}.
 *
 * The using gui must call the following methods from its respective method:
 * * {@link #handleMouseInput()}
 * * {@link #drawCurrentScreen(int, int, float)}
 * * {@link #drawGuiContainerBackgroundLayer(float, int, int)}
 * * {@link #drawGuiContainerForegroundLayer(int, int)}
 *
 * @author rubensworks
 */
public class GuiCraftingPlan extends Gui {

    private static final int ELEMENT_WIDTH = 221;
    private static final int ELEMENT_HEIGHT = 16;

    private final GuiContainer parentGui;
    private final int guiLeft;
    private final int guiTop;
    private final int x;
    private final int y;
    private final List<GuiCraftingPlan.Element> elements;
    private final boolean valid;
    private final GuiScrollBar scrollBar;

    private int firstRow;

    public GuiCraftingPlan(GuiContainer parentGui, ITerminalCraftingPlan craftingPlan, int guiLeft, int guiTop, int x, int y, int visibleRows) {
        this.parentGui = parentGui;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
        this.x = x;
        this.y = y;
        this.elements = getElements(craftingPlan);
        this.valid = craftingPlan.getStatus() != TerminalCraftingJobStatus.INVALID;
        this.scrollBar = new GuiScrollBar(guiLeft + x + 227, guiTop + y + 0, 178, this::setFirstRow, visibleRows);
        this.scrollBar.setTotalRows(elements.size() - 1);
    }

    public void setFirstRow(int firstRow) {
        this.firstRow = firstRow;
    }

    public void drawCurrentScreen(int mouseX, int mouseY, float partialTicks) {
        scrollBar.drawCurrentScreen(mouseX, mouseY, partialTicks);
    }

    public void drawGuiContainerLayer(int guiLeft, int guiTop, GuiTerminalStorage.DrawLayer layer, float partialTick, int mouseX, int mouseY) {
        int offsetY = 0;
        for (GuiCraftingPlan.Element element : this.elements.subList(firstRow, Math.min(this.elements.size(), firstRow + scrollBar.getVisibleRows()))) {
            int indent = element.getIndent() * 8;
            drawElement(element,  indent, guiLeft + x, guiTop + y + offsetY, ELEMENT_WIDTH, ELEMENT_HEIGHT, layer, partialTick, mouseX, mouseY);
            offsetY += 18;
        }
    }

    private void drawElement(Element element, int indent, int x, int y, int width, int height, GuiTerminalStorage.DrawLayer layer, float partialTick, int mouseX, int mouseY) {
        if (layer == GuiTerminalStorage.DrawLayer.BACKGROUND) {
            // Draw background
            drawRect(x, y, x + width, y + height + 1, element.getColor());
        }

        int xOriginal = x;
        x += indent;

        // Draw outputs
        for (IPrototypedIngredient<?, ?> output : element.getOutputs()) {
            IngredientComponent<?, ?> ingredientComponent = output.getComponent();
            long quantity = ((IngredientComponent) ingredientComponent).getMatcher().getQuantity(output.getPrototype());
            ingredientComponent.getCapability(IngredientComponentTerminalStorageHandlerConfig.CAPABILITY)
                    .drawInstance(output.getPrototype(), quantity, GuiHelpers.quantityToScaledString(quantity),
                            this.parentGui, layer, partialTick, x, y, mouseX, mouseY);
            x += GuiHelpers.SLOT_SIZE_INNER;
        }

        if (layer == GuiTerminalStorage.DrawLayer.BACKGROUND) {
            x = xOriginal + width - 50;
            // Draw counters
            if (element.getStorageQuantity() > 0) {
                renderItem(new ItemStack(Blocks.CHEST), x, y, 0.45F);
                RenderHelpers.drawScaledString(Minecraft.getMinecraft().fontRenderer, L10NHelpers.localize("gui.integratedterminals.terminal_storage.stored", element.getStorageQuantity()), x + 9, y + 1, 0.5F, 16777215, true);
                y += 8;
            }
            if (element.getCraftQuantity() > 0) {
                renderItem(new ItemStack(Blocks.CRAFTING_TABLE), x, y, 0.45F);
                RenderHelpers.drawScaledString(Minecraft.getMinecraft().fontRenderer, L10NHelpers.localize("gui.integratedterminals.terminal_storage.crafting", element.getCraftQuantity()), x + 9, y + 1, 0.5F, 16777215, true);
                y += 8;
            }
            if (element.getMissingQuantity() > 0) {
                renderItem(new ItemStack(Blocks.BARRIER), x, y, 0.45F);
                RenderHelpers.drawScaledString(Minecraft.getMinecraft().fontRenderer, L10NHelpers.localize("gui.integratedterminals.terminal_storage.missing", element.getMissingQuantity()), x + 9, y + 1, 0.5F, 16777215, true);
            }
            GlStateManager.color(1, 1, 1);
        }
    }

    protected static void renderItem(ItemStack itemStack, int x, int y, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, scale);

        RenderItemExtendedSlotCount renderItem = RenderItemExtendedSlotCount.getInstance();
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableDepth();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        renderItem.renderItemAndEffectIntoGUI(itemStack, 0, 0);
        renderItem.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, itemStack, 0, 0, "");
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();
    }

    public void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawGuiContainerLayer(guiLeft, guiTop, GuiTerminalStorage.DrawLayer.BACKGROUND, partialTicks, mouseX, mouseY);
        scrollBar.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
    }

    public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawGuiContainerLayer(0, 0, GuiTerminalStorage.DrawLayer.FOREGROUND, 0, mouseX, mouseY);
    }

    public void handleMouseInput() throws IOException {
        scrollBar.handleMouseInput();
    }

    public static List<GuiCraftingPlan.Element> getElements(ITerminalCraftingPlan craftingPlan) {
        List<GuiCraftingPlan.Element> elements = Lists.newArrayList();
        addElements(0, craftingPlan, elements);
        return elements;
    }

    protected static void addElements(int indent, ITerminalCraftingPlan craftingPlan, List<GuiCraftingPlan.Element> elements) {
        boolean valid = craftingPlan.getStatus() != TerminalCraftingJobStatus.INVALID
                || (!craftingPlan.getStorageIngredients().isEmpty() || !craftingPlan.getDependencies().isEmpty());
        elements.add(new Element(
                indent,
                craftingPlan.getOutputs(),
                0,
                valid ? craftingPlan.getCraftingQuantity() : 0,
                valid ? 0 : craftingPlan.getCraftingQuantity(),
                craftingPlan.getStatus().getColor()
        ));
        for (IPrototypedIngredient storageIngredient : craftingPlan.getStorageIngredients()) {
            elements.add(new Element(indent + 1, Collections.singletonList(storageIngredient),
                    storageIngredient.getComponent().getMatcher().getQuantity(storageIngredient.getPrototype()),
                    0, 0, TerminalCraftingJobStatus.FINISHED.getColor()));
        }
        for (ITerminalCraftingPlan dependency : craftingPlan.getDependencies()) {
            addElements(indent + 1, dependency, elements);
        }
    }

    public boolean isValid() {
        return valid;
    }

    public static class Element {

        private final int indent;
        private final List<IPrototypedIngredient<?, ?>> outputs;
        private final long storageQuantity;
        private final long craftQuantity;
        private final long missingQuantity;
        private final int color;

        public Element(int indent, List<IPrototypedIngredient<?, ?>> outputs, long storageQuantity, long craftQuantity,
                       long missingQuantity, int color) {
            this.indent = indent;
            this.outputs = outputs;
            this.storageQuantity = storageQuantity;
            this.craftQuantity = craftQuantity;
            this.missingQuantity = missingQuantity;
            this.color = color;
        }

        public int getIndent() {
            return indent;
        }

        public List<IPrototypedIngredient<?, ?>> getOutputs() {
            return outputs;
        }

        public long getStorageQuantity() {
            return storageQuantity;
        }

        public long getCraftQuantity() {
            return craftQuantity;
        }

        public long getMissingQuantity() {
            return missingQuantity;
        }

        public int getColor() {
            return color;
        }
    }

}
