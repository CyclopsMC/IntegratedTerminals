package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
        groupDependenciesByPrototype(indexedEntries, this);

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
        private final Map<IPrototypedIngredient<?, ?>, TerminalCraftingPlanFlatStatic.Entry> indexedEntries;

        public IndexedEntries() {
            this.indexedEntries = Maps.newHashMap();
        }

        public TerminalCraftingPlanFlatStatic.Entry get(IPrototypedIngredient<?, ?> prototypedIngredient) {
            IPrototypedIngredient<?, ?> prototype = getPrototype(prototypedIngredient);
            return indexedEntries.computeIfAbsent(prototype, k -> new TerminalCraftingPlanFlatStatic.Entry(new PrototypedIngredient(prototypedIngredient.getComponent(), prototype.getPrototype(), prototypedIngredient.getCondition())));
        }

        protected <T, M> IPrototypedIngredient<T, M> getPrototype(IPrototypedIngredient<T, M> prototypedIngredient) {
            IIngredientMatcher<T, M> matcher = prototypedIngredient.getComponent().getMatcher();
            return new PrototypedIngredient(prototypedIngredient.getComponent(), matcher.withQuantity(prototypedIngredient.getPrototype(), 1L), matcher.getExactMatchNoQuantityCondition());
        }

        public static long getQuantity(IPrototypedIngredient<?, ?> prototypedIngredient) {
            IIngredientMatcher matcher = prototypedIngredient.getComponent().getMatcher();
            return matcher.getQuantity(prototypedIngredient.getPrototype());
        }

        public Collection<TerminalCraftingPlanFlatStatic.Entry> getEntries() {
            return indexedEntries.values();
        }
    }

    protected static <I> void groupDependenciesByPrototype(IndexedEntries indexedEntries, ITerminalCraftingPlan<I> plan) {
        // Determine outputs that are invalid or will be crafted
        for (IPrototypedIngredient<?, ?> output : plan.getOutputs()) {
            TerminalCraftingPlanFlatStatic.Entry entry = indexedEntries.get(output);
            long quantity = IndexedEntries.getQuantity(output);

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
                    || plan.getStatus() == TerminalCraftingJobStatus.CRAFTING
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
            TerminalCraftingPlanFlatStatic.Entry entry = indexedEntries.get(output);
            long quantity = IndexedEntries.getQuantity(output);
            entry.setQuantityInStorage(entry.getQuantityInStorage() + quantity);
        }

        // Determine missing ingredients
        for (List<IPrototypedIngredient<?, ?>> outputVariants : plan.getLastMissingIngredients()) {
            IPrototypedIngredient<?, ?> output = outputVariants.stream().findFirst().get();
            TerminalCraftingPlanFlatStatic.Entry entry = indexedEntries.get(output);
            long quantity = IndexedEntries.getQuantity(output);
            entry.setQuantityMissing(entry.getQuantityMissing() + quantity * plan.getCraftingQuantity());
        }

        // Recurse into dependencies
        for (ITerminalCraftingPlan<I> dependency : plan.getDependencies()) {
            groupDependenciesByPrototype(indexedEntries, dependency);
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
