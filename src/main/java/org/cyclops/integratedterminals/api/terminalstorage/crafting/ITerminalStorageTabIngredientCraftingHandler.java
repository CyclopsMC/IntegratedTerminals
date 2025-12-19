package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.api.item.TagPathElement;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * Handles crafting actions inside ingredient-based terminal storage tabs.
 * @param <O> The type of crafting option.
 * @param <I> The type of crafting plan identifier.
 * @author rubensworks
 */
public interface ITerminalStorageTabIngredientCraftingHandler<O extends ITerminalCraftingOption<?>, I> {

    static final Logger LOGGER = LogUtils.getLogger();

    /**
     * @return The unique id of this handler.
     */
    public Identifier getId();

    /**
     * @param tab An ingredient tab.
     * @param <T> The instance type.
     * @param <M> The matching condition parameter.
     * @return All channels that have crafting options.
     */
    public <T, M> int[] getChannels(TerminalStorageTabIngredientComponentServer<T, M> tab);

    /**
     * Get all crafting options in the given tab.
     * @param tab An ingredient tab.
     * @param channel The channel to get the options for.
     * @param <T> The instance type.
     * @param <M> The matching condition parameter.
     * @return Crafting options.
     */
    public <T, M> Collection<O> getCraftingOptions(TerminalStorageTabIngredientComponentServer<T, M> tab, int channel);

    /**
     * Get all crafting options in the given tab that produce the given output instance.
     * @param tab An ingredient tab.
     * @param channel The channel to get the options for.
     * @param instance The expect output instance.
     * @param matchCondition The expected output match condition.
     * @param <T> The instance type.
     * @param <M> The matching condition parameter.
     * @return Crafting options.
     */
    public <T, M> Collection<O> getCraftingOptionsWithOutput(TerminalStorageTabIngredientComponentServer<T, M> tab, int channel, T instance, M matchCondition);

    /**
     * Serialize a crafting option to NBT.
     *
     * @param valueOutput
     * @param craftingOption A crafting option.
     */
    public void serializeCraftingOption(ValueOutput valueOutput, O craftingOption);

    /**
     * Deserialize a crafting option from NBT.
     *
     * @param <T>                 The instance type.
     * @param <M>                 The matching condition parameter.
     * @param valueInput          An NBT tag representing a crafting option.
     * @param ingredientComponent The ingredient component for the crafting option.
     * @return A crafting option.
     * @throws IllegalArgumentException If the given tag was invalid.
     */
    public <T, M> O deserializeCraftingOption(ValueInput valueInput, IngredientComponent<T, M> ingredientComponent) throws IllegalArgumentException;

    /**
     * Calculate a crafting plan for the given crafting option.
     * @param network The network in which the plan should be calculated.
     * @param channel The channel to get the options for.
     * @param craftingOption A crafting option.
     * @param quantity The requested output quantity.
     * @return The calculated crafting plan.
     */
    public ITerminalCraftingPlan<I> calculateCraftingPlan(INetwork network, int channel,
                                                       ITerminalCraftingOption craftingOption, long quantity);

    /**
     * Serialize a crafting plan to NBT.
     *
     * @param valueOutput  The lookup provider.
     * @param craftingPlan A crafting plan.
     */
    public default void serializeCraftingPlan(ValueOutput valueOutput, ITerminalCraftingPlan<I> craftingPlan) {
        TerminalCraftingPlanStatic.serialize(valueOutput, (TerminalCraftingPlanStatic) craftingPlan, this);
    }

    public default CompoundTag serializeCraftingPlanToNbt(ITerminalCraftingPlan<I> craftingPlan) {
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new TagPathElement(new CompoundTag()), LOGGER)) {
            TagValueOutput valueOutput = TagValueOutput.createWithoutContext(scopedCollector);
            serializeCraftingPlan(valueOutput, craftingPlan);
            return valueOutput.buildResult();
        }
    }

    /**
     * Deserialize a crafting plan from NBT.
     *
     * @param valueInput An NBT tag representing a crafting plan.
     * @return A crafting option.
     * @throws IllegalArgumentException If the given tag was invalid.
     */
    public default ITerminalCraftingPlan<I> deserializeCraftingPlan(ValueInput valueInput) throws IllegalArgumentException {
        return TerminalCraftingPlanStatic.deserialize(valueInput, this);
    }

    /**
     * Serialize a flat crafting plan to NBT.
     *
     * @param valueOutput  The lookup provider.
     * @param craftingPlan A flat crafting plan.
     */
    public default void serializeCraftingPlanFlat(ValueOutput valueOutput, ITerminalCraftingPlanFlat<I> craftingPlan) {
        TerminalCraftingPlanFlatStatic.serialize(valueOutput, (TerminalCraftingPlanFlatStatic) craftingPlan, this);
    }

    /**
     * Deserialize a flat crafting plan from NBT.
     *
     * @param valueInput An NBT tag representing a flat crafting plan.
     * @return A crafting option.
     * @throws IllegalArgumentException If the given tag was invalid.
     */
    public default ITerminalCraftingPlanFlat<I> deserializeCraftingPlanFlat(ValueInput valueInput) throws IllegalArgumentException {
        return TerminalCraftingPlanFlatStatic.deserialize(valueInput, this);
    }

    /**
     * Serializes a crafting job id.
     *
     * @param valueOutput An NBT tag.
     * @param id          An id.
     */
    public void serializeCraftingJobId(ValueOutput valueOutput, I id);

    /**
     * Deserialize a crafting job id.
     *
     * @param valueInput An NBT tag.
     * @return An id.
     */
    public I deserializeCraftingJobId(ValueInput valueInput);

    /**
     * Start a crafting job.
     * @param network The network in which the plan should be started.
     * @param channel The channel to get the options for.
     * @param craftingPlan A crafting plan.
     * @param player The player that started the crafting job.
     * @throws CraftingJobStartException If the crafting job failed to start.
     */
    public void startCraftingJob(INetwork network, int channel, ITerminalCraftingPlan<I> craftingPlan,
                                 ServerPlayer player) throws CraftingJobStartException;

    /**
     * @param network The network in which the plan should be started.
     * @param channel The channel to get the options for.
     * @return All running crafting plans.
     */
    public List<ITerminalCraftingPlan<I>> getCraftingJobs(INetwork network, int channel);

    /**
     * Get the crafting job with the given id.
     * @param network The network in which the plan should be started.
     * @param channel The channel to get the options for.
     * @param craftingJobId A crafting job id.
     * @return A crafting job or null.
     */
    @Nullable
    public ITerminalCraftingPlan<I> getCraftingJob(INetwork network, int channel, I craftingJobId);

    /**
     * Cancel the crafting job with the given id.
     * @param network The network in which the plan should be started.
     * @param channel The channel to get the options for.
     * @param craftingJobId A crafting job id.
     * @return If the crafting job was successfully cancelled.
     */
    public boolean cancelCraftingJob(INetwork network, int channel, I craftingJobId);

}
