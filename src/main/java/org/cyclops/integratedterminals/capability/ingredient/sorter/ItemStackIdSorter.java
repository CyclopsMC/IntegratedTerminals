package org.cyclops.integratedterminals.capability.ingredient.sorter;

import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
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
        return Optional.ofNullable(ForgeRegistries.ITEMS.getKey(itemStack.getItem()))
                .orElseGet(() -> new ResourceLocation(itemStack.getItem().getDescriptionId())).toString();
    }

    @Override
    public int compare(ItemStack o1, ItemStack o2) {
        return getItemStackId(o1).compareTo(getItemStackId(o2));
    }
}
