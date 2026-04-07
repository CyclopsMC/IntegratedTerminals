package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import com.google.common.collect.Lists;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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

    public static <I> void serialize(ValueOutput valueOutput, TerminalCraftingPlanFlatStatic<I> plan,
                                     ITerminalStorageTabIngredientCraftingHandler<?, I> handler) {
        handler.serializeCraftingJobId(valueOutput, plan.getId());

        ValueOutput.ValueOutputList entries = valueOutput.childrenList("entries");
        for (TerminalCraftingPlanFlatStatic.Entry entry : plan.getEntries()) {
            TerminalCraftingPlanFlatStatic.Entry.serialize(entries.addChild(), entry);
        }

        ValueOutput.ValueOutputList outputs = valueOutput.childrenList("outputs");
        for (IPrototypedIngredient<?, ?> output : plan.getOutputs()) {
            IPrototypedIngredient.serialize(outputs.addChild(), (PrototypedIngredient) output);
        }

        valueOutput.putInt("status", plan.getStatus().ordinal());

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

    public static <I> TerminalCraftingPlanFlatStatic<I> deserialize(ValueInput valueInput,
                                                                    ITerminalStorageTabIngredientCraftingHandler<?, I> handler) {
        I id = handler.deserializeCraftingJobId(valueInput);

        List<TerminalCraftingPlanFlatStatic.Entry> entries = Lists.newArrayList();
        for (ValueInput nbtBase : valueInput.childrenList("entries").orElseThrow()) {
            entries.add(TerminalCraftingPlanFlatStatic.Entry.deserialize(nbtBase));
        }

        List<IPrototypedIngredient<?, ?>> outputs = Lists.newArrayList();
        for (ValueInput nbtBase : valueInput.childrenList("outputs").orElseThrow()) {
            outputs.add(IPrototypedIngredient.deserialize(nbtBase));
        }

        TerminalCraftingJobStatus status = TerminalCraftingJobStatus.values()[valueInput.getInt("status").orElseThrow()];

        TerminalCraftingPlanStatic.Label label = TerminalCraftingPlanStatic.Label.values()[valueInput.getInt("label").orElseThrow()];

        String unlocalizedLabelOverride = valueInput.getStringOr("unlocalizedLabelOverride", null);

        long tickDuration = valueInput.getLong("tickDuration").orElseThrow();

        int channel = valueInput.getInt("channel").orElseThrow();

        String initiatorName = valueInput.getStringOr("initiatorName", null);

        TerminalCraftingPlanFlatStatic<I> plan = new TerminalCraftingPlanFlatStatic<>(id, entries, outputs, status, label, tickDuration, channel, initiatorName);
        if (unlocalizedLabelOverride != null) {
            plan.unlocalizedLabelOverride = unlocalizedLabelOverride;
        }
        return plan;
    }

    public static class Entry implements ITerminalCraftingPlanFlat.IEntry {

        private final List<IPrototypedIngredient<?, ?>> instances;
        private long quantityToCraft;
        private long quantityCrafting;
        private long quantityInStorage;
        private long quantityMissing;

        public Entry(List<IPrototypedIngredient<?, ?>> instances, long quantityToCraft, long quantityCrafting, long quantityInStorage, long quantityMissing) {
            this.instances = instances;
            this.quantityToCraft = quantityToCraft;
            this.quantityCrafting = quantityCrafting;
            this.quantityInStorage = quantityInStorage;
            this.quantityMissing = quantityMissing;
        }

        public Entry(List<IPrototypedIngredient<?, ?>> instances) {
            this(instances, 0, 0, 0, 0);
        }

        @Override
        public List<IPrototypedIngredient<?, ?>> getInstances() {
            return instances;
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

        public static void serialize(ValueOutput valueOutput, Entry entry) {
            ValueOutput.ValueOutputList instances = valueOutput.childrenList("instances");
            for (IPrototypedIngredient<?, ?> instance : entry.getInstances()) {
                IPrototypedIngredient.serialize(instances.addChild(), instance);
            }
            valueOutput.putLong("quantityToCraft", entry.getQuantityToCraft());
            valueOutput.putLong("quantityCrafting", entry.getQuantityCrafting());
            valueOutput.putLong("quantityInStorage", entry.getQuantityInStorage());
            valueOutput.putLong("quantityMissing", entry.getQuantityMissing());
        }

        public static TerminalCraftingPlanFlatStatic.Entry deserialize(ValueInput valueInput) {
            List<IPrototypedIngredient<?, ?>> instances = Lists.newArrayList();
            for (ValueInput instance : valueInput.childrenList("instances").orElseThrow()) {
                instances.add(IPrototypedIngredient.deserialize(instance));
            }
            long quantityToCraft = valueInput.getLong("quantityToCraft").orElseThrow();
            long quantityCrafting = valueInput.getLong("quantityCrafting").orElseThrow();
            long quantityInStorage = valueInput.getLong("quantityInStorage").orElseThrow();
            long quantityMissing = valueInput.getLong("quantityMissing").orElseThrow();

            return new TerminalCraftingPlanFlatStatic.Entry(instances, quantityToCraft, quantityCrafting, quantityInStorage, quantityMissing);
        }

    }
}
