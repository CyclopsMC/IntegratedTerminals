package org.cyclops.integratedterminals.client.gui.container;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
import org.cyclops.cyclopscore.client.gui.component.WidgetScrollBar;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetArrowedListField;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetTextFieldExtended;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.*;
import org.cyclops.cyclopscore.inventory.container.ContainerExtended;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageSlot;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.core.terminalstorage.button.TerminalButtonItemStackCraftingGridClear;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientItemStackCraftingGridBalance;
import org.cyclops.integratedterminals.proxy.ClientProxy;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author rubensworks
 */
public class ContainerScreenTerminalStorage<L, C extends ContainerTerminalStorageBase<L>> extends ContainerScreenExtended<C> {

    private static int TAB_OFFSET_X = 24;
    private static int TAB_WIDTH = 24;
    private static int TAB_UNSELECTED_HEIGHT = 21;
    private static int TAB_SELECTED_HEIGHT = 24;
    private static int TAB_ICON_OFFSET = 4;
    private static int TAB_UNSELECTED_TEXTURE_X = 118;
    private static int TAB_SELECTED_TEXTURE_X = 142;
    private static int TAB_UNSELECTED_TEXTURE_Y = 0;
    private static int TAB_SELECTED_TEXTURE_Y = 0;
    private static int SCROLL_Y = 40;

    private static int SEARCH_X = 103;
    private static int SEARCH_Y = 27;
    private static int SEARCH_HEIGHT = 20;

    private static int CHANNEL_X = 58;
    private static int CHANNEL_Y = 25;
    private static int CHANNEL_WIDTH = 42;
    private static int CHANNEL_HEIGHT = 15;

    private static int BUTTONS_OFFSET_X = 0;
    private static int BUTTONS_OFFSET_Y = 22;
    private static int BUTTONS_OFFSET = 4;

    private WidgetArrowedListField<String> fieldChannel;
    private WidgetScrollBar scrollBar;
    private WidgetTextFieldExtended fieldSearch;
    private ButtonImage buttonSetDefaults;
    private int firstRow;
    private boolean initialized;
    protected final Set<Slot> terminalDragSplittingSlots = Sets.<Slot>newHashSet();
    protected boolean terminalDragSplitting;
    private int terminalDragMode;
    private int terminalDragSplittingButton;
    private int terminalDragSplittingRemnant;
    private boolean clicked;

    public ContainerScreenTerminalStorage(C container, Inventory inventory, Component title) {
        super(container, inventory, title);
        container.screen = this;
    }

    @Override
    public void init() {
        clearWidgets();
        super.init();
        this.initialized = false;

        fieldChannel = new WidgetArrowedListField<String>(Minecraft.getInstance().font, leftPos + CHANNEL_X,
                topPos + CHANNEL_Y, CHANNEL_WIDTH, CHANNEL_HEIGHT, true,
                Component.translatable("gui.integratedterminals.channel"), true,
                getMenu().getChannelStrings());
        fieldChannel.setMaxLength(15);
        fieldChannel.setVisible(true);
        fieldChannel.setTextColor(16777215);
        fieldChannel.setCanLoseFocus(true);
        fieldChannel.setEditable(true);
        int activeChannel = getMenu().getSelectedChannel();
        if (activeChannel != IPositionedAddonsNetwork.WILDCARD_CHANNEL) {
            fieldChannel.setValue(Integer.toString(activeChannel));
        }

        firstRow = 0;
        scrollBar = new WidgetScrollBar(leftPos + getGridXSize() + 33, topPos + SCROLL_Y + 1, getScrollHeight() - 2,
                Component.empty(),
                firstRow -> this.firstRow = firstRow, 0) {
            @Override
            public int getTotalRows() {
                ContainerTerminalStorageBase container = getMenu();
                Optional<ITerminalStorageTabClient<?>> tabOptional = getSelectedClientTab();
                if (!tabOptional.isPresent()) {
                    return 0;
                }
                int totalSlots = tabOptional.get().getSlotCount(container.getSelectedChannel());
                return (int) Math.ceil((double) totalSlots / getSlotRowLength());
            }

            @Override
            public int getVisibleRows() {
                return getSlotVisibleRows();
            }
        };
        addWidget(this.scrollBar);

        fieldSearch = new WidgetTextFieldExtended(Minecraft.getInstance().font, leftPos + SEARCH_X,
                topPos + SEARCH_Y, getSearchWidth() - 10, SEARCH_HEIGHT, Component.translatable("gui.cyclopscore.search"));
        fieldSearch.setMaxLength(50);
        fieldSearch.setVisible(true);
        fieldSearch.setTextColor(16777215);
        fieldSearch.setCanLoseFocus(true);
        fieldSearch.setEditable(true);
        fieldSearch.setBordered(false);

        buttonSetDefaults = addRenderableWidget(new ButtonImage(this.leftPos + ITerminalStorageTabClient.DEFAULT_SLOT_OFFSET_X + (getGridXSize() / 2) + getPlayerInventoryOffsetX() + (9 * GuiHelpers.SLOT_SIZE / 2) + 27, this.topPos + getGridYSize() + getPlayerInventoryOffsetY() + 120, 15, 15,
                Component.translatable("gui.integratedterminals.terminal_storage.setdefaults"),
                createServerPressable(ContainerTerminalStorageBase.BUTTON_SET_DEFAULTS, b -> {}), true,
                Images.ANVIL, -2, -3));

        repositionInventorySlots();
    }

