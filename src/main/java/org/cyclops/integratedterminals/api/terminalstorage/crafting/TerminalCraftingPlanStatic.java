package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author rubensworks
 */
public class TerminalCraftingPlanStatic<I> implements ITerminalCraftingPlan<I> {

    private final I id;
    private final List<ITerminalCraftingPlan<I>> dependencies;
    private final List<IPrototypedIngredient<?, ?>> outputs;
    private TerminalCraftingJobStatus status;
    private final long craftingQuantity;
    private final List<IPrototypedIngredient<?, ?>> bufferedIngredients;
    private final List<List<IPrototypedIngredient<?, ?>>> lastMissingIngredients;
    private TerminalCraftingPlanStatic.Label label;
    @Nullable
    private String unlocalizedLabelOverride;
    private final long tickDuration;
    private final int channel;
    @Nullable
    private final String initiatorName;

    public TerminalCraftingPlanStatic(I id,
                                      List<ITerminalCraftingPlan<I>> dependencies,
                                      List<IPrototypedIngredient<?, ?>> outputs,
                                      TerminalCraftingJobStatus status,
                                      long craftingQuantity,
                                      List<IPrototypedIngredient<?, ?>> bufferedIngredients,
                                      List<List<IPrototypedIngredient<?, ?>>> lastMissingIngredients,
                                      TerminalCraftingPlanStatic.Label label,
                                      long tickDuration,
                                      int channel,
                                      @Nullable String initiatorName) {
        this.id = id;
        this.dependencies = dependencies;
        this.outputs = outputs;
        this.status = status;
        this.craftingQuantity = craftingQuantity;
        this.bufferedIngredients = bufferedIngredients;
        this.lastMissingIngredients = lastMissingIngredients;
        this.label = label;
        this.unlocalizedLabelOverride = null;
        this.tickDuration = tickDuration;
        this.channel = channel;
        this.initiatorName = initiatorName;
    }

    @Override
    public I getId() {
        return id;
    }

    @Override
    public List<ITerminalCraftingPlan<I>> getDependencies() {
        return dependencies;
    }

    @Override
    public List<IPrototypedIngredient<?, ?>> getOutputs() {
        return outputs;
    }

    @Override
    public TerminalCraftingJobStatus getStatus() {
        return status;
    }

    @Override
    public long getCraftingQuantity() {
        return craftingQuantity;
    }

    @Override
    public List<IPrototypedIngredient<?,?>> getBufferedIngredients() {
        return bufferedIngredients;
    }

    @Override
    public List<List<IPrototypedIngredient<?, ?>>> getLastMissingIngredients() {
        return lastMissingIngredients;
    }

    public Label getLabel() {
        return label;
    }

    @Nullable
    public String getUnlocalizedLabelOverride() {
        return this.unlocalizedLabelOverride;
    }

    public void setUnlocalizedLabelOverride(@Nullable String unlocalizedLabelOverride) {
        this.unlocalizedLabelOverride = unlocalizedLabelOverride;
    }

    @Override
    public String getUnlocalizedLabel() {
        if (this.unlocalizedLabelOverride == null) {
            return this.label.getUnlocalizedMessage();
        }
        return this.unlocalizedLabelOverride;
    }

    @Override
    public long getTickDuration() {
        return tickDuration;
    }

    @Override
    public int getChannel() {
        return channel;
    }

    @Override
    @Nullable
    public String getInitiatorName() {
        return initiatorName;
    }

    @Override
    public void setError(String unlocalizedError) {
        this.status = TerminalCraftingJobStatus.ERROR;
        this.unlocalizedLabelOverride = unlocalizedError;
    }

