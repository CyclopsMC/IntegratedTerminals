package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integratedcrafting.api.crafting.CraftingJob;
import org.cyclops.integratedcrafting.api.event.CraftingJobFinishedEvent;
import org.cyclops.integratedcrafting.core.CraftingHelpers;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.network.packet.CraftingJobFinishedToastPacket;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Sends a toast to the player that requested a crafting job once that job is completed.
 * @author rubensworks
 */
public class CraftingJobFinishedToastListener {

    public static void register() {
        NeoForge.EVENT_BUS.register(CraftingJobFinishedToastListener.class);
    }

    @SubscribeEvent
    public static void onCraftingJobFinished(CraftingJobFinishedEvent event) {
        CraftingJob craftingJob = event.getCraftingJob();

        // Only notify for the job that was requested, not for its dependencies.
        if (!event.isRootJob() || !craftingJob.isNotifyInitiator() || craftingJob.getInitiatorUuid() == null) {
            return;
        }

        ServerPlayer player = getPlayer(craftingJob.getInitiatorUuid());
        if (player == null) {
            // The initiator is not online, so there is nobody to notify.
            return;
        }

        // Jobs that were scheduled before the initial amount was tracked report an amount of 0
        int amount = Math.max(1, craftingJob.getInitialAmount());
        List recipeOutputs = IntegratedCraftingHelpers.getPrototypesFromIngredients(craftingJob.getRecipe().getOutput());
        List<IPrototypedIngredient<?, ?>> outputs = CraftingHelpers.multiplyPrototypedIngredients(recipeOutputs, amount);
        if (outputs.isEmpty()) {
            return;
        }

        IPrototypedIngredient<?, ?> output = outputs.get(0);
        ItemStack outputItem = output.getPrototype() instanceof ItemStack itemStack ? itemStack : ItemStack.EMPTY;
        IntegratedTerminals._instance.getPacketHandler().sendToPlayer(
                new CraftingJobFinishedToastPacket(outputItem, outputItem.isEmpty() ? formatOutput(output) : ""), player);
    }

    /**
     * Describe an output that can not be sent to the client as an item stack.
     */
    protected static <T, M> String formatOutput(IPrototypedIngredient<T, M> output) {
        T prototype = output.getPrototype();
        if (prototype instanceof FluidStack fluidStack) {
            return fluidStack.getAmount() + "x " + fluidStack.getHoverName().getString();
        }
        IngredientComponent<T, M> component = output.getComponent();
        return component.getMatcher().getQuantity(prototype) + "x " + component.getName();
    }

    @Nullable
    protected static ServerPlayer getPlayer(String initiatorUuid) {
        try {
            return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(UUID.fromString(initiatorUuid));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