    public void repositionInventorySlots() {
        int gridXSize = getGridXSize();
        int gridYSize = getGridYSize();
        int playerInventoryOffsetX = getPlayerInventoryOffsetX();
        int playerInventoryOffsetY = getPlayerInventoryOffsetY();
        ITerminalStorageTabCommon.SlotPositionFactors factors = new ITerminalStorageTabCommon.SlotPositionFactors(offsetX, offsetY, gridXSize, gridYSize, playerInventoryOffsetX, playerInventoryOffsetY);

        // Reposition regular inventory slots
        for (int y = 0; y < 1; y++) {
            for (int x = 0; x < 9; x++) {
                Slot slot = this.container.getSlot(x + y * 9 + 0);
                ContainerExtended.setSlotPosX(slot, offsetX + ITerminalStorageTabClient.DEFAULT_SLOT_OFFSET_X - 1 + (gridXSize / 2) - (9 * GuiHelpers.SLOT_SIZE / 2) + playerInventoryOffsetX + 19 + x * GuiHelpers.SLOT_SIZE);
                ContainerExtended.setSlotPosY(slot, offsetY + 58 + 63 + gridYSize + playerInventoryOffsetY + y * GuiHelpers.SLOT_SIZE);
            }
        }
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                Slot slot = this.container.getSlot(x + y * 9 + 9);
                ContainerExtended.setSlotPosX(slot, offsetX + ITerminalStorageTabClient.DEFAULT_SLOT_OFFSET_X - 1 + (gridXSize / 2) - (9 * GuiHelpers.SLOT_SIZE / 2) + playerInventoryOffsetX + 19 + x * GuiHelpers.SLOT_SIZE);
                ContainerExtended.setSlotPosY(slot, offsetY + 63 + gridYSize + playerInventoryOffsetY + y * GuiHelpers.SLOT_SIZE);
            }
        }
        for (int y = 0; y < 4; y++) {
            Slot slot = this.container.getSlot(36 + y);
            ContainerExtended.setSlotPosX(slot, offsetX + ITerminalStorageTabClient.DEFAULT_SLOT_OFFSET_X - 1 + (gridXSize / 2) - (9 * GuiHelpers.SLOT_SIZE / 2) + playerInventoryOffsetX - 19 + (y % 2) * GuiHelpers.SLOT_SIZE);
            ContainerExtended.setSlotPosY(slot, offsetY + 63 + gridYSize + playerInventoryOffsetY + 9 + ((int) Math.floor(y / 2)) * GuiHelpers.SLOT_SIZE);
        }
        {
            Slot slot = this.container.getSlot(40);
            ContainerExtended.setSlotPosX(slot, offsetX + ITerminalStorageTabClient.DEFAULT_SLOT_OFFSET_X - 1 + (gridXSize / 2) - (9 * GuiHelpers.SLOT_SIZE / 2) + playerInventoryOffsetX - 10);
            ContainerExtended.setSlotPosY(slot, offsetY + 63 + gridYSize + playerInventoryOffsetY + 9 + 49);
        }



        // Reposition tab slots
        Optional<ITerminalStorageTabClient<?>> tabOptional = getSelectedClientTab();
        tabOptional.ifPresent(tab -> {
            String tabName = getMenu().getSelectedTab();
            Optional<ITerminalStorageTabCommon> tabCommonOptional = getCommonTab(tabName);
            tabCommonOptional.ifPresent(tabCommon -> {
                for (Pair<Slot, ITerminalStorageTabCommon.ISlotPositionCallback> slot : getMenu().getTabSlots(tabName)) {
                    Pair<Integer, Integer> slotPos = slot.getRight().getSlotPosition(factors);
                    ContainerExtended.setSlotPosX(slot.getLeft(), slotPos.getLeft());
                    ContainerExtended.setSlotPosY(slot.getLeft(), slotPos.getRight());
                }
            });
        });
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (!initialized && getSelectedClientTab().isPresent()) {
            initialized = true;
            fieldSearch.setValue(getSelectedClientTab().get().getInstanceFilter(getMenu().getSelectedChannel()));
        }
        fieldSearch.tick();
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/part_terminal_storage.png");
    }

    public int getGridXSize() {
        return getSlotRowLength() * GuiHelpers.SLOT_SIZE;
    }

    public int getGridYSize() {
        return getSlotVisibleRows() * GuiHelpers.SLOT_SIZE;
    }

    public int getScrollHeight() {
        return getGridYSize();
    }

    public int getSearchWidth() {
        return getBaseXSize() - 7 * GuiHelpers.SLOT_SIZE - 2;
    }

    @Override
    public int getBaseXSize() {
        return 56 + getGridXSize();
    }

    @Override
    public int getBaseYSize() {
        return 135 + getGridYSize() + getPlayerInventoryOffsetY() + 10;
    }

    protected int getPlayerInventoryOffsetX() {
        return getSelectedClientTab()
                .map(ITerminalStorageTabClient::getPlayerInventoryOffsetX)
                .orElse(0);
    }

    protected int getPlayerInventoryOffsetY() {
        return getSelectedClientTab()
                .map(ITerminalStorageTabClient::getPlayerInventoryOffsetY)
                .orElse(0);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float f, int mouseX, int mouseY) {
        //super.renderBg(matrixStack, f, mouseX, mouseY);
        RenderHelpers.bindTexture(texture);
        this.renderBgTab(matrixStack, f, mouseX, mouseY);
        this.renderBgPlayerInventory(matrixStack, f, mouseX, mouseY);

        fieldChannel.render(matrixStack, mouseX, mouseY, f);
        fieldSearch.render(matrixStack, mouseX, mouseY, f);
        drawTabsBackground(matrixStack);
        drawTabContents(matrixStack, getMenu().getSelectedTab(), getMenu().getSelectedChannel(), DrawLayer.BACKGROUND,
                f, getGuiLeftTotal() + getSlotsOffsetX(), getGuiTopTotal() + getSlotsOffsetY(), mouseX, mouseY);
        scrollBar.drawGuiContainerBackgroundLayer(matrixStack, f, mouseX, mouseY);

        Optional<ITerminalStorageTabClient<?>> tabOptional = getSelectedClientTab();
        tabOptional.ifPresent(tab -> {
            int offset = 0;
            int gridXSize = getGridXSize();
            int gridYSize = getGridYSize();
            int playerInventoryOffsetX = getPlayerInventoryOffsetX();
            int playerInventoryOffsetY = getPlayerInventoryOffsetY();
            ITerminalStorageTabCommon.SlotPositionFactors factors = new ITerminalStorageTabCommon.SlotPositionFactors(offsetX, offsetY, gridXSize, gridYSize, playerInventoryOffsetX, playerInventoryOffsetY);
            for (ITerminalButton button : tab.getButtons()) {
                Button guiButton = button.createButton(button.getX(leftPos, BUTTONS_OFFSET_X, gridXSize, gridYSize, playerInventoryOffsetX, playerInventoryOffsetY), button.getY(topPos, BUTTONS_OFFSET_Y + offset, gridXSize, gridYSize, playerInventoryOffsetX, playerInventoryOffsetY));
                guiButton.render(matrixStack, mouseX, mouseY, f);
                if (button.isInLeftColumn()) {
                    offset += BUTTONS_OFFSET + guiButton.getHeight();
                }
            }

            String tabName = getMenu().getSelectedTab();
            Optional<ITerminalStorageTabCommon> tabCommonOptional = getCommonTab(tabName);
            tabCommonOptional.ifPresent(tabCommon -> {
                for (Pair<Slot, ITerminalStorageTabCommon.ISlotPositionCallback> slot : getMenu().getTabSlots(tabName)) {
                    Pair<Integer, Integer> slotPos = slot.getRight().getSlotPosition(factors);
                    tab.onCommonSlotRender(this, matrixStack, DrawLayer.BACKGROUND,
                            0, leftPos + slotPos.getLeft(), topPos + slotPos.getRight(), mouseX, mouseY, slot.getLeft().index, tabCommon);
                }
            });
        });
    }

    protected void renderBgTab(PoseStack matrixStack, float f, int mouseX, int mouseY) {
        int tabWidth = getGridXSize() + 29;
        int tabHeight = getGridYSize() + 40;
        int offset = 21;
        int blitOffset = 0;
        int cornerSize = 7;
        int columns = getSlotRowLength();
        int rows = getSlotVisibleRows();

        // Corners
        //blit(matrixStack, leftPos + offsetX, topPos + offsetY, 0, 0, imageWidth - 2 * offsetX, imageHeight - 2 * offsetY); // top-left
        blit(matrixStack, leftPos + offset, topPos + offset, cornerSize, 0, cornerSize, cornerSize); // top-left
        blit(matrixStack, leftPos + offset + tabWidth - cornerSize, topPos + offset, 0, 0, cornerSize, cornerSize); // top-right
        blit(matrixStack, leftPos + offset + tabWidth - cornerSize, topPos + offset + tabHeight - cornerSize, cornerSize * 2, 0, cornerSize, cornerSize); // bottom-right
        blit(matrixStack, leftPos + offset, topPos + offset + tabHeight - cornerSize, cornerSize * 3, 0, cornerSize, cornerSize); // bottom-left

        // Sides
        blitRescalable(matrixStack, leftPos + offset + cornerSize, topPos + offset, blitOffset, cornerSize + 4, 0, 1, cornerSize, 256, 256, tabWidth - cornerSize * 2, cornerSize); // top
        blitRescalable(matrixStack, leftPos + offset + tabWidth - cornerSize, topPos + offset + cornerSize, blitOffset, 0, 4, cornerSize, 1, 256, 256, cornerSize, tabHeight - cornerSize * 2); // right
        blitRescalable(matrixStack, leftPos + offset + cornerSize, topPos + offset + tabHeight - cornerSize, blitOffset, 25, 0, 1, cornerSize, 256, 256, tabWidth - cornerSize * 2, cornerSize); // bottom
        blitRescalable(matrixStack, leftPos + offset, topPos + offset + cornerSize, blitOffset, cornerSize, 4, cornerSize, 1, 256, 256, cornerSize, tabHeight - cornerSize * 2); // left

        // Background
        blitRescalable(matrixStack, leftPos + offset + cornerSize, topPos + offset + cornerSize, blitOffset, 0, 3, 1, 1, 256, 256, tabWidth - cornerSize * 2, tabHeight - cornerSize * 2);

        // Slots
        for (int j = 0; j < rows; j++) {
            int renderRows = Math.min(3, rows - j); // Try rendering multiple rows for optimizing efficiency (if possible)
            for (int i = 0; i < columns; i++) {
                int renderColumns = Math.min(9, columns - i); // Try rendering multiple columns for optimizing efficiency (if possible)
                blit(matrixStack, leftPos + offset + 10 + i * GuiHelpers.SLOT_SIZE, topPos + offset + 18 + j * GuiHelpers.SLOT_SIZE, 80, 34, GuiHelpers.SLOT_SIZE * renderColumns, GuiHelpers.SLOT_SIZE * renderRows);
                i += renderColumns - 1;
            }
            j += renderRows - 1;
        }

        // Scrollbar background
        blit(matrixStack, leftPos + getGridXSize() + 32, topPos + SCROLL_Y - 1, 20, 12, 14, 1); // top
        blitRescalable(matrixStack, leftPos + getGridXSize() + 32, topPos + SCROLL_Y, blitOffset, 20, 13, 14, 1, 256, 256, 14, getScrollHeight() - 2); // middle
        blit(matrixStack, leftPos + getGridXSize() + 32, topPos + SCROLL_Y + getScrollHeight() - 2, 20, 101, 14, 1); // bottom

        // Textbox background
        blit(matrixStack, leftPos + SEARCH_X - 1, topPos + SEARCH_Y - 2, 28, 0, 1, SEARCH_HEIGHT - 8); // left
        blitRescalable(matrixStack, leftPos + SEARCH_X, topPos + SEARCH_Y - 2, blitOffset, 29, 0, 1, SEARCH_HEIGHT - 8, 256, 256, getSearchWidth(), SEARCH_HEIGHT - 8); // middle
        blit(matrixStack, leftPos + SEARCH_X + getSearchWidth() - 1, topPos + SEARCH_Y - 2, 117, 0, 1, SEARCH_HEIGHT - 8); // right

        // Render tab-specific things
        getSelectedClientTab().ifPresent(tab -> tab.onTabBackgroundRender(this, matrixStack, f, mouseX, mouseY));
    }

    public static void blitRescalable(PoseStack p_93144_, int p_93145_, int p_93146_, int p_93147_, float p_93148_, float p_93149_, int p_93150_, int p_93151_, int p_93152_, int p_93153_, int realWidth, int realHeight) {
        innerBlit(p_93144_, p_93145_, p_93145_ + realWidth, p_93146_, p_93146_ + realHeight, p_93147_, p_93150_, p_93151_, p_93148_, p_93149_, p_93152_, p_93153_);
    }

    protected void renderBgPlayerInventory(PoseStack matrixStack, float f, int mouseX, int mouseY) {
        // Render player inventory
        blit(matrixStack, leftPos + (getGridXSize() / 2) - (9 * GuiHelpers.SLOT_SIZE / 2) + getPlayerInventoryOffsetX() + 3, topPos + 52 + getGridYSize() + getPlayerInventoryOffsetY() , 34, 24, 216, 93);

        // Auxiliary slots
        blit(matrixStack, leftPos + (getGridXSize() / 2) + (9 * GuiHelpers.SLOT_SIZE / 2) + getPlayerInventoryOffsetX() + 57, topPos + 61 + getGridYSize() + getPlayerInventoryOffsetY(), 0, 12, 20, 57);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        drawTabsForeground(matrixStack, mouseX, mouseY);
        drawTabContents(matrixStack, getMenu().getSelectedTab(), getMenu().getSelectedChannel(), DrawLayer.FOREGROUND,
                0, getSlotsOffsetX(), getSlotsOffsetY(), mouseX, mouseY);
        RenderItemExtendedSlotCount.getInstance().blitOffset = 150.0F;
        drawActiveStorageSlotItem(matrixStack, mouseX, mouseY);
        RenderItemExtendedSlotCount.getInstance().blitOffset = 0F;

        // Draw button tooltips
        Optional<ITerminalStorageTabClient<?>> tabOptional = getSelectedClientTab();
        tabOptional.ifPresent(tab -> {
            int offset = 0;
            int gridXSize = getGridXSize();
            int gridYSize = getGridYSize();
            int playerInventoryOffsetX = getPlayerInventoryOffsetX();
            int playerInventoryOffsetY = getPlayerInventoryOffsetY();
            ITerminalStorageTabCommon.SlotPositionFactors factors = new ITerminalStorageTabCommon.SlotPositionFactors(offsetX, offsetY, gridXSize, gridYSize, playerInventoryOffsetX, playerInventoryOffsetY);
            for (ITerminalButton button : tab.getButtons()) {
                Button guiButton = button.createButton(button.getX(leftPos, BUTTONS_OFFSET_X, gridXSize, gridYSize, playerInventoryOffsetX, playerInventoryOffsetY), button.getY(topPos, BUTTONS_OFFSET_Y + offset, gridXSize, gridYSize, playerInventoryOffsetX, playerInventoryOffsetY));
                if (isHovering(button.getX(0, BUTTONS_OFFSET_X, gridXSize, gridYSize, playerInventoryOffsetX, playerInventoryOffsetY), button.getY(0, BUTTONS_OFFSET_Y + offset, gridXSize, gridYSize, playerInventoryOffsetX, playerInventoryOffsetY),
                        guiButton.getWidth(), guiButton.getHeight(), mouseX, mouseY)) {
                    List<Component> lines = Lists.newArrayList();
                    lines.add(Component.translatable(button.getTranslationKey()));
                    button.getTooltip(getMinecraft().player, TooltipFlag.Default.NORMAL, lines);
                    drawTooltip(lines, matrixStack, mouseX - leftPos, mouseY - topPos);
                }
                if (button.isInLeftColumn()) {
                    offset += BUTTONS_OFFSET + guiButton.getHeight();
                }
            }

            String tabName = getMenu().getSelectedTab();
            Optional<ITerminalStorageTabCommon> tabCommonOptional = getCommonTab(tabName);
            tabCommonOptional.ifPresent(tabCommon -> {
                for (Pair<Slot, ITerminalStorageTabCommon.ISlotPositionCallback> slot : getMenu().getTabSlots(tabName)) {
                    Pair<Integer, Integer> slotPos = slot.getRight().getSlotPosition(factors);
                    tab.onCommonSlotRender(this, matrixStack, DrawLayer.FOREGROUND,
                            0, leftPos + slotPos.getLeft(), topPos + slotPos.getRight(), mouseX, mouseY, slot.getLeft().index, tabCommon);
                }
            });
        });

        // Draw save defaults button
        if (buttonSetDefaults.isHoveredOrFocused()) {
            List<Component> lines = Lists.newArrayList();
            lines.add(Component.translatable("gui.integratedterminals.terminal_storage.setdefaults"));
            lines.add(Component.translatable("gui.integratedterminals.terminal_storage.setdefaults.info")
                .withStyle(ChatFormatting.GRAY));
            drawTooltip(lines, matrixStack, mouseX - leftPos, mouseY - topPos);
        }
    }

    @Override
    protected void drawCurrentScreen(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        scrollBar.render(matrixStack, mouseX, mouseY, partialTicks);

        ResourceLocation oldTexture = this.texture;
        getSelectedClientTab().ifPresent(tab -> {
            ResourceLocation texture = tab.getBackgroundTexture();
            if (texture != null) {
                this.texture = texture;
            }
        });

        super.drawCurrentScreen(matrixStack, mouseX, mouseY, partialTicks);

        // Draw slots
        GlStateManager._disableDepthTest();
        //this.zLevel = 0F;
        for (int i1 = 0; i1 < getMenu().slots.size(); ++i1) {
            Slot slot = getMenu().slots.get(i1);

            if (slot.isActive()) {
                this.drawSlotOverlay(matrixStack, slot);
            }
        }
        //this.zLevel = 0F;

        this.texture = oldTexture;
    }

    private void drawSlotOverlay(PoseStack matrixStack, Slot slot) {
        getSelectedClientTab().ifPresent(tab -> {
            if (this.terminalDragSplitting && this.terminalDragSplittingSlots.contains(slot)) {
                if (tab.isSlotValidForDraggingInto(getMenu().getSelectedChannel(), slot)) {
                    if (this.terminalDragSplittingSlots.size() == 1) {
                        return;
                    }

                    int dragQuantity = tab.computeDraggingQuantity(this.terminalDragSplittingSlots, this.terminalDragMode, slot.getItem(), tab.getActiveSlotQuantity());
                    if (dragQuantity > 0) {
                        String dragString = "+" + GuiHelpers.quantityToScaledString(dragQuantity);
                        RenderHelpers.drawScaledStringWithShadow(matrixStack, font, dragString, leftPos + slot.x, topPos + slot.y, 0.5F, 16777045);
                    }
                } else {
                    this.terminalDragSplittingSlots.remove(slot);
                    this.updateTerminalDragSplitting(tab);
                }
            }
        });
    }

    @Override
    public C getMenu() {
        return super.getMenu();
    }

    protected Optional<ITerminalStorageTabClient<?>> getTabByIndex(int tabIndex) {
        Collection<ITerminalStorageTabClient<?>> tabsClientList = getMenu().getTabsClient().values();
        if (tabIndex >= 0 && tabIndex < tabsClientList.size()) {
            return Optional.of(Iterables.get(tabsClientList, tabIndex));
        }
        return Optional.empty();
    }

    protected void setTabByIndex(int tabIndex) {
        // Save tab index
        getTabByIndex(tabIndex).ifPresent(tab -> {
            getMenu().setSelectedTab(tab.getName().toString());

            // Reset active slot
            tab.resetActiveSlot();

            // Update the filter
            fieldSearch.setValue(tab.getInstanceFilter(getMenu().getSelectedChannel()));
        });

        // Reset scrollbar
        scrollBar.scrollTo(0);

        // Re-init screen, as scale might be different in the new tab
        init();
    }

    protected void playButtonClickSound() {
        this.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        Optional<ITerminalStorageTabClient<?>> tabOptional = getSelectedClientTab();
        this.clicked = true;

        // Select a tab
        if (mouseButton == 0
                && mouseY < getGuiTop() + TAB_UNSELECTED_HEIGHT
                && mouseX > getGuiLeft() + TAB_OFFSET_X
                && mouseX <= getGuiLeft() + TAB_OFFSET_X + (TAB_WIDTH * getMenu().getTabsClientCount() - 1)) {
            // Save tab index
            setTabByIndex((int) ((mouseX - TAB_OFFSET_X - getGuiLeft()) / TAB_WIDTH));
            playButtonClickSound();

            return true;
        }

        // Update channel when changing channel field
        if (this.fieldChannel.mouseClicked(mouseX, mouseY, mouseButton)) {
            int channel;
            try {
                channel = Integer.parseInt(this.fieldChannel.getActiveElement());
            } catch (NumberFormatException e) {
                channel = -1;
            }
            final int finalChannel = channel;
            getMenu().setSelectedChannel(channel);
            scrollBar.scrollTo(0); // Reset scrollbar

            // Update the filter
            tabOptional.ifPresent(tab -> fieldSearch.setValue(tab.getInstanceFilter(finalChannel)));

            playButtonClickSound();

            return true;
        }

        if (tabOptional.isPresent()) {
            ITerminalStorageTabClient<?> tab = tabOptional.get();
            Slot playerSlot = getSlotUnderMouse();

            // Start dragging over container slots when a storage slot is selected
            if (tab.getActiveSlotId() >= 0
                    && (mouseButton == 0 || mouseButton == 1 || this.getMinecraft().options.keyPickItem.getKey().getValue() == mouseButton - 100)) {
                if (playerSlot != null && !this.terminalDragSplitting) {
                    this.terminalDragSplitting = true;
                    this.terminalDragSplittingButton = mouseButton;
                    this.terminalDragSplittingSlots.clear();

                    if (mouseButton == 0) {
                        this.terminalDragMode = 0;
                    } else if (mouseButton == 1) {
                        this.terminalDragMode = 1;
                    } else if (this.getMinecraft().options.keyPickItem.getKey().getValue() == mouseButton - 100) {
                        this.terminalDragMode = 2;
                    }
                    return true;
                }
            }
            if(MinecraftHelpers.isShifted() && playerSlot != null && tab.isQuickMovePrevented(playerSlot)) {
                return true;
            }
        } else if (getSlotUnderMouse() != null) {
            // Don't allow shift clicking items into container when no tab has been selected
            return false;
        }

        // Click in search field
        fieldSearch.mouseClicked(mouseX, mouseY, mouseButton);

        // Handle buttons clicks
        tabOptional.ifPresent(tab -> {
            int offset = 0;
            ITerminalStorageTabCommon tabCommon = getMenu().getTabCommon(tab.getName().toString());
            int gridXSize = getGridXSize();
            int gridYSize = getGridYSize();
            int playerInventoryOffsetX = getPlayerInventoryOffsetX();
            int playerInventoryOffsetY = getPlayerInventoryOffsetY();
            for (ITerminalButton button : tab.getButtons()) {
                Button guiButton = button.createButton(button.getX(leftPos, BUTTONS_OFFSET_X, gridXSize, gridYSize, playerInventoryOffsetX, playerInventoryOffsetY), button.getY(topPos, BUTTONS_OFFSET_Y + offset, gridXSize, gridYSize, playerInventoryOffsetX, playerInventoryOffsetY));
                if (isHovering(button.getX(0, BUTTONS_OFFSET_X, gridXSize, gridYSize, playerInventoryOffsetX, playerInventoryOffsetY), button.getY(0, BUTTONS_OFFSET_Y + offset, gridXSize, gridYSize, playerInventoryOffsetX, playerInventoryOffsetY), guiButton.getWidth(), guiButton.getHeight(), mouseX, mouseY)) {
                    button.onClick(tab, tabCommon, guiButton, getMenu().getSelectedChannel(), mouseButton);
                    this.clicked = false; // To avoid grid slots being selected on mouse release
                    playButtonClickSound();
                    return;
                }
                if (button.isInLeftColumn()) {
                    offset += BUTTONS_OFFSET + guiButton.getHeight();
                }
            }
        });

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Nullable
    @Override
    public Slot getSlotUnderMouse() {
        Slot slot = super.getSlotUnderMouse();
        // Safety for hacky disabled slots
        if (slot != null && slot.x < 0) {
            return null;
        }
        return slot;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double mouseXPrev, double mouseYPrev) {
        if (getSelectedClientTab().map(tab -> {
            if (this.terminalDragSplitting & tab.getActiveSlotId() >= 0) {
                Slot slot = this.getSlotUnderMouse();
                if (slot != null
                        && (tab.getActiveSlotQuantity() > this.terminalDragSplittingSlots.size() || this.terminalDragMode == 2)
                        && tab.isSlotValidForDraggingInto(getMenu().getSelectedChannel(), slot)) {
                    this.terminalDragSplittingSlots.add(slot);
                    this.updateTerminalDragSplitting(tab);
                    return true;
                }
            }
            return false;
        }).orElse(false)) {
            return true;
        }
        return this.getFocused() != null && this.isDragging() && mouseButton == 0 && this.getFocused().mouseDragged(mouseX, mouseY, mouseButton, mouseXPrev, mouseYPrev) ? true : super.mouseDragged(mouseX, mouseY, mouseButton, mouseXPrev, mouseYPrev);
    }

    private void updateTerminalDragSplitting(ITerminalStorageTabClient<?> tab) {
        if (this.terminalDragSplitting) {
            int quantityTotal = tab.getActiveSlotQuantity();
            this.terminalDragSplittingRemnant = tab.getActiveSlotQuantity();

            for (Slot slot : this.terminalDragSplittingSlots) {
                if (tab.isSlotValidForDraggingInto(getMenu().getSelectedChannel(), slot)) {
                    int dragQuantity = tab.computeDraggingQuantity(this.terminalDragSplittingSlots, this.terminalDragMode, slot.getItem(), quantityTotal);
                    this.terminalDragSplittingRemnant -= tab.dragIntoSlot(container, getMenu().getSelectedChannel(), slot, dragQuantity, true);
                }
            }
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        // Validate dragging process
        if (this.terminalDragSplitting
                && (this.terminalDragSplittingSlots.size() <= 1 || this.terminalDragSplittingButton != mouseButton)) {
            this.terminalDragSplitting = false;
            this.terminalDragSplittingSlots.clear();
            if (this.terminalDragSplittingButton != mouseButton) {
                return true;
            }
        }

        // Handle dragging ends
        boolean dragged = false;
        if (this.terminalDragSplitting) {
            dragged = true;
            // If we were dragging, distribute the dragging instance over the dragged slots.
            getSelectedClientTab().ifPresent(tab -> {
                if (tab.getActiveSlotQuantity() > 0) {
                    int quantityTotal = tab.getActiveSlotQuantity();
                    int quantity = quantityTotal;
                    for (Slot slot : this.terminalDragSplittingSlots) {
                        if (tab.isSlotValidForDraggingInto(getMenu().getSelectedChannel(), slot)) {
                            int dragQuantity = tab.computeDraggingQuantity(this.terminalDragSplittingSlots, this.terminalDragMode, slot.getItem(), quantityTotal);
                            quantity -= tab.dragIntoSlot(container, getMenu().getSelectedChannel(), slot, dragQuantity, false);
                        }
                    }
                    tab.setActiveSlotQuantity(quantity);
                }
            });
        }

        // Reset dragging state
        this.terminalDragSplitting = false;
        this.terminalDragSplittingSlots.clear();
        this.terminalDragSplittingButton = -1;
        this.terminalDragMode = -1;
        this.terminalDragSplittingRemnant = 0;

        // Handle plain clicks
        if (!dragged && this.clicked) {
            this.clicked = false;
            Optional<ITerminalStorageTabClient<?>> tabOptional = getSelectedClientTab();
            if (tabOptional.isPresent()) {
                int slot = getStorageSlotIndexAtPosition(mouseX, mouseY);
                Slot playerSlot = getSlotUnderMouse();

                // Handle clicks on storage slots
                boolean hasClickedOutside = this.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, mouseButton);
                boolean hasClickedInStorage = this.hasClickedInStorage(mouseX, mouseY);
                if (tabOptional.get().handleClick(getMenu(), getMenu().getSelectedChannel(), slot, mouseButton,
                        hasClickedOutside, hasClickedInStorage, playerSlot != null ? playerSlot.index : -1, false)) {
                    return true;
                }
            }
        }

        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Handle scrolls
        Optional<ITerminalStorageTabClient<?>> tabOptional = getSelectedClientTab();
        if (tabOptional.isPresent()) {
            int slot = getStorageSlotIndexAtPosition(mouseX, mouseY);
            Slot playerSlot = getSlotUnderMouse();

            // Handle clicks on storage slots
            boolean hasClickedOutside = this.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, 0);
            boolean hasClickedInStorage = this.hasClickedInStorage(mouseX, mouseY);
            if (tabOptional.get().handleScroll(getMenu(), getMenu().getSelectedChannel(), slot, delta,
                    hasClickedOutside, hasClickedInStorage, playerSlot != null ? playerSlot.index : -1)) {
                return true;
            }
        }

        return this.getChildAt(mouseX, mouseY).filter((listener) -> {
            return listener.mouseScrolled(mouseX, mouseY, delta);
        }).isPresent();
    }

    protected boolean handleKeyCodeFirst(int keyCode, int scanCode) {
        InputConstants.Key inputCode = InputConstants.getKey(keyCode, scanCode);
        if (org.cyclops.integrateddynamics.proxy.ClientProxy.FOCUS_LP_SEARCH.isActiveAndMatches(inputCode)) {
            fieldSearch.changeFocus(true);
            return true;
        } else if (ClientProxy.TERMINAL_TAB_NEXT.isActiveAndMatches(inputCode)) {
            if (getMenu().getTabsClientCount() > 0) {
                // Go to next tab
                setTabByIndex((getSelectedClientTabIndex() + 1) % getMenu().getTabsClientCount());
                playButtonClickSound();
                return true;
            }
        } else if (ClientProxy.TERMINAL_TAB_PREVIOUS.isActiveAndMatches(inputCode)) {
            if (getMenu().getTabsClientCount() > 0) {
                // Go to previous tab
                setTabByIndex((getMenu().getTabsClientCount() + getSelectedClientTabIndex() - 1) % getMenu().getTabsClientCount());
                playButtonClickSound();
                return true;
            }
        }
        return false;
    }

    protected boolean handleKeyCodeLast(int keyCode, int scanCode) {
        InputConstants.Key inputCode = InputConstants.getKey(keyCode, scanCode);
        if (ClientProxy.TERMINAL_CRAFTINGGRID_CLEARPLAYER.isActiveAndMatches(inputCode)) {
            clearCraftingGrid(false);
            playButtonClickSound();
            return true;
        } else if (ClientProxy.TERMINAL_CRAFTINGGRID_CLEARSTORAGE.isActiveAndMatches(inputCode)) {
            clearCraftingGrid(true);
            playButtonClickSound();
            return true;
        } else if (ClientProxy.TERMINAL_CRAFTINGGRID_BALANCE.isActiveAndMatches(inputCode)) {
            balanceCraftingGrid();
            playButtonClickSound();
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char keyCode, int scanCode) {
        if (handleKeyCodeFirst(keyCode, scanCode)) {
            return true;
        }
        if (fieldSearch.isFocused()) {
            if (fieldSearch.charTyped(keyCode, scanCode)) {
                getSelectedClientTab()
                        .ifPresent(tab -> tab.setInstanceFilter(getMenu().getSelectedChannel(), fieldSearch.getValue()));
            }
            return true;
        }
        return handleKeyCodeLast(keyCode, scanCode) || super.charTyped(keyCode, scanCode);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
            if (handleKeyCodeFirst(keyCode, scanCode)) {
                return true;
            }
            if (fieldSearch.isFocused()) {
                if (this.fieldSearch.keyPressed(keyCode, scanCode, modifiers)) {
                    getSelectedClientTab()
                            .ifPresent(tab -> tab.setInstanceFilter(getMenu().getSelectedChannel(), fieldSearch.getValue()));
                }
                return true;
            }
            if (handleKeyCodeLast(keyCode, scanCode)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    protected void clearCraftingGrid(boolean toStorage) {
        ITerminalStorageTabCommon commonTab = getMenu().getTabCommon(getMenu().getSelectedTab());
        if (commonTab instanceof TerminalStorageTabIngredientComponentItemStackCraftingCommon){
            TerminalButtonItemStackCraftingGridClear.clearGrid(
                    (TerminalStorageTabIngredientComponentItemStackCraftingCommon) commonTab,
                    getMenu().getSelectedChannel(), toStorage);
        }
    }

    protected void balanceCraftingGrid() {
        ITerminalStorageTabCommon commonTab = getMenu().getTabCommon(getMenu().getSelectedTab());
        if (commonTab instanceof TerminalStorageTabIngredientComponentItemStackCraftingCommon){
            IntegratedTerminals._instance.getPacketHandler().sendToServer(
                    new TerminalStorageIngredientItemStackCraftingGridBalance(commonTab.getName().toString()));
        }
    }

    private boolean hasClickedInStorage(double mouseX, double mouseY) {
        return mouseX >= getGuiLeftTotal() + getSlotsOffsetX()
                && mouseX < getGuiLeftTotal() + getSlotsOffsetX() + getSlotRowLength() * GuiHelpers.SLOT_SIZE - 1
                && mouseY >= getGuiTopTotal() + getSlotsOffsetY()
                && mouseY < getGuiTopTotal() + getSlotsOffsetY() + getSlotVisibleRows() * GuiHelpers.SLOT_SIZE;
    }

    public int getStorageSlotIndexAtPosition(double mouseX, double mouseY) {
        if (hasClickedInStorage(mouseX, mouseY)) {
            if ((mouseX - getGuiLeftTotal() - getSlotsOffsetX()) % GuiHelpers.SLOT_SIZE < GuiHelpers.SLOT_SIZE_INNER
                    && (mouseY - getGuiTopTotal() - getSlotsOffsetY()) % GuiHelpers.SLOT_SIZE < GuiHelpers.SLOT_SIZE_INNER) {
                int rowLength = getSlotRowLength();
                int offset = getSelectedFirstRow() * rowLength;
                return offset + ((((int) mouseX) - getGuiLeftTotal() - getSlotsOffsetX()) / GuiHelpers.SLOT_SIZE)
                        + ((((int) mouseY) - getGuiTopTotal() - getSlotsOffsetY()) / GuiHelpers.SLOT_SIZE) * getSlotRowLength();
            }
        }

        return -1;
    }

    /**
     * Returns the rectangle that a storage slot occupies even if the slot is not visible.
     * Use {@link ContainerScreenTerminalStorage#getStorageSlotIndexAtPosition(double, double)} to get a currently visible slot.
     * @param slotIndex
     * @return {@link Rect2i} of the slot.
     */
    public Rect2i getStorageSlotRect(int slotIndex) {
        int rowLength = getSlotRowLength();
        int offset = getSelectedFirstRow() * rowLength;
        // Skip slots that are not visible due to scroll bar
        int visibleIndex = slotIndex - offset;

        int xIndex = visibleIndex % rowLength;
        int yIndex = visibleIndex / rowLength;
        // +1 because slots have a 1 pixel border
        int x = getGuiLeftTotal() + getSlotsOffsetX() + xIndex * GuiHelpers.SLOT_SIZE + 1;
        int y = getGuiTopTotal() + getSlotsOffsetY() + yIndex * GuiHelpers.SLOT_SIZE + 1;

        Rect2i slotRect = new Rect2i(x, y, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER);

        return slotRect;
    }

    protected void drawTabsBackground(PoseStack matrixStack) {
        int offsetX = TAB_OFFSET_X;

        // Draw channels label
        drawString(matrixStack, font, L10NHelpers.localize("gui.integratedterminals.terminal_storage.channel"), getGuiLeft() + 30, getGuiTop() + 26, 16777215);

        // Draw all tabs next to each other horizontally
        for (ITerminalStorageTabClient tab : getMenu().getTabsClient().values()) {
            boolean selected = tab.getName().toString().equals(getMenu().getSelectedTab());
            int x = getGuiLeft() + offsetX;
            int y = getGuiTop();
            int width = TAB_WIDTH;
            int height = selected ? TAB_SELECTED_HEIGHT : TAB_UNSELECTED_HEIGHT;
            int textureX = selected ? TAB_SELECTED_TEXTURE_X : TAB_UNSELECTED_TEXTURE_X;
            int textureY = selected ? TAB_SELECTED_TEXTURE_Y : TAB_UNSELECTED_TEXTURE_Y;

            // Draw background
            RenderHelpers.bindTexture(this.texture);
            this.blit(matrixStack, x, y, textureX, textureY, width, height);

            // Draw icon
            ItemStack icon = tab.getIcon();
            ItemRenderer renderItem = Minecraft.getInstance().getItemRenderer();
            matrixStack.pushPose();
            GlStateManager._enableBlend();
            GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            Lighting.setupFor3DItems();
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            renderItem.renderAndDecorateItem(icon, x + TAB_ICON_OFFSET, y + TAB_ICON_OFFSET);
            Lighting.setupForFlatItems();
            matrixStack.popPose();
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            offsetX += width;
        }
    }

    protected int getSlotsOffsetX() {
        return getSelectedClientTab()
                .map(ITerminalStorageTabClient::getSlotOffsetX)
                .orElse(ITerminalStorageTabClient.DEFAULT_SLOT_OFFSET_X);
    }

    protected int getSlotsOffsetY() {
        return getSelectedClientTab()
                .map(ITerminalStorageTabClient::getSlotOffsetY)
                .orElse(ITerminalStorageTabClient.DEFAULT_SLOT_OFFSET_Y);
    }

    protected int getSlotVisibleRows() {
        return getSelectedClientTab()
                .map(ITerminalStorageTabClient::getSlotVisibleRows)
                .orElse(ITerminalStorageTabClient.DEFAULT_SLOT_VISIBLE_ROWS);
    }

    protected int getSlotRowLength() {
        return getSelectedClientTab()
                .map(ITerminalStorageTabClient::getSlotRowLength)
                .orElse(ITerminalStorageTabClient.DEFAULT_SLOT_ROW_LENGTH);
    }

    protected int getSelectedFirstRow() {
        return firstRow;
    }

    protected void drawTabContents(PoseStack matrixStack, String tabId, int channel, DrawLayer layer,
                                   float partialTick, int x, int y, int mouseX, int mouseY) {
        Optional<ITerminalStorageTabClient<?>> optionalTab = getClientTab(tabId);
        if (optionalTab.isPresent()) {
            ITerminalStorageTabClient<?> tab = optionalTab.get();
            // Draw status string
            if (layer == DrawLayer.BACKGROUND) {
                drawCenteredString(matrixStack, font, tab.getStatus(channel), leftPos + ITerminalStorageTabClient.DEFAULT_SLOT_OFFSET_X + (GuiHelpers.SLOT_SIZE * tab.getRowColumnProvider().getRowsAndColumns().columns()) / 2,
                        y + 2 + getSlotVisibleRows() * GuiHelpers.SLOT_SIZE, 16777215);
                RenderSystem.setShaderColor(1, 1, 1, 1);
            }

            // Draw slots
            int rowLength = getSlotRowLength();
            int limit = getSlotVisibleRows() * rowLength;
            int offset = getSelectedFirstRow() * rowLength;
            List<ITerminalStorageSlot> slots = (List<ITerminalStorageSlot>) tab.getSlots(channel, offset, limit);
            int slotX = x;
            int slotY = y;
            int slotI = 0;
            for (ITerminalStorageSlot slot : slots) {
                if (layer == DrawLayer.BACKGROUND) {
                    // highlight slot on hover
                    RenderHelpers.bindTexture(this.texture);
                    if (RenderHelpers.isPointInRegion(slotX, slotY, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER, mouseX, mouseY)) {
                        fill(matrixStack, slotX, slotY, slotX + GuiHelpers.SLOT_SIZE_INNER, slotY + GuiHelpers.SLOT_SIZE_INNER, -2130706433);
                    }
                }

                //this.zLevel = 200F;
                slot.drawGuiContainerLayer(this, matrixStack, layer, partialTick, slotX, slotY, mouseX, mouseY, tab, channel, null);
                //this.zLevel = 0F;

                if (++slotI >= rowLength) {
                    slotX = x;
                    slotY += GuiHelpers.SLOT_SIZE;
                    slotI = 0;
                } else {
                    slotX += GuiHelpers.SLOT_SIZE;
                }
            }
        } else {
            RenderSystem.setShaderColor(0.3F, 0.3F, 0.3F, 0.3F);
            fill(matrixStack, x - 1, y - 1, x - 1 + GuiHelpers.SLOT_SIZE * getSlotRowLength(), y - 1 + GuiHelpers.SLOT_SIZE * getSlotVisibleRows(), Helpers.RGBAToInt(50, 50, 50, 100));
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
    }

    private void drawActiveStorageSlotItem(PoseStack matrixStack, int mouseX, int mouseY) {
        Optional<ITerminalStorageTabClient<?>> optionalTab = getSelectedClientTab();
        optionalTab.ifPresent(tab -> {
            int slotId = tab.getActiveSlotId();
            if (slotId >= 0) {
                int quantity = tab.getActiveSlotQuantity();
                List<?> slots = tab.getSlots(getMenu().getSelectedChannel(), slotId, 1);
                if (!slots.isEmpty()) {
                    ITerminalStorageSlot slot = (ITerminalStorageSlot) slots.get(0);
                    RenderHelpers.bindTexture(this.texture);
                    RenderSystem.setShaderColor(1, 1, 1, 1);

                    if (this.terminalDragSplitting && this.terminalDragSplittingSlots.size() > 1) {
                        quantity = this.terminalDragSplittingRemnant;
                    }

                    String quantityString = GuiHelpers.quantityToScaledString(quantity);
                    if (quantity == 0) {
                        quantityString = ChatFormatting.YELLOW + quantityString;
                    }

                    slot.drawGuiContainerLayer(this, matrixStack, DrawLayer.BACKGROUND,
                            0, mouseX - this.leftPos - GuiHelpers.SLOT_SIZE_INNER / 4,
                            mouseY - this.topPos - GuiHelpers.SLOT_SIZE_INNER / 4, mouseX, mouseY, tab, getMenu().getSelectedChannel(), quantityString);
                }
            }
        });
    }

    protected Optional<ITerminalStorageTabClient<?>> getClientTab(String tab) {
        return Optional.ofNullable(getMenu().getTabsClient().get(tab));
    }

    protected Optional<ITerminalStorageTabCommon> getCommonTab(String tab) {
        return Optional.ofNullable(getMenu().getTabsCommon().get(tab));
    }

    public Optional<ITerminalStorageTabClient<?>> getSelectedClientTab() {
        return getClientTab(getMenu().getSelectedTab());
    }

    protected int getSelectedClientTabIndex() {
        Optional<ITerminalStorageTabClient<?>> selectedTab = getSelectedClientTab();
        if (selectedTab.isPresent()) {
            int tabIndex = 0;
            for (ITerminalStorageTabClient<?> tabClient : getMenu().getTabsClient().values()) {
                if (tabClient == selectedTab.get()) {
                    return tabIndex;
                }
                tabIndex++;
            }
        }
        return -1;
    }

    protected void drawTabsForeground(PoseStack poseStack, int mouseX, int mouseY) {
        if (mouseY < getGuiTop() + TAB_UNSELECTED_HEIGHT
                && mouseX > getGuiLeft() + TAB_OFFSET_X
                && mouseX <= getGuiLeft() + TAB_OFFSET_X + (TAB_WIDTH * getMenu().getTabsClientCount() - 1)) {
            int tabIndex = (mouseX - TAB_OFFSET_X - getGuiLeft()) / TAB_WIDTH;
            getTabByIndex(tabIndex)
                    .ifPresent(tab -> this.drawTooltip(tab.getTooltip(), poseStack, mouseX - getGuiLeft(), mouseY - getGuiTop()));
        }
    }

    public WidgetTextFieldExtended getFieldSearch() {
        return fieldSearch;
    }

    /**
     * The layer to draw on.
     */
    public static enum DrawLayer {
        BACKGROUND,
        FOREGROUND
    }
}