    @Override
    public ITerminalCraftingPlanFlat<I> flatten() {
        // Group dependencies by prototype
        IndexedEntries indexedEntries = new IndexedEntries();
        Set<I> handledPlans = new HashSet<>();
        groupDependenciesByPrototype(indexedEntries, handledPlans, this);

        // Make plan
        TerminalCraftingPlanFlatStatic<I> planFlat = new TerminalCraftingPlanFlatStatic<>(
                getId(),
                indexedEntries.getEntries().stream()
                        .sorted((e1, e2) -> {
                            if (e1.getQuantityMissing() != e2.getQuantityMissing()) {
                                return Math.toIntExact(e2.getQuantityMissing() - e1.getQuantityMissing());
                            }
                            if (e1.getQuantityCrafting() != e2.getQuantityCrafting()) {
                                return Math.toIntExact(e2.getQuantityCrafting() - e1.getQuantityCrafting());
                            }
                            if (e1.getQuantityToCraft() != e2.getQuantityToCraft()) {
                                return Math.toIntExact(e2.getQuantityToCraft() - e1.getQuantityToCraft());
                            }
                            if (e1.getQuantityInStorage() != e2.getQuantityInStorage()) {
                                return Math.toIntExact(e2.getQuantityInStorage() - e1.getQuantityInStorage());
                            }
                            return 0;
                        })
                        .toList(),
                getOutputs(),
                getStatus(),
                getLabel(),
                getTickDuration(),
                getChannel(),
                getInitiatorName()
        );
        if (getUnlocalizedLabelOverride() != null) {
            planFlat.setUnlocalizedLabelOverride(getUnlocalizedLabelOverride());
        }
        return planFlat;
    }

    public static class IndexedEntries {
        /**
         * Entries indexed by a canonical list of prototype ingredients.
         * <p>
         * Each key is a list of {@link IPrototypedIngredient} where:
         * <ul>
         *     <li>Every element has quantity 1.</li>
         *     <li>Every element uses the exact-match-no-quantity condition.</li>
         * </ul>
         * The original (possibly non-normalized) alternatives list is stored inside the
         * {@link TerminalCraftingPlanFlatStatic.Entry} for rendering purposes.
         */
        private final Map<List<IPrototypedIngredient<?, ?>>, TerminalCraftingPlanFlatStatic.Entry> indexedEntries;

        public IndexedEntries() {
            this.indexedEntries = Maps.newHashMap();
        }

        /**
         * Get (or create) the entry corresponding to the given list of alternative ingredients.
         *
         * @param prototypedIngredients A non-empty list of alternatives.
         * @return The corresponding flat plan entry.
         */
        public TerminalCraftingPlanFlatStatic.Entry get(List<IPrototypedIngredient<?, ?>> prototypedIngredients) {
            List<IPrototypedIngredient<?, ?>> key = getPrototypes(prototypedIngredients);
            return indexedEntries.computeIfAbsent(key,
                    k -> new TerminalCraftingPlanFlatStatic.Entry(k));
        }

        /**
         * Build a canonical list of prototype ingredients for the given alternatives.
         * Quantities are normalized to 1 and the exact-match-no-quantity condition is used.
         */
        protected List<IPrototypedIngredient<?, ?>> getPrototypes(List<IPrototypedIngredient<?, ?>> prototypedIngredients) {
            List<IPrototypedIngredient<?, ?>> result = new ArrayList<>(prototypedIngredients.size());
            for (IPrototypedIngredient<?, ?> ingredient : prototypedIngredients) {
                IIngredientMatcher matcher = ingredient.getComponent().getMatcher();
                result.add(new PrototypedIngredient(
                        ingredient.getComponent(),
                        matcher.withQuantity(ingredient.getPrototype(), 1L),
                        matcher.getExactMatchNoQuantityCondition()));
            }
            return result;
        }

        /**
         * Get the quantity associated with the given alternatives list.
         * <p>
         * This is derived from the first element, which is consistent with prior behaviour
         * when only a single prototype was available.
         */
        public static long getQuantity(List<IPrototypedIngredient<?, ?>> prototypedIngredients) {
            if (prototypedIngredients.isEmpty()) {
                return 0;
            }
            IPrototypedIngredient<?, ?> first = prototypedIngredients.get(0);
            IIngredientMatcher matcher = first.getComponent().getMatcher();
            return matcher.getQuantity(first.getPrototype());
        }

        public Collection<TerminalCraftingPlanFlatStatic.Entry> getEntries() {
            return indexedEntries.values();
        }
    }

