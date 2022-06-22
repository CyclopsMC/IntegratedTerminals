package org.cyclops.integratedterminals.capability.ingredient.sorter;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
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
        return ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid()).toString();
    }

    @Override
    public int compare(FluidStack o1, FluidStack o2) {
        return getFluidStackId(o1).compareTo(getFluidStackId(o2));
    }
}
