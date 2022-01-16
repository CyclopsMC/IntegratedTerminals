package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import com.google.common.collect.Lists;
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
public class TerminalCraftingPlanStatic<I> implements ITerminalCraftingPlan<I> {

    private final I id;
    private final List<ITerminalCraftingPlan<I>> dependencies;
    private final List<IPrototypedIngredient<?, ?>> outputs;
    private TerminalCraftingJobStatus status;
    private final long craftingQuantity;
    private final List<IPrototypedIngredient<?, ?>> storageIngredients;
    private final List<List<IPrototypedIngredient<?, ?>>> lastMissingIngredients;
    private String unlocalizedLabel;
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
                                      String unlocalizedLabel,
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
        this.unlocalizedLabel = unlocalizedLabel;
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

    @Override
    public String getUnlocalizedLabel() {
        return unlocalizedLabel;
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
        this.unlocalizedLabel = unlocalizedError;
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

        ListTag storageIngredients = new ListTag();
        for (IPrototypedIngredient<?, ?> storageIngredient : plan.getStorageIngredients()) {
            storageIngredients.add(IPrototypedIngredient.serialize((PrototypedIngredient) storageIngredient));
        }
        tag.put("storageIngredients", storageIngredients);

        ListTag lastMissingIngredients = new ListTag();
        for (List<IPrototypedIngredient<?, ?>> lastMissingIngredient : plan.getLastMissingIngredients()) {
            ListTag lastMissingIngredientTag = new ListTag();
            for (IPrototypedIngredient<?, ?> prototypedIngredient : lastMissingIngredient) {
                lastMissingIngredientTag.add(IPrototypedIngredient.serialize((PrototypedIngredient) prototypedIngredient));
            }
            lastMissingIngredients.add(lastMissingIngredientTag);
        }
        tag.put("lastMissingIngredients", lastMissingIngredients);

        tag.putString("unlocalizedLabel", plan.getUnlocalizedLabel());

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
        if (!tag.contains("storageIngredients", Tag.TAG_LIST)) {
            throw new IllegalArgumentException("Could not find a storageIngredients entry in the given tag");
        }
        if (!tag.contains("lastMissingIngredients", Tag.TAG_LIST)) {
            throw new IllegalArgumentException("Could not find a lastMissingIngredients entry in the given tag");
        }
        if (!tag.contains("unlocalizedLabel", Tag.TAG_STRING)) {
            throw new IllegalArgumentException("Could not find a unlocalizedLabel entry in the given tag");
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

        ListTag storageIngredientsTag = tag.getList("storageIngredients", Tag.TAG_COMPOUND);
        List<IPrototypedIngredient<?, ?>> storageIngredients = Lists.newArrayListWithExpectedSize(storageIngredientsTag.size());
        for (Tag nbtBase : storageIngredientsTag) {
            storageIngredients.add(IPrototypedIngredient.deserialize((CompoundTag) nbtBase));
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

        String unlocalizedLabel = tag.getString("unlocalizedLabel");

        long tickDuration = tag.getLong("tickDuration");

        int channel = tag.getInt("channel");

        String initiatorName = null;
        if (tag.contains("initiatorName", Tag.TAG_STRING)) {
            initiatorName = tag.getString("initiatorName");
        }

        return new TerminalCraftingPlanStatic<>(id, dependencies, outputs, status, craftingQuantity, storageIngredients,
                lastMissingIngredients, unlocalizedLabel, tickDuration, channel, initiatorName);
    }
}
