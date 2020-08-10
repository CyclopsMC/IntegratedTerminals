package org.cyclops.integratedterminals.core.terminalstorage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTab;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabServer;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;

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
    public ITerminalStorageTabClient<?> createClientTab(ContainerTerminalStorage container, PlayerEntity player, PartTarget target) {
        return new TerminalStorageTabIngredientComponentClient<>(container, getName(), ingredientComponent);
    }

    @Override
    public ITerminalStorageTabServer createServerTab(ContainerTerminalStorage container, PlayerEntity player, PartTarget target) {
        INetwork network = NetworkHelpers.getNetworkChecked(target.getCenter());
        IPositionedAddonsNetworkIngredients<T, M> ingredientNetwork = NetworkHelpers.getIngredientNetwork(LazyOptional.of(() -> network), ingredientComponent)
                .orElseThrow(() -> new IllegalStateException("Could not find an ingredient network"));
        return new TerminalStorageTabIngredientComponentServer<>(getName(), network, ingredientComponent,
                ingredientNetwork, target.getCenter(), (ServerPlayerEntity) player);
    }

    @Nullable
    @Override
    public ITerminalStorageTabCommon createCommonTab(ContainerTerminalStorage container, PlayerEntity player, PartTarget target) {
        return new TerminalStorageTabIngredientComponentCommon<>(container, getName(), ingredientComponent);
    }
}
