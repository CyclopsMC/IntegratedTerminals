package org.cyclops.integratedterminals.client.gui.container.component;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.cyclopscore.client.gui.GuiGraphicsExtended;
import org.cyclops.cyclopscore.client.gui.component.WidgetScrollBar;
import org.cyclops.cyclopscore.client.gui.image.Image;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integratedterminals.Capabilities;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A gui component for visualizing {@link CraftingOptionGuiData}.
 *
 * The using gui must call the following methods from its respective method:
 * * {@link #extractRenderState(GuiGraphicsExtractor, int, int, float)}
 * * {@link #drawGuiContainerBackgroundLayer(GuiGraphicsExtractor, float, int, int)}
 * * {@link #drawGuiContainerForegroundLayer(GuiGraphicsExtractor, int, int)}
 * * {@link #mouseScrolled(double, double, double, double)}}
 * * {@link #mouseDragged(MouseButtonEvent, double, double)}
 *
 * @author rubensworks
 */
public class GuiCraftingPlan extends AbstractWidget {

    public static final int ELEMENT_WIDTH = 221;
    private static final int ELEMENT_HEIGHT = 16;
    private static final int ELEMENT_HEIGHT_TOTAL = 18;
    private static final int ELEMENT_HEIGHT_MAX = 178;

    protected static final int TICK_DELAY = 30;

    private final AbstractContainerScreen parentGui;
    private final int guiLeft;
    private final int guiTop;
    private final List<GuiCraftingPlan.Element> elements;
    private final List<GuiCraftingPlan.Element> visibleElements;
    private final boolean valid;
    private final WidgetScrollBar scrollBar;
    private final String label;
    private final long tickDuration;
    private final int channel;
    @Nullable
    private final String initiatorName;

    private int firstRow;

    public GuiCraftingPlan(AbstractContainerScreen parentGui, ITerminalCraftingPlan<?> craftingPlan, int guiLeft, int guiTop, int x, int y, int visibleRows) {
        super(x, y, ELEMENT_WIDTH, ELEMENT_HEIGHT_MAX, Component.literal(""));
        this.parentGui = parentGui;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
        this.elements = getElements(craftingPlan);
        this.visibleElements = Lists.newArrayList(this.elements);
        this.valid = craftingPlan.getStatus().isValid();
        this.scrollBar = new WidgetScrollBar(guiLeft + x + 227, guiTop + y + 0, ELEMENT_HEIGHT_MAX, Component.translatable("gui.cyclopscore.scrollbar"), this::setFirstRow, visibleRows);
        this.scrollBar.setTotalRows(visibleElements.size());
        this.label = IModHelpers.get().getL10NHelpers().localize(craftingPlan.getUnlocalizedLabel());
        this.tickDuration = craftingPlan.getTickDuration();
        this.channel = craftingPlan.getChannel();
        this.initiatorName = craftingPlan.getInitiatorName();
    }

    public void inheritVisualizationState(GuiCraftingPlan guiCraftingPlan) {
        // Inherit scroll state
        float lastScroll = guiCraftingPlan.scrollBar.getCurrentScroll();
        this.scrollBar.scrollTo(lastScroll);

        // Inherit toggle state
        IntOpenHashSet disabledElementIds = new IntOpenHashSet();
        for (Element element : guiCraftingPlan.elements) {
            if (!element.isEnabled()) {
                disabledElementIds.add(element.getId());
            }
        }
        for (Element element : this.elements) {
            if (disabledElementIds.contains(element.getId())) {
                element.setEnabled(false);
            }
        }

        // Recalculate visible items
        refreshList();
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
        this.firstRow = Math.max(0, firstRow);
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int p_268034_, int p_268009_, float p_268085_) {

    }

    protected List<Element> getVisibleElements() {
        return this.visibleElements.subList(firstRow, Math.min(this.visibleElements.size(), firstRow + scrollBar.getVisibleRows()));
    }

    protected int getAbsoluteElementIndent(Element element) {
        return element.getIndent() * 8;
    }

    public void drawGuiContainerLayer(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int mouseX, int mouseY) {
        int offsetY = 0;
        for (GuiCraftingPlan.Element element : getVisibleElements()) {
            drawElement(guiGraphics, element,  getAbsoluteElementIndent(element), guiLeft + getX(), guiTop + getY() + offsetY, ELEMENT_WIDTH, ELEMENT_HEIGHT, layer, partialTick, mouseX, mouseY);
            offsetY += ELEMENT_HEIGHT_TOTAL;
        }
    }

    protected int getTick() {
        return (int) Minecraft.getInstance().level.getGameTime() / TICK_DELAY;
    }

    private void drawElement(GuiGraphicsExtractor guiGraphics, Element element, int indent, int x, int y, int width, int height, ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int mouseX, int mouseY) {
        if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND) {
            // Draw background
            guiGraphics.fill(x, y, x + width, y + height + 1, element.getColor());
        }

        int xOriginal = x;
        x += indent;

        // Draw dropdown arrow
        if (!element.getChildren().isEmpty() && layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND) {
            Image image = element.getChildren().get(0).isEnabled() ? Images.ARROW_DOWN : Images.ARROW_RIGHT;
            image.draw(guiGraphics, x, y);
        }
        x += 16;

        // Draw outputs
        int tick = getTick();
        for (List<IPrototypedIngredient<?, ?>> alternatives : element.getOutputs()) {
            IPrototypedIngredient<?, ?> output = alternatives.get(tick % alternatives.size());
            IngredientComponent<?, ?> ingredientComponent = output.getComponent();
            long quantity = ((IngredientComponent) ingredientComponent).getMatcher().getQuantity(output.getPrototype());
            int finalX = x;
            int finalY = y;
            ingredientComponent.getCapability(Capabilities.IngredientComponentTerminalStorageHandler.INGREDIENT)
                    .ifPresent(h -> h.getClient().drawInstance(guiGraphics, output.getPrototype(), quantity,
                            IModHelpers.get().getGuiHelpers().quantityToScaledString(quantity), this.parentGui, layer, partialTick, finalX, finalY, mouseX, mouseY, null));
            x += IModHelpers.get().getGuiHelpers().getSlotSizeInner();
        }

        x = xOriginal + width - 50;
        if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND) {
            // Draw counters
            int moved = 0;
            if (element.getStorageQuantity() > 0) {
                renderItem(guiGraphics, new ItemStack(Blocks.CHEST), x, y, 0.45F);
                IModHelpers.get().getRenderHelpers().drawScaledString(guiGraphics, Minecraft.getInstance().font, IModHelpers.get().getL10NHelpers().localize("gui.integratedterminals.terminal_storage.stored", element.getStorageQuantity()), x + 9, y + 1, 0.5F, ARGB.opaque(16777215), true, Font.DisplayMode.NORMAL);
                y += 8;
                moved++;
            }
            if (element.getCraftQuantity() > 0) {
                renderItem(guiGraphics, new ItemStack(Blocks.CRAFTING_TABLE), x, y, 0.45F);
                IModHelpers.get().getRenderHelpers().drawScaledString(guiGraphics, Minecraft.getInstance().font, IModHelpers.get().getL10NHelpers().localize("gui.integratedterminals.terminal_storage.crafting", element.getCraftQuantity()), x + 9, y + 1, 0.5F, ARGB.opaque(16777215), true, Font.DisplayMode.NORMAL);
                y += 8;
                moved++;
            }
            if (element.getMissingQuantity() > 0) {
                if (moved == 2) {
                    y -= 16;
                    x -= 44;
                }
                renderItem(guiGraphics, new ItemStack(Blocks.BARRIER), x, y, 0.45F);
                IModHelpers.get().getRenderHelpers().drawScaledString(guiGraphics, Minecraft.getInstance().font, IModHelpers.get().getL10NHelpers().localize("gui.integratedterminals.terminal_storage.missing", element.getMissingQuantity()), x + 9, y + 1, 0.5F, ARGB.opaque(16777215), true, Font.DisplayMode.NORMAL);
            }
        } else {
            // Draw tooltip over crafting status
            IModHelpers.get().getGuiHelpers().renderTooltipOptional(this.parentGui, guiGraphics, x, y, 50, IModHelpers.get().getGuiHelpers().getSlotSize(), mouseX, mouseY, () -> {
                String unlocalizedName = "gui.integratedterminals.craftingplan.status." + element.getStatus().name().toLowerCase(Locale.ENGLISH);
                return Optional.of(Lists.newArrayList(
                        Component.translatable(unlocalizedName),
                        Component.translatable(unlocalizedName + ".desc")
                ));
            });
        }
    }

    protected static void renderItem(GuiGraphicsExtractor guiGraphics, ItemStack itemStack, int x, int y, float scale) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        guiGraphics.pose().scale(scale, scale);

        GuiGraphicsExtended renderItem = new GuiGraphicsExtended(guiGraphics);
        guiGraphics.item(itemStack, 0, 0);
        renderItem.itemDecorations(Minecraft.getInstance().font, itemStack, 0, 0, "");

        guiGraphics.pose().popMatrix();
    }

    public static String getDurationString(long tickDuration) {
        long durationMs = tickDuration * 1000 / IModHelpers.get().getMinecraftHelpers().getSecondInTicks();
        return IModHelpers.get().getL10NHelpers().localize("gui.integratedterminals.terminal_crafting_job.craftingplan.duration",
                DurationFormatUtils.formatDuration(durationMs, "H:mm:ss", true));
    }

    public void drawGuiContainerBackgroundLayer(GuiGraphicsExtractor guiGraphics, float partialTicks, int mouseX, int mouseY) {
        Font fontRenderer = Minecraft.getInstance().font;

        // Draw plan label
        guiGraphics.centeredText(Minecraft.getInstance().font, this.label, guiLeft + getX() + ELEMENT_WIDTH / 2 + 8, guiTop + getY() - 13, ARGB.opaque(16777215));

        // Draw duration
        if (tickDuration >= 0) {
            String durationString = getDurationString(tickDuration);
            IModHelpers.get().getRenderHelpers().drawScaledString(guiGraphics, fontRenderer, durationString, guiLeft + getX() + 200, guiTop + getY() - 14, 0.5f, ARGB.opaque(16777215), true, Font.DisplayMode.NORMAL);
        }

        // Draw channel
        if (channel != -1) {
            String channelString = IModHelpers.get().getL10NHelpers().localize("gui.integratedterminals.terminal_crafting_job.craftingplan.crafting_channel", channel);
            IModHelpers.get().getRenderHelpers().drawScaledString(guiGraphics, fontRenderer, channelString, guiLeft + getX() + 200, guiTop + getY() - 8, 0.5f, ARGB.opaque(16777215), true, Font.DisplayMode.NORMAL);
        }

        // Draw initiator
        if (initiatorName != null) {
            String initiatorString = IModHelpers.get().getL10NHelpers().localize("gui.integratedterminals.terminal_crafting_job.craftingplan.owner", initiatorName);
            IModHelpers.get().getRenderHelpers().drawScaledString(guiGraphics, fontRenderer, initiatorString, guiLeft + getX() - 4, guiTop + getY() - 14, 0.5f, ARGB.opaque(16777215), true, Font.DisplayMode.NORMAL);
        }

        drawGuiContainerLayer(guiGraphics, guiLeft, guiTop, ContainerScreenTerminalStorage.DrawLayer.BACKGROUND, partialTicks, mouseX, mouseY);
        scrollBar.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTicks);
    }

    public void drawGuiContainerForegroundLayer(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        drawGuiContainerLayer(guiGraphics, 0, 0, ContainerScreenTerminalStorage.DrawLayer.FOREGROUND, 0, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return scrollBar.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouse, double offsetX, double offsetY) {
        return scrollBar.mouseDragged(mouse, offsetX, offsetY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouse, boolean isDoubleClick   ) {
        int offsetY = 0;
        for (GuiCraftingPlan.Element element : getVisibleElements()) {
            int x = this.guiLeft + this.getX() + getAbsoluteElementIndent(element);
            int y = this.guiTop + this.getY() + offsetY;
            offsetY += ELEMENT_HEIGHT_TOTAL;
            if (IModHelpers.get().getRenderHelpers().isPointInRegion(new Rectangle(x, y, ELEMENT_WIDTH, ELEMENT_HEIGHT), new Point((int) mouse.x(), (int) mouse.y()))) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                // Toggle children
                for (Element child : element.getChildren()) {
                    child.setEnabled(!child.isEnabled());
                }
                refreshList();
                return true;
            }
        }
        return false;
    }

    public static List<GuiCraftingPlan.Element> getElements(ITerminalCraftingPlan<?> craftingPlan) {
        List<GuiCraftingPlan.Element> elements = Lists.newArrayList();
        addElements(null, 0, craftingPlan, elements);
        return elements;
    }

    protected static void addElements(@Nullable Element parent, int indent, ITerminalCraftingPlan<?> craftingPlan, List<GuiCraftingPlan.Element> elements) {
        boolean valid = craftingPlan.getStatus().isValid()
                || (!craftingPlan.getBufferedIngredients().isEmpty() || !craftingPlan.getDependencies().isEmpty());
        int elementId = Objects.hash(craftingPlan.getId()) * 100;
        Element currentElement = new Element(
                elementId++,
                indent,
                craftingPlan.getOutputs()
                    .stream()
                    .map(i -> Collections.singletonList((IPrototypedIngredient) i))
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
                List<List<IPrototypedIngredient>> outputs = Collections.singletonList(lastMissingIngredient
                        .stream()
                        .map(prototypedIngredient -> {
                            IIngredientMatcher matcher = prototypedIngredient.getComponent().getMatcher();
                            Object instance = matcher.withQuantity(prototypedIngredient.getPrototype(),
                                    matcher.getQuantity(prototypedIngredient.getPrototype()) * craftingPlan.getCraftingQuantity());
                            return new PrototypedIngredient(prototypedIngredient.getComponent(), instance, prototypedIngredient.getCondition());
                        })
                        .collect(Collectors.toList()));
                elements.add(currentElement.addChild(new Element(elementId++, indent + 1, outputs,
                        0, 0, craftingPlan.getCraftingQuantity(), TerminalCraftingJobStatus.INVALID.getColor(), TerminalCraftingJobStatus.INVALID)));
            }
        } else if (craftingPlan.getStatus() != TerminalCraftingJobStatus.CRAFTING) {
            for (IPrototypedIngredient bufferedIngredient : craftingPlan.getBufferedIngredients()) {
                elements.add(currentElement.addChild(new Element(elementId++, indent + 1, Collections.singletonList(Collections.singletonList(bufferedIngredient)),
                        bufferedIngredient.getComponent().getMatcher().getQuantity(bufferedIngredient.getPrototype()),
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

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }

    public static class Element {

        private final int id;
        private final int indent;
        private final List<List<IPrototypedIngredient<?, ?>>> outputs;
        private final long storageQuantity;
        private final long craftQuantity;
        private final long missingQuantity;
        private final int color;
        private final List<Element> children;
        private final TerminalCraftingJobStatus status;

        private boolean enabled;

        public Element(int id, int indent, List<List<IPrototypedIngredient>> outputs, long storageQuantity, long craftQuantity,
                       long missingQuantity, int color, TerminalCraftingJobStatus status) {
            this.id = id;
            this.indent = indent;
            this.outputs = (List) outputs;
            this.storageQuantity = storageQuantity;
            this.craftQuantity = craftQuantity;
            this.missingQuantity = missingQuantity;
            this.color = color;
            this.status = status;
            this.children = Lists.newArrayList();

            this.enabled = true;
        }

        public int getId() {
            return id;
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
