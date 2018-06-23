package org.cyclops.integratedterminals.capability.ingredient;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentViewHandler;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;

import java.util.List;

/**
 * View handler for fluids.
 * @author rubensworks
 */
public class IngredientComponentViewHandlerFluidStack implements IIngredientComponentViewHandler<FluidStack, Integer> {

    private final IngredientComponent<FluidStack, Integer> ingredientComponent;

    public IngredientComponentViewHandlerFluidStack(IngredientComponent<FluidStack, Integer> ingredientComponent) {
        this.ingredientComponent = ingredientComponent;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.WATER_BUCKET);
    }

    @Override
    public void drawInstanceSlot(FluidStack instance, GuiContainer gui, DrawLayer layer, float partialTick, int x, int y,
                                 int mouseX, int mouseY, ITerminalStorageTabClient tab, int channel) {
        if (instance != null) {
            if (layer == DrawLayer.BACKGROUND) {
                // Draw fluid
                GuiHelpers.renderFluidSlot(gui, instance, x, y);

                // Draw amount
                RenderItemExtendedSlotCount.drawSlotText(Minecraft.getMinecraft().fontRenderer, GuiHelpers.quantityToScaledString(instance.amount), x, y);
            } else {
                GuiHelpers.renderTooltip(gui, x, y, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER, mouseX, mouseY, () -> {
                    List<String> lines = Lists.newArrayList();
                    lines.add(instance.getFluid().getRarity().rarityColor + instance.getLocalizedName());
                    lines.add(TextFormatting.GRAY.toString() + instance.amount + " mB");
                    return lines;
                });
            }
        }
    }
}
