package org.cyclops.integratedterminals.client.gui.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;

import java.util.List;

/**
 * A tooltip component holding the ingredients that are required by a crafting option.
 *
 * Every entry in {@link #ingredients()} represents a single required input,
 * which holds all ingredients that are valid alternatives for that input.
 *
 * @param ingredients The required inputs, with their alternatives.
 * @author rubensworks
 */
public record CraftingOptionIngredientsTooltip(
        List<List<IPrototypedIngredient<?, ?>>> ingredients) implements TooltipComponent {
}
