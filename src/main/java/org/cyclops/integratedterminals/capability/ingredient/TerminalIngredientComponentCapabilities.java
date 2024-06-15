package org.cyclops.integratedterminals.capability.ingredient;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.capability.IngredientComponentCapabilityAttacherAdapter;
import org.cyclops.commoncapabilities.api.ingredient.capability.IngredientComponentCapabilityAttacherManager;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.integrateddynamics.capability.ingredient.IngredientComponentCapabilities;
import org.cyclops.integratedterminals.Capabilities;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;

/**
 * @author rubensworks
 */
public class TerminalIngredientComponentCapabilities {

    public static void load() {
        IngredientComponentCapabilityAttacherManager attacherManager = new IngredientComponentCapabilityAttacherManager();

        // Views
        attacherManager.addAttacher(new IngredientComponentCapabilityAttacherAdapter<ItemStack, Integer>(IngredientComponentCapabilities.INGREDIENT_ITEMSTACK_NAME, Capabilities.IngredientComponentTerminalStorageHandler.INGREDIENT) {
            @Override
            public ICapabilityProvider<IngredientComponent<ItemStack, Integer>, Void, IIngredientComponentTerminalStorageHandler<ItemStack, Integer>> createCapabilityProvider(IngredientComponent<ItemStack, Integer> ingredientComponent) {
                return new DefaultCapabilityProvider<>(new IngredientComponentTerminalStorageHandlerItemStack(ingredientComponent));
            }
        });
        attacherManager.addAttacher(new IngredientComponentCapabilityAttacherAdapter<FluidStack, Integer>(IngredientComponentCapabilities.INGREDIENT_FLUIDSTACK_NAME, Capabilities.IngredientComponentTerminalStorageHandler.INGREDIENT) {
            @Override
            public ICapabilityProvider<IngredientComponent<FluidStack, Integer>, Void, IIngredientComponentTerminalStorageHandler<FluidStack, Integer>> createCapabilityProvider(IngredientComponent<FluidStack, Integer> ingredientComponent) {
                return new DefaultCapabilityProvider<>(new IngredientComponentTerminalStorageHandlerFluidStack(ingredientComponent));
            }
        });
        attacherManager.addAttacher(new IngredientComponentCapabilityAttacherAdapter<Long, Boolean>(IngredientComponentCapabilities.INGREDIENT_ENERGY_NAME, Capabilities.IngredientComponentTerminalStorageHandler.INGREDIENT) {
            @Override
            public ICapabilityProvider<IngredientComponent<Long, Boolean>, Void, IIngredientComponentTerminalStorageHandler<Long, Boolean>> createCapabilityProvider(IngredientComponent<Long, Boolean> ingredientComponent) {
                return new DefaultCapabilityProvider<>(new IngredientComponentTerminalStorageHandlerEnergy(ingredientComponent));
            }
        });
    }

}
