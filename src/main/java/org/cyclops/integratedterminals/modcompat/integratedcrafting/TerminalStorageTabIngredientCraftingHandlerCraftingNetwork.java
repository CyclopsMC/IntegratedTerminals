package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integratedcrafting.api.network.ICraftingNetwork;
import org.cyclops.integratedcrafting.api.recipe.IRecipeIndex;
import org.cyclops.integratedcrafting.api.recipe.PrioritizedRecipe;
import org.cyclops.integratedcrafting.core.CraftingHelpers;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingJob;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingOption;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * An {@link ITerminalStorageTabIngredientCraftingHandler} implementation for
 * {@link org.cyclops.integratedcrafting.api.network.ICraftingNetwork}.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientCraftingHandlerCraftingNetwork
        implements ITerminalStorageTabIngredientCraftingHandler<TerminalCraftingOptionPrioritizedRecipe<?, ?>> {

    private static final ResourceLocation ID = new ResourceLocation(Reference.MOD_INTEGRATECRAFTING, "craftingNetwork");

    protected <T, M> IRecipeIndex getRecipeIndex(TerminalStorageTabIngredientComponentServer<T, M> tab, int channel) {
        INetwork network = tab.getNetwork();
        ICraftingNetwork craftingNetwork = CraftingHelpers.getCraftingNetwork(network);
        return craftingNetwork.getRecipeIndex(channel);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public <T, M> int[] getChannels(TerminalStorageTabIngredientComponentServer<T, M> tab) {
        INetwork network = tab.getNetwork();
        ICraftingNetwork craftingNetwork = CraftingHelpers.getCraftingNetwork(network);
        return craftingNetwork.getChannels();
    }

    @Override
    public <T, M> Collection<TerminalCraftingOptionPrioritizedRecipe<?, ?>> getCraftingOptions(TerminalStorageTabIngredientComponentServer<T, M> tab, int channel) {
        IngredientComponent<T, M> ingredientComponent = tab.getIngredientNetwork().getComponent();
        IIngredientMatcher<T, M> matcher = ingredientComponent.getMatcher();
        IRecipeIndex recipeIndex = getRecipeIndex(tab, channel);
        Iterable<PrioritizedRecipe> recipes = () -> recipeIndex.getRecipes(ingredientComponent, matcher.getEmptyInstance(), matcher.getAnyMatchCondition());
        return StreamSupport.stream(recipes.spliterator(), false)
                .map((recipe) -> new TerminalCraftingOptionPrioritizedRecipe<>(ingredientComponent, recipe))
                .collect(Collectors.toList());
    }

    @Override
    public NBTTagCompound serializeCraftingOption(TerminalCraftingOptionPrioritizedRecipe craftingOption) {
        return PrioritizedRecipe.serialize(craftingOption.getPrioritizedRecipe());
    }

    @Override
    public <T, M> TerminalCraftingOptionPrioritizedRecipe deserializeCraftingOption(IngredientComponent<T, M> ingredientComponent, NBTTagCompound tag) throws IllegalArgumentException {
        return new TerminalCraftingOptionPrioritizedRecipe<>(ingredientComponent, PrioritizedRecipe.deserialize(tag));
    }

    @Override
    public <T, M> ITerminalCraftingJob<T> calculateCraftingJob(TerminalStorageTabIngredientComponentServer<T, M> tab, int channel, ITerminalCraftingOption<T> craftingOption, long quantity) {
        throw new UnsupportedOperationException("Not yet implemented"); // TODO
    }

    @Override
    public <T, M> void startCraftingJob(TerminalStorageTabIngredientComponentServer<T, M> tab, int channel, ITerminalCraftingJob<T> craftingJob) {
        throw new UnsupportedOperationException("Not yet implemented"); // TODO
    }
}
