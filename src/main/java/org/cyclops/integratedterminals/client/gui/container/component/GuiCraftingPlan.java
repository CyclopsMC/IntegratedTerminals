package org.cyclops.integratedterminals.client.gui.container.component;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
import org.cyclops.cyclopscore.client.gui.component.GuiScrollBar;
import org.cyclops.cyclopscore.client.gui.image.Image;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.capability.ingredient.IngredientComponentTerminalStorageHandlerConfig;
import org.cyclops.integratedterminals.client.gui.container.GuiTerminalStorage;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
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
 * * {@link #mouseClicked(int, int, int)}
 *
 * @author rubensworks
 */
public class GuiCraftingPlan extends Gui {

    private static final int ELEMENT_WIDTH = 221;
    private static final int ELEMENT_HEIGHT = 16;
    private static final int ELEMENT_HEIGHT_TOTAL = 18;

    private final GuiContainer parentGui;
    private final int guiLeft;
    private final int guiTop;
    private final int x;
    private final int y;
    private final List<GuiCraftingPlan.Element> elements;
    private final List<GuiCraftingPlan.Element> visibleElements;
    private final boolean valid;
    private final GuiScrollBar scrollBar;
    private final String label;

    private int firstRow;

    public GuiCraftingPlan(GuiContainer parentGui, ITerminalCraftingPlan<?> craftingPlan, int guiLeft, int guiTop, int x, int y, int visibleRows) {
        this.parentGui = parentGui;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
        this.x = x;
        this.y = y;
        this.elements = getElements(craftingPlan);
        this.visibleElements = Lists.newArrayList(this.elements);
        this.valid = craftingPlan.getStatus() != TerminalCraftingJobStatus.INVALID;
        this.scrollBar = new GuiScrollBar(guiLeft + x + 227, guiTop + y + 0, 178, this::setFirstRow, visibleRows);
        this.scrollBar.setTotalRows(visibleElements.size() - 1);
        this.label = L10NHelpers.localize(craftingPlan.getUnlocalizedLabel());
    }

    protected void refreshList() {
        visibleElements.clear();
        addActiveElements(elements.get(0), visibleElements);
        this.scrollBar.setTotalRows(visibleElements.size());
    }

    protected static void addActiveElements(GuiCraftingPlan.Element root, List<GuiCraftingPlan.Element> elements) {
        if (root.isEnabled()) {
            elements.add(root);
            for (Element child : root.getChildren()) {
                addActiveElements(child, elements);
            }
        }
    }

    public void setFirstRow(int firstRow) {
        this.firstRow = firstRow;
    }

    public void drawCurrentScreen(int mouseX, int mouseY, float partialTicks) {
        scrollBar.drawCurrentScreen(mouseX, mouseY, partialTicks);
    }

    protected List<Element> getVisibleElements() {
        return this.visibleElements.subList(firstRow, Math.min(this.visibleElements.size(), firstRow + scrollBar.getVisibleRows()));
    }

    protected int getAbsoluteElementIndent(Element element) {
        return element.getIndent() * 8;
    }

    public void drawGuiContainerLayer(int guiLeft, int guiTop, GuiTerminalStorage.DrawLayer layer, float partialTick, int mouseX, int mouseY) {
        int offsetY = 0;
        for (GuiCraftingPlan.Element element : getVisibleElements()) {
            drawElement(element,  getAbsoluteElementIndent(element), guiLeft + x, guiTop + y + offsetY, ELEMENT_WIDTH, ELEMENT_HEIGHT, layer, partialTick, mouseX, mouseY);
            offsetY += ELEMENT_HEIGHT_TOTAL;
        }
    }

    private void drawElement(Element element, int indent, int x, int y, int width, int height, GuiTerminalStorage.DrawLayer layer, float partialTick, int mouseX, int mouseY) {
        if (layer == GuiTerminalStorage.DrawLayer.BACKGROUND) {
            // Draw background
            drawRect(x, y, x + width, y + height + 1, element.getColor());
        }

        int xOriginal = x;
        x += indent;

        // Draw dropdown arrow
        if (!element.getChildren().isEmpty() && layer == GuiTerminalStorage.DrawLayer.BACKGROUND) {
            GlStateManager.color(1, 1, 1, 1);
            Image image = element.getChildren().get(0).isEnabled() ? Images.ARROW_DOWN : Images.ARROW_RIGHT;
            image.draw(this, x, y);
        }
        x += 16;

        // Draw outputs
        for (IPrototypedIngredient<?, ?> output : element.getOutputs()) {
            IngredientComponent<?, ?> ingredientComponent = output.getComponent();
            long quantity = ((IngredientComponent) ingredientComponent).getMatcher().getQuantity(output.getPrototype());
            ingredientComponent.getCapability(IngredientComponentTerminalStorageHandlerConfig.CAPABILITY)
                    .drawInstance(output.getPrototype(), quantity, GuiHelpers.quantityToScaledString(quantity),
                            this.parentGui, layer, partialTick, x, y, mouseX, mouseY, null);
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
        // Draw plan label
        drawCenteredString(Minecraft.getMinecraft().fontRenderer, this.label, guiLeft + x + ELEMENT_WIDTH / 2, y - 3, 16777215);

        drawGuiContainerLayer(guiLeft, guiTop, GuiTerminalStorage.DrawLayer.BACKGROUND, partialTicks, mouseX, mouseY);
        scrollBar.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
    }

    public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawGuiContainerLayer(0, 0, GuiTerminalStorage.DrawLayer.FOREGROUND, 0, mouseX, mouseY);
    }

    public void handleMouseInput() throws IOException {
        scrollBar.handleMouseInput();
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int offsetY = 0;
        for (GuiCraftingPlan.Element element : getVisibleElements()) {
            int x = this.guiLeft + this.x + getAbsoluteElementIndent(element);
            int y = this.guiTop + this.y + offsetY;
            offsetY += ELEMENT_HEIGHT_TOTAL;
            if (RenderHelpers.isPointInRegion(new Rectangle(x, y, ELEMENT_WIDTH, ELEMENT_HEIGHT), new Point(mouseX, mouseY))) {
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                // Toggle children
                for (Element child : element.getChildren()) {
                    child.setEnabled(!child.isEnabled());
                }
                refreshList();
                break;
            }
        }
    }

    public static List<GuiCraftingPlan.Element> getElements(ITerminalCraftingPlan<?> craftingPlan) {
        List<GuiCraftingPlan.Element> elements = Lists.newArrayList();
        addElements(null, 0, craftingPlan, elements);
        return elements;
    }

    protected static void addElements(@Nullable Element parent, int indent, ITerminalCraftingPlan<?> craftingPlan, List<GuiCraftingPlan.Element> elements) {
        boolean valid = craftingPlan.getStatus() != TerminalCraftingJobStatus.INVALID
                || (!craftingPlan.getStorageIngredients().isEmpty() || !craftingPlan.getDependencies().isEmpty());
        Element currentElement = new Element(
                indent,
                craftingPlan.getOutputs(),
                0,
                valid ? craftingPlan.getCraftingQuantity() : 0,
                valid ? 0 : craftingPlan.getCraftingQuantity(),
                craftingPlan.getStatus().getColor()
        );
        if (parent != null) {
            parent.addChild(currentElement);
        }
        elements.add(currentElement);
        for (IPrototypedIngredient storageIngredient : craftingPlan.getStorageIngredients()) {
            elements.add(currentElement.addChild(new Element(indent + 1, Collections.singletonList(storageIngredient),
                    storageIngredient.getComponent().getMatcher().getQuantity(storageIngredient.getPrototype()),
                    0, 0, TerminalCraftingJobStatus.FINISHED.getColor())));
        }
        for (ITerminalCraftingPlan<?> dependency : craftingPlan.getDependencies()) {
            addElements(currentElement, indent + 1, dependency, elements);
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
        private final List<Element> children;

        private boolean enabled;

        public Element(int indent, List<IPrototypedIngredient<?, ?>> outputs, long storageQuantity, long craftQuantity,
                       long missingQuantity, int color) {
            this.indent = indent;
            this.outputs = outputs;
            this.storageQuantity = storageQuantity;
            this.craftQuantity = craftQuantity;
            this.missingQuantity = missingQuantity;
            this.color = color;
            this.children = Lists.newArrayList();

            this.enabled = true;
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

        public Element addChild(Element element) {
            children.add(element);
            return element;
        }

        public List<Element> getChildren() {
            return children;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

}
