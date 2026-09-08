package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.integratedcrafting.api.crafting.CraftingJob;
import org.cyclops.integratedcrafting.api.event.CraftingJobFinishedEvent;
import org.cyclops.integratedcrafting.core.CraftingHelpers;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;
import org.cyclops.integratedterminals.api.terminalstorage.event.TerminalCraftingJobFinishedEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Re-emits Integrated Crafting's job completion events as crafting handler-agnostic
 * {@link TerminalCraftingJobFinishedEvent}s, so that other mods can react to completed crafting jobs
 * without depending on Integrated Crafting.
 * @author rubensworks
 */
public class CraftingJobFinishedEventForwarder {

    private static ITerminalStorageTabIngredientCraftingHandler<?, ?> handler;

    public static void register(ITerminalStorageTabIngredientCraftingHandler<?, ?> handler) {
        CraftingJobFinishedEventForwarder.handler = handler;
        NeoForge.EVENT_BUS.register(CraftingJobFinishedEventForwarder.class);
    }

    @SubscribeEvent
    public static void onCraftingJobFinished(CraftingJobFinishedEvent event) {
        CraftingJob craftingJob = event.getCraftingJob();

        List recipeOutputs = IntegratedCraftingHelpers.getPrototypesFromIngredients(craftingJob.getRecipe().getOutput());
        List<IPrototypedIngredient<?, ?>> outputs = CraftingHelpers.multiplyPrototypedIngredients(
                recipeOutputs, craftingJob.getAmountTotal());

        TerminalCraftingJobFinishedEvent.post(handler, craftingJob.getId(), craftingJob.getChannel(),
                event.isRootJob(), getInitiator(craftingJob.getInitiatorUuid()), outputs);
    }

    @Nullable
    protected static UUID getInitiator(@Nullable String initiatorUuid) {
        if (initiatorUuid == null) {
            return null;
        }
        try {
            return UUID.fromString(initiatorUuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
