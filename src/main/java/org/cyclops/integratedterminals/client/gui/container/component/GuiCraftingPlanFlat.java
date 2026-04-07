package org.cyclops.integratedterminals.client.gui.container.component;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.client.gui.GuiGraphicsExtended;
import org.cyclops.cyclopscore.client.gui.component.WidgetScrollBar;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integratedterminals.Capabilities;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlanFlat;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A gui component for visualizing {@link CraftingOptionGuiData} as a flat list.
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
public class GuiCraftingPlanFlat extends AbstractWidget {

    private static final int COLUMNS = 2;
    private static final int COLUMN_PADDING = 2;
    private static final int ELEMENT_WIDTH = 110;
    private static final int ELEMENT_HEIGHT = 16;
    private static final int ELEMENT_HEIGHT_TOTAL = 18;
    private static final int ELEMENT_HEIGHT_MAX = 178;

    protected static final int TICK_DELAY = 30;

    private final AbstractContainerScreen parentGui;
    private final int guiLeft;
    private final int guiTop;
    private final List<GuiCraftingPlanFlat.Element> elements;
    private final List<GuiCraftingPlanFlat.Element> visibleElements;
    private final boolean valid;
    private final WidgetScrollBar scrollBar;
    private final String label;
    private final long tickDuration;
    private final int channel;
    @Nullable
    private final String initiatorName;

    private int firstRow;

    public GuiCraftingPlanFlat(AbstractContainerScreen parentGui, ITerminalCraftingPlanFlat<?> craftingPlan, int guiLeft, int guiTop, int x, int y, int visibleRows) {
        super(x, y, ELEMENT_WIDTH, ELEMENT_HEIGHT_MAX, Component.literal(""));
        this.parentGui = parentGui;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
        this.elements = getElements(craftingPlan);
        this.visibleElements = Lists.newArrayList(this.elements);
        this.valid = craftingPlan.getStatus().isValid();
        this.scrollBar = new WidgetScrollBar(guiLeft + x + 227, guiTop + y + 0, ELEMENT_HEIGHT_MAX, Component.translatable("gui.cyclopscore.scrollbar"), this::setFirstRow, visibleRows);
        refreshList();
        this.label = IModHelpers.get().getL10NHelpers().localize(craftingPlan.getUnlocalizedLabel());
        this.tickDuration = craftingPlan.getTickDuration();
        this.channel = craftingPlan.getChannel();
        this.initiatorName = craftingPlan.getInitiatorName();
    }

    public void inheritVisualizationState(GuiCraftingPlanFlat guiCraftingPlan) {
        // Inherit scroll state
        float lastScroll = guiCraftingPlan.scrollBar.getCurrentScroll();
        this.scrollBar.scrollTo(lastScroll);

        // Recalculate visible items
        refreshList();
    }

    protected void refreshList() {
        this.scrollBar.setTotalRows((int) Math.ceil(visibleElements.size() / COLUMNS));
    }

    public void setFirstRow(int firstRow) {
        this.firstRow = Math.max(0, firstRow);
    }

    protected int getTick() {
        return (int) Minecraft.getInstance().level.getGameTime() / TICK_DELAY;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int p_268034_, int p_268009_, float p_268085_) {

    }

    protected List<Element> getVisibleElements() {
        return this.visibleElements.subList(firstRow, Math.min(this.visibleElements.size(), firstRow + scrollBar.getVisibleRows()));
    }

    public void drawGuiContainerLayer(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int mouseX, int mouseY) {
        int offsetY = 0;
        int column = 0;
        for (GuiCraftingPlanFlat.Element element : getVisibleElements()) {
            drawElement(guiGraphics, element, guiLeft + getX() + (column * (ELEMENT_WIDTH + COLUMN_PADDING)), guiTop + getY() + offsetY, ELEMENT_WIDTH + (column == 0 ? 1 : 0), ELEMENT_HEIGHT, layer, partialTick, mouseX, mouseY);

            column++;
            if (column >= COLUMNS) {
                column = 0;
                offsetY += ELEMENT_HEIGHT_TOTAL;
            }
        }
    }

    private void drawElement(GuiGraphicsExtractor guiGraphics, Element element, int x, int y, int width, int height, ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int mouseX, int mouseY) {
        if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND) {
            // Draw background
            guiGraphics.fill(x, y, x + width, y + height + 1, element.getColor());
        }

        int xOriginal = x;

