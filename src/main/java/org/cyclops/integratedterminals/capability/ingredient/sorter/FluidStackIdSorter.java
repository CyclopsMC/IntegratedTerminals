package org.cyclops.integratedterminals.capability.ingredient.sorter;

import net.minecraftforge.fluids.FluidStack;
import org.cyclops.integratedterminals.client.gui.image.Images;

/**
 * Sorts fluids by internal ID.
 * @author rubensworks
 */
public class FluidStackIdSorter extends IngredientInstanceSorterAdapter<FluidStack> {

    public FluidStackIdSorter() {
        super(Images.BUTTON_MIDDLE_ID, "fluidstack", "id");
    }

    protected String getFluidStackId(FluidStack fluidStack) {
        return fluidStack.getFluid().getName();
    }

    @Override
    public int compare(FluidStack o1, FluidStack o2) {
        return getFluidStackId(o1).compareTo(getFluidStackId(o2));
    }
}
