package org.cyclops.integratedterminals.client.gui.container;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
import org.cyclops.cyclopscore.client.gui.component.WidgetScrollBar;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetArrowedListField;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetTextFieldExtended;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageSlot;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.core.terminalstorage.button.TerminalButtonItemStackCraftingGridClear;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;
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
public class ContainerScreenTerminalStorage extends ContainerScreenExtended<ContainerTerminalStorage> {

    private static int TAB_OFFSET_X = 24;
    private static int TAB_WIDTH = 24;
    private static int TAB_UNSELECTED_HEIGHT = 21;
    private static int TAB_SELECTED_HEIGHT = 24;
    private static int TAB_ICON_OFFSET = 4;
    private static int TAB_UNSELECTED_TEXTURE_X = 0;
    private static int TAB_SELECTED_TEXTURE_X = 24;
    private static int TAB_UNSELECTED_TEXTURE_Y = 225;
    private static int TAB_SELECTED_TEXTURE_Y = 225;
    private static int SCROLL_X = 198;
    private static int SCROLL_Y = 39;
    private static int SCROLL_HEIGHT = 88;

    private static int SEARCH_X = 104;
    private static int SEARCH_Y = 27;
    private static int SEARCH_WIDTH = 80;
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
    private int firstRow;
    private boolean initialized;
    protected final Set<Slot> terminalDragSplittingSlots = Sets.<Slot>newHashSet();
    protected boolean terminalDragSplitting;
    private int terminalDragMode;
    private int terminalDragSplittingButton;
    private int terminalDragSplittingRemnant;
    private boolean clicked;

    public ContainerScreenTerminalStorage(ContainerTerminalStorage container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
    }

