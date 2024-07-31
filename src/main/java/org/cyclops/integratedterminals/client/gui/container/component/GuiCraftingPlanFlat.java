package org.cyclops.integratedterminals.client.gui.container.component;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
import org.cyclops.cyclopscore.client.gui.component.WidgetScrollBar;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlanFlat;
import org.cyclops.integratedterminals.capability.ingredient.IngredientComponentTerminalStorageHandlerConfig;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A gui component for visualizing {@link CraftingOptionGuiData} as a flat list.
 *
 * The using gui must call the following methods from its respective method:
 * * {@link #render(PoseStack, int, int, float)}}
 * * {@link #drawGuiContainerBackgroundLayer(PoseStack, float, int, int)}
 * * {@link #drawGuiContainerForegroundLayer(PoseStack, int, int)}
 * * {@link #mouseScrolled(double, double, double)}}
 * * {@link #mouseDragged(double, double, int, double, double)}}
 *
 * @author rubensworks
 */
public class GuiCraftingPlanFlat extends AbstractWidget {

    private static final int COLUMNS = 2;
    private static final int COLUMN_PADDING = 2;
    private static final int ELEMENT_WIDTH = 110;
    private static final int ELEMENT_HEIGHT = 16;
    private static final int ELEMENT_HEIGHT_TOTAL = 18;

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
        super(x, y, 0, 0, Component.literal(""));
        this.parentGui = parentGui;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
        this.elements = getElements(craftingPlan);
        this.visibleElements = Lists.newArrayList(this.elements);
        this.valid = craftingPlan.getStatus().isValid();
        this.scrollBar = new WidgetScrollBar(guiLeft + x + 227, guiTop + y + 0, 178, Component.translatable("gui.cyclopscore.scrollbar"), this::setFirstRow, visibleRows);
        this.scrollBar.setTotalRows((int) Math.ceil(visibleElements.size() / COLUMNS));
        this.label = L10NHelpers.localize(craftingPlan.getUnlocalizedLabel());
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
        visibleElements.clear();
        this.scrollBar.setTotalRows(visibleElements.size());
    }

    public void setFirstRow(int firstRow) {
        this.firstRow = Math.max(0, firstRow);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {

    }

    protected List<Element> getVisibleElements() {
        return this.visibleElements.subList(firstRow, Math.min(this.visibleElements.size(), firstRow + scrollBar.getVisibleRows()));
    }

    public void drawGuiContainerLayer(PoseStack matrixStack, int guiLeft, int guiTop, ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int mouseX, int mouseY) {
        int offsetY = 0;
        int column = 0;
        for (GuiCraftingPlanFlat.Element element : getVisibleElements()) {
            drawElement(matrixStack, element, guiLeft + x + (column * (ELEMENT_WIDTH + COLUMN_PADDING)), guiTop + y + offsetY, ELEMENT_WIDTH + (column == 0 ? 1 : 0), ELEMENT_HEIGHT, layer, partialTick, mouseX, mouseY);

            column++;
            if (column >= COLUMNS) {
                column = 0;
                offsetY += ELEMENT_HEIGHT_TOTAL;
            }
        }
    }

    private void drawElement(PoseStack matrixStack, Element element, int x, int y, int width, int height, ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int mouseX, int mouseY) {
        if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND) {
            // Draw background
            fill(matrixStack, x, y, x + width, y + height + 1, element.getColor());
        }

        int xOriginal = x;

        // Draw instance
        IPrototypedIngredient<?, ?> output = element.getInstance();
        IngredientComponent<?, ?> ingredientComponent = output.getComponent();
        long quantity = ((IngredientComponent) ingredientComponent).getMatcher().getQuantity(output.getPrototype());
        int finalX = x;
        int finalY = y;
        ingredientComponent.getCapability(IngredientComponentTerminalStorageHandlerConfig.CAPABILITY)
                .ifPresent(h -> h.drawInstance(matrixStack, output.getPrototype(), quantity,
                        "", this.parentGui, layer, partialTick, finalX, finalY, mouseX, mouseY, null));

        x = xOriginal + width - 50;
        if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND) {
            // Draw counters
            if (element.getStorageQuantity() > 0) {
                renderItem(RenderSystem.getModelViewStack(), new ItemStack(Blocks.CHEST), x, y, 0.45F);
                RenderHelpers.drawScaledStringWithShadow(matrixStack, Minecraft.getInstance().font, L10NHelpers.localize("gui.integratedterminals.terminal_storage.stored", element.getStorageQuantity()), x + 9, y + 1, 0.5F, 16777215);
                y += 8;
            }
            if (element.getToCraftQuantity() > 0) {
                renderItem(RenderSystem.getModelViewStack(), new ItemStack(Blocks.CRAFTING_TABLE), x, y, 0.45F);
                RenderHelpers.drawScaledStringWithShadow(matrixStack, Minecraft.getInstance().font, L10NHelpers.localize("gui.integratedterminals.terminal_storage.to_craft", element.getToCraftQuantity()), x + 9, y + 1, 0.5F, 16777215);
                y += 8;
            }
            if (element.getCraftingQuantity() > 0) {
                renderItem(RenderSystem.getModelViewStack(), new ItemStack(Blocks.CRAFTING_TABLE), x, y, 0.45F);
                RenderHelpers.drawScaledStringWithShadow(matrixStack, Minecraft.getInstance().font, L10NHelpers.localize("gui.integratedterminals.terminal_storage.crafting", element.getCraftingQuantity()), x + 9, y + 1, 0.5F, 16777215);
                y += 8;
            }
            if (element.getMissingQuantity() > 0) {
                renderItem(RenderSystem.getModelViewStack(), new ItemStack(Blocks.BARRIER), x, y, 0.45F);
                RenderHelpers.drawScaledStringWithShadow(matrixStack, Minecraft.getInstance().font, L10NHelpers.localize("gui.integratedterminals.terminal_storage.missing", element.getMissingQuantity()), x + 9, y + 1, 0.5F, 16777215);
            }
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
    }

    protected static void renderItem(PoseStack poseStack, ItemStack itemStack, int x, int y, float scale) {
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.scale(scale, scale, scale);
        RenderSystem.applyModelViewMatrix();

        RenderItemExtendedSlotCount renderItem = RenderItemExtendedSlotCount.getInstance();
        GlStateManager._enableBlend();
        GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Lighting.setupFor3DItems();
        GlStateManager._enableDepthTest();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        renderItem.renderAndDecorateItem(itemStack, 0, 0);
        renderItem.renderGuiItemDecorations(Minecraft.getInstance().font, itemStack, 0, 0, "");
        Lighting.setupForFlatItems();

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public static String getDurationString(long tickDuration) {
        long durationMs = tickDuration * 1000 / MinecraftHelpers.SECOND_IN_TICKS;
        return L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.duration",
                DurationFormatUtils.formatDuration(durationMs, "H:mm:ss", true));
    }

    public void drawGuiContainerBackgroundLayer(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        Font fontRenderer = Minecraft.getInstance().font;

        // Draw plan label
        drawCenteredString(matrixStack, Minecraft.getInstance().font, this.label, guiLeft + x + GuiCraftingPlan.ELEMENT_WIDTH / 2 + 8, guiTop + y - 13, 16777215);

        // Draw duration
        if (tickDuration >= 0) {
            String durationString = getDurationString(tickDuration);
            RenderHelpers.drawScaledStringWithShadow(matrixStack, fontRenderer, durationString, guiLeft + x + 200, guiTop + y - 14, 0.5f, 16777215);
        }

        // Draw channel
        if (channel != -1) {
            String channelString = L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.crafting_channel", channel);
            RenderHelpers.drawScaledStringWithShadow(matrixStack, fontRenderer, channelString, guiLeft + x + 200, guiTop + y - 8, 0.5f, 16777215);
        }

        // Draw initiator
        if (initiatorName != null) {
            String initiatorString = L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.owner", initiatorName);
            RenderHelpers.drawScaledStringWithShadow(matrixStack, fontRenderer, initiatorString, guiLeft + x - 4, guiTop + y - 14, 0.5f, 16777215);
        }

        drawGuiContainerLayer(matrixStack, guiLeft, guiTop, ContainerScreenTerminalStorage.DrawLayer.BACKGROUND, partialTicks, mouseX, mouseY);
        scrollBar.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
    }

    public void drawGuiContainerForegroundLayer(PoseStack matrixStack, int mouseX, int mouseY) {
        drawGuiContainerLayer(matrixStack, 0, 0, ContainerScreenTerminalStorage.DrawLayer.FOREGROUND, 0, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        return scrollBar.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double offsetX, double offsetY) {
        return scrollBar.mouseDragged(mouseX, mouseY, mouseButton, offsetX, offsetY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
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
                craftingPlan.getInstance(),
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
    public void updateNarration(NarrationElementOutput p_169152_) {

    }

    public static class Element {

        private final IPrototypedIngredient<?, ?> instance;
        private final long storageQuantity;
        private final long toCraftQuantity;
        private final long craftingQuantity;
        private final long missingQuantity;

        public Element(IPrototypedIngredient<?, ?> instance,
                       long storageQuantity, long toCraftQuantity, long craftingQuantity, long missingQuantity) {
            this.instance = instance;
            this.storageQuantity = storageQuantity;
            this.toCraftQuantity = toCraftQuantity;
            this.craftingQuantity = craftingQuantity;
            this.missingQuantity = missingQuantity;
        }

        public IPrototypedIngredient<?, ?> getInstance() {
            return instance;
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
                return Helpers.RGBAToInt(250, 10, 13, 150);
            }
            if (getCraftingQuantity() > 0) {
                return Helpers.RGBAToInt(43, 174, 231, 150);
            }
            if (getToCraftQuantity() > 0) {
                return Helpers.RGBAToInt(243, 245, 150, 150);
            }
            return Helpers.RGBAToInt(43, 231, 47, 150);
        }
    }

}
