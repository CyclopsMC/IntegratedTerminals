package org.cyclops.integratedterminals.core.terminalstorage;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTab;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabServer;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Terminal storage tab for the item crafting grid.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientComponentItemStackCrafting implements ITerminalStorageTab {

    public static ResourceLocation NAME;

    private final IngredientComponent<ItemStack, Integer> ingredientComponent;
    private final ResourceLocation name;

    public TerminalStorageTabIngredientComponentItemStackCrafting(IngredientComponent<ItemStack, Integer> ingredientComponent) {
        this.ingredientComponent = ingredientComponent;
        this.name = new ResourceLocation(ingredientComponent.getName().getNamespace(),
                ingredientComponent.getName().getPath() + "_crafting");
        TerminalStorageTabIngredientComponentItemStackCrafting.NAME = this.name;
    }

    @Override
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public ITerminalStorageTabClient<?> createClientTab(ContainerTerminalStorageBase container, Player player) {
        return new TerminalStorageTabIngredientComponentItemStackCraftingClient(container, getName(), ingredientComponent);
    }

    @Override
    public ITerminalStorageTabServer createServerTab(ContainerTerminalStorageBase container, Player player, INetwork network) {
        IPositionedAddonsNetworkIngredients<ItemStack, Integer> ingredientNetwork = NetworkHelpers.getIngredientNetwork(Optional.of(network), ingredientComponent)
                .orElseThrow(() -> new IllegalStateException("Could not find an ingredient network"));
        return new TerminalStorageTabIngredientComponentItemStackCraftingServer(getName(), network, ingredientComponent,
                ingredientNetwork, (ServerPlayer) player);
    }

    @Nullable
    @Override
    public ITerminalStorageTabCommon createCommonTab(ContainerTerminalStorageBase container, Player player) {
        return new TerminalStorageTabIngredientComponentItemStackCraftingCommon(container, getName(), IngredientComponents.ITEMSTACK);
    }
}
