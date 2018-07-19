package org.cyclops.integratedterminals.capability.ingredient.sorter;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.cyclops.integratedterminals.client.gui.image.Images;

import java.util.Optional;

/**
 * Sorts items by internal ID.
 * @author rubensworks
 */
public class ItemStackIdSorter extends IngredientInstanceSorterAdapter<ItemStack> {

    public ItemStackIdSorter() {
        super(Images.BUTTON_MIDDLE_ID, "itemstack", "id");
    }

    protected String getItemStackId(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getItem().getRegistryName())
                .orElseGet(() -> new ResourceLocation(itemStack.getItem().getUnlocalizedName())).toString();
    }

    @Override
    public int compare(ItemStack o1, ItemStack o2) {
        return getItemStackId(o1).compareTo(getItemStackId(o2));
    }
}