    protected static <I> void groupDependenciesByPrototype(IndexedEntries indexedEntries, Set<I> handledPlans, ITerminalCraftingPlan<I> plan) {
        // Since jobs can have multiple dependents due to job splitting, we only consider each job once during flattening.
        if ((!(plan.getId() instanceof Integer id) || id > 0) && handledPlans.contains(plan.getId())) {
            return;
        }
        handledPlans.add(plan.getId());

        // Determine outputs that are invalid or will be crafted
        for (IPrototypedIngredient<?, ?> output : plan.getOutputs()) {
            List<IPrototypedIngredient<?, ?>> outputs = List.of(output);
            TerminalCraftingPlanFlatStatic.Entry entry = indexedEntries.get(outputs);
            long quantity = IndexedEntries.getQuantity(outputs);

            if (plan.getStatus() == TerminalCraftingJobStatus.ERROR
                    || plan.getStatus() == TerminalCraftingJobStatus.INVALID
                    || plan.getStatus() == TerminalCraftingJobStatus.INVALID_INPUTS) {
                if (plan.getDependencies().isEmpty()) {
                    entry.setQuantityMissing(entry.getQuantityMissing() + quantity);
                } else {
                    entry.setQuantityToCraft(entry.getQuantityToCraft() + quantity);
                }
            }
            if (plan.getStatus() == TerminalCraftingJobStatus.QUEUEING
                    || plan.getStatus() == TerminalCraftingJobStatus.PENDING_DEPENDENCIES
                    || plan.getStatus() == TerminalCraftingJobStatus.PENDING_INPUTS
                    || plan.getStatus() == TerminalCraftingJobStatus.UNSTARTED) {
                entry.setQuantityToCraft(entry.getQuantityToCraft() + quantity);
            }
            if (plan.getStatus() == TerminalCraftingJobStatus.CRAFTING ||
                    plan.getStatus() == TerminalCraftingJobStatus.FINISHED) {
                entry.setQuantityCrafting(entry.getQuantityCrafting() + quantity);
            }
        }

        // Determine storage ingredients
        for (IPrototypedIngredient<?, ?> output : plan.getBufferedIngredients()) {
            List<IPrototypedIngredient<?, ?>> outputs = List.of(output);
            TerminalCraftingPlanFlatStatic.Entry entry = indexedEntries.get(outputs);
            long quantity = IndexedEntries.getQuantity(outputs);
            entry.setQuantityInStorage(entry.getQuantityInStorage() + quantity);
        }

        // Determine missing ingredients
        for (List<IPrototypedIngredient<?, ?>> outputVariants : plan.getLastMissingIngredients()) {
            TerminalCraftingPlanFlatStatic.Entry entry = indexedEntries.get(outputVariants);
            long quantity = IndexedEntries.getQuantity(outputVariants);
            entry.setQuantityMissing(entry.getQuantityMissing() + quantity * plan.getCraftingQuantity());
        }

        // Recurse into dependencies
        for (ITerminalCraftingPlan<I> dependency : plan.getDependencies()) {
            groupDependenciesByPrototype(indexedEntries, handledPlans, dependency);
        }
    }

    public static <I> void serialize(ValueOutput valueOutput, TerminalCraftingPlanStatic<I> plan,
                                     ITerminalStorageTabIngredientCraftingHandler<?, I> handler) {
        handler.serializeCraftingJobId(valueOutput, plan.getId());

        ValueOutput.ValueOutputList dependencies = valueOutput.childrenList("dependencies");
        for (ITerminalCraftingPlan<I> dependency : plan.getDependencies()) {
            TerminalCraftingPlanStatic.serialize(dependencies.addChild(), (TerminalCraftingPlanStatic) dependency, handler);
        }

        ValueOutput.ValueOutputList outputs = valueOutput.childrenList("outputs");
        for (IPrototypedIngredient<?, ?> output : plan.getOutputs()) {
            IPrototypedIngredient.serialize(outputs.addChild(), (PrototypedIngredient) output);
        }

        valueOutput.putInt("status", plan.getStatus().ordinal());

        valueOutput.putLong("craftingQuantity", plan.getCraftingQuantity());

        ValueOutput.ValueOutputList bufferedIngredients = valueOutput.childrenList("bufferedIngredients");
        for (IPrototypedIngredient<?, ?> storageIngredient : plan.getBufferedIngredients()) {
            IPrototypedIngredient.serialize(bufferedIngredients.addChild(), (PrototypedIngredient) storageIngredient);
        }

        ValueOutput.ValueOutputList lastMissingIngredients = valueOutput.childrenList("lastMissingIngredients");
        for (List<IPrototypedIngredient<?, ?>> lastMissingIngredient : plan.getLastMissingIngredients()) {
            ValueOutput child = lastMissingIngredients.addChild();
            ValueOutput.ValueOutputList lastMissingIngredientTag = child.childrenList("v");
            for (IPrototypedIngredient<?, ?> prototypedIngredient : lastMissingIngredient) {
                IPrototypedIngredient.serialize(lastMissingIngredientTag.addChild(), (PrototypedIngredient) prototypedIngredient);
            }
        }

        valueOutput.putInt("label", plan.label.ordinal());
        if (plan.unlocalizedLabelOverride != null) {
            valueOutput.putString("unlocalizedLabelOverride", plan.unlocalizedLabelOverride);
        }

        valueOutput.putLong("tickDuration", plan.getTickDuration());

        valueOutput.putInt("channel", plan.getChannel());

        if (plan.getInitiatorName() != null) {
            valueOutput.putString("initiatorName", plan.getInitiatorName());
        }
    }

