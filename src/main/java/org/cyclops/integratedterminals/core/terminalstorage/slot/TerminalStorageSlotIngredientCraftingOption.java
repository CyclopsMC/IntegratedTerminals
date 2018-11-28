package org.cyclops.integratedterminals.core.terminalstorage.slot;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingOption;
import org.cyclops.integratedterminals.client.gui.container.GuiTerminalStorage;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;

import javax.annotation.Nullable;
import java.util.List;

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
            viewHandler.drawInstance(getInstance(), maxQuantity, null, gui, layer, partialTick, x, y, mouseX, mouseY, null);
            drawCraftLabel(x, y);
        } else {
            long maxQuantity = ((TerminalStorageTabIngredientComponentClient) tab).getMaxQuantity(channel);
            getIngredientComponentViewHandler().drawInstance(getInstance(), maxQuantity, label, gui, layer, partialTick, x, y, mouseX, mouseY, getTooltipLines());
        }
    }

    protected List<String> getTooltipLines() {
        List<String> tooltipLines = Lists.newArrayList();
        tooltipLines.add(TextFormatting.YELLOW + L10NHelpers.localize("gui.integratedterminals.terminal_storage.tooltip.requirements"));
        for (IngredientComponent<?, ?> inputComponent : getCraftingOption().getInputComponents()) {
            IIngredientMatcher matcher = inputComponent.getMatcher();
            for (Object inputInstance : getCraftingOption().getInputs(inputComponent)) {
                if (!matcher.isEmpty(inputInstance)) {
                    tooltipLines.add(String.format("%s- %s (%s)",
                            TextFormatting.GRAY, matcher.localize(inputInstance), matcher.getQuantity(inputInstance)));
                }
            }
        }
        return tooltipLines;
    }

    public ITerminalCraftingOption<T> getCraftingOption() {
        return craftingOption;
    }

    private void drawCraftLabel(int x, int y) {
        RenderItemExtendedSlotCount.drawSlotText(Minecraft.getMinecraft().fontRenderer,
                TextFormatting.GOLD + L10NHelpers.localize("gui.integratedterminals.terminal_storage.craft"), x, y - 11);
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.disableLighting();
    }

}
