package org.cyclops.integratedterminals.capability.ingredient;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.integrateddynamics.block.BlockEnergyBattery;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentViewHandler;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageTabIngredientComponentClient;

/**
 * View handler for energy.
 * @author rubensworks
 */
public class IngredientComponentViewHandlerEnergy implements IIngredientComponentViewHandler<Integer, Boolean> {

    private final IngredientComponent<Integer, Boolean> ingredientComponent;

    public IngredientComponentViewHandlerEnergy(IngredientComponent<Integer, Boolean> ingredientComponent) {
        this.ingredientComponent = ingredientComponent;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(BlockEnergyBattery.getInstance());
    }

    @Override
    public void drawInstanceSlot(Integer instance, GuiContainer gui, DrawLayer layer, float partialTick, int x, int y,
                                 int mouseX, int mouseY, ITerminalStorageTabClient tab, int channel) {
        int maxQuantity = (int) ((TerminalStorageTabIngredientComponentClient<Integer, Boolean>) tab).getMaxQuantity(channel);

        if (instance > 0) {
            if (layer == DrawLayer.BACKGROUND){
                // Draw background
                gui.drawTexturedModalRect(x, y, 48, 225, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER);

                // Draw progress
                GuiHelpers.renderProgressBar(gui, x, y, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER,
                        64, 225, GuiHelpers.ProgressDirection.UP, instance, maxQuantity);

                // Draw amount
                RenderItemExtendedSlotCount.drawSlotText(Minecraft.getMinecraft().fontRenderer, GuiHelpers.quantityToScaledString(instance), x, y);
            } else {
                GuiHelpers.renderTooltip(gui, x, y, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER,
                        mouseX, mouseY, () -> Lists.newArrayList(TextFormatting.GRAY.toString() + instance + " FE"));
            }
        }
    }
}
