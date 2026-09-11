package org.cyclops.integratedterminals.client.gui.tooltip;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;

import java.util.List;

/**
 * Renders the ingredients of a {@link CraftingOptionIngredientsTooltip} as a grid of icons.
 *
 * Inputs that accept multiple alternatives are cycled through within their own slot.
 *
 * @author rubensworks
 */
public class ClientCraftingOptionIngredientsTooltip extends ClientCraftingOptionSlotsTooltip {

    private final List<List<IPrototypedIngredient<?, ?>>> ingredients;

    public ClientCraftingOptionIngredientsTooltip(CraftingOptionIngredientsTooltip tooltip) {
        super(tooltip.ingredients().size());
        this.ingredients = tooltip.ingredients();
    }

    @Override
    protected void drawSlot(GuiGraphicsExtractor guiGraphics, int slot, int x, int y) {
        // Cycle over the alternatives of this input
        List<IPrototypedIngredient<?, ?>> alternatives = this.ingredients.get(slot);
        drawIngredient(guiGraphics, alternatives.get(getTick() % alternatives.size()), x, y);
    }

}
