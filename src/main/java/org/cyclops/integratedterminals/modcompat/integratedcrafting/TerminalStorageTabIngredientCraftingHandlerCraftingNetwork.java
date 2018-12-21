package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.integratedcrafting.api.crafting.CraftingJob;
import org.cyclops.integratedcrafting.api.crafting.CraftingJobDependencyGraph;
import org.cyclops.integratedcrafting.api.crafting.FailedCraftingRecipeException;
import org.cyclops.integratedcrafting.api.crafting.ICraftingInterface;
import org.cyclops.integratedcrafting.api.crafting.RecursiveCraftingRecipeException;
import org.cyclops.integratedcrafting.api.crafting.UnknownCraftingRecipeException;
import org.cyclops.integratedcrafting.api.network.ICraftingNetwork;
import org.cyclops.integratedcrafting.api.recipe.IRecipeIndex;
import org.cyclops.integratedcrafting.api.recipe.PrioritizedRecipe;
import org.cyclops.integratedcrafting.core.CraftingHelpers;
import org.cyclops.integratedcrafting.core.MissingIngredients;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingOption;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingPlanStatic;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * An {@link ITerminalStorageTabIngredientCraftingHandler} implementation for
 * {@link org.cyclops.integratedcrafting.api.network.ICraftingNetwork}.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientCraftingHandlerCraftingNetwork
        implements ITerminalStorageTabIngredientCraftingHandler<TerminalCraftingOptionPrioritizedRecipe<?, ?>, Integer> {

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
    public ITerminalCraftingPlan<Integer> calculateCraftingPlan(INetwork network, int channel,
                                                       ITerminalCraftingOption craftingOption, long quantity) {
        TerminalCraftingOptionPrioritizedRecipe<?, ?> safeCraftingOption = (TerminalCraftingOptionPrioritizedRecipe<?, ?>) craftingOption;
        PrioritizedRecipe recipe = safeCraftingOption.getPrioritizedRecipe();

        CraftingJobDependencyGraph dependencyGraph = new CraftingJobDependencyGraph();
        try {
            CraftingJob rootJob = CraftingHelpers.calculateCraftingJobs(network, channel, recipe, (int) quantity,
                    true, CraftingHelpers.getGlobalCraftingJobIdentifier(), dependencyGraph, true);
            return newCraftingPlan(rootJob, dependencyGraph, true);
        } catch (FailedCraftingRecipeException e) {
            return newCraftingPlanFailed(e, dependencyGraph);
        } catch (RecursiveCraftingRecipeException e) {
            return newCraftingPlanErrorRecursive(Lists.reverse(e.getRecipeStack()));
        }
    }

    protected static ITerminalCraftingPlan<Integer> newCraftingPlan(CraftingJob craftingJob,
                                                                    CraftingJobDependencyGraph dependencyGraph,
                                                                    boolean root) {
        List recipeOutputs = IntegratedCraftingHelpers.getPrototypesFromIngredients(craftingJob.getRecipe().getRecipe().getOutput());
        List<ITerminalCraftingPlan<Integer>> dependencies = dependencyGraph.getDependencies(craftingJob)
                .stream()
                .map(subCraftingJob -> newCraftingPlan(subCraftingJob, dependencyGraph, false))
                .collect(Collectors.toList());
        if (root) {
            return new TerminalCraftingPlanCraftingJobDependencyGraph(
                    craftingJob.getId(),
                    dependencies,
                    CraftingHelpers.multiplyPrototypedIngredients(recipeOutputs, craftingJob.getAmount()),
                    TerminalCraftingJobStatus.UNSTARTED,
                    craftingJob.getAmount(),
                    IntegratedCraftingHelpers.getPrototypesFromIngredients(craftingJob.getIngredientsStorage()),
                    Collections.emptyList(),
                    "gui.integratedterminals.terminal_storage.craftingplan.label.valid",
                    -1,
                    craftingJob.getChannel(),
                    dependencyGraph);
        } else {
            return new TerminalCraftingPlanStatic<Integer>(
                    craftingJob.getId(),
                    dependencies,
                    CraftingHelpers.multiplyPrototypedIngredients(recipeOutputs, craftingJob.getAmount()),
                    TerminalCraftingJobStatus.UNSTARTED,
                    craftingJob.getAmount(),
                    IntegratedCraftingHelpers.getPrototypesFromIngredients(craftingJob.getIngredientsStorage()),
                    Collections.emptyList(),
                    "gui.integratedterminals.terminal_storage.craftingplan.label.valid",
                    -1,
                    craftingJob.getChannel());
        }
    }

    protected static ITerminalCraftingPlan<Integer> newCraftingPlanUnknown(UnknownCraftingRecipeException exception, CraftingJobDependencyGraph dependencyGraph) {
        List<ITerminalCraftingPlan<Integer>> dependencies = Lists.newArrayList();
        // Add all valid jobs
        dependencies.addAll(
                exception.getPartialCraftingJobs()
                        .stream()
                        .map(subCraftingJob -> newCraftingPlan(subCraftingJob, dependencyGraph, false))
                        .collect(Collectors.toList()));
        // Add all sub-unknown jobs
        dependencies.addAll(exception.getMissingChildRecipes()
                .stream()
                .map(subCraftingJob -> newCraftingPlanUnknown(subCraftingJob, dependencyGraph))
                .collect(Collectors.toList()));
        return new TerminalCraftingPlanStatic<>(
                0,
                dependencies,
                Collections.singletonList(exception.getIngredient()),
                TerminalCraftingJobStatus.INVALID,
                exception.getQuantityMissing(),
                IntegratedCraftingHelpers.getPrototypesFromIngredients(exception.getIngredientsStorage()),
                Collections.emptyList(),
                "gui.integratedterminals.terminal_storage.craftingplan.label.failed.incomplete",
                -1,
                -1);
    }

    protected static ITerminalCraftingPlan<Integer> newCraftingPlanFailed(FailedCraftingRecipeException exception, CraftingJobDependencyGraph dependencyGraph) {
        List<ITerminalCraftingPlan<Integer>> dependencies = Lists.newArrayList();
        // Add all valid jobs
        dependencies.addAll(
                exception.getPartialCraftingJobs()
                        .stream()
                        .map(subCraftingJob -> newCraftingPlan(subCraftingJob, dependencyGraph, false))
                        .collect(Collectors.toList()));
        // Add all sub-unknown jobs
        dependencies.addAll(exception.getMissingChildRecipes()
                .stream()
                .map(subCraftingJob -> newCraftingPlanUnknown(subCraftingJob, dependencyGraph))
                .collect(Collectors.toList()));
        List recipeOutputs = IntegratedCraftingHelpers.getPrototypesFromIngredients(exception.getRecipe().getRecipe().getOutput());
        return new TerminalCraftingPlanStatic<Integer>(
                0,
                dependencies,
                CraftingHelpers.multiplyPrototypedIngredients(recipeOutputs, exception.getQuantityMissing()),
                TerminalCraftingJobStatus.INVALID,
                exception.getQuantityMissing(),
                IntegratedCraftingHelpers.getPrototypesFromIngredients(exception.getIngredientsStorage()),
                Collections.emptyList(),
                "gui.integratedterminals.terminal_storage.craftingplan.label.failed.incomplete",
                -1,
                -1);
    }

    protected static ITerminalCraftingPlan<Integer> newCraftingPlanErrorRecursive(List<PrioritizedRecipe> childRecipes) {
        List<IPrototypedIngredient<?, ?>> recipeOutputs = IntegratedCraftingHelpers.getPrototypesFromIngredients(childRecipes.get(0).getRecipe().getOutput());
        return new TerminalCraftingPlanStatic<>(
                0,
                childRecipes.size() > 1 ?
                        Lists.newArrayList(TerminalStorageTabIngredientCraftingHandlerCraftingNetwork
                                .newCraftingPlanErrorRecursive(childRecipes.subList(1, childRecipes.size())))
                        : Collections.emptyList(),
                recipeOutputs,
                TerminalCraftingJobStatus.INVALID,
                0,
                Collections.emptyList(),
                Collections.emptyList(),
                "gui.integratedterminals.terminal_storage.craftingplan.label.failed.recursion",
                -1,
                -1);
    }

    @Override
    public void startCraftingJob(INetwork network, int channel, ITerminalCraftingPlan<Integer> craftingPlan) {
        if (craftingPlan instanceof TerminalCraftingPlanCraftingJobDependencyGraph
                && craftingPlan.getStatus() == TerminalCraftingJobStatus.UNSTARTED) {
            CraftingJobDependencyGraph craftingJobDependencyGraph = ((TerminalCraftingPlanCraftingJobDependencyGraph) craftingPlan).getCraftingJobDependencyGraph();
            CraftingHelpers.scheduleCraftingJobs(CraftingHelpers.getCraftingNetwork(network), craftingJobDependencyGraph);
        } else {
            IntegratedTerminals.clog(Level.WARN, "Tried to start an invalid crafting plan with status " + craftingPlan.getStatus());
        }
    }

    protected static ITerminalCraftingPlan<Integer> newActiveCraftingJob(ICraftingNetwork craftingNetwork, int channel,
                                                                         CraftingJob craftingJob,
                                                                         CraftingJobDependencyGraph dependencyGraph) {
        List recipeOutputs = IntegratedCraftingHelpers.getPrototypesFromIngredients(craftingJob.getRecipe().getRecipe().getOutput());
        List<ITerminalCraftingPlan<Integer>> dependencies = dependencyGraph.getDependencies(craftingJob)
                .stream()
                .filter(Objects::nonNull) // This should not be needed, and may indicate a corrupted dependency graph
                .map(subCraftingJob -> newActiveCraftingJob(craftingNetwork, channel, subCraftingJob, dependencyGraph))
                .collect(Collectors.toList());

        int craftingJobId = craftingJob.getId();
        ICraftingInterface craftingInterface = craftingNetwork.getCraftingJobInterface(craftingJob.getChannel(), craftingJobId);

        // Determine status
        TerminalCraftingJobStatus jobStatus = TerminalCraftingJobStatus.UNSTARTED;
        switch (craftingInterface.getCraftingJobStatus(craftingNetwork, craftingJob.getChannel(), craftingJobId)) {
            case PENDING_DEPENDENCIES:
                jobStatus = TerminalCraftingJobStatus.PENDING_DEPENDENCIES;
                break;
            case PENDING_INGREDIENTS:
                jobStatus = TerminalCraftingJobStatus.PENDING_INPUTS;
                break;
            case INVALID_INPUTS:
                jobStatus = TerminalCraftingJobStatus.INVALID_INPUTS;
                break;
            case PROCESSING:
                jobStatus = TerminalCraftingJobStatus.CRAFTING;
                break;
            case FINISHED:
                jobStatus = TerminalCraftingJobStatus.FINISHED;
                break;
        }

        // Determine auxiliary output
        List<IPrototypedIngredient<?, ?>> auxiliaryPendingOutputs = Lists.newArrayList();
        int recipeOutputAmount = craftingJob.getAmount();
        if (jobStatus == TerminalCraftingJobStatus.CRAFTING) {
            // If we are in this stage, we are crafting 1 amount,
            // and we are waiting for the outputs of this single

            // Determine pending ingredients
            for (List<IPrototypedIngredient<?, ?>> value : craftingInterface.getPendingCraftingJobOutputs(craftingJobId).values()) {
                auxiliaryPendingOutputs.addAll(value);
            }

            // Reduce the amount by one, as we consider this separately.
            recipeOutputAmount--;
        }

        List<IPrototypedIngredient<?, ?>> pendingOutputs = CraftingHelpers.multiplyPrototypedIngredients(recipeOutputs, recipeOutputAmount);
        pendingOutputs.addAll(auxiliaryPendingOutputs);

        // If the job is missing inputs, add those to the plan
        List<List<IPrototypedIngredient<?, ?>>> lastMissingIngredients = Lists.newArrayList();
        if (jobStatus == TerminalCraftingJobStatus.PENDING_INPUTS) {
            for (MissingIngredients<?, ?> value : craftingJob.getLastMissingIngredients().values()) {
                for (MissingIngredients.Element<?, ?> element : value.getElements()) {
                    List<IPrototypedIngredient<?, ?>> alternatives = Lists.newArrayList();
                    for (MissingIngredients.PrototypedWithRequested<?, ?> alternative : element.getAlternatives()) {
                        IngredientComponent component = alternative.getRequestedPrototype().getComponent();
                        alternatives.add(new PrototypedIngredient<>(
                                component,
                                component.getMatcher().withQuantity(alternative.getRequestedPrototype().getPrototype(),
                                        alternative.getQuantityMissing()),
                                alternative.getRequestedPrototype().getCondition()
                        ));
                    }
                    lastMissingIngredients.add(alternatives);
                }
            }
        }

        return new TerminalCraftingPlanStatic<>(
                craftingJob.getId(),
                dependencies,
                pendingOutputs,
                jobStatus,
                craftingJob.getAmount(),
                IntegratedCraftingHelpers.getPrototypesFromIngredients(craftingJob.getIngredientsStorage()),
                lastMissingIngredients,
                "gui.integratedterminals.terminal_storage.craftingplan.label.running",
                craftingNetwork.getRunningTicks(craftingJob),
                craftingJob.getChannel());
    }

    @Override
    public List<ITerminalCraftingPlan<Integer>> getCraftingJobs(INetwork network, int channel) {
        ICraftingNetwork craftingNetwork = CraftingHelpers.getCraftingNetwork(network);
        Iterable<CraftingJob> iterable = () -> craftingNetwork.getCraftingJobs(channel);
        CraftingJobDependencyGraph dependencyGraph = craftingNetwork.getCraftingJobDependencyGraph();
        return StreamSupport.stream(iterable.spliterator(), false)
                .filter(job -> job.getDependentCraftingJobs().isEmpty()) // Only expose root jobs
                .map(job -> newActiveCraftingJob(craftingNetwork, channel, job, dependencyGraph))
                .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public ITerminalCraftingPlan<Integer> getCraftingJob(INetwork network, int channel, Integer craftingJobId) {
        ICraftingNetwork craftingNetwork = CraftingHelpers.getCraftingNetwork(network);
        CraftingJob craftingJob = craftingNetwork.getCraftingJob(channel, craftingJobId);
        if (craftingJob != null) {
            CraftingJobDependencyGraph dependencyGraph = craftingNetwork.getCraftingJobDependencyGraph();
            return newActiveCraftingJob(craftingNetwork, channel, craftingJob, dependencyGraph);
        }
        return null;
    }

    @Override
    public boolean cancelCraftingJob(INetwork network, int channel, Integer craftingJobId) {
        ICraftingNetwork craftingNetwork = CraftingHelpers.getCraftingNetwork(network);
        return craftingNetwork.cancelCraftingJob(channel, craftingJobId);
    }

    @Override
    public NBTTagCompound serializeCraftingPlan(ITerminalCraftingPlan<Integer> craftingPlan) {
        NBTTagCompound tag = TerminalCraftingPlanStatic.serialize((TerminalCraftingPlanStatic<Integer>) craftingPlan, this);
        if (craftingPlan instanceof TerminalCraftingPlanCraftingJobDependencyGraph) {
            NBTTagCompound serializedGraph = CraftingJobDependencyGraph.serialize(
                    ((TerminalCraftingPlanCraftingJobDependencyGraph) craftingPlan).getCraftingJobDependencyGraph());
            tag.setTag("craftingJobDependencyGraph", serializedGraph);
        }
        return tag;
    }

    @Override
    public ITerminalCraftingPlan<Integer> deserializeCraftingPlan(NBTTagCompound tag) throws IllegalArgumentException {
        TerminalCraftingPlanStatic<Integer> planStatic = TerminalCraftingPlanStatic.deserialize(tag, this);
        if (tag.hasKey("craftingJobDependencyGraph")) {
            CraftingJobDependencyGraph craftingJobDependencyGraph = CraftingJobDependencyGraph.deserialize(
                    tag.getCompoundTag("craftingJobDependencyGraph"));
            return new TerminalCraftingPlanCraftingJobDependencyGraph(
                    planStatic.getId(),
                    planStatic.getDependencies(),
                    planStatic.getOutputs(),
                    planStatic.getStatus(),
                    planStatic.getCraftingQuantity(),
                    planStatic.getStorageIngredients(),
                    planStatic.getLastMissingIngredients(),
                    planStatic.getUnlocalizedLabel(),
                    planStatic.getTickDuration(),
                    planStatic.getChannel(),
                    craftingJobDependencyGraph
            );
        } else {
            return planStatic;
        }
    }

    @Override
    public NBTBase serializeCraftingJobId(Integer id) {
        return new NBTTagInt(id);
    }

    @Override
    public Integer deserializeCraftingJobId(NBTBase tag) {
        return ((NBTTagInt) tag).getInt();
    }
}