        // Draw instance (rotate over alternatives if present)
        List<IPrototypedIngredient<?, ?>> instances = element.getInstances();
        if (!instances.isEmpty()) {
            int tick = getTick();
            IPrototypedIngredient<?, ?> output = instances.get(tick % instances.size());
            IngredientComponent<?, ?> ingredientComponent = output.getComponent();
            long quantity = ((IngredientComponent) ingredientComponent).getMatcher().getQuantity(output.getPrototype());
            int finalX = x;
            int finalY = y;
            ingredientComponent.getCapability(Capabilities.IngredientComponentTerminalStorageHandler.INGREDIENT)
                    .ifPresent(h -> h.getClient().drawInstance(guiGraphics, output.getPrototype(), quantity,
                            "", this.parentGui, layer, partialTick, finalX, finalY, mouseX, mouseY, null));
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
            if (element.getToCraftQuantity() > 0) {
                renderItem(guiGraphics, new ItemStack(Items.PAINTING), x, y, 0.45F);
                IModHelpers.get().getRenderHelpers().drawScaledString(guiGraphics, Minecraft.getInstance().font, IModHelpers.get().getL10NHelpers().localize("gui.integratedterminals.terminal_storage.to_craft", element.getToCraftQuantity()), x + 9, y + 1, 0.5F, ARGB.opaque(16777215), true, Font.DisplayMode.NORMAL);
                y += 8;
                moved++;
            }
            if (element.getCraftingQuantity() > 0) {
                if (moved == 2) {
                    y -= 16;
                    x -= 44;
                }
                renderItem(guiGraphics, new ItemStack(Blocks.CRAFTING_TABLE), x, y, 0.45F);
                IModHelpers.get().getRenderHelpers().drawScaledString(guiGraphics, Minecraft.getInstance().font, IModHelpers.get().getL10NHelpers().localize("gui.integratedterminals.terminal_storage.crafting", element.getCraftingQuantity()), x + 9, y + 1, 0.5F, ARGB.opaque(16777215), true, Font.DisplayMode.NORMAL);
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
        guiGraphics.centeredText(Minecraft.getInstance().font, this.label, guiLeft + getX() + GuiCraftingPlan.ELEMENT_WIDTH / 2 + 8, guiTop + getY() - 13, ARGB.opaque(16777215));

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
    public boolean mouseScrolled(double mouseX, double mouseY, double mouseZ, double scroll) {
        return scrollBar.mouseScrolled(mouseX, mouseY, mouseZ, scroll);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouse, double offsetX, double offsetY) {
        return scrollBar.mouseDragged(mouse, offsetX, offsetY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouse, boolean isDoubleClick) {
        return false;
    }

    public static List<GuiCraftingPlanFlat.Element> getElements(ITerminalCraftingPlanFlat<?> craftingPlan) {
        List<GuiCraftingPlanFlat.Element> elements = Lists.newArrayList();
        for (ITerminalCraftingPlanFlat.IEntry entry : craftingPlan.getEntries()) {
            addElements(entry, elements);
        }
        return elements;
    }

    protected static void addElements(ITerminalCraftingPlanFlat.IEntry craftingPlan, List<GuiCraftingPlanFlat.Element> elements) {
        elements.add(new Element(
                craftingPlan.getInstances(),
                craftingPlan.getQuantityInStorage(),
                craftingPlan.getQuantityToCraft(),
                craftingPlan.getQuantityCrafting(),
                craftingPlan.getQuantityMissing()
        ));
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }

    public static class Element {

        private final List<IPrototypedIngredient<?, ?>> instances;
        private final long storageQuantity;
        private final long toCraftQuantity;
        private final long craftingQuantity;
        private final long missingQuantity;

        public Element(List<IPrototypedIngredient<?, ?>> instances,
                       long storageQuantity, long toCraftQuantity, long craftingQuantity, long missingQuantity) {
            this.instances = instances;
            this.storageQuantity = storageQuantity;
            this.toCraftQuantity = toCraftQuantity;
            this.craftingQuantity = craftingQuantity;
            this.missingQuantity = missingQuantity;
        }

        public List<IPrototypedIngredient<?, ?>> getInstances() {
            return instances;
        }

        public long getStorageQuantity() {
            return storageQuantity;
        }

        public long getToCraftQuantity() {
            return toCraftQuantity;
        }

        public long getCraftingQuantity() {
            return craftingQuantity;
        }

        public long getMissingQuantity() {
            return missingQuantity;
        }

        public int getColor() {
            if (getMissingQuantity() > 0) {
                return IModHelpers.get().getBaseHelpers().RGBAToInt(250, 10, 13, 150);
            }
            if (getCraftingQuantity() > 0) {
                return IModHelpers.get().getBaseHelpers().RGBAToInt(43, 174, 231, 150);
            }
            if (getToCraftQuantity() > 0) {
                return IModHelpers.get().getBaseHelpers().RGBAToInt(243, 245, 150, 150);
            }
            return IModHelpers.get().getBaseHelpers().RGBAToInt(43, 231, 47, 150);
        }
    }

}
