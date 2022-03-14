package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Level;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.integratedcrafting.api.crafting.CraftingJob;
import org.cyclops.integratedcrafting.api.crafting.CraftingJobDependencyGraph;
import org.cyclops.integratedcrafting.api.crafting.FailedCraftingRecipeException;
import org.cyclops.integratedcrafting.api.crafting.ICraftingInterface;
import org.cyclops.integratedcrafting.api.crafting.RecursiveCraftingRecipeException;
import org.cyclops.integratedcrafting.api.crafting.UnavailableCraftingInterfacesException;
import org.cyclops.integratedcrafting.api.crafting.UnknownCraftingRecipeException;
import org.cyclops.integratedcrafting.api.network.ICraftingNetwork;
import org.cyclops.integratedcrafting.api.recipe.IRecipeIndex;
import org.cyclops.integratedcrafting.core.CraftingHelpers;
import org.cyclops.integratedcrafting.core.MissingIngredients;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.CraftingJobStartException;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingOption;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingPlanStatic;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * An {@link ITerminalStorageTabIngredientCraftingHandler} implementation for
 * {@link org.cyclops.integratedcrafting.api.network.ICraftingNetwork}.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientCraftingHandlerCraftingNetwork
        implements ITerminalStorageTabIngredientCraftingHandler<TerminalCraftingOptionRecipeDefinition<?, ?>, Integer> {

    private static final ResourceLocation ID = new ResourceLocation(Reference.MOD_INTEGRATECRAFTING, "crafting_network");

    protected IRecipeIndex getRecipeIndex(INetwork network, int channel) {
        ICraftingNetwork craftingNetwork = CraftingHelpers.getCraftingNetworkChecked(network);
        return craftingNetwork.getRecipeIndex(channel);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public <T, M> int[] getChannels(TerminalStorageTabIngredientComponentServer<T, M> tab) {
        INetwork network = tab.getNetwork();
        ICraftingNetwork craftingNetwork = CraftingHelpers.getCraftingNetworkChecked(network);
        return craftingNetwork.getChannels();
    }

    @Override
    public <T, M> Collection<TerminalCraftingOptionRecipeDefinition<?, ?>> getCraftingOptions(TerminalStorageTabIngredientComponentServer<T, M> tab, int channel) {
        IngredientComponent<T, M> ingredientComponent = tab.getIngredientNetwork().getComponent();
        IIngredientMatcher<T, M> matcher = ingredientComponent.getMatcher();
        IRecipeIndex recipeIndex = getRecipeIndex(tab.getNetwork(), channel);
        Iterable<IRecipeDefinition> recipes = () -> recipeIndex.getRecipes(ingredientComponent, matcher.getEmptyInstance(), matcher.getAnyMatchCondition());
        return StreamSupport.stream(recipes.spliterator(), false)
                .map((recipe) -> new TerminalCraftingOptionRecipeDefinition<>(ingredientComponent, recipe))
                .collect(Collectors.toList());
    }

    @Override
    public CompoundNBT serializeCraftingOption(TerminalCraftingOptionRecipeDefinition craftingOption) {
        return IRecipeDefinition.serialize(craftingOption.getRecipe());
    }

    @Override
    public <T, M> TerminalCraftingOptionRecipeDefinition deserializeCraftingOption(IngredientComponent<T, M> ingredientComponent, CompoundNBT tag) throws IllegalArgumentException {
        return new TerminalCraftingOptionRecipeDefinition<>(ingredientComponent, IRecipeDefinition.deserialize(tag));
    }

    @Override
    public ITerminalCraftingPlan<Integer> calculateCraftingPlan(INetwork network, int channel,
                                                       ITerminalCraftingOption craftingOption, long quantity) {
        TerminalCraftingOptionRecipeDefinition<?, ?> safeCraftingOption = (TerminalCraftingOptionRecipeDefinition<?, ?>) craftingOption;
        IRecipeDefinition recipe = safeCraftingOption.getRecipe();

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
        List recipeOutputs = IntegratedCraftingHelpers.getPrototypesFromIngredients(craftingJob.getRecipe().getOutput());
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
                    TerminalCraftingPlanStatic.Label.VALID,
                    -1,
                    craftingJob.getChannel(),
                    null,
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
                    TerminalCraftingPlanStatic.Label.VALID,
                    -1,
                    craftingJob.getChannel(),
                    null);
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
                TerminalCraftingPlanStatic.Label.INCOMPLETE,
                -1,
                -1,
                null);
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
        List recipeOutputs = IntegratedCraftingHelpers.getPrototypesFromIngredients(exception.getRecipe().getOutput());
        return new TerminalCraftingPlanStatic<Integer>(
                0,
                dependencies,
                CraftingHelpers.multiplyPrototypedIngredients(recipeOutputs, exception.getQuantityMissing()),
                TerminalCraftingJobStatus.INVALID,
                exception.getQuantityMissing(),
                IntegratedCraftingHelpers.getPrototypesFromIngredients(exception.getIngredientsStorage()),
                Collections.emptyList(),
                TerminalCraftingPlanStatic.Label.INCOMPLETE,
                -1,
                -1,
                null);
    }

    protected static ITerminalCraftingPlan<Integer> newCraftingPlanErrorRecursive(List<IRecipeDefinition> childRecipes) {
        List<IPrototypedIngredient<?, ?>> recipeOutputs = IntegratedCraftingHelpers.getPrototypesFromIngredients(childRecipes.get(0).getOutput());
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
                TerminalCraftingPlanStatic.Label.RECURSION,
                -1,
                -1,
                null);
    }

    @Override
    public void startCraftingJob(INetwork network, int channel, ITerminalCraftingPlan<Integer> craftingPlan,
                                 ServerPlayerEntity player) throws CraftingJobStartException {
        if (craftingPlan instanceof TerminalCraftingPlanCraftingJobDependencyGraph
                && craftingPlan.getStatus() == TerminalCraftingJobStatus.UNSTARTED) {
            CraftingJobDependencyGraph craftingJobDependencyGraph = ((TerminalCraftingPlanCraftingJobDependencyGraph) craftingPlan).getCraftingJobDependencyGraph();
            try {
                CraftingHelpers.scheduleCraftingJobs(CraftingHelpers.getCraftingNetworkChecked(network), craftingJobDependencyGraph, true, player.getUniqueID());
            } catch (UnavailableCraftingInterfacesException e) {
                throw new CraftingJobStartException("gui.integratedterminals.terminal_storage.craftingplan.label.failed.insufficient_crafting_interfaces");
            }
        } else {
            IntegratedTerminals.clog(Level.WARN, "Tried to start an invalid crafting plan with status " + craftingPlan.getStatus());
        }
    }

    protected static ITerminalCraftingPlan<Integer> newErroredCraftingJob() {
        return new TerminalCraftingPlanStatic<>(
                0,
                Collections.emptyList(),
                Collections.emptyList(),
                TerminalCraftingJobStatus.INVALID,
                0,
                Collections.emptyList(),
                Collections.emptyList(),
                TerminalCraftingPlanStatic.Label.ERROR,
                -1,
                -1,
                null);
    }

    protected static ITerminalCraftingPlan<Integer> newActiveCraftingJob(ICraftingNetwork craftingNetwork, int channel,
                                                                         CraftingJob craftingJob,
                                                                         CraftingJobDependencyGraph dependencyGraph) {
        if (craftingJob == null) {
            return newErroredCraftingJob();
        }

        List recipeOutputs = IntegratedCraftingHelpers.getPrototypesFromIngredients(craftingJob.getRecipe().getOutput());
        List<ITerminalCraftingPlan<Integer>> dependencies = dependencyGraph.getDependencies(craftingJob)
                .stream()
                .map(subCraftingJob -> newActiveCraftingJob(craftingNetwork, channel, subCraftingJob, dependencyGraph))
                .collect(Collectors.toList());

        int craftingJobId = craftingJob.getId();
        ICraftingInterface craftingInterface = craftingNetwork.getCraftingJobInterface(craftingJob.getChannel(), craftingJobId);

        if (craftingInterface == null) {
            return newErroredCraftingJob();
        }

        // Determine status
        TerminalCraftingJobStatus jobStatus = TerminalCraftingJobStatus.UNSTARTED;
        switch (craftingInterface.getCraftingJobStatus(craftingNetwork, craftingJob.getChannel(), craftingJobId)) {
            case PENDING_INTERFACE:
                jobStatus = TerminalCraftingJobStatus.QUEUEING;
                break;
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

        List<IPrototypedIngredient<?, ?>> pendingOutputs = recipeOutputAmount == 0
                ? Lists.newArrayList() : CraftingHelpers.multiplyPrototypedIngredients(recipeOutputs, recipeOutputAmount);
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
                TerminalCraftingPlanStatic.Label.RUNNING,
                craftingNetwork.getRunningTicks(craftingJob),
                craftingJob.getChannel(),
                uuidToName(craftingJob.getInitiatorUuid()));
    }

    @Nullable
    protected static String uuidToName(@Nullable String uuid) {
        if (uuid != null) {
            try {
                UUID uuidObject = UUID.fromString(uuid);
                GameProfile profile = ServerLifecycleHooks.getCurrentServer().getPlayerProfileCache()
                        .getProfileByUUID(uuidObject);
                if (profile != null) {
                    return profile.getName();
                }
            } catch (IllegalArgumentException e) {}
        }
        return null;
    }

    @Override
    public List<ITerminalCraftingPlan<Integer>> getCraftingJobs(INetwork network, int channel) {
        ICraftingNetwork craftingNetwork = CraftingHelpers.getCraftingNetwork(network).orElse(null);
        if (craftingNetwork == null) {
            return Collections.emptyList();
        }

        Iterable<CraftingJob> iterable = () -> craftingNetwork.getCraftingJobs(channel);
        CraftingJobDependencyGraph dependencyGraph = craftingNetwork.getCraftingJobDependencyGraph();
        return StreamSupport.stream(iterable.spliterator(), false)
                .filter(job -> job.getDependentCraftingJobs().isEmpty()) // Only expose root jobs
                .map(job -> newActiveCraftingJob(craftingNetwork, channel, job, dependencyGraph))
                .sorted(Comparator.comparingInt(ITerminalCraftingPlan::getId))
                .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public ITerminalCraftingPlan<Integer> getCraftingJob(INetwork network, int channel, Integer craftingJobId) {
        ICraftingNetwork craftingNetwork = CraftingHelpers.getCraftingNetworkChecked(network);
        CraftingJob craftingJob = craftingNetwork.getCraftingJob(channel, craftingJobId);
        if (craftingJob != null) {
            CraftingJobDependencyGraph dependencyGraph = craftingNetwork.getCraftingJobDependencyGraph();
            return newActiveCraftingJob(craftingNetwork, channel, craftingJob, dependencyGraph);
        }
        return null;
    }

    @Override
    public boolean cancelCraftingJob(INetwork network, int channel, Integer craftingJobId) {
        ICraftingNetwork craftingNetwork = CraftingHelpers.getCraftingNetworkChecked(network);
        return craftingNetwork.cancelCraftingJob(channel, craftingJobId);
    }

    @Override
    public CompoundNBT serializeCraftingPlan(ITerminalCraftingPlan<Integer> craftingPlan) {
        CompoundNBT tag = TerminalCraftingPlanStatic.serialize((TerminalCraftingPlanStatic<Integer>) craftingPlan, this);
        if (craftingPlan instanceof TerminalCraftingPlanCraftingJobDependencyGraph) {
            CompoundNBT serializedGraph = CraftingJobDependencyGraph.serialize(
                    ((TerminalCraftingPlanCraftingJobDependencyGraph) craftingPlan).getCraftingJobDependencyGraph());
            tag.put("craftingJobDependencyGraph", serializedGraph);
        }
        return tag;
    }

    @Override
    public ITerminalCraftingPlan<Integer> deserializeCraftingPlan(CompoundNBT tag) throws IllegalArgumentException {
        TerminalCraftingPlanStatic<Integer> planStatic = TerminalCraftingPlanStatic.deserialize(tag, this);
        if (tag.contains("craftingJobDependencyGraph")) {
            CraftingJobDependencyGraph craftingJobDependencyGraph = CraftingJobDependencyGraph.deserialize(
                    tag.getCompound("craftingJobDependencyGraph"));
            TerminalCraftingPlanCraftingJobDependencyGraph graph = new TerminalCraftingPlanCraftingJobDependencyGraph(
                    planStatic.getId(),
                    planStatic.getDependencies(),
                    planStatic.getOutputs(),
                    planStatic.getStatus(),
                    planStatic.getCraftingQuantity(),
                    planStatic.getStorageIngredients(),
                    planStatic.getLastMissingIngredients(),
                    planStatic.getLabel(),
                    planStatic.getTickDuration(),
                    planStatic.getChannel(),
                    planStatic.getInitiatorName(),
                    craftingJobDependencyGraph
            );
            if (planStatic.getUnlocalizedLabelOverride() != null) {
                graph.setUnlocalizedLabelOverride(planStatic.getUnlocalizedLabelOverride());
            }
            return graph;
        } else {
            return planStatic;
        }
    }

    @Override
    public INBT serializeCraftingJobId(Integer id) {
        return IntNBT.valueOf(id);
    }

    @Override
    public Integer deserializeCraftingJobId(INBT tag) {
        return ((IntNBT) tag).getInt();
    }
}
