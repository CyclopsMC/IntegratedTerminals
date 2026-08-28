package org.cyclops.integratedterminals.core.terminalstorage.crafting;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The pending outputs of all running crafting jobs of a single ingredient component, indexed by channel.
 *
 * Instances are indexed independent of their quantity,
 * so that they can be looked up by the instances that are shown in the storage terminal.
 *
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class PendingCraftingJobOutputs<T, M> {

    private final IngredientComponent<T, M> ingredientComponent;
    private final Int2ObjectMap<Map<T, PendingCraftingJobOutput<T>>> channeledOutputs;

    /**
     * Collect the outputs that all running crafting jobs in the given network are still expected to produce.
     * @param ingredientComponent The ingredient component to collect the outputs for.
     * @param network A network.
     * @param <T> The instance type.
     * @param <M> The matching condition parameter.
     * @return The pending crafting job outputs.
     */
    public static <T, M> PendingCraftingJobOutputs<T, M> collectFromNetwork(IngredientComponent<T, M> ingredientComponent,
                                                                           INetwork network) {
        PendingCraftingJobOutputs<T, M> pendingCraftingJobOutputs = new PendingCraftingJobOutputs<>(ingredientComponent);
        for (ITerminalStorageTabIngredientCraftingHandler<?, ?> handler : TerminalStorageTabIngredientCraftingHandlers.REGISTRY.getHandlers()) {
            Set<Object> handledPlans = Sets.newHashSet();
            for (ITerminalCraftingPlan<?> craftingJob : handler.getCraftingJobs(network, IPositionedAddonsNetwork.WILDCARD_CHANNEL)) {
                pendingCraftingJobOutputs.addCraftingPlan(craftingJob, handledPlans);
            }
        }
        return pendingCraftingJobOutputs;
    }

    public PendingCraftingJobOutputs(IngredientComponent<T, M> ingredientComponent) {
        this.ingredientComponent = ingredientComponent;
        this.channeledOutputs = new Int2ObjectOpenHashMap<>();
    }

    public IngredientComponent<T, M> getIngredientComponent() {
        return ingredientComponent;
    }

    /**
     * Add a pending crafting job output.
     *
     * If the given instance is already pending in the given channel,
     * the quantities are summed, and the most relevant status is kept.
     *
     * @param channel A channel id.
     * @param instance An instance, where the quantity indicates the pending quantity.
     * @param status The status of the crafting job that will produce the given instance.
     */
    public void add(int channel, T instance, TerminalCraftingJobStatus status) {
        IIngredientMatcher<T, M> matcher = this.ingredientComponent.getMatcher();
        if (matcher.isEmpty(instance)) {
            return;
        }

        Map<T, PendingCraftingJobOutput<T>> outputs = this.channeledOutputs
                .computeIfAbsent(channel, (c) -> new TreeMap<>(matcher));
        T key = matcher.withQuantity(instance, 1);
        PendingCraftingJobOutput<T> existingOutput = outputs.get(key);
        if (existingOutput != null) {
            instance = matcher.withQuantity(instance, addQuantities(matcher,
                    matcher.getQuantity(existingOutput.getInstance()), matcher.getQuantity(instance)));
            if (PendingCraftingJobOutput.getStatusPriority(existingOutput.getStatus())
                    >= PendingCraftingJobOutput.getStatusPriority(status)) {
                status = existingOutput.getStatus();
            }
        }
        outputs.put(key, new PendingCraftingJobOutput<>(instance, status));
    }

    /**
     * Add all outputs that the given crafting plan and its dependencies are still expected to produce.
     * @param craftingPlan A crafting plan.
     * @param handledPlans The ids of the plans that were already taken into account.
     */
    public void addCraftingPlan(ITerminalCraftingPlan<?> craftingPlan, Set<Object> handledPlans) {
        // Jobs can occur multiple times within a plan due to job splitting, so only take each of them into account once.
        if ((!(craftingPlan.getId() instanceof Integer id) || id > 0) && !handledPlans.add(craftingPlan.getId())) {
            return;
        }

        // Jobs report FINISHED in-between the batches that they hand to their crafting interface,
        // so this status does not mean that the job is done: a job that is done is not exposed as a running job anymore.
        // As the outputs of such a job are still pending, we show them as actively being crafted.
        TerminalCraftingJobStatus status = craftingPlan.getStatus() == TerminalCraftingJobStatus.FINISHED
                ? TerminalCraftingJobStatus.CRAFTING : craftingPlan.getStatus();
        for (IPrototypedIngredient<?, ?> output : craftingPlan.getOutputs()) {
            if (output.getComponent() == this.ingredientComponent) {
                add(craftingPlan.getChannel(), (T) output.getPrototype(), status);
            }
        }

        for (ITerminalCraftingPlan<?> dependency : craftingPlan.getDependencies()) {
            addCraftingPlan(dependency, handledPlans);
        }
    }

    /**
     * Get the pending output for the given instance, independent of the instance's quantity.
     * @param channel A channel id.
     * @param instance An instance.
     * @return The pending output, or null if the given instance is not being crafted.
     */
    @Nullable
    public PendingCraftingJobOutput<T> get(int channel, T instance) {
        Map<T, PendingCraftingJobOutput<T>> outputs = this.channeledOutputs.get(channel);
        if (outputs == null) {
            return null;
        }
        IIngredientMatcher<T, M> matcher = this.ingredientComponent.getMatcher();
        return matcher.isEmpty(instance) ? null : outputs.get(matcher.withQuantity(instance, 1));
    }

    /**
     * @return All pending outputs, indexed by channel.
     */
    public Int2ObjectMap<Map<T, PendingCraftingJobOutput<T>>> getChanneledOutputs() {
        return channeledOutputs;
    }

    /**
     * @return If no crafting job outputs are pending.
     */
    public boolean isEmpty() {
        return this.channeledOutputs.isEmpty();
    }

    private static <T, M> long addQuantities(IIngredientMatcher<T, M> matcher, long quantity, long quantityToAdd) {
        long maxQuantity = matcher.getMaximumQuantity();
        try {
            return Math.min(maxQuantity, Math.addExact(quantity, quantityToAdd));
        } catch (ArithmeticException e) {
            return maxQuantity;
        }
    }

}
