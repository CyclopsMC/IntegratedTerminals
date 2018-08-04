package org.cyclops.integratedterminals.core.terminalstorage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.cyclops.commoncapabilities.IngredientComponents;
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
 * Terminal storage tab for the item crafting grid.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientComponentItemStackCrafting implements ITerminalStorageTab {

    private final IngredientComponent<ItemStack, Integer> ingredientComponent;
    private final ResourceLocation name;

    public TerminalStorageTabIngredientComponentItemStackCrafting(IngredientComponent<ItemStack, Integer> ingredientComponent) {
        this.ingredientComponent = ingredientComponent;
        this.name = new ResourceLocation(ingredientComponent.getName().getResourceDomain(),
                ingredientComponent.getName().getResourcePath() + "_crafting");
    }

    @Override
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public ITerminalStorageTabClient<?> createClientTab(ContainerTerminalStorage container, EntityPlayer player, PartTarget target) {
        return new TerminalStorageTabIngredientComponentItemStackCraftingClient(getName(), ingredientComponent);
    }

    @Override
    public ITerminalStorageTabServer createServerTab(ContainerTerminalStorage container, EntityPlayer player, PartTarget target) {
        INetwork network = NetworkHelpers.getNetwork(target.getCenter());
        IPositionedAddonsNetworkIngredients<ItemStack, Integer> ingredientNetwork = NetworkHelpers.getIngredientNetwork(network, ingredientComponent);
        return new TerminalStorageTabIngredientComponentItemStackCraftingServer(getName(), ingredientComponent,
                ingredientNetwork, target.getCenter(), (EntityPlayerMP) player);
    }

    @Nullable
    @Override
    public ITerminalStorageTabCommon createCommonTab(ContainerTerminalStorage container, EntityPlayer player, PartTarget target) {
        return new TerminalStorageTabIngredientComponentItemStackCraftingCommon(container, getName(), IngredientComponents.ITEMSTACK);
    }
}
