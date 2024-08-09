package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_INTEGRATECRAFTING, "crafting_network");

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
    public CompoundTag serializeCraftingOption(HolderLookup.Provider lookupProvider, TerminalCraftingOptionRecipeDefinition craftingOption) {
        return IRecipeDefinition.serialize(lookupProvider, craftingOption.getRecipe());
    }

    @Override
    public <T, M> TerminalCraftingOptionRecipeDefinition deserializeCraftingOption(HolderLookup.Provider lookupProvider, IngredientComponent<T, M> ingredientComponent, CompoundTag tag) throws IllegalArgumentException {
        return new TerminalCraftingOptionRecipeDefinition<>(ingredientComponent, IRecipeDefinition.deserialize(lookupProvider, tag));
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
                                 ServerPlayer player) throws CraftingJobStartException {
        if (craftingPlan instanceof TerminalCraftingPlanCraftingJobDependencyGraph
                && craftingPlan.getStatus() == TerminalCraftingJobStatus.UNSTARTED) {
            CraftingJobDependencyGraph craftingJobDependencyGraph = ((TerminalCraftingPlanCraftingJobDependencyGraph) craftingPlan).getCraftingJobDependencyGraph();
            try {
                CraftingHelpers.scheduleCraftingJobs(CraftingHelpers.getCraftingNetworkChecked(network), craftingJobDependencyGraph, true, player.getUUID());
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
            List<Map<IngredientComponent<?, ?>, List<IPrototypedIngredient<?, ?>>>> pendingOutputEntries = craftingInterface.getPendingCraftingJobOutputs(craftingJobId);
            for (Map<IngredientComponent<?, ?>, List<IPrototypedIngredient<?, ?>>> map : craftingInterface.getPendingCraftingJobOutputs(craftingJobId)) {
                for (List<IPrototypedIngredient<?, ?>> values : map.values()) {
                    for (IPrototypedIngredient<?, ?> value : values) {
                        // Add by stacking with identical prototypes
                        boolean stacked = false;
                        Iterator<IPrototypedIngredient<?, ?>> it = auxiliaryPendingOutputs.iterator();
                        while (it.hasNext()) {
                            IPrototypedIngredient<?, ?> existingOutput = it.next();
                            IIngredientMatcher matcher = existingOutput.getComponent().getMatcher();
                            if (existingOutput.getComponent() == value.getComponent()
                                && existingOutput.getCondition().equals(value.getCondition())
                                && matcher.matches(existingOutput.getPrototype(), value.getPrototype(), matcher.getExactMatchNoQuantityCondition())) {
                                stacked = true;
                                it.remove();
                                auxiliaryPendingOutputs.add(new PrototypedIngredient(
                                        existingOutput.getComponent(),
                                        matcher.withQuantity(existingOutput.getPrototype(),
                                                matcher.getQuantity(existingOutput.getPrototype()) + matcher.getQuantity(value.getPrototype())),
                                        existingOutput.getCondition()
                                ));
                                break;
                            }
                        }

                        if (!stacked) {
                            auxiliaryPendingOutputs.add(value);
                        }
                    }
                }
            }

            // Reduce the amount by the amount of running entries, as we consider this separately.
            recipeOutputAmount -= pendingOutputEntries.size();
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
                return ServerLifecycleHooks.getCurrentServer().getProfileCache()
                        .get(uuidObject)
                        .map(GameProfile::getName)
                        .orElse(null);
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
    public CompoundTag serializeCraftingPlan(HolderLookup.Provider lookupProvider, ITerminalCraftingPlan<Integer> craftingPlan) {
        CompoundTag tag = TerminalCraftingPlanStatic.serialize(lookupProvider, (TerminalCraftingPlanStatic<Integer>) craftingPlan, this);
        if (craftingPlan instanceof TerminalCraftingPlanCraftingJobDependencyGraph) {
            CompoundTag serializedGraph = CraftingJobDependencyGraph.serialize(
                    lookupProvider,
                    ((TerminalCraftingPlanCraftingJobDependencyGraph) craftingPlan).getCraftingJobDependencyGraph());
            tag.put("craftingJobDependencyGraph", serializedGraph);
        }
        return tag;
    }

    @Override
    public ITerminalCraftingPlan<Integer> deserializeCraftingPlan(HolderLookup.Provider lookupProvider, CompoundTag tag) throws IllegalArgumentException {
        TerminalCraftingPlanStatic<Integer> planStatic = TerminalCraftingPlanStatic.deserialize(lookupProvider, tag, this);
        if (tag.contains("craftingJobDependencyGraph")) {
            CraftingJobDependencyGraph craftingJobDependencyGraph = CraftingJobDependencyGraph.deserialize(
                    lookupProvider,
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
    public Tag serializeCraftingJobId(Integer id) {
        return IntTag.valueOf(id);
    }

    @Override
    public Integer deserializeCraftingJobId(Tag tag) {
        return ((IntTag) tag).getAsInt();
    }
}
