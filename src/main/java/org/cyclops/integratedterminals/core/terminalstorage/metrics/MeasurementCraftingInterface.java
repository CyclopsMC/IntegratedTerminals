package org.cyclops.integratedterminals.core.terminalstorage.metrics;

import com.google.common.collect.Lists;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.integratedcrafting.api.crafting.CraftingJob;
import org.cyclops.integratedcrafting.api.crafting.CraftingJobStatus;
import org.cyclops.integratedcrafting.api.crafting.ICraftingInterface;
import org.cyclops.integratedcrafting.api.network.ICraftingNetwork;
import org.cyclops.integrateddynamics.api.part.PrioritizedPartPos;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A crafting interface that only advertises recipes, so crafting options can be measured
 * without building hundreds of real crafting interface parts.
 *
 * Debug-only. It never schedules or runs jobs.
 */
public class MeasurementCraftingInterface implements ICraftingInterface {

    private final PrioritizedPartPos position;
    private final List<IRecipeDefinition> recipes = Lists.newArrayList();

    public MeasurementCraftingInterface(PrioritizedPartPos position) {
        this.position = position;
    }

    public void addRecipe(IRecipeDefinition recipe) {
        this.recipes.add(recipe);
    }

    @Override
    public Collection<IRecipeDefinition> getRecipes() {
        return this.recipes;
    }

    @Override
    public boolean canScheduleCraftingJobs() {
        return false;
    }

    @Override
    public void scheduleCraftingJob(CraftingJob craftingJob) {
    }

    @Override
    public void fillCraftingJobBufferFromStorage(CraftingJob craftingJob,
                                                 Function<IngredientComponent<?, ?>, IIngredientComponentStorage> storageGetter) {
    }

    @Override
    public int getCraftingJobsCount() {
        return 0;
    }

    @Override
    public Iterator<CraftingJob> getCraftingJobs() {
        return Collections.emptyIterator();
    }

    @Override
    public List<Map<IngredientComponent<?, ?>, List<IPrototypedIngredient<?, ?>>>> getPendingCraftingJobOutputs(int channel) {
        return Collections.emptyList();
    }

    @Override
    public CraftingJobStatus getCraftingJobStatus(ICraftingNetwork network, int channel, int craftingJobId) {
        return CraftingJobStatus.PENDING_INTERFACE;
    }

    @Override
    public void cancelCraftingJob(int channel, int craftingJobId) {
    }

    @Override
    public long getCraftingJobEntryStartTick(int craftingJobId) {
        return 0;
    }

    @Override
    public long getEstimatedRecipeDuration(IRecipeDefinition recipe) {
        return 1;
    }

    @Override
    public PrioritizedPartPos getPosition() {
        return this.position;
    }
}
