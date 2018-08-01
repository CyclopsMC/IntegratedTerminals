package org.cyclops.integratedterminals.core.terminalstorage;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabRegistry;

/**
 * @author rubensworks
 */
public class TerminalStorageTabs {

    public static ITerminalStorageTabRegistry REGISTRY = IntegratedTerminals._instance.getRegistryManager()
            .getRegistry(ITerminalStorageTabRegistry.class);

    public static void load() {
        MinecraftForge.EVENT_BUS.register(TerminalStorageTabs.class);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void afterIngredientComponentsRegistration(RegistryEvent.Register event) {
        if (event.getRegistry() == IngredientComponent.REGISTRY) {
            // Create tabs for all ingredient component types
            for (IngredientComponent<?, ?> ingredientComponent : IngredientComponent.REGISTRY.getValuesCollection()) {
                TerminalStorageTabs.REGISTRY.register(new TerminalStorageTabIngredientComponent<>(ingredientComponent));
            }

            // Add custom tabs
            TerminalStorageTabs.REGISTRY.register(new TerminalStorageTabIngredientComponentItemStackCrafting(
                    (IngredientComponent<ItemStack, Integer>) IngredientComponent.REGISTRY.getValue(
                            new ResourceLocation("minecraft:itemstack"))));
        }
    }

}
