package org.cyclops.integratedterminals.core.terminalstorage.slot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingOption;
import org.cyclops.integratedterminals.client.gui.container.GuiTerminalStorage;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;

import javax.annotation.Nullable;

/**
 * An ingredient slot for a crafting option.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class TerminalStorageSlotIngredientCraftingOption<T, M> extends TerminalStorageSlotIngredient<T, M> {

    private final ITerminalCraftingOption<T> craftingOption;

    public TerminalStorageSlotIngredientCraftingOption(IIngredientComponentTerminalStorageHandler<T, M> ingredientComponentViewHandler, T instance, ITerminalCraftingOption<T> craftingOption) {
        super(ingredientComponentViewHandler, instance);
        this.craftingOption = craftingOption;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawGuiContainerLayer(GuiContainer gui, GuiTerminalStorage.DrawLayer layer,
                                      float partialTick, int x, int y, int mouseX, int mouseY,
                                      ITerminalStorageTabClient tab, int channel, @Nullable String label) {
        IIngredientComponentTerminalStorageHandler<T, M> viewHandler = getIngredientComponentViewHandler();
        if (layer == GuiTerminalStorage.DrawLayer.BACKGROUND) {
            long maxQuantity = ((TerminalStorageTabIngredientComponentClient) tab).getMaxQuantity(channel);
            viewHandler.drawInstance(getInstance(), maxQuantity, null, gui, layer, partialTick, x, y, mouseX, mouseY, channel);
            drawCraftLabel(x, y);
        } else {
            super.drawGuiContainerLayer(gui, layer, partialTick, x, y, mouseX, mouseY, tab, channel, label);
        }
    }

    public ITerminalCraftingOption<T> getCraftingOption() {
        return craftingOption;
    }

    private void drawCraftLabel(int x, int y) {
        RenderItemExtendedSlotCount.drawSlotText(Minecraft.getMinecraft().fontRenderer,
                TextFormatting.GOLD + L10NHelpers.localize("gui.integratedterminals.terminal_storage.craft"), x, y - 11);
    }

}