    public static <I> TerminalCraftingPlanStatic<I> deserialize(ValueInput valueInput,
                                                                ITerminalStorageTabIngredientCraftingHandler<?, I> handler) {
        I id = handler.deserializeCraftingJobId(valueInput);

        List<ITerminalCraftingPlan<I>> dependencies = Lists.newArrayList();
        for (ValueInput dependency : valueInput.childrenList("dependencies").orElseThrow()) {
            dependencies.add(TerminalCraftingPlanStatic.deserialize(dependency, handler));
        }

        List<IPrototypedIngredient<?, ?>> outputs = Lists.newArrayList();
        for (ValueInput output : valueInput.childrenList("outputs").orElseThrow()) {
            outputs.add(IPrototypedIngredient.deserialize(output));
        }

        TerminalCraftingJobStatus status = TerminalCraftingJobStatus.values()[valueInput.getInt("status").orElseThrow()];

        long craftingQuantity = valueInput.getLong("craftingQuantity").orElseThrow();

        List<IPrototypedIngredient<?, ?>> bufferedIngredients = Lists.newArrayList();
        for (ValueInput storageIngredient : valueInput.childrenList("bufferedIngredients").orElseThrow()) {
            bufferedIngredients.add(IPrototypedIngredient.deserialize(storageIngredient));
        }

        List<List<IPrototypedIngredient<?, ?>>> lastMissingIngredients = Lists.newArrayList();
        for (ValueInput lastMissingIngredientValue : valueInput.childrenList("lastMissingIngredients").orElseThrow()) {
            List<IPrototypedIngredient<?, ?>> lastMissingIngredient = Lists.newArrayList();
            for (ValueInput base : lastMissingIngredientValue.childrenList("v").orElseThrow()) {
                lastMissingIngredient.add(IPrototypedIngredient.deserialize(base));
            }
            lastMissingIngredients.add(lastMissingIngredient);
        }

        Label label = Label.values()[valueInput.getInt("label").orElseThrow()];

        String unlocalizedLabelOverride = valueInput.getStringOr("unlocalizedLabelOverride", null);

        long tickDuration = valueInput.getLong("tickDuration").orElseThrow();

        int channel = valueInput.getInt("channel").orElseThrow();

        String initiatorName = valueInput.getStringOr("initiatorName", null);

        TerminalCraftingPlanStatic<I> plan = new TerminalCraftingPlanStatic<>(id, dependencies, outputs, status, craftingQuantity, bufferedIngredients,
                lastMissingIngredients, label, tickDuration, channel, initiatorName);
        if (unlocalizedLabelOverride != null) {
            plan.unlocalizedLabelOverride = unlocalizedLabelOverride;
        }
        return plan;
    }

    public static enum Label {
        RUNNING("gui.integratedterminals.terminal_storage.craftingplan.label.running"),
        VALID("gui.integratedterminals.terminal_storage.craftingplan.label.valid"),
        INCOMPLETE("gui.integratedterminals.terminal_storage.craftingplan.label.failed.incomplete"),
        RECURSION("gui.integratedterminals.terminal_storage.craftingplan.label.failed.recursion"),
        ERROR("ERROR");

        private final String unlocalizedMessage;

        Label(String unlocalizedMessage) {
            this.unlocalizedMessage = unlocalizedMessage;
        }

        public String getUnlocalizedMessage() {
            return this.unlocalizedMessage;
        }
    }
}
