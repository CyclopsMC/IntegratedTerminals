package org.cyclops.integratedterminals.client.gui.container.component;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
import org.cyclops.cyclopscore.client.gui.component.GuiScrollBar;
import org.cyclops.cyclopscore.client.gui.image.Image;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
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
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

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

    protected static final int TICK_DELAY = 30;

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
    private final long tickDuration;
    private final int channel;
    @Nullable
    private final String initiatorName;

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
        this.tickDuration = craftingPlan.getTickDuration();
        this.channel = craftingPlan.getChannel();
        this.initiatorName = craftingPlan.getInitiatorName();
    }

    public void inheritVisualizationState(GuiCraftingPlan guiCraftingPlan) {
        float lastScroll = guiCraftingPlan.scrollBar.getCurrentScroll();
        this.scrollBar.scrollTo(lastScroll);
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

    protected int getTick() {
        return (int) Minecraft.getMinecraft().world.getWorldTime() / TICK_DELAY;
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
        int tick = getTick();
        for (List<IPrototypedIngredient<?, ?>> alternatives : element.getOutputs()) {
            IPrototypedIngredient<?, ?> output = alternatives.get(tick % alternatives.size());
            IngredientComponent<?, ?> ingredientComponent = output.getComponent();
            long quantity = ((IngredientComponent) ingredientComponent).getMatcher().getQuantity(output.getPrototype());
            ingredientComponent.getCapability(IngredientComponentTerminalStorageHandlerConfig.CAPABILITY)
                    .drawInstance(output.getPrototype(), quantity, GuiHelpers.quantityToScaledString(quantity),
                            this.parentGui, layer, partialTick, x, y, mouseX, mouseY, null);
            x += GuiHelpers.SLOT_SIZE_INNER;
        }

        x = xOriginal + width - 50;
        if (layer == GuiTerminalStorage.DrawLayer.BACKGROUND) {
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
        } else {
            // Draw tooltip over crafting status
            GuiHelpers.renderTooltipOptional(this.parentGui, x, y, 50, GuiHelpers.SLOT_SIZE, mouseX, mouseY, () -> {
                String unlocalizedName = "gui.integratedterminals.craftingplan.status." + element.getStatus().name().toLowerCase(Locale.ENGLISH) + ".";
                return Optional.of(Lists.newArrayList(
                        L10NHelpers.localize(unlocalizedName + "name"),
                        L10NHelpers.localize(unlocalizedName + "desc")
                ));
            });
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

    public static String getDurationString(long tickDuration) {
        long durationMs = tickDuration * 1000 / MinecraftHelpers.SECOND_IN_TICKS;
        return L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.duration",
                DurationFormatUtils.formatDuration(durationMs, "H:mm:ss", true));
    }

    public void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

        // Draw plan label
        drawCenteredString(Minecraft.getMinecraft().fontRenderer, this.label, guiLeft + x + ELEMENT_WIDTH / 2, guiTop + y - 13, 16777215);

        // Draw duration
        if (tickDuration >= 0) {
            String durationString = getDurationString(tickDuration);
            RenderHelpers.drawScaledString(fontRenderer, durationString, guiLeft + x + 200, guiTop + y - 14, 0.5f, 16777215, true);
        }

        // Draw channel
        if (channel != -1) {
            String channelString = L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.crafting_channel", channel);
            RenderHelpers.drawScaledString(fontRenderer, channelString, guiLeft + x + 200, guiTop + y - 8, 0.5f, 16777215, true);
        }

        // Draw initiator
        if (initiatorName != null) {
            String initiatorString = L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.owner", initiatorName);
            RenderHelpers.drawScaledString(fontRenderer, initiatorString, guiLeft + x - 4, guiTop + y - 14, 0.5f, 16777215, true);
        }

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
                (List) craftingPlan.getOutputs()
                    .stream()
                    .map(Collections::singletonList)
                    .collect(Collectors.toList()),
                0,
                valid ? craftingPlan.getCraftingQuantity() : 0,
                valid ? 0 : craftingPlan.getCraftingQuantity(),
                craftingPlan.getStatus().getColor(),
                craftingPlan.getStatus());
        if (parent != null) {
            parent.addChild(currentElement);
        }
        elements.add(currentElement);
        if (craftingPlan.getStatus() == TerminalCraftingJobStatus.PENDING_INPUTS) {
            // Add last missing ingredients
            for (List<IPrototypedIngredient<?, ?>> lastMissingIngredient : craftingPlan.getLastMissingIngredients()) {
                List outputs = Collections.singletonList(lastMissingIngredient
                        .stream()
                        .map(prototypedIngredient -> {
                            IIngredientMatcher matcher = prototypedIngredient.getComponent().getMatcher();
                            Object instance = matcher.withQuantity(prototypedIngredient.getPrototype(),
                                    matcher.getQuantity(prototypedIngredient.getPrototype()) * craftingPlan.getCraftingQuantity());
                            return new PrototypedIngredient(prototypedIngredient.getComponent(), instance, prototypedIngredient.getCondition());
                        })
                        .collect(Collectors.toList()));
                elements.add(currentElement.addChild(new Element(indent + 1, outputs,
                        0, 0, craftingPlan.getCraftingQuantity(), TerminalCraftingJobStatus.INVALID.getColor(), TerminalCraftingJobStatus.INVALID)));
            }
        } else if (craftingPlan.getStatus() != TerminalCraftingJobStatus.CRAFTING) {
            for (IPrototypedIngredient storageIngredient : craftingPlan.getStorageIngredients()) {
                elements.add(currentElement.addChild(new Element(indent + 1, Collections.singletonList(Collections.singletonList(storageIngredient)),
                        storageIngredient.getComponent().getMatcher().getQuantity(storageIngredient.getPrototype()),
                        0, 0, TerminalCraftingJobStatus.FINISHED.getColor(), TerminalCraftingJobStatus.FINISHED)));
            }
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
        private final List<List<IPrototypedIngredient<?, ?>>> outputs;
        private final long storageQuantity;
        private final long craftQuantity;
        private final long missingQuantity;
        private final int color;
        private final List<Element> children;
        private final TerminalCraftingJobStatus status;

        private boolean enabled;

        public Element(int indent, List<List<IPrototypedIngredient<?, ?>>> outputs, long storageQuantity, long craftQuantity,
                       long missingQuantity, int color, TerminalCraftingJobStatus status) {
            this.indent = indent;
            this.outputs = outputs;
            this.storageQuantity = storageQuantity;
            this.craftQuantity = craftQuantity;
            this.missingQuantity = missingQuantity;
            this.color = color;
            this.status = status;
            this.children = Lists.newArrayList();

            this.enabled = true;
        }

        public int getIndent() {
            return indent;
        }

        public List<List<IPrototypedIngredient<?, ?>>> getOutputs() {
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

        public TerminalCraftingJobStatus getStatus() {
            return status;
        }
    }

}
