package org.cyclops.integratedterminals.capability.ingredient.sorter;

import net.minecraftforge.fluids.FluidStack;
import org.cyclops.integratedterminals.client.gui.image.Images;

/**
 * Sorts fluids by display name.
 * @author rubensworks
 */
public class FluidStackNameSorter extends IngredientInstanceSorterAdapter<FluidStack> {

    public FluidStackNameSorter() {
        super(Images.BUTTON_MIDDLE_NAME, "fluidstack", "name");
    }

    @Override
    public int compare(FluidStack o1, FluidStack o2) {
        return o1.getLocalizedName().compareTo(o2.getLocalizedName());
    }
}
