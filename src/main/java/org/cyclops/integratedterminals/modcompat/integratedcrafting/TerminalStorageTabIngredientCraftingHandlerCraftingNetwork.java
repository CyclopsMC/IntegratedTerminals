package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integratedcrafting.api.crafting.CraftingJob;
import org.cyclops.integratedcrafting.api.crafting.CraftingJobDependencyGraph;
import org.cyclops.integratedcrafting.api.crafting.FailedCraftingRecipeException;
import org.cyclops.integratedcrafting.api.crafting.RecursiveCraftingRecipeException;
import org.cyclops.integratedcrafting.api.crafting.UnknownCraftingRecipeException;
import org.cyclops.integratedcrafting.api.network.ICraftingNetwork;
import org.cyclops.integratedcrafting.api.recipe.IRecipeIndex;
import org.cyclops.integratedcrafting.api.recipe.PrioritizedRecipe;
import org.cyclops.integratedcrafting.core.CraftingHelpers;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.FailedCraftingPlanException;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingOption;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingPlanStatic;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

    protected IRecipeIndex getRecipeIndex(INetwork network, int channel) {
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
        IRecipeIndex recipeIndex = getRecipeIndex(tab.getNetwork(), channel);
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
    public ITerminalCraftingPlan calculateCraftingPlan(INetwork network, int channel,
                                                       ITerminalCraftingOption craftingOption, long quantity)
            throws FailedCraftingPlanException {
        TerminalCraftingOptionPrioritizedRecipe<?, ?> safeCraftingOption = (TerminalCraftingOptionPrioritizedRecipe<?, ?>) craftingOption;
        PrioritizedRecipe recipe = safeCraftingOption.getPrioritizedRecipe();

        CraftingJobDependencyGraph dependencyGraph = new CraftingJobDependencyGraph();
        try {
            CraftingJob rootJob = CraftingHelpers.calculateCraftingJobs(network, channel, recipe, (int) quantity,
                    true, CraftingHelpers.getGlobalCraftingJobIdentifier(), dependencyGraph, true);
            return newCraftingPlan(rootJob, dependencyGraph);
        } catch (FailedCraftingRecipeException e) {
            return newCraftingPlanFailed(e);
        } catch (RecursiveCraftingRecipeException e) {
            throw new FailedCraftingPlanException(e.getMessage());
        }
    }

    protected static ITerminalCraftingPlan newCraftingPlan(CraftingJob craftingJob, CraftingJobDependencyGraph dependencyGraph) {
        List recipeOutputs = IntegratedCraftingHelpers.getPrototypesFromIngredients(craftingJob.getRecipe().getRecipe().getOutput());
        return new TerminalCraftingPlanStatic(
                dependencyGraph.getDependencies(craftingJob)
                        .stream()
                        .map(subCraftingJob -> newCraftingPlan(subCraftingJob, dependencyGraph))
                        .collect(Collectors.toList()),
                CraftingHelpers.multiplyPrototypedIngredients(recipeOutputs, craftingJob.getAmount()),
                TerminalCraftingJobStatus.UNSTARTED,
                craftingJob.getAmount(),
                IntegratedCraftingHelpers.getPrototypesFromIngredients(craftingJob.getIngredientsStorage()));
    }

    protected static ITerminalCraftingPlan newCraftingPlanUnknown(UnknownCraftingRecipeException exception) {
        return new TerminalCraftingPlanStatic(
                exception.getMissingChildRecipes()
                        .stream()
                        .map(TerminalStorageTabIngredientCraftingHandlerCraftingNetwork::newCraftingPlanUnknown)
                        .collect(Collectors.toList()),
                Collections.singletonList(exception.getIngredient()),
                TerminalCraftingJobStatus.INVALID,
                exception.getQuantityMissing(),
                Collections.emptyList()
        );
    }

    protected static ITerminalCraftingPlan newCraftingPlanFailed(FailedCraftingRecipeException exception) {
        List recipeOutputs = IntegratedCraftingHelpers.getPrototypesFromIngredients(exception.getRecipe().getRecipe().getOutput());
        return new TerminalCraftingPlanStatic(
                exception.getMissingChildRecipes()
                        .stream()
                        .map(TerminalStorageTabIngredientCraftingHandlerCraftingNetwork::newCraftingPlanUnknown)
                        .collect(Collectors.toList()),
                CraftingHelpers.multiplyPrototypedIngredients(recipeOutputs, exception.getQuantityMissing()),
                TerminalCraftingJobStatus.INVALID,
                exception.getQuantityMissing(),
                Collections.emptyList()
        );
    }

    @Override
    public void startCraftingJob(INetwork network, int channel, ITerminalCraftingPlan craftingPlan) {
        throw new UnsupportedOperationException("Not yet implemented"); // TODO
    }
}
