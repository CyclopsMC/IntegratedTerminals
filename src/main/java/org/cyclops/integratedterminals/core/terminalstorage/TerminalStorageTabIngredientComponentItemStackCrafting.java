package org.cyclops.integratedterminals.core.terminalstorage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
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
    public ITerminalStorageTabClient<?> createClientTab(ContainerTerminalStorageBase container, PlayerEntity player) {
        return new TerminalStorageTabIngredientComponentItemStackCraftingClient(container, getName(), ingredientComponent);
    }

    @Override
    public ITerminalStorageTabServer createServerTab(ContainerTerminalStorageBase container, PlayerEntity player, INetwork network) {
        IPositionedAddonsNetworkIngredients<ItemStack, Integer> ingredientNetwork = NetworkHelpers.getIngredientNetwork(LazyOptional.of(() -> network), ingredientComponent)
                .orElseThrow(() -> new IllegalStateException("Could not find an ingredient network"));
        return new TerminalStorageTabIngredientComponentItemStackCraftingServer(getName(), network, ingredientComponent,
                ingredientNetwork, (ServerPlayerEntity) player);
    }

    @Nullable
    @Override
    public ITerminalStorageTabCommon createCommonTab(ContainerTerminalStorageBase container, PlayerEntity player) {
        return new TerminalStorageTabIngredientComponentItemStackCraftingCommon(container, getName(), IngredientComponents.ITEMSTACK);
    }
}
