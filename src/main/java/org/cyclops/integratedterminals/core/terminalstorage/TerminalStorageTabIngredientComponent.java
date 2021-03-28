package org.cyclops.integratedterminals.core.terminalstorage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
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
 * Terminal storage tab for ingredient components.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientComponent<T, M> implements ITerminalStorageTab {

    private final IngredientComponent<T, M> ingredientComponent;

    public TerminalStorageTabIngredientComponent(IngredientComponent<T, M> ingredientComponent) {
        this.ingredientComponent = ingredientComponent;
    }

    @Override
    public ResourceLocation getName() {
        return ingredientComponent.getName();
    }

    @Override
    public ITerminalStorageTabClient<?> createClientTab(ContainerTerminalStorageBase container, PlayerEntity player) {
        return new TerminalStorageTabIngredientComponentClient<>(container, getName(), ingredientComponent);
    }

    @Override
    public ITerminalStorageTabServer createServerTab(ContainerTerminalStorageBase container, PlayerEntity player, INetwork network) {
        IPositionedAddonsNetworkIngredients<T, M> ingredientNetwork = NetworkHelpers.getIngredientNetwork(LazyOptional.of(() -> network), ingredientComponent)
                .orElseThrow(() -> new IllegalStateException("Could not find an ingredient network"));
        return new TerminalStorageTabIngredientComponentServer<>(getName(), network, ingredientComponent,
                ingredientNetwork, (ServerPlayerEntity) player);
    }

    @Nullable
    @Override
    public ITerminalStorageTabCommon createCommonTab(ContainerTerminalStorageBase container, PlayerEntity player) {
        return new TerminalStorageTabIngredientComponentCommon<>(container, getName(), ingredientComponent);
    }
}
