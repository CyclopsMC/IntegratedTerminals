package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
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

        List recipeOutputs = IntegratedCraftingHelpers.getPrototypesFromIngredients(craftingJob.getRecipe().getOutput());
        List<IPrototypedIngredient<?, ?>> outputs = CraftingHelpers.multiplyPrototypedIngredients(
                recipeOutputs, craftingJob.getAmountTotal());
        if (outputs.isEmpty()) {
            return;
        }

        sendToast(player, outputs.get(0));
    }

    protected static <T, M> void sendToast(ServerPlayer player, IPrototypedIngredient<T, M> output) {
        IngredientComponent<T, M> ingredientComponent = output.getComponent();
        IntegratedTerminals._instance.getPacketHandler().sendToPlayer(
                new CraftingJobFinishedToastPacket<>(player.registryAccess(), ingredientComponent,
                        output.getPrototype()), player);
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
