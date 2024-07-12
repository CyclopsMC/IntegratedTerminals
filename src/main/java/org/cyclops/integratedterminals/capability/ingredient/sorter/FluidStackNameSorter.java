package org.cyclops.integratedterminals.capability.ingredient.sorter;

import net.neoforged.neoforge.fluids.FluidStack;
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
        return o1.getHoverName().getString().compareTo(o2.getHoverName().getString());
    }
}
