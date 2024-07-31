package org.cyclops.integratedterminals.client.gui.container.component;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
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
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integratedterminals.Capabilities;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A gui component for visualizing {@link CraftingOptionGuiData}.
 *
 * The using gui must call the following methods from its respective method:
 * * {@link #render(GuiGraphics, int, int, float)}
 * * {@link #drawGuiContainerBackgroundLayer(GuiGraphics, float, int, int)}
 * * {@link #drawGuiContainerForegroundLayer(GuiGraphics, int, int)}
 * * {@link #mouseScrolled(double, double, double, double)}}
 * * {@link #mouseDragged(double, double, int, double, double)}}
 *
 * @author rubensworks
 */
public class GuiCraftingPlan extends AbstractWidget {

    public static final int ELEMENT_WIDTH = 221;
    private static final int ELEMENT_HEIGHT = 16;
    private static final int ELEMENT_HEIGHT_TOTAL = 18;

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
        super(x, y, 0, 0, Component.literal(""));
        this.parentGui = parentGui;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
        this.elements = getElements(craftingPlan);
        this.visibleElements = Lists.newArrayList(this.elements);
        this.valid = craftingPlan.getStatus().isValid();
        this.scrollBar = new WidgetScrollBar(guiLeft + x + 227, guiTop + y + 0, 178, Component.translatable("gui.cyclopscore.scrollbar"), this::setFirstRow, visibleRows);
        this.scrollBar.setTotalRows(visibleElements.size());
        this.label = L10NHelpers.localize(craftingPlan.getUnlocalizedLabel());
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
    public void renderWidget(GuiGraphics guiGraphics, int p_268034_, int p_268009_, float p_268085_) {

    }

    protected List<Element> getVisibleElements() {
        return this.visibleElements.subList(firstRow, Math.min(this.visibleElements.size(), firstRow + scrollBar.getVisibleRows()));
    }

    protected int getAbsoluteElementIndent(Element element) {
        return element.getIndent() * 8;
    }

    public void drawGuiContainerLayer(GuiGraphics guiGraphics, int guiLeft, int guiTop, ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int mouseX, int mouseY) {
        int offsetY = 0;
        for (GuiCraftingPlan.Element element : getVisibleElements()) {
            drawElement(guiGraphics, element,  getAbsoluteElementIndent(element), guiLeft + getX(), guiTop + getY() + offsetY, ELEMENT_WIDTH, ELEMENT_HEIGHT, layer, partialTick, mouseX, mouseY);
            offsetY += ELEMENT_HEIGHT_TOTAL;
        }
    }

    protected int getTick() {
        return (int) Minecraft.getInstance().level.getGameTime() / TICK_DELAY;
    }

    private void drawElement(GuiGraphics guiGraphics, Element element, int indent, int x, int y, int width, int height, ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int mouseX, int mouseY) {
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
                    .ifPresent(h -> h.drawInstance(guiGraphics, output.getPrototype(), quantity,
                            GuiHelpers.quantityToScaledString(quantity), this.parentGui, layer, partialTick, finalX, finalY, mouseX, mouseY, null));
            x += GuiHelpers.SLOT_SIZE_INNER;
        }

        x = xOriginal + width - 50;
        if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND) {
            // Draw counters
            if (element.getStorageQuantity() > 0) {
                renderItem(guiGraphics, new ItemStack(Blocks.CHEST), x, y, 0.45F);
                RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), Minecraft.getInstance().font, L10NHelpers.localize("gui.integratedterminals.terminal_storage.stored", element.getStorageQuantity()), x + 9, y + 1, 0.5F, 16777215, true, Font.DisplayMode.NORMAL);
                y += 8;
            }
            if (element.getCraftQuantity() > 0) {
                renderItem(guiGraphics, new ItemStack(Blocks.CRAFTING_TABLE), x, y, 0.45F);
                RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), Minecraft.getInstance().font, L10NHelpers.localize("gui.integratedterminals.terminal_storage.crafting", element.getCraftQuantity()), x + 9, y + 1, 0.5F, 16777215, true, Font.DisplayMode.NORMAL);
                y += 8;
            }
            if (element.getMissingQuantity() > 0) {
                renderItem(guiGraphics, new ItemStack(Blocks.BARRIER), x, y, 0.45F);
                RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), Minecraft.getInstance().font, L10NHelpers.localize("gui.integratedterminals.terminal_storage.missing", element.getMissingQuantity()), x + 9, y + 1, 0.5F, 16777215, true, Font.DisplayMode.NORMAL);
            }
            RenderSystem.setShaderColor(1, 1, 1, 1);
        } else {
            // Draw tooltip over crafting status
            GuiHelpers.renderTooltipOptional(this.parentGui, guiGraphics.pose(), x, y, 50, GuiHelpers.SLOT_SIZE, mouseX, mouseY, () -> {
                String unlocalizedName = "gui.integratedterminals.craftingplan.status." + element.getStatus().name().toLowerCase(Locale.ENGLISH);
                return Optional.of(Lists.newArrayList(
                        Component.translatable(unlocalizedName),
                        Component.translatable(unlocalizedName + ".desc")
                ));
            });
        }
    }

    protected static void renderItem(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y, float scale) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(scale, scale, scale);
        RenderSystem.applyModelViewMatrix();

        GuiGraphicsExtended renderItem = new GuiGraphicsExtended(guiGraphics);
        GlStateManager._enableBlend();
        GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Lighting.setupFor3DItems();
        GlStateManager._enableDepthTest();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        guiGraphics.renderItem(itemStack, 0, 0);
        renderItem.renderItemDecorations(Minecraft.getInstance().font, itemStack, 0, 0, "");
        Lighting.setupForFlatItems();

        guiGraphics.pose().popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public static String getDurationString(long tickDuration) {
        long durationMs = tickDuration * 1000 / MinecraftHelpers.SECOND_IN_TICKS;
        return L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.duration",
                DurationFormatUtils.formatDuration(durationMs, "H:mm:ss", true));
    }

    public void drawGuiContainerBackgroundLayer(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        Font fontRenderer = Minecraft.getInstance().font;

        // Draw plan label
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.label, guiLeft + getX() + ELEMENT_WIDTH / 2 + 8, guiTop + getY() - 13, 16777215);

        // Draw duration
        if (tickDuration >= 0) {
            String durationString = getDurationString(tickDuration);
            RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), fontRenderer, durationString, guiLeft + getX() + 200, guiTop + getY() - 14, 0.5f, 16777215, true, Font.DisplayMode.NORMAL);
        }

        // Draw channel
        if (channel != -1) {
            String channelString = L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.crafting_channel", channel);
            RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), fontRenderer, channelString, guiLeft + getX() + 200, guiTop + getY() - 8, 0.5f, 16777215, true, Font.DisplayMode.NORMAL);
        }

        // Draw initiator
        if (initiatorName != null) {
            String initiatorString = L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.owner", initiatorName);
            RenderHelpers.drawScaledString(guiGraphics.pose(), guiGraphics.bufferSource(), fontRenderer, initiatorString, guiLeft + getX() - 4, guiTop + getY() - 14, 0.5f, 16777215, true, Font.DisplayMode.NORMAL);
        }

        drawGuiContainerLayer(guiGraphics, guiLeft, guiTop, ContainerScreenTerminalStorage.DrawLayer.BACKGROUND, partialTicks, mouseX, mouseY);
        scrollBar.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    public void drawGuiContainerForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        drawGuiContainerLayer(guiGraphics, 0, 0, ContainerScreenTerminalStorage.DrawLayer.FOREGROUND, 0, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return scrollBar.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double offsetX, double offsetY) {
        return scrollBar.mouseDragged(mouseX, mouseY, mouseButton, offsetX, offsetY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        int offsetY = 0;
        for (GuiCraftingPlan.Element element : getVisibleElements()) {
            int x = this.guiLeft + this.getX() + getAbsoluteElementIndent(element);
            int y = this.guiTop + this.getY() + offsetY;
            offsetY += ELEMENT_HEIGHT_TOTAL;
            if (RenderHelpers.isPointInRegion(new Rectangle(x, y, ELEMENT_WIDTH, ELEMENT_HEIGHT), new Point((int) mouseX, (int) mouseY))) {
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
                || (!craftingPlan.getStorageIngredients().isEmpty() || !craftingPlan.getDependencies().isEmpty());
        int elementId = Objects.hash(craftingPlan.getId()) * 100;
        Element currentElement = new Element(
                elementId++,
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
                elements.add(currentElement.addChild(new Element(elementId++, indent + 1, outputs,
                        0, 0, craftingPlan.getCraftingQuantity(), TerminalCraftingJobStatus.INVALID.getColor(), TerminalCraftingJobStatus.INVALID)));
            }
        } else if (craftingPlan.getStatus() != TerminalCraftingJobStatus.CRAFTING) {
            for (IPrototypedIngredient storageIngredient : craftingPlan.getStorageIngredients()) {
                elements.add(currentElement.addChild(new Element(elementId++, indent + 1, Collections.singletonList(Collections.singletonList(storageIngredient)),
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

        public Element(int id, int indent, List<List<IPrototypedIngredient<?, ?>>> outputs, long storageQuantity, long craftQuantity,
                       long missingQuantity, int color, TerminalCraftingJobStatus status) {
            this.id = id;
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
