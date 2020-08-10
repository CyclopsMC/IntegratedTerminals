package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;
import org.cyclops.integratedterminals.core.client.gui.CraftingJobGuiData;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.TerminalStorageTabIngredientCraftingHandlers;

/**
 * Packet for opening a live crafting plan gui.
 * @author rubensworks
 *
 */
public class CancelCraftingJobPacket extends PacketCodec {

    @CodecField
    private BlockPos pos;
    @CodecField
    private Direction side;
    @CodecField
    private int channel;
    @CodecField
    private String craftingPlanHandler;
    @CodecField
    private CompoundNBT craftingJobId;

    public CancelCraftingJobPacket() {

    }

    public CancelCraftingJobPacket(CraftingJobGuiData craftingPlanGuiData) {
        this.pos = craftingPlanGuiData.getPos();
        this.side = craftingPlanGuiData.getSide();
        this.channel = craftingPlanGuiData.getChannel();
        this.craftingPlanHandler = craftingPlanGuiData.getHandler().getId().toString();
        this.craftingJobId = new CompoundNBT();
        this.craftingJobId.put("id", craftingPlanGuiData.getHandler().serializeCraftingJobId(craftingPlanGuiData.getCraftingJob()));
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, PlayerEntity player) {

    }

    @Override
    public void actionServer(World world, ServerPlayerEntity player) {
        NetworkHelpers.getNetwork(world, pos, side)
                .ifPresent(network -> {
                    ITerminalStorageTabIngredientCraftingHandler handler = getHandler();
                    Object craftingJobId = handler.deserializeCraftingJobId(this.craftingJobId.get("id"));
                    handler.cancelCraftingJob(network, channel, craftingJobId);
                });
    }

    protected ITerminalStorageTabIngredientCraftingHandler getHandler() {
        return TerminalStorageTabIngredientCraftingHandlers.REGISTRY.getHandler(
                new ResourceLocation(this.craftingPlanHandler));
    }

}