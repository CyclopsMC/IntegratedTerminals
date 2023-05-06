package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.api.terminalstorage.event.TerminalStorageScreenSizeEvent;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.core.terminalstorage.button.TerminalButtonItemStackCraftingGridAutoRefill;
import org.cyclops.integratedterminals.core.terminalstorage.button.TerminalButtonItemStackCraftingGridBalance;
import org.cyclops.integratedterminals.core.terminalstorage.button.TerminalButtonItemStackCraftingGridClear;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientItemStackCraftingGridShiftClickOutput;

import java.util.List;

/**
 * A client-side storage terminal ingredient tab for crafting with {@link ItemStack} instances.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientComponentItemStackCraftingClient
        extends TerminalStorageTabIngredientComponentClient<ItemStack, Integer> {

    private final ItemStack icon;

    public TerminalStorageTabIngredientComponentItemStackCraftingClient(ContainerTerminalStorageBase container,
                                                                        ResourceLocation name,
                                                                        IngredientComponent<?, ?> ingredientComponent) {
        super(container, name, ingredientComponent);
        this.icon = new ItemStack(Blocks.CRAFTING_TABLE);
    }

    @Override
    public ResourceLocation getTabSettingsName() {
        return GeneralConfig.syncItemStorageAndCraftingTabStates ? ingredientComponent.getName() : getName();
    }

    @Override
    protected void loadButtons(List<ITerminalButton<?, ?, ?>> buttons) {
        super.loadButtons(buttons);

        buttons.add(new TerminalButtonItemStackCraftingGridAutoRefill<>(container.getGuiState(), this));
        buttons.add(new TerminalButtonItemStackCraftingGridClear<>());
        buttons.add(new TerminalButtonItemStackCraftingGridBalance<>());
    }

    @Override
    public ItemStack getIcon() {
        return this.icon;
    }

    @Override
    public List<Component> getTooltip() {
        return Lists.newArrayList(new TranslatableComponent("gui.integratedterminals.terminal_storage.crafting_name",
                new TranslatableComponent(this.ingredientComponent.getTranslationKey())));
    }

    protected boolean isCraftingGridCenter() {;
        return TerminalStorageScreenSizeEvent.getWidthHeight().getLeft() < 374 ||
                getRowColumnProvider().getRowsAndColumns().columns() < 17 ||
                GeneralConfig.guiStorageForceCraftingGridCenter;
    }

    @Override
    public int getSlotVisibleRows() {
        if (isCraftingGridCenter()) {
            return Math.max(2, super.getSlotVisibleRows() - 4);
        }
        return super.getSlotVisibleRows();
    }

    @Override
    public int getPlayerInventoryOffsetX() {
        return super.getPlayerInventoryOffsetX() + (isCraftingGridCenter() ? 0 : 60);
    }

    @Override
    public int getPlayerInventoryOffsetY() {
        return super.getPlayerInventoryOffsetY() + (isCraftingGridCenter() ? 68 : 0);
    }

    @Override
    public boolean handleClick(AbstractContainerMenu container, int channel, int hoveringStorageSlot, int mouseButton,
                               boolean hasClickedOutside, boolean hasClickedInStorage, int hoveredContainerSlot,
                               boolean isQuickMove) {
        int craftingResultSlotIndex = TerminalStorageTabIngredientComponentItemStackCraftingCommon
                .getCraftingResultSlotIndex(container, getName());
        boolean shift = MinecraftHelpers.isShifted();
        if (hoveredContainerSlot == craftingResultSlotIndex && shift) {
            IntegratedTerminals._instance.getPacketHandler().sendToServer(
                    new TerminalStorageIngredientItemStackCraftingGridShiftClickOutput(getName().toString(), channel,
                            GeneralConfig.shiftClickCraftingResultLimit));
            return true;
        }
        if (hoveredContainerSlot > craftingResultSlotIndex
                && hoveredContainerSlot <= craftingResultSlotIndex + 9 && getActiveSlotId() < 0) {
            return false;
        }
        return super.handleClick(container, channel, hoveringStorageSlot, mouseButton, hasClickedOutside,
                hasClickedInStorage, hoveredContainerSlot, isQuickMove);
    }

    @Override
    public void onTabBackgroundRender(ContainerScreenTerminalStorage<?, ?> screen, PoseStack matrixStack, float f, int mouseX, int mouseY) {
        super.onTabBackgroundRender(screen, matrixStack, f, mouseX, mouseY);

        // Render crafting grid
        screen.blit(matrixStack, screen.getGuiLeft() + (screen.getGridXSize() / 2) - (9 * GuiHelpers.SLOT_SIZE / 2) + 51 - (isCraftingGridCenter() ? 0 : 107), screen.getGuiTop() + 52 + screen.getGridYSize() , 0, 117, 120, 68);
    }

    @Override
    public void onCommonSlotRender(AbstractContainerScreen gui, PoseStack matrixStack, ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int x, int y, int mouseX, int mouseY, int slot, ITerminalStorageTabCommon tabCommon) {
        // Delegate to regular itemstack tab
        String name = ingredientComponent.getName().toString();
        ITerminalStorageTabClient<?> tabClient = container.getTabClient(name);
        tabCommon = container.getTabCommon(name);
        tabClient.onCommonSlotRender(gui, matrixStack, layer, partialTick, x, y, mouseX, mouseY, slot, tabCommon);
    }

    @Override
    public boolean isQuickMovePrevented(int slotIndex) {
        // Prevent quick move on the crafting result slot to stop accidental mass crafting due to inventory mods
        // spamming quick moves
        int craftingResultSlotIndex = TerminalStorageTabIngredientComponentItemStackCraftingCommon
                .getCraftingResultSlotIndex(container, getName());
        return slotIndex == craftingResultSlotIndex;
    }
}
