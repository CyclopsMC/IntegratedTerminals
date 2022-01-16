package org.cyclops.integratedterminals.core.terminalstorage;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
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
    public ITerminalStorageTabClient<?> createClientTab(ContainerTerminalStorageBase container, Player player) {
        return new TerminalStorageTabIngredientComponentClient<>(container, getName(), ingredientComponent);
    }

    @Override
    public ITerminalStorageTabServer createServerTab(ContainerTerminalStorageBase container, Player player, INetwork network) {
        IPositionedAddonsNetworkIngredients<T, M> ingredientNetwork = NetworkHelpers.getIngredientNetwork(LazyOptional.of(() -> network), ingredientComponent)
                .orElseThrow(() -> new IllegalStateException("Could not find an ingredient network"));
        return new TerminalStorageTabIngredientComponentServer<>(getName(), network, ingredientComponent,
                ingredientNetwork, (ServerPlayer) player);
    }

    @Nullable
    @Override
    public ITerminalStorageTabCommon createCommonTab(ContainerTerminalStorageBase container, Player player) {
        return new TerminalStorageTabIngredientComponentCommon<>(container, getName(), ingredientComponent);
    }
}
