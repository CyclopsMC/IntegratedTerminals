package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;

import java.util.List;

/**
 * @author rubensworks
 */
public class TerminalCraftingPlanStatic<I> implements ITerminalCraftingPlan<I> {

    private final I id;
    private final List<ITerminalCraftingPlan<I>> dependencies;
    private final List<IPrototypedIngredient<?, ?>> outputs;
    private final TerminalCraftingJobStatus status;
    private final long craftingQuantity;
    private final List<IPrototypedIngredient<?, ?>> storageIngredients;
    private final List<List<IPrototypedIngredient<?, ?>>> lastMissingIngredients;
    private final String unlocalizedLabel;

    public TerminalCraftingPlanStatic(I id,
                                      List<ITerminalCraftingPlan<I>> dependencies,
                                      List<IPrototypedIngredient<?, ?>> outputs,
                                      TerminalCraftingJobStatus status,
                                      long craftingQuantity,
                                      List<IPrototypedIngredient<?, ?>> storageIngredients,
                                      List<List<IPrototypedIngredient<?, ?>>> lastMissingIngredients,
                                      String unlocalizedLabel) {
        this.id = id;
        this.dependencies = dependencies;
        this.outputs = outputs;
        this.status = status;
        this.craftingQuantity = craftingQuantity;
        this.storageIngredients = storageIngredients;
        this.lastMissingIngredients = lastMissingIngredients;
        this.unlocalizedLabel = unlocalizedLabel;
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

    public static <I> NBTTagCompound serialize(TerminalCraftingPlanStatic<I> plan,
                                               ITerminalStorageTabIngredientCraftingHandler<?, I> handler) {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setTag("id", handler.serializeCraftingJobId(plan.getId()));

        NBTTagList dependencies = new NBTTagList();
        for (ITerminalCraftingPlan<I> dependency : plan.getDependencies()) {
            dependencies.appendTag(TerminalCraftingPlanStatic.serialize((TerminalCraftingPlanStatic) dependency, handler));
        }
        tag.setTag("dependencies", dependencies);

        NBTTagList outputs = new NBTTagList();
        for (IPrototypedIngredient<?, ?> output : plan.getOutputs()) {
            outputs.appendTag(IPrototypedIngredient.serialize((PrototypedIngredient) output));
        }
        tag.setTag("outputs", outputs);

        tag.setInteger("status", plan.getStatus().ordinal());

        tag.setLong("craftingQuantity", plan.getCraftingQuantity());

        NBTTagList storageIngredients = new NBTTagList();
        for (IPrototypedIngredient<?, ?> storageIngredient : plan.getStorageIngredients()) {
            storageIngredients.appendTag(IPrototypedIngredient.serialize((PrototypedIngredient) storageIngredient));
        }
        tag.setTag("storageIngredients", storageIngredients);

        NBTTagList lastMissingIngredients = new NBTTagList();
        for (List<IPrototypedIngredient<?, ?>> lastMissingIngredient : plan.getLastMissingIngredients()) {
            NBTTagList lastMissingIngredientTag = new NBTTagList();
            for (IPrototypedIngredient<?, ?> prototypedIngredient : lastMissingIngredient) {
                lastMissingIngredientTag.appendTag(IPrototypedIngredient.serialize((PrototypedIngredient) prototypedIngredient));
            }
            lastMissingIngredients.appendTag(lastMissingIngredientTag);
        }
        tag.setTag("lastMissingIngredients", lastMissingIngredients);

        tag.setString("unlocalizedLabel", plan.getUnlocalizedLabel());

        return tag;
    }

    public static <I> TerminalCraftingPlanStatic<I> deserialize(NBTTagCompound tag,
                                                                ITerminalStorageTabIngredientCraftingHandler<?, I> handler) {
        if (!tag.hasKey("id")) {
            throw new IllegalArgumentException("Could not find an id entry in the given tag");
        }
        if (!tag.hasKey("dependencies", Constants.NBT.TAG_LIST)) {
            throw new IllegalArgumentException("Could not find a dependencies entry in the given tag");
        }
        if (!tag.hasKey("outputs", Constants.NBT.TAG_LIST)) {
            throw new IllegalArgumentException("Could not find a outputs entry in the given tag");
        }
        if (!tag.hasKey("status", Constants.NBT.TAG_INT)) {
            throw new IllegalArgumentException("Could not find a status entry in the given tag");
        }
        if (!tag.hasKey("craftingQuantity", Constants.NBT.TAG_LONG)) {
            throw new IllegalArgumentException("Could not find a craftingQuantity entry in the given tag");
        }
        if (!tag.hasKey("storageIngredients", Constants.NBT.TAG_LIST)) {
            throw new IllegalArgumentException("Could not find a storageIngredients entry in the given tag");
        }
        if (!tag.hasKey("lastMissingIngredients", Constants.NBT.TAG_LIST)) {
            throw new IllegalArgumentException("Could not find a lastMissingIngredients entry in the given tag");
        }
        if (!tag.hasKey("unlocalizedLabel", Constants.NBT.TAG_STRING)) {
            throw new IllegalArgumentException("Could not find a unlocalizedLabel entry in the given tag");
        }

        I id = handler.deserializeCraftingJobId(tag.getTag("id"));

        NBTTagList dependenciesTag = tag.getTagList("dependencies", Constants.NBT.TAG_COMPOUND);
        List<ITerminalCraftingPlan<I>> dependencies = Lists.newArrayListWithExpectedSize(dependenciesTag.tagCount());
        for (NBTBase nbtBase : dependenciesTag) {
            dependencies.add(TerminalCraftingPlanStatic.deserialize((NBTTagCompound) nbtBase, handler));
        }

        NBTTagList outputsTag = tag.getTagList("outputs", Constants.NBT.TAG_COMPOUND);
        List<IPrototypedIngredient<?, ?>> outputs = Lists.newArrayListWithExpectedSize(outputsTag.tagCount());
        for (NBTBase nbtBase : outputsTag) {
            outputs.add(IPrototypedIngredient.deserialize((NBTTagCompound) nbtBase));
        }

        TerminalCraftingJobStatus status = TerminalCraftingJobStatus.values()[tag.getInteger("status")];

        long craftingQuantity = tag.getLong("craftingQuantity");

        NBTTagList storageIngredientsTag = tag.getTagList("storageIngredients", Constants.NBT.TAG_COMPOUND);
        List<IPrototypedIngredient<?, ?>> storageIngredients = Lists.newArrayListWithExpectedSize(storageIngredientsTag.tagCount());
        for (NBTBase nbtBase : storageIngredientsTag) {
            storageIngredients.add(IPrototypedIngredient.deserialize((NBTTagCompound) nbtBase));
        }

        NBTTagList lastMissingIngredientsTag = tag.getTagList("lastMissingIngredients", Constants.NBT.TAG_LIST);
        List<List<IPrototypedIngredient<?, ?>>> lastMissingIngredients = Lists.newArrayListWithExpectedSize(lastMissingIngredientsTag.tagCount());
        for (NBTBase nbtBase : lastMissingIngredientsTag) {
            NBTTagList list = ((NBTTagList) nbtBase);
            List<IPrototypedIngredient<?, ?>> lastMissingIngredient = Lists.newArrayListWithExpectedSize(list.tagCount());
            for (NBTBase base : list) {
                lastMissingIngredient.add(IPrototypedIngredient.deserialize((NBTTagCompound) base));
            }
            lastMissingIngredients.add(lastMissingIngredient);
        }

        String unlocalizedLabel = tag.getString("unlocalizedLabel");

        return new TerminalCraftingPlanStatic<>(id, dependencies, outputs, status, craftingQuantity, storageIngredients,
                lastMissingIngredients, unlocalizedLabel);
    }
}
