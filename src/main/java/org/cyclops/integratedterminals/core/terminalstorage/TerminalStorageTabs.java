package org.cyclops.integratedterminals.core.terminalstorage;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.capability.AttachCapabilitiesEventIngredientComponent;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabRegistry;

/**
 * @author rubensworks
 */
public class TerminalStorageTabs {

    public static ITerminalStorageTabRegistry REGISTRY = IntegratedTerminals._instance.getRegistryManager()
            .getRegistry(ITerminalStorageTabRegistry.class);

    public static void load() {
        IntegratedTerminals._instance.getModEventBus().register(TerminalStorageTabs.class);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void afterIngredientComponentCapabilitiesRegistration(AttachCapabilitiesEventIngredientComponent event) {
        // Create tabs for all ingredient component types
        for (IngredientComponent<?, ?> ingredientComponent : IngredientComponent.REGISTRY) {
            if (ingredientComponent.getCapability(Capabilities.PositionedAddonsNetworkIngredientsHandler.INGREDIENT).isPresent()) {
                TerminalStorageTabs.REGISTRY.register(new TerminalStorageTabIngredientComponent<>(ingredientComponent));
            }
        }

        // Add custom tabs
        IngredientComponent<ItemStack, Integer> ingredientComponentItemStack = (IngredientComponent<ItemStack, Integer>)
                IngredientComponent.REGISTRY.get(new ResourceLocation("minecraft:itemstack"));
        if (ingredientComponentItemStack != null
                && ingredientComponentItemStack.getCapability(Capabilities.PositionedAddonsNetworkIngredientsHandler.INGREDIENT).isPresent()) {
            TerminalStorageTabs.REGISTRY.register(new TerminalStorageTabIngredientComponentItemStackCrafting(ingredientComponentItemStack));
        }
    }

}
