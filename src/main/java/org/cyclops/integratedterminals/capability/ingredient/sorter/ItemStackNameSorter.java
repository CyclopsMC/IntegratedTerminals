package org.cyclops.integratedterminals.capability.ingredient.sorter;

import net.minecraft.world.item.ItemStack;
import org.cyclops.integratedterminals.client.gui.image.Images;

/**
 * Sorts items by display name.
 * @author rubensworks
 */
public class ItemStackNameSorter extends IngredientInstanceSorterAdapter<ItemStack> {

    public ItemStackNameSorter() {
        super(Images.BUTTON_MIDDLE_NAME, "itemstack", "name");
    }

    @Override
    public int compare(ItemStack o1, ItemStack o2) {
        return o1.getHoverName().getString().compareTo(o2.getHoverName().getString());
    }
}
