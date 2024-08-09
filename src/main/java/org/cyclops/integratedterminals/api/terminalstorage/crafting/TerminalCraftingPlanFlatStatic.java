package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import com.google.common.collect.Lists;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author rubensworks
 */
public class TerminalCraftingPlanFlatStatic<I> implements ITerminalCraftingPlanFlat<I> {

    private final I id;
    private final List<IPrototypedIngredient<?, ?>> outputs;
    private final List<TerminalCraftingPlanFlatStatic.Entry> entries;
    private TerminalCraftingJobStatus status;
    private TerminalCraftingPlanStatic.Label label;
    @Nullable
    private String unlocalizedLabelOverride;
    private final long tickDuration;
    private final int channel;
    @Nullable
    private final String initiatorName;

    public TerminalCraftingPlanFlatStatic(I id,
                                          List<TerminalCraftingPlanFlatStatic.Entry> entries,
                                          List<IPrototypedIngredient<?, ?>> outputs,
                                          TerminalCraftingJobStatus status,
                                          TerminalCraftingPlanStatic.Label label,
                                          long tickDuration,
                                          int channel,
                                          @Nullable String initiatorName) {
        this.id = id;
        this.entries = entries;
        this.outputs = outputs;
        this.status = status;
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
    public List<TerminalCraftingPlanFlatStatic.Entry> getEntries() {
        return this.entries;
    }

    @Override
    public List<IPrototypedIngredient<?, ?>> getOutputs() {
        return outputs;
    }

    @Override
    public TerminalCraftingJobStatus getStatus() {
        return status;
    }

    public TerminalCraftingPlanStatic.Label getLabel() {
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
        if (this.getUnlocalizedLabelOverride() == null) {
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

    public static <I> CompoundTag serialize(HolderLookup.Provider lookupProvider, TerminalCraftingPlanFlatStatic<I> plan,
                                            ITerminalStorageTabIngredientCraftingHandler<?, I> handler) {
        CompoundTag tag = new CompoundTag();

        tag.put("id", handler.serializeCraftingJobId(plan.getId()));

        ListTag entries = new ListTag();
        for (TerminalCraftingPlanFlatStatic.Entry entry : plan.getEntries()) {
            entries.add(TerminalCraftingPlanFlatStatic.Entry.serialize(lookupProvider, entry));
        }
        tag.put("entries", entries);

        ListTag outputs = new ListTag();
        for (IPrototypedIngredient<?, ?> output : plan.getOutputs()) {
            outputs.add(IPrototypedIngredient.serialize(lookupProvider, (PrototypedIngredient) output));
        }
        tag.put("outputs", outputs);

        tag.putInt("status", plan.getStatus().ordinal());

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

    public static <I> TerminalCraftingPlanFlatStatic<I> deserialize(HolderLookup.Provider lookupProvider, CompoundTag tag,
                                                                    ITerminalStorageTabIngredientCraftingHandler<?, I> handler) {
        if (!tag.contains("id")) {
            throw new IllegalArgumentException("Could not find an id entry in the given tag");
        }
        if (!tag.contains("entries", Tag.TAG_LIST)) {
            throw new IllegalArgumentException("Could not find a entries entry in the given tag");
        }
        if (!tag.contains("outputs", Tag.TAG_LIST)) {
            throw new IllegalArgumentException("Could not find a outputs entry in the given tag");
        }
        if (!tag.contains("status", Tag.TAG_INT)) {
            throw new IllegalArgumentException("Could not find a status entry in the given tag");
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

        ListTag entriesTag = tag.getList("entries", Tag.TAG_COMPOUND);
        List<TerminalCraftingPlanFlatStatic.Entry> entries = Lists.newArrayListWithExpectedSize(entriesTag.size());
        for (Tag nbtBase : entriesTag) {
            entries.add(TerminalCraftingPlanFlatStatic.Entry.deserialize(lookupProvider, (CompoundTag) nbtBase));
        }

        ListTag outputsTag = tag.getList("outputs", Tag.TAG_COMPOUND);
        List<IPrototypedIngredient<?, ?>> outputs = Lists.newArrayListWithExpectedSize(outputsTag.size());
        for (Tag nbtBase : outputsTag) {
            outputs.add(IPrototypedIngredient.deserialize(lookupProvider, (CompoundTag) nbtBase));
        }

        TerminalCraftingJobStatus status = TerminalCraftingJobStatus.values()[tag.getInt("status")];

        TerminalCraftingPlanStatic.Label label = TerminalCraftingPlanStatic.Label.values()[tag.getInt("label")];

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

        TerminalCraftingPlanFlatStatic<I> plan = new TerminalCraftingPlanFlatStatic<>(id, entries, outputs, status, label, tickDuration, channel, initiatorName);
        if (unlocalizedLabelOverride != null) {
            plan.unlocalizedLabelOverride = unlocalizedLabelOverride;
        }
        return plan;
    }

    public static class Entry implements ITerminalCraftingPlanFlat.IEntry {

        private final IPrototypedIngredient<?, ?> instance;
        private long quantityToCraft;
        private long quantityCrafting;
        private long quantityInStorage;
        private long quantityMissing;

        public Entry(IPrototypedIngredient<?, ?> instance, long quantityToCraft, long quantityCrafting, long quantityInStorage, long quantityMissing) {
            this.instance = instance;
            this.quantityToCraft = quantityToCraft;
            this.quantityCrafting = quantityCrafting;
            this.quantityInStorage = quantityInStorage;
            this.quantityMissing = quantityMissing;
        }

        public Entry(IPrototypedIngredient<?, ?> instance) {
            this(instance, 0, 0, 0, 0);
        }

        @Override
        public IPrototypedIngredient<?, ?> getInstance() {
            return instance;
        }

        @Override
        public long getQuantityToCraft() {
            return quantityToCraft;
        }

        public void setQuantityToCraft(long quantityToCraft) {
            this.quantityToCraft = quantityToCraft;
        }

        @Override
        public long getQuantityCrafting() {
            return quantityCrafting;
        }

        public void setQuantityCrafting(long quantityCrafting) {
            this.quantityCrafting = quantityCrafting;
        }

        @Override
        public long getQuantityInStorage() {
            return quantityInStorage;
        }

        public void setQuantityInStorage(long quantityInStorage) {
            this.quantityInStorage = quantityInStorage;
        }

        @Override
        public long getQuantityMissing() {
            return quantityMissing;
        }

        public void setQuantityMissing(long quantityMissing) {
            this.quantityMissing = quantityMissing;
        }

        public static CompoundTag serialize(HolderLookup.Provider lookupProvider, TerminalCraftingPlanFlatStatic.Entry entry) {
            CompoundTag tag = new CompoundTag();
            tag.put("instance", IPrototypedIngredient.serialize(lookupProvider, entry.getInstance()));
            tag.putLong("quantityToCraft", entry.getQuantityToCraft());
            tag.putLong("quantityCrafting", entry.getQuantityCrafting());
            tag.putLong("quantityInStorage", entry.getQuantityInStorage());
            tag.putLong("quantityMissing", entry.getQuantityMissing());
            return tag;
        }

        public static TerminalCraftingPlanFlatStatic.Entry deserialize(HolderLookup.Provider lookupProvider, CompoundTag tag) {
            if (!tag.contains("instance", Tag.TAG_COMPOUND)) {
                throw new IllegalArgumentException("Could not find a instance entry in the given tag");
            }
            if (!tag.contains("quantityToCraft", Tag.TAG_LONG)) {
                throw new IllegalArgumentException("Could not find a quantityToCraft entry in the given tag");
            }
            if (!tag.contains("quantityCrafting", Tag.TAG_LONG)) {
                throw new IllegalArgumentException("Could not find a quantityCrafting entry in the given tag");
            }
            if (!tag.contains("quantityInStorage", Tag.TAG_LONG)) {
                throw new IllegalArgumentException("Could not find a quantityInStorage entry in the given tag");
            }
            if (!tag.contains("quantityMissing", Tag.TAG_LONG)) {
                throw new IllegalArgumentException("Could not find a quantityMissing entry in the given tag");
            }

            IPrototypedIngredient<?, ?> instance = IPrototypedIngredient.deserialize(lookupProvider, tag.getCompound("instance"));
            long quantityToCraft = tag.getLong("quantityToCraft");
            long quantityCrafting = tag.getLong("quantityCrafting");
            long quantityInStorage = tag.getLong("quantityInStorage");
            long quantityMissing = tag.getLong("quantityMissing");

            return new TerminalCraftingPlanFlatStatic.Entry(instance, quantityToCraft, quantityCrafting, quantityInStorage, quantityMissing);
        }

    }
}
