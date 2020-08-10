package org.cyclops.integratedterminals.capability.ingredient;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.capability.IngredientComponentCapabilityAttacherAdapter;
import org.cyclops.commoncapabilities.api.ingredient.capability.IngredientComponentCapabilityAttacherManager;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.integrateddynamics.capability.ingredient.IngredientComponentCapabilities;
import org.cyclops.integratedterminals.Reference;

/**
 * @author rubensworks
 */
public class TerminalIngredientComponentCapabilities {

    public static void load() {
        IngredientComponentCapabilityAttacherManager attacherManager = new IngredientComponentCapabilityAttacherManager();

        // Views
        ResourceLocation capabilityIngredientComponentViewHandler = new ResourceLocation(Reference.MOD_ID, "view_handler");
        attacherManager.addAttacher(new IngredientComponentCapabilityAttacherAdapter<ItemStack, Integer>(IngredientComponentCapabilities.INGREDIENT_ITEMSTACK_NAME, capabilityIngredientComponentViewHandler) {
            @Override
            public ICapabilityProvider createCapabilityProvider(IngredientComponent<ItemStack, Integer> ingredientComponent) {
                return new DefaultCapabilityProvider<>(() -> IngredientComponentTerminalStorageHandlerConfig.CAPABILITY,
                        new IngredientComponentTerminalStorageHandlerItemStack(ingredientComponent));
            }
        });
        attacherManager.addAttacher(new IngredientComponentCapabilityAttacherAdapter<FluidStack, Integer>(IngredientComponentCapabilities.INGREDIENT_FLUIDSTACK_NAME, capabilityIngredientComponentViewHandler) {
            @Override
            public ICapabilityProvider createCapabilityProvider(IngredientComponent<FluidStack, Integer> ingredientComponent) {
                return new DefaultCapabilityProvider<>(() -> IngredientComponentTerminalStorageHandlerConfig.CAPABILITY,
                        new IngredientComponentTerminalStorageHandlerFluidStack(ingredientComponent));
            }
        });
        attacherManager.addAttacher(new IngredientComponentCapabilityAttacherAdapter<Integer, Boolean>(IngredientComponentCapabilities.INGREDIENT_ENERGY_NAME, capabilityIngredientComponentViewHandler) {
            @Override
            public ICapabilityProvider createCapabilityProvider(IngredientComponent<Integer, Boolean> ingredientComponent) {
                return new DefaultCapabilityProvider<>(() -> IngredientComponentTerminalStorageHandlerConfig.CAPABILITY,
                        new IngredientComponentTerminalStorageHandlerEnergy(ingredientComponent));
            }
        });
    }

}
