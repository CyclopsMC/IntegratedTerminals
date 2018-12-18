package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;

import java.util.Collection;
import java.util.List;

/**
 * Handles crafting actions inside ingredient-based terminal storage tabs.
 * @author rubensworks
 */
public interface ITerminalStorageTabIngredientCraftingHandler<O extends ITerminalCraftingOption<?>> {

    /**
     * @return The unique id of this handler.
     */
    public ResourceLocation getId();

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
     * Serialize a crafting option to NBT.
     * @param craftingOption A crafting option.
     * @return An NBT tag.
     */
    public NBTTagCompound serializeCraftingOption(O craftingOption);

    /**
     * Deserialize a crafting option from NBT.
     * @param tag An NBT tag representing a crafting option.
     * @param <T> The instance type.
     * @param <M> The matching condition parameter.
     * @return A crafting option.
     * @throws IllegalArgumentException If the given tag was invalid.
     */
    public <T, M> O deserializeCraftingOption(IngredientComponent<T, M> ingredientComponent, NBTTagCompound tag) throws IllegalArgumentException;

    /**
     * Calculate a crafting plan for the given crafting option.
     * @param network The network in which the plan should be calculated.
     * @param channel The channel to get the options for.
     * @param craftingOption A crafting option.
     * @param quantity The requested output quantity.
     * @return The calculated crafting plan.
     */
    public ITerminalCraftingPlan calculateCraftingPlan(INetwork network, int channel,
                                                       ITerminalCraftingOption craftingOption, long quantity);

    /**
     * Serialize a crafting plan to NBT.
     * @param craftingPlan A crafting plan.
     * @return An NBT tag.
     */
    public default NBTTagCompound serializeCraftingPlan(ITerminalCraftingPlan craftingPlan) {
        return TerminalCraftingPlanStatic.serialize((TerminalCraftingPlanStatic) craftingPlan);
    }

    /**
     * Deserialize a crafting plan from NBT.
     * @param tag An NBT tag representing a crafting plan.
     * @return A crafting option.
     * @throws IllegalArgumentException If the given tag was invalid.
     */
    public default ITerminalCraftingPlan deserializeCraftingPlan(NBTTagCompound tag) throws IllegalArgumentException {
        return TerminalCraftingPlanStatic.deserialize(tag);
    }

    /**
     * Start a crafting job.
     * @param network The network in which the plan should be started.
     * @param channel The channel to get the options for.
     * @param craftingPlan A crafting plan.
     */
    public void startCraftingJob(INetwork network, int channel, ITerminalCraftingPlan craftingPlan);

    /**
     * @param network The network in which the plan should be started.
     * @param channel The channel to get the options for.
     * @return All running crafting plans.
     */
    public List<ITerminalCraftingPlan> getCraftingJobs(INetwork network, int channel);

}
