package org.cyclops.integratedterminals.api.terminalstorage.crafting;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;

import java.util.Collection;

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
     * Calculate a crafting job for the given crafting option.
     * @param tab An ingredient tab.
     * @param channel The channel to get the options for.
     * @param craftingOption A crafting option.
     * @param quantity The requested output quantity.
     * @param <T> The instance type.
     * @param <M> The matching condition parameter.
     * @return The calculated crafting job.
     */
    public <T, M> ITerminalCraftingJob<T> calculateCraftingJob(TerminalStorageTabIngredientComponentServer<T, M> tab, int channel, ITerminalCraftingOption<T> craftingOption, long quantity);

    /**
     * Start a crafting job.
     * @param tab An ingredient tab.
     * @param channel The channel to get the options for.
     * @param craftingJob A crafting job.
     * @param <T> The instance type.
     * @param <M> The matching condition parameter.
     */
    public <T, M> void startCraftingJob(TerminalStorageTabIngredientComponentServer<T, M> tab, int channel, ITerminalCraftingJob<T> craftingJob);

}
