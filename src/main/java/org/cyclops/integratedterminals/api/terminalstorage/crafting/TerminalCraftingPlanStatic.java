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
public class TerminalCraftingPlanStatic implements ITerminalCraftingPlan {

    private final List<ITerminalCraftingPlan> dependencies;
    private final List<IPrototypedIngredient<?, ?>> outputs;
    private final TerminalCraftingJobStatus status;
    private final long craftingQuantity;
    private final List<IPrototypedIngredient<?, ?>> storageIngredients;
    private final String unlocalizedLabel;

    public TerminalCraftingPlanStatic(List<ITerminalCraftingPlan> dependencies,
                                      List<IPrototypedIngredient<?, ?>> outputs,
                                      TerminalCraftingJobStatus status,
                                      long craftingQuantity,
                                      List<IPrototypedIngredient<?, ?>> storageIngredients,
                                      String unlocalizedLabel) {
        this.dependencies = dependencies;
        this.outputs = outputs;
        this.status = status;
        this.craftingQuantity = craftingQuantity;
        this.storageIngredients = storageIngredients;
        this.unlocalizedLabel = unlocalizedLabel;
    }

    @Override
    public List<ITerminalCraftingPlan> getDependencies() {
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
    public String getUnlocalizedLabel() {
        return unlocalizedLabel;
    }

    public static NBTTagCompound serialize(TerminalCraftingPlanStatic plan) {
        NBTTagCompound tag = new NBTTagCompound();

        NBTTagList dependencies = new NBTTagList();
        for (ITerminalCraftingPlan dependency : plan.getDependencies()) {
            dependencies.appendTag(TerminalCraftingPlanStatic.serialize((TerminalCraftingPlanStatic) dependency));
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

        tag.setString("unlocalizedLabel", plan.getUnlocalizedLabel());

        return tag;
    }

    public static TerminalCraftingPlanStatic deserialize(NBTTagCompound tag) {
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
        if (!tag.hasKey("unlocalizedLabel", Constants.NBT.TAG_STRING)) {
            throw new IllegalArgumentException("Could not find a unlocalizedLabel entry in the given tag");
        }

        NBTTagList dependenciesTag = tag.getTagList("dependencies", Constants.NBT.TAG_COMPOUND);
        List<ITerminalCraftingPlan> dependencies = Lists.newArrayListWithExpectedSize(dependenciesTag.tagCount());
        for (NBTBase nbtBase : dependenciesTag) {
            dependencies.add(TerminalCraftingPlanStatic.deserialize((NBTTagCompound) nbtBase));
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

        String unlocalizedLabel = tag.getString("unlocalizedLabel");

        return new TerminalCraftingPlanStatic(dependencies, outputs, status, craftingQuantity, storageIngredients, unlocalizedLabel);
    }
}
