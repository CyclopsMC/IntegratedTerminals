package org.cyclops.integratedterminals.core.terminalstorage;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;

/**
 * A server-side storage terminal ingredient tab for crafting with {@link ItemStack} instances.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientComponentItemStackCraftingServer
        extends TerminalStorageTabIngredientComponentServer<ItemStack, Integer> {

    public TerminalStorageTabIngredientComponentItemStackCraftingServer(ResourceLocation name, INetwork network, IngredientComponent<ItemStack, Integer> ingredientComponent,
                                                                        IPositionedAddonsNetworkIngredients<ItemStack, Integer> ingredientNetwork,
                                                                        ServerPlayer player) {
        super(name, network, ingredientComponent, ingredientNetwork, player);
    }

    @Override
    public void init() {
        // No inv syncing needed, we handle this from the canonical tab
    }

    @Override
    protected void initChannel(int channel) {
        // No inv syncing needed, we handle this from the canonical tab
    }

    @Override
    public void deInit() {
        // No inv syncing needed, we handle this from the canonical tab
    }
}
