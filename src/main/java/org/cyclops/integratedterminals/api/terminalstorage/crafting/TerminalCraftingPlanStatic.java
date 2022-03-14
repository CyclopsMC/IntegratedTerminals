package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author rubensworks
 */
public class TerminalCraftingPlanStatic<I> implements ITerminalCraftingPlan<I> {

    private final I id;
    private final List<ITerminalCraftingPlan<I>> dependencies;
    private final List<IPrototypedIngredient<?, ?>> outputs;
    private TerminalCraftingJobStatus status;
    private final long craftingQuantity;
    private final List<IPrototypedIngredient<?, ?>> storageIngredients;
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
                                      List<IPrototypedIngredient<?, ?>> storageIngredients,
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
        this.storageIngredients = storageIngredients;
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
    public List<IPrototypedIngredient<?, ?>> getStorageIngredients() {
        return storageIngredients;
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

    public static <I> CompoundNBT serialize(TerminalCraftingPlanStatic<I> plan,
                                            ITerminalStorageTabIngredientCraftingHandler<?, I> handler) {
        CompoundNBT tag = new CompoundNBT();

        tag.put("id", handler.serializeCraftingJobId(plan.getId()));

        ListNBT dependencies = new ListNBT();
        for (ITerminalCraftingPlan<I> dependency : plan.getDependencies()) {
            dependencies.add(TerminalCraftingPlanStatic.serialize((TerminalCraftingPlanStatic) dependency, handler));
        }
        tag.put("dependencies", dependencies);

        ListNBT outputs = new ListNBT();
        for (IPrototypedIngredient<?, ?> output : plan.getOutputs()) {
            outputs.add(IPrototypedIngredient.serialize((PrototypedIngredient) output));
        }
        tag.put("outputs", outputs);

        tag.putInt("status", plan.getStatus().ordinal());

        tag.putLong("craftingQuantity", plan.getCraftingQuantity());

        ListNBT storageIngredients = new ListNBT();
        for (IPrototypedIngredient<?, ?> storageIngredient : plan.getStorageIngredients()) {
            storageIngredients.add(IPrototypedIngredient.serialize((PrototypedIngredient) storageIngredient));
        }
        tag.put("storageIngredients", storageIngredients);

        ListNBT lastMissingIngredients = new ListNBT();
        for (List<IPrototypedIngredient<?, ?>> lastMissingIngredient : plan.getLastMissingIngredients()) {
            ListNBT lastMissingIngredientTag = new ListNBT();
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

    public static <I> TerminalCraftingPlanStatic<I> deserialize(CompoundNBT tag,
                                                                ITerminalStorageTabIngredientCraftingHandler<?, I> handler) {
        if (!tag.contains("id")) {
            throw new IllegalArgumentException("Could not find an id entry in the given tag");
        }
        if (!tag.contains("dependencies", Constants.NBT.TAG_LIST)) {
            throw new IllegalArgumentException("Could not find a dependencies entry in the given tag");
        }
        if (!tag.contains("outputs", Constants.NBT.TAG_LIST)) {
            throw new IllegalArgumentException("Could not find a outputs entry in the given tag");
        }
        if (!tag.contains("status", Constants.NBT.TAG_INT)) {
            throw new IllegalArgumentException("Could not find a status entry in the given tag");
        }
        if (!tag.contains("craftingQuantity", Constants.NBT.TAG_LONG)) {
            throw new IllegalArgumentException("Could not find a craftingQuantity entry in the given tag");
        }
        if (!tag.contains("storageIngredients", Constants.NBT.TAG_LIST)) {
            throw new IllegalArgumentException("Could not find a storageIngredients entry in the given tag");
        }
        if (!tag.contains("lastMissingIngredients", Constants.NBT.TAG_LIST)) {
            throw new IllegalArgumentException("Could not find a lastMissingIngredients entry in the given tag");
        }
        if (!tag.contains("label", Constants.NBT.TAG_INT)) {
            throw new IllegalArgumentException("Could not find a label entry in the given tag");
        }
        if (!tag.contains("tickDuration", Constants.NBT.TAG_LONG)) {
            throw new IllegalArgumentException("Could not find a tickDuration entry in the given tag");
        }
        if (!tag.contains("channel", Constants.NBT.TAG_INT)) {
            throw new IllegalArgumentException("Could not find a channel entry in the given tag");
        }

        I id = handler.deserializeCraftingJobId(tag.get("id"));

        ListNBT dependenciesTag = tag.getList("dependencies", Constants.NBT.TAG_COMPOUND);
        List<ITerminalCraftingPlan<I>> dependencies = Lists.newArrayListWithExpectedSize(dependenciesTag.size());
        for (INBT nbtBase : dependenciesTag) {
            dependencies.add(TerminalCraftingPlanStatic.deserialize((CompoundNBT) nbtBase, handler));
        }

        ListNBT outputsTag = tag.getList("outputs", Constants.NBT.TAG_COMPOUND);
        List<IPrototypedIngredient<?, ?>> outputs = Lists.newArrayListWithExpectedSize(outputsTag.size());
        for (INBT nbtBase : outputsTag) {
            outputs.add(IPrototypedIngredient.deserialize((CompoundNBT) nbtBase));
        }

        TerminalCraftingJobStatus status = TerminalCraftingJobStatus.values()[tag.getInt("status")];

        long craftingQuantity = tag.getLong("craftingQuantity");

        ListNBT storageIngredientsTag = tag.getList("storageIngredients", Constants.NBT.TAG_COMPOUND);
        List<IPrototypedIngredient<?, ?>> storageIngredients = Lists.newArrayListWithExpectedSize(storageIngredientsTag.size());
        for (INBT nbtBase : storageIngredientsTag) {
            storageIngredients.add(IPrototypedIngredient.deserialize((CompoundNBT) nbtBase));
        }

        ListNBT lastMissingIngredientsTag = tag.getList("lastMissingIngredients", Constants.NBT.TAG_LIST);
        List<List<IPrototypedIngredient<?, ?>>> lastMissingIngredients = Lists.newArrayListWithExpectedSize(lastMissingIngredientsTag.size());
        for (INBT nbtBase : lastMissingIngredientsTag) {
            ListNBT list = ((ListNBT) nbtBase);
            List<IPrototypedIngredient<?, ?>> lastMissingIngredient = Lists.newArrayListWithExpectedSize(list.size());
            for (INBT base : list) {
                lastMissingIngredient.add(IPrototypedIngredient.deserialize((CompoundNBT) base));
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
        if (tag.contains("initiatorName", Constants.NBT.TAG_STRING)) {
            initiatorName = tag.getString("initiatorName");
        }

        TerminalCraftingPlanStatic<I> plan = new TerminalCraftingPlanStatic<>(id, dependencies, outputs, status, craftingQuantity, storageIngredients,
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
