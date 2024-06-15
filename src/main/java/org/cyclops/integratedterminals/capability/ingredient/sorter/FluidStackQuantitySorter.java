package org.cyclops.integratedterminals.capability.ingredient.sorter;

import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.integratedterminals.client.gui.image.Images;

/**
 * Sorts fluids by amount.
 * @author rubensworks
 */
public class FluidStackQuantitySorter extends IngredientInstanceSorterAdapter<FluidStack> {

    public FluidStackQuantitySorter() {
        super(Images.BUTTON_MIDDLE_QUANTITY, "fluidstack", "quantity");
    }

    @Override
    public int compare(FluidStack o1, FluidStack o2) {
        return -Integer.compare(o2.getAmount(), o1.getAmount());
    }
}
