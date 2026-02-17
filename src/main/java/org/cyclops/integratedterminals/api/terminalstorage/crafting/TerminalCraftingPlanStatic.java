package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
        if (handledPlans.contains(plan.getId())) {
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

    public static <I> CompoundTag serialize(TerminalCraftingPlanStatic<I> plan,
                                            ITerminalStorageTabIngredientCraftingHandler<?, I> handler) {
        CompoundTag tag = new CompoundTag();

        tag.put("id", handler.serializeCraftingJobId(plan.getId()));

        ListTag dependencies = new ListTag();
        for (ITerminalCraftingPlan<I> dependency : plan.getDependencies()) {
            dependencies.add(TerminalCraftingPlanStatic.serialize((TerminalCraftingPlanStatic) dependency, handler));
        }
        tag.put("dependencies", dependencies);

        ListTag outputs = new ListTag();
        for (IPrototypedIngredient<?, ?> output : plan.getOutputs()) {
            outputs.add(IPrototypedIngredient.serialize((PrototypedIngredient) output));
        }
        tag.put("outputs", outputs);

        tag.putInt("status", plan.getStatus().ordinal());

        tag.putLong("craftingQuantity", plan.getCraftingQuantity());

        ListTag bufferedIngredients = new ListTag();
        for (IPrototypedIngredient<?, ?> storageIngredient : plan.getBufferedIngredients()) {
            bufferedIngredients.add(IPrototypedIngredient.serialize((PrototypedIngredient) storageIngredient));
        }
        tag.put("bufferedIngredients", bufferedIngredients);

        ListTag lastMissingIngredients = new ListTag();
        for (List<IPrototypedIngredient<?, ?>> lastMissingIngredient : plan.getLastMissingIngredients()) {
            ListTag lastMissingIngredientTag = new ListTag();
            for (IPrototypedIngredient<?, ?> prototypedIngredient : lastMissingIngredient) {
                lastMissingIngredientTag.add(IPrototypedIngredient.serialize((PrototypedIngredient) prototypedIngredient));
            }
            lastMissingIngredients.add(lastMissingIngredientTag);
        }
        tag.put("lastMissingIngredients", lastMissingIngredients);

        tag.putInt("label", plan.label.ordinal());
        if (plan.unlocalizedLabelOverride != null) {
            tag.putString("unlocalizedLabelOverride", plan.unlocalizedLabelOverride);
        }

        tag.putLong("tickDuration", plan.getTickDuration());

        tag.putInt("channel", plan.getChannel());

        if (plan.getInitiatorName() != null) {
            tag.putString("initiatorName", plan.getInitiatorName());
        }

        return tag;
    }

    public static <I> TerminalCraftingPlanStatic<I> deserialize(CompoundTag tag,
                                                                ITerminalStorageTabIngredientCraftingHandler<?, I> handler) {
        if (!tag.contains("id")) {
            throw new IllegalArgumentException("Could not find an id entry in the given tag");
        }
        if (!tag.contains("dependencies", Tag.TAG_LIST)) {
            throw new IllegalArgumentException("Could not find a dependencies entry in the given tag");
        }
        if (!tag.contains("outputs", Tag.TAG_LIST)) {
            throw new IllegalArgumentException("Could not find a outputs entry in the given tag");
        }
        if (!tag.contains("status", Tag.TAG_INT)) {
            throw new IllegalArgumentException("Could not find a status entry in the given tag");
        }
        if (!tag.contains("craftingQuantity", Tag.TAG_LONG)) {
            throw new IllegalArgumentException("Could not find a craftingQuantity entry in the given tag");
        }
        if (!tag.contains("bufferedIngredients", Tag.TAG_LIST)) {
            throw new IllegalArgumentException("Could not find a bufferedIngredients entry in the given tag");
        }
        if (!tag.contains("lastMissingIngredients", Tag.TAG_LIST)) {
            throw new IllegalArgumentException("Could not find a lastMissingIngredients entry in the given tag");
        }
        if (!tag.contains("label", Tag.TAG_INT)) {
            throw new IllegalArgumentException("Could not find a label entry in the given tag");
        }
        if (!tag.contains("tickDuration", Tag.TAG_LONG)) {
            throw new IllegalArgumentException("Could not find a tickDuration entry in the given tag");
        }
        if (!tag.contains("channel", Tag.TAG_INT)) {
            throw new IllegalArgumentException("Could not find a channel entry in the given tag");
        }

        I id = handler.deserializeCraftingJobId(tag.get("id"));

        ListTag dependenciesTag = tag.getList("dependencies", Tag.TAG_COMPOUND);
        List<ITerminalCraftingPlan<I>> dependencies = Lists.newArrayListWithExpectedSize(dependenciesTag.size());
        for (Tag nbtBase : dependenciesTag) {
            dependencies.add(TerminalCraftingPlanStatic.deserialize((CompoundTag) nbtBase, handler));
        }

        ListTag outputsTag = tag.getList("outputs", Tag.TAG_COMPOUND);
        List<IPrototypedIngredient<?, ?>> outputs = Lists.newArrayListWithExpectedSize(outputsTag.size());
        for (Tag nbtBase : outputsTag) {
            outputs.add(IPrototypedIngredient.deserialize((CompoundTag) nbtBase));
        }

        TerminalCraftingJobStatus status = TerminalCraftingJobStatus.values()[tag.getInt("status")];

        long craftingQuantity = tag.getLong("craftingQuantity");

        ListTag bufferedIngredientsTag = tag.getList("bufferedIngredients", Tag.TAG_COMPOUND);
        List<IPrototypedIngredient<?, ?>> bufferedIngredients = Lists.newArrayListWithExpectedSize(bufferedIngredientsTag.size());
        for (Tag nbtBase : bufferedIngredientsTag) {
            bufferedIngredients.add(IPrototypedIngredient.deserialize((CompoundTag) nbtBase));
        }

        ListTag lastMissingIngredientsTag = tag.getList("lastMissingIngredients", Tag.TAG_LIST);
        List<List<IPrototypedIngredient<?, ?>>> lastMissingIngredients = Lists.newArrayListWithExpectedSize(lastMissingIngredientsTag.size());
        for (Tag nbtBase : lastMissingIngredientsTag) {
            ListTag list = ((ListTag) nbtBase);
            List<IPrototypedIngredient<?, ?>> lastMissingIngredient = Lists.newArrayListWithExpectedSize(list.size());
            for (Tag base : list) {
                lastMissingIngredient.add(IPrototypedIngredient.deserialize((CompoundTag) base));
            }
            lastMissingIngredients.add(lastMissingIngredient);
        }

        Label label = Label.values()[tag.getInt("label")];

        String unlocalizedLabelOverride = null;
        if (tag.contains("unlocalizedLabelOverride")) {
            unlocalizedLabelOverride = tag.getString("unlocalizedLabelOverride");
        }

        long tickDuration = tag.getLong("tickDuration");

        int channel = tag.getInt("channel");

        String initiatorName = null;
        if (tag.contains("initiatorName", Tag.TAG_STRING)) {
            initiatorName = tag.getString("initiatorName");
        }

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
