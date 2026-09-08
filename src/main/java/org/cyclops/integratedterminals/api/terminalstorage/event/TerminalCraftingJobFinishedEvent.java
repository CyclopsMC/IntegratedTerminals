package org.cyclops.integratedterminals.api.terminalstorage.event;

import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * An event that is emitted on the NeoForge event bus when a crafting job has been completed.
 *
 * This is the crafting handler-agnostic counterpart of the completion events that the
 * separate crafting mods may emit, so that consumers don't need those mods on their classpath.
 *
 * This is only emitted for jobs that ran to completion, so not for cancelled jobs.
 *
 * This is emitted for every completed job, including the dependencies of a job.
 * Jobs that were requested directly can be identified via {@link #isRootJob()}.
 *
 * @author rubensworks
 */
public class TerminalCraftingJobFinishedEvent extends Event {

    private final ITerminalStorageTabIngredientCraftingHandler<?, ?> handler;
    private final Object craftingJobId;
    private final int channel;
    private final boolean rootJob;
    @Nullable
    private final UUID initiator;
    private final List<IPrototypedIngredient<?, ?>> outputs;

    public TerminalCraftingJobFinishedEvent(ITerminalStorageTabIngredientCraftingHandler<?, ?> handler,
                                            Object craftingJobId, int channel, boolean rootJob,
                                            @Nullable UUID initiator, List<IPrototypedIngredient<?, ?>> outputs) {
        this.handler = handler;
        this.craftingJobId = craftingJobId;
        this.channel = channel;
        this.rootJob = rootJob;
        this.initiator = initiator;
        this.outputs = outputs;
    }

    /**
     * @return The crafting handler that was running the job.
     */
    public ITerminalStorageTabIngredientCraftingHandler<?, ?> getHandler() {
        return handler;
    }

    /**
     * The id is only meaningful to {@link #getHandler()}, so it should be compared by equality
     * to ids that were obtained from that same handler.
     * @return The id of the completed crafting job.
     */
    public Object getCraftingJobId() {
        return craftingJobId;
    }

    /**
     * @return The channel the job was running in.
     */
    public int getChannel() {
        return channel;
    }

    /**
     * @return If the job was requested directly, as opposed to being a dependency of another job.
     */
    public boolean isRootJob() {
        return rootJob;
    }

    /**
     * @return The player that requested the job, or null if unknown.
     */
    @Nullable
    public UUID getInitiator() {
        return initiator;
    }

    /**
     * @return All outputs that the job has produced.
     */
    public List<IPrototypedIngredient<?, ?>> getOutputs() {
        return outputs;
    }

    public static void post(ITerminalStorageTabIngredientCraftingHandler<?, ?> handler, Object craftingJobId,
                            int channel, boolean rootJob, @Nullable UUID initiator,
                            List<IPrototypedIngredient<?, ?>> outputs) {
        NeoForge.EVENT_BUS.post(new TerminalCraftingJobFinishedEvent(handler, craftingJobId, channel, rootJob,
                initiator, outputs));
    }

}
