package org.cyclops.integratedterminals.capability.ingredient.sorter;

import net.minecraft.world.item.ItemStack;
import org.cyclops.integratedterminals.client.gui.image.Images;

/**
 * Sorts items by count.
 * @author rubensworks
 */
public class ItemStackQuantitySorter extends IngredientInstanceSorterAdapter<ItemStack> {

    public ItemStackQuantitySorter() {
        super(Images.BUTTON_MIDDLE_QUANTITY, "itemstack", "quantity");
    }

    @Override
    public int compare(ItemStack o1, ItemStack o2) {
        return -Integer.compare(o2.getCount(), o1.getCount());
    }
}