    @Override
    public void init() {
        super.init();

        fieldChannel = new WidgetArrowedListField<String>(Minecraft.getInstance().fontRenderer, guiLeft + CHANNEL_X,
                guiTop + CHANNEL_Y, CHANNEL_WIDTH, CHANNEL_HEIGHT, true,
                L10NHelpers.localize("gui.integratedterminals.channel"), true,
                getContainer().getChannelStrings());
        fieldChannel.setMaxStringLength(15);
        fieldChannel.setVisible(true);
        fieldChannel.setTextColor(16777215);
        fieldChannel.setCanLoseFocus(true);
        fieldChannel.setEnabled(true);
        int activeChannel = getContainer().getSelectedChannel();
        if (activeChannel != IPositionedAddonsNetwork.WILDCARD_CHANNEL) {
            fieldChannel.setText(Integer.toString(activeChannel));
        }

        scrollBar = new WidgetScrollBar(guiLeft + SCROLL_X, guiTop + SCROLL_Y, SCROLL_HEIGHT,
                L10NHelpers.localize("gui.cyclopscore.scrollbar"),
                firstRow -> this.firstRow = firstRow, 0) {
            @Override
            public int getTotalRows() {
                ContainerTerminalStorage container = getContainer();
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

        fieldSearch = new WidgetTextFieldExtended(Minecraft.getInstance().fontRenderer, guiLeft + SEARCH_X,
                guiTop + SEARCH_Y, SEARCH_WIDTH, SEARCH_HEIGHT, L10NHelpers.localize("gui.cyclopscore.search"));
        fieldSearch.setMaxStringLength(50);
        fieldSearch.setVisible(true);
        fieldSearch.setTextColor(16777215);
        fieldSearch.setCanLoseFocus(true);
        fieldSearch.setEnabled(true);
        fieldSearch.setEnableBackgroundDrawing(false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!initialized && getSelectedClientTab().isPresent()) {
            initialized = true;

            fieldSearch.setText(getSelectedClientTab().get().getInstanceFilter(getContainer().getSelectedChannel()));
        }
        fieldSearch.tick();
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/part_terminal_storage.png");
    }

    @Override
    public int getBaseXSize() {
        return 218;
    }

    @Override
    public int getBaseYSize() {
        return 225;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
        fieldChannel.render(mouseX, mouseY, f);
        fieldSearch.render(mouseX, mouseY, f);
        drawTabsBackground();
        drawTabContents(getContainer().getSelectedTab(), getContainer().getSelectedChannel(), DrawLayer.BACKGROUND,
                f, getGuiLeftTotal() + getSlotsOffsetX(), getGuiTopTotal() + getSlotsOffsetY(), mouseX, mouseY);
        scrollBar.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);

        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.disableLighting();
        Optional<ITerminalStorageTabClient<?>> tabOptional = getSelectedClientTab();
        tabOptional.ifPresent(tab -> {
            int offset = 0;
            for (ITerminalButton button : tab.getButtons()) {
                Button guiButton = button.createButton(button.getX(guiLeft, BUTTONS_OFFSET_X), button.getY(guiTop, BUTTONS_OFFSET_Y + offset));
                guiButton.render(mouseX, mouseY, f);
                if (button.isInLeftColumn()) {
                    offset += BUTTONS_OFFSET + guiButton.getHeight();
                }
            }

            String tabName = getContainer().getSelectedTab();
            Optional<ITerminalStorageTabCommon> tabCommonOptional = getCommonTab(tabName);
            tabCommonOptional.ifPresent(tabCommon -> {
                for (Triple<Slot, Integer, Integer> slot : getContainer().getTabSlots(tabName)) {
                    tab.onCommonSlotRender(this, DrawLayer.BACKGROUND, 0,
                            guiLeft + slot.getMiddle(), guiTop + slot.getRight(), mouseX, mouseY, slot.getLeft().slotNumber, tabCommon);
                }
            });
        });
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        drawTabsForeground(mouseX, mouseY);
        drawTabContents(getContainer().getSelectedTab(), getContainer().getSelectedChannel(), DrawLayer.FOREGROUND,
                0, getSlotsOffsetX(), getSlotsOffsetY(), mouseX, mouseY);
        RenderItemExtendedSlotCount.getInstance().zLevel = 150.0F;
        drawActiveStorageSlotItem(mouseX, mouseY);
        RenderItemExtendedSlotCount.getInstance().zLevel = 0F;

        // Draw button tooltips
        Optional<ITerminalStorageTabClient<?>> tabOptional = getSelectedClientTab();
        tabOptional.ifPresent(tab -> {
            int offset = 0;
            for (ITerminalButton button : tab.getButtons()) {
                Button guiButton = button.createButton(button.getX(guiLeft, BUTTONS_OFFSET_X), button.getY(guiTop, BUTTONS_OFFSET_Y + offset));
                if (isPointInRegion(button.getX(0, BUTTONS_OFFSET_X), button.getY(0, BUTTONS_OFFSET_Y + offset),
                        guiButton.getWidth(), guiButton.getHeight(), mouseX, mouseY)) {
                    List<ITextComponent> lines = Lists.newArrayList();
                    lines.add(new TranslationTextComponent(button.getTranslationKey()));
                    button.getTooltip(getMinecraft().player, ITooltipFlag.TooltipFlags.NORMAL, lines);
                    drawTooltip(lines, mouseX - guiLeft, mouseY - guiTop);
                }
                if (button.isInLeftColumn()) {
                    offset += BUTTONS_OFFSET + guiButton.getHeight();
                }
            }

            String tabName = getContainer().getSelectedTab();
            Optional<ITerminalStorageTabCommon> tabCommonOptional = getCommonTab(tabName);
            tabCommonOptional.ifPresent(tabCommon -> {
                for (Triple<Slot, Integer, Integer> slot : getContainer().getTabSlots(tabName)) {
                    tab.onCommonSlotRender(this, DrawLayer.FOREGROUND, 0,
                            guiLeft + slot.getMiddle(), guiTop + slot.getRight(), mouseX, mouseY, slot.getLeft().slotNumber, tabCommon);
                }
            });
        });
    }

    @Override
    protected void drawCurrentScreen(int mouseX, int mouseY, float partialTicks) {
        scrollBar.render(mouseX, mouseY, partialTicks);

        ResourceLocation oldTexture = this.texture;
        getSelectedClientTab().ifPresent(tab -> {
            ResourceLocation texture = tab.getBackgroundTexture();
            if (texture != null) {
                this.texture = texture;
            }
        });

        super.drawCurrentScreen(mouseX, mouseY, partialTicks);

        // Draw slots
        GlStateManager.disableLighting();
        GlStateManager.disableDepthTest();
        //this.zLevel = 0F;
        for (int i1 = 0; i1 < getContainer().inventorySlots.size(); ++i1) {
            Slot slot = getContainer().inventorySlots.get(i1);

            if (slot.isEnabled()) {
                this.drawSlotOverlay(slot);
            }
        }
        //this.zLevel = 0F;

        this.texture = oldTexture;
    }

    private void drawSlotOverlay(Slot slot) {
        getSelectedClientTab().ifPresent(tab -> {
            if (this.terminalDragSplitting && this.terminalDragSplittingSlots.contains(slot)) {
                if (tab.isSlotValidForDraggingInto(getContainer().getSelectedChannel(), slot)) {
                    if (this.terminalDragSplittingSlots.size() == 1) {
                        return;
                    }

                    int dragQuantity = tab.computeDraggingQuantity(this.terminalDragSplittingSlots, this.terminalDragMode, slot.getStack(), tab.getActiveSlotQuantity());
                    if (dragQuantity > 0) {
                        String dragString = "+" + GuiHelpers.quantityToScaledString(dragQuantity);
                        RenderHelpers.drawScaledStringWithShadow(font, dragString, guiLeft + slot.xPos, guiTop + slot.yPos, 0.5F, 16777045);
                    }
                } else {
                    this.terminalDragSplittingSlots.remove(slot);
                    this.updateTerminalDragSplitting(tab);
                }
            }
        });
    }

    @Override
    public ContainerTerminalStorage getContainer() {
        return (ContainerTerminalStorage) super.getContainer();
    }

    protected Optional<ITerminalStorageTabClient<?>> getTabByIndex(int tabIndex) {
        Collection<ITerminalStorageTabClient<?>> tabsClientList = getContainer().getTabsClient().values();
        if (tabIndex >= 0 && tabIndex < tabsClientList.size()) {
            return Optional.of(Iterables.get(tabsClientList, tabIndex));
        }
        return Optional.empty();
    }

    protected void setTabByIndex(int tabIndex) {
        // Save tab index
        getTabByIndex(tabIndex).ifPresent(tab -> {
            getContainer().setSelectedTab(tab.getName().toString());

            // Reset active slot
            tab.resetActiveSlot();

            // Update the filter
            fieldSearch.setText(tab.getInstanceFilter(getContainer().getSelectedChannel()));
        });

        // Reset scrollbar
        scrollBar.scrollTo(0);
    }

    protected void playButtonClickSound() {
        this.getMinecraft().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        Optional<ITerminalStorageTabClient<?>> tabOptional = getSelectedClientTab();
        this.clicked = true;

        // Select a tab
        if (mouseButton == 0
                && mouseY < getGuiTop() + TAB_UNSELECTED_HEIGHT
                && mouseX > getGuiLeft() + TAB_OFFSET_X
                && mouseX <= getGuiLeft() + TAB_OFFSET_X + (TAB_WIDTH * getContainer().getTabsClientCount() - 1)) {
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
            getContainer().setSelectedChannel(channel);
            scrollBar.scrollTo(0); // Reset scrollbar

            // Update the filter
            tabOptional.ifPresent(tab -> fieldSearch.setText(tab.getInstanceFilter(finalChannel)));

            playButtonClickSound();

            return true;
        }

        if (tabOptional.isPresent()) {
            ITerminalStorageTabClient<?> tab = tabOptional.get();
            Slot playerSlot = getSlotUnderMouse();

            // Start dragging over container slots when a storage slot is selected
            if (tab.getActiveSlotId() >= 0
                    && (mouseButton == 0 || mouseButton == 1 || this.getMinecraft().gameSettings.keyBindPickBlock.getKey().getKeyCode() == mouseButton - 100)) {
                if (playerSlot != null && !this.terminalDragSplitting) {
                    this.terminalDragSplitting = true;
                    this.terminalDragSplittingButton = mouseButton;
                    this.terminalDragSplittingSlots.clear();

                    if (mouseButton == 0) {
                        this.terminalDragMode = 0;
                    } else if (mouseButton == 1) {
                        this.terminalDragMode = 1;
                    } else if (this.getMinecraft().gameSettings.keyBindPickBlock.getKey().getKeyCode() == mouseButton - 100) {
                        this.terminalDragMode = 2;
                    }
                    return true;
                }
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
            ITerminalStorageTabCommon tabCommon = getContainer().getTabCommon(tab.getName().toString());
            for (ITerminalButton button : tab.getButtons()) {
                Button guiButton = button.createButton(button.getX(guiLeft, BUTTONS_OFFSET_X), button.getY(guiTop, BUTTONS_OFFSET_Y + offset));
                if (isPointInRegion(button.getX(0, BUTTONS_OFFSET_X), button.getY(0, BUTTONS_OFFSET_Y + offset), guiButton.getWidth(), guiButton.getHeight(), mouseX, mouseY)) {
                    button.onClick(tab, tabCommon, guiButton, getContainer().getSelectedChannel(), mouseButton);
                    playButtonClickSound();
                    return;
                }
                if (button.isInLeftColumn()) {
                    offset += BUTTONS_OFFSET + guiButton.getHeight();
                }
            }
        });

        if (!MinecraftHelpers.isShifted()) {
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        return false;
    }

    @Nullable
    @Override
    public Slot getSlotUnderMouse() {
        Slot slot = super.getSlotUnderMouse();
        // Safety for hacky disabled slots
        if (slot != null && slot.xPos < 0) {
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
                        && tab.isSlotValidForDraggingInto(getContainer().getSelectedChannel(), slot)) {
                    this.terminalDragSplittingSlots.add(slot);
                    this.updateTerminalDragSplitting(tab);
                    return true;
                }
            }
            return false;
        }).orElse(false)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, mouseButton, mouseXPrev, mouseYPrev);
    }

    private void updateTerminalDragSplitting(ITerminalStorageTabClient<?> tab) {
        if (this.terminalDragSplitting) {
            int quantityTotal = tab.getActiveSlotQuantity();
            this.terminalDragSplittingRemnant = tab.getActiveSlotQuantity();

            for (Slot slot : this.terminalDragSplittingSlots) {
                if (tab.isSlotValidForDraggingInto(getContainer().getSelectedChannel(), slot)) {
                    int dragQuantity = tab.computeDraggingQuantity(this.terminalDragSplittingSlots, this.terminalDragMode, slot.getStack(), quantityTotal);
                    this.terminalDragSplittingRemnant -= tab.dragIntoSlot(container, getContainer().getSelectedChannel(), slot, dragQuantity, true);
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
                        if (tab.isSlotValidForDraggingInto(getContainer().getSelectedChannel(), slot)) {
                            int dragQuantity = tab.computeDraggingQuantity(this.terminalDragSplittingSlots, this.terminalDragMode, slot.getStack(), quantityTotal);
                            quantity -= tab.dragIntoSlot(container, getContainer().getSelectedChannel(), slot, dragQuantity, false);
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
                boolean hasClickedOutside = this.hasClickedOutside(mouseX, mouseY, this.guiLeft, this.guiTop, mouseButton);
                boolean hasClickedInStorage = this.hasClickedInStorage(mouseX, mouseY);
                if (tabOptional.get().handleClick(getContainer(), getContainer().getSelectedChannel(), slot, mouseButton,
                        hasClickedOutside, hasClickedInStorage, playerSlot != null ? playerSlot.slotNumber : -1)) {
                    return true;
                }
            }
        }

        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    protected boolean handleKeyCodeFirst(int keyCode, int scanCode) {
        InputMappings.Input inputCode = InputMappings.getInputByCode(keyCode, scanCode);
        if (org.cyclops.integrateddynamics.proxy.ClientProxy.FOCUS_LP_SEARCH.isActiveAndMatches(inputCode)) {
            fieldSearch.changeFocus(true);
            return true;
        } else if (ClientProxy.TERMINAL_TAB_NEXT.isActiveAndMatches(inputCode)) {
            if (getContainer().getTabsClientCount() > 0) {
                // Go to next tab
                setTabByIndex((getSelectedClientTabIndex() + 1) % getContainer().getTabsClientCount());
                playButtonClickSound();
                return true;
            }
        } else if (ClientProxy.TERMINAL_TAB_PREVIOUS.isActiveAndMatches(inputCode)) {
            if (getContainer().getTabsClientCount() > 0) {
                // Go to previous tab
                setTabByIndex((getContainer().getTabsClientCount() + getSelectedClientTabIndex() - 1) % getContainer().getTabsClientCount());
                playButtonClickSound();
                return true;
            }
        }
        return false;
    }

    protected boolean handleKeyCodeLast(int keyCode, int scanCode) {
        InputMappings.Input inputCode = InputMappings.getInputByCode(keyCode, scanCode);
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
                        .ifPresent(tab -> tab.setInstanceFilter(getContainer().getSelectedChannel(), fieldSearch.getText()));
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
                            .ifPresent(tab -> tab.setInstanceFilter(getContainer().getSelectedChannel(), fieldSearch.getText()));
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
        ITerminalStorageTabCommon commonTab = getContainer().getTabCommon(getContainer().getSelectedTab());
        if (commonTab instanceof TerminalStorageTabIngredientComponentItemStackCraftingCommon){
            TerminalButtonItemStackCraftingGridClear.clearGrid(
                    (TerminalStorageTabIngredientComponentItemStackCraftingCommon) commonTab,
                    getContainer().getSelectedChannel(), toStorage);
        }
    }

    protected void balanceCraftingGrid() {
        ITerminalStorageTabCommon commonTab = getContainer().getTabCommon(getContainer().getSelectedTab());
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

    protected void drawTabsBackground() {
        int offsetX = TAB_OFFSET_X;

        // Draw channels label
        drawString(font, L10NHelpers.localize("gui.integratedterminals.terminal_storage.channel"), getGuiLeft() + 30, getGuiTop() + 26, 16777215);

        // Draw all tabs next to each other horizontally
        for (ITerminalStorageTabClient tab : getContainer().getTabsClient().values()) {
            boolean selected = tab.getName().toString().equals(getContainer().getSelectedTab());
            int x = getGuiLeft() + offsetX;
            int y = getGuiTop();
            int width = TAB_WIDTH;
            int height = selected ? TAB_SELECTED_HEIGHT : TAB_UNSELECTED_HEIGHT;
            int textureX = selected ? TAB_SELECTED_TEXTURE_X : TAB_UNSELECTED_TEXTURE_X;
            int textureY = selected ? TAB_SELECTED_TEXTURE_Y : TAB_UNSELECTED_TEXTURE_Y;

            // Draw background
            RenderHelpers.bindTexture(this.texture);
            this.blit(x, y, textureX, textureY, width, height);

            // Draw icon
            ItemStack icon = tab.getIcon();
            ItemRenderer renderItem = Minecraft.getInstance().getItemRenderer();
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            renderItem.renderItemAndEffectIntoGUI(icon, x + TAB_ICON_OFFSET, y + TAB_ICON_OFFSET);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
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

    protected void drawTabContents(String tabId, int channel, DrawLayer layer,
                                   float partialTick, int x, int y, int mouseX, int mouseY) {
        Optional<ITerminalStorageTabClient<?>> optionalTab = getClientTab(tabId);
        if (optionalTab.isPresent()) {
            ITerminalStorageTabClient<?> tab = optionalTab.get();
            // Draw status string
            if (layer == DrawLayer.BACKGROUND) {
                drawCenteredString(font, tab.getStatus(channel), guiLeft + ITerminalStorageTabClient.DEFAULT_SLOT_OFFSET_X + (GuiHelpers.SLOT_SIZE * ITerminalStorageTabClient.DEFAULT_SLOT_ROW_LENGTH) / 2,
                        y + 2 + getSlotVisibleRows() * GuiHelpers.SLOT_SIZE, 16777215);
                GlStateManager.color4f(1, 1, 1, 1);
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
                        fill(slotX, slotY, slotX + GuiHelpers.SLOT_SIZE_INNER, slotY + GuiHelpers.SLOT_SIZE_INNER, -2130706433);
                    }
                }

                //this.zLevel = 200F;
                slot.drawGuiContainerLayer(this, layer, partialTick, slotX, slotY, mouseX, mouseY, tab, channel, null);
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
            GlStateManager.color4f(0.3F, 0.3F, 0.3F, 0.3F);
            fill(x - 1, y - 1, x - 1 + GuiHelpers.SLOT_SIZE * getSlotRowLength(), y - 1 + GuiHelpers.SLOT_SIZE * getSlotVisibleRows(), Helpers.RGBAToInt(50, 50, 50, 100));
            GlStateManager.color4f(1, 1, 1, 1);
        }
    }

    private void drawActiveStorageSlotItem(int mouseX, int mouseY) {
        Optional<ITerminalStorageTabClient<?>> optionalTab = getSelectedClientTab();
        optionalTab.ifPresent(tab -> {
            int slotId = tab.getActiveSlotId();
            if (slotId >= 0) {
                int quantity = tab.getActiveSlotQuantity();
                List<?> slots = tab.getSlots(getContainer().getSelectedChannel(), slotId, 1);
                if (!slots.isEmpty()) {
                    ITerminalStorageSlot slot = (ITerminalStorageSlot) slots.get(0);
                    RenderHelpers.bindTexture(this.texture);
                    GlStateManager.color4f(1, 1, 1, 1);

                    if (this.terminalDragSplitting && this.terminalDragSplittingSlots.size() > 1) {
                        quantity = this.terminalDragSplittingRemnant;
                    }

                    String quantityString = GuiHelpers.quantityToScaledString(quantity);
                    if (quantity == 0) {
                        quantityString = TextFormatting.YELLOW + quantityString;
                    }

                    slot.drawGuiContainerLayer(this, DrawLayer.BACKGROUND, 0,
                            mouseX - this.guiLeft - GuiHelpers.SLOT_SIZE_INNER / 4, mouseY - this.guiTop - GuiHelpers.SLOT_SIZE_INNER / 4,
                            mouseX, mouseY, tab, getContainer().getSelectedChannel(), quantityString);
                }
            }
        });
    }

    protected Optional<ITerminalStorageTabClient<?>> getClientTab(String tab) {
        return Optional.ofNullable(getContainer().getTabsClient().get(tab));
    }

    protected Optional<ITerminalStorageTabCommon> getCommonTab(String tab) {
        return Optional.ofNullable(getContainer().getTabsCommon().get(tab));
    }

    public Optional<ITerminalStorageTabClient<?>> getSelectedClientTab() {
        return getClientTab(getContainer().getSelectedTab());
    }

    protected int getSelectedClientTabIndex() {
        Optional<ITerminalStorageTabClient<?>> selectedTab = getSelectedClientTab();
        if (selectedTab.isPresent()) {
            int tabIndex = 0;
            for (ITerminalStorageTabClient<?> tabClient : getContainer().getTabsClient().values()) {
                if (tabClient == selectedTab.get()) {
                    return tabIndex;
                }
                tabIndex++;
            }
        }
        return -1;
    }

    protected void drawTabsForeground(int mouseX, int mouseY) {
        if (mouseY < getGuiTop() + TAB_UNSELECTED_HEIGHT
                && mouseX > getGuiLeft() + TAB_OFFSET_X
                && mouseX <= getGuiLeft() + TAB_OFFSET_X + (TAB_WIDTH * getContainer().getTabsClientCount() - 1)) {
            int tabIndex = (mouseX - TAB_OFFSET_X - getGuiLeft()) / TAB_WIDTH;
            getTabByIndex(tabIndex)
                    .ifPresent(tab -> this.drawTooltip(tab.getTooltip(), mouseX - getGuiLeft(), mouseY - getGuiTop()));
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
