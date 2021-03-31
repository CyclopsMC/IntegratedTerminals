package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.core.terminalstorage.button.TerminalButtonItemStackCraftingGridAutoRefill;
import org.cyclops.integratedterminals.core.terminalstorage.button.TerminalButtonItemStackCraftingGridBalance;
import org.cyclops.integratedterminals.core.terminalstorage.button.TerminalButtonItemStackCraftingGridClear;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientItemStackCraftingGridShiftClickOutput;

import javax.annotation.Nullable;
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
    public List<ITextComponent> getTooltip() {
        return Lists.newArrayList(new TranslationTextComponent("gui.integratedterminals.terminal_storage.crafting_name",
                new TranslationTextComponent(this.ingredientComponent.getTranslationKey())));
    }

    @Override
    public int getSlotOffsetX() {
        return ITerminalStorageTabClient.DEFAULT_SLOT_OFFSET_X + 108;
    }

    @Override
    public int getSlotRowLength() {
        return 3;
    }

    @Nullable
    @Override
    public ResourceLocation getBackgroundTexture() {
        return new ResourceLocation(Reference.MOD_ID, IntegratedTerminals._instance
                .getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI) + "part_terminal_storage_crafting.png");
    }

    @Override
    public boolean handleClick(Container container, int channel, int hoveringStorageSlot, int mouseButton,
                               boolean hasClickedOutside, boolean hasClickedInStorage, int hoveredContainerSlot) {
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
                hasClickedInStorage, hoveredContainerSlot);
    }

    @Override
    public void onCommonSlotRender(ContainerScreen gui, MatrixStack matrixStack, ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int x, int y, int mouseX, int mouseY, int slot, ITerminalStorageTabCommon tabCommon) {
        // Delegate to regular itemstack tab
        String name = ingredientComponent.getName().toString();
        ITerminalStorageTabClient<?> tabClient = container.getTabClient(name);
        tabCommon = container.getTabCommon(name);
        tabClient.onCommonSlotRender(gui, matrixStack, layer, partialTick, x, y, mouseX, mouseY, slot, tabCommon);
    }
}
