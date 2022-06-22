package org.cyclops.integratedterminals.core.terminalstorage;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.capability.network.PositionedAddonsNetworkIngredientsHandlerConfig;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabRegistry;

/**
 * @author rubensworks
 */
public class TerminalStorageTabs {

    public static ITerminalStorageTabRegistry REGISTRY = IntegratedTerminals._instance.getRegistryManager()
            .getRegistry(ITerminalStorageTabRegistry.class);

    public static void load() {
        FMLJavaModLoadingContext.get().getModEventBus().register(TerminalStorageTabs.class);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void afterIngredientComponentsRegistration(RegisterEvent event) {
        if (event.getRegistryKey() == IngredientComponent.REGISTRY.getRegistryKey()) {
            // Create tabs for all ingredient component types
            for (IngredientComponent<?, ?> ingredientComponent : IngredientComponent.REGISTRY.getValues()) {
                if (ingredientComponent.getCapability(PositionedAddonsNetworkIngredientsHandlerConfig.CAPABILITY).isPresent()) {
                    TerminalStorageTabs.REGISTRY.register(new TerminalStorageTabIngredientComponent<>(ingredientComponent));
                }
            }

            // Add custom tabs
            IngredientComponent<ItemStack, Integer> ingredientComponentItemStack = (IngredientComponent<ItemStack, Integer>)
                    IngredientComponent.REGISTRY.getValue(new ResourceLocation("minecraft:itemstack"));
            if (ingredientComponentItemStack != null
                    && ingredientComponentItemStack.getCapability(PositionedAddonsNetworkIngredientsHandlerConfig.CAPABILITY).isPresent()) {
                TerminalStorageTabs.REGISTRY.register(new TerminalStorageTabIngredientComponentItemStackCrafting(ingredientComponentItemStack));
            }
        }
    }

}
