package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.client.gui.container.GuiTerminalStorage;
import org.cyclops.integratedterminals.core.terminalstorage.button.TerminalButtonItemStackCraftingGridAutoRefill;
import org.cyclops.integratedterminals.core.terminalstorage.button.TerminalButtonItemStackCraftingGridBalance;
import org.cyclops.integratedterminals.core.terminalstorage.button.TerminalButtonItemStackCraftingGridClear;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientItemStackCraftingGridShiftClickOutput;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A client-side storage terminal ingredient tab for crafting with {@link ItemStack} instances.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientComponentItemStackCraftingClient
        extends TerminalStorageTabIngredientComponentClient<ItemStack, Integer> {

    private final ItemStack icon;

    public TerminalStorageTabIngredientComponentItemStackCraftingClient(ContainerTerminalStorage container,
                                                                        ResourceLocation name,
                                                                        IngredientComponent<?, ?> ingredientComponent) {
        super(container, name, ingredientComponent);
        this.icon = new ItemStack(Blocks.CRAFTING_TABLE);
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
    public List<String> getTooltip() {
        return Lists.newArrayList(L10NHelpers.localize("gui.integratedterminals.terminal_storage.crafting_name",
                L10NHelpers.localize(this.ingredientComponent.getTranslationKey())));
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
        boolean shift = (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));
        if (hoveredContainerSlot == craftingResultSlotIndex && shift) {
            IntegratedTerminals._instance.getPacketHandler().sendToServer(
                    new TerminalStorageIngredientItemStackCraftingGridShiftClickOutput(getName().toString(), channel));
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
    public void onCommonSlotRender(GuiContainer gui, GuiTerminalStorage.DrawLayer layer, float partialTick, int x, int y, int mouseX, int mouseY, int slot, ITerminalStorageTabCommon tabCommon) {
        // Delegate to regular itemstack tab
        String name = ingredientComponent.getName().toString();
        ITerminalStorageTabClient<?> tabClient = container.getTabClient(name);
        tabCommon = container.getTabCommon(name);
        tabClient.onCommonSlotRender(gui, layer, partialTick, x, y, mouseX, mouseY, slot, tabCommon);
    }
}
