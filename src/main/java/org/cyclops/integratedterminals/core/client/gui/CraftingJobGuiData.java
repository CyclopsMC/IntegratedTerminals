package org.cyclops.integratedterminals.core.client.gui;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.TerminalStorageTabIngredientCraftingHandlers;

/**
 * @author rubensworks
 */
public class CraftingJobGuiData {

    private final BlockPos pos;
    private final Direction side;
    private final int channel;
    private final ITerminalStorageTabIngredientCraftingHandler handler;
    private final Object craftingJob;

    public CraftingJobGuiData(BlockPos pos, Direction side, int channel,
                              ITerminalStorageTabIngredientCraftingHandler handler, Object craftingJob) {
        this.pos = pos;
        this.side = side;
        this.channel = channel;
        this.handler = handler;
        this.craftingJob = craftingJob;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getSide() {
        return side;
    }

    public int getChannel() {
        return channel;
    }

    public ITerminalStorageTabIngredientCraftingHandler getHandler() {
        return handler;
    }

    public Object getCraftingJob() {
        return craftingJob;
    }

    public void writeToPacketBuffer(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeBlockPos(pos);
        packetBuffer.writeInt(side.ordinal());
        packetBuffer.writeInt(channel);
        packetBuffer.writeUtf(handler.getId().toString());
        CompoundTag tag = new CompoundTag();
        tag.put("id", handler.serializeCraftingJobId(craftingJob));
        packetBuffer.writeNbt(tag);
    }

    public static CraftingJobGuiData readFromPacketBuffer(FriendlyByteBuf packetBuffer) {
        BlockPos pos = packetBuffer.readBlockPos();
        Direction side = Direction.values()[packetBuffer.readInt()];
        int channel = packetBuffer.readInt();
        ITerminalStorageTabIngredientCraftingHandler handler = TerminalStorageTabIngredientCraftingHandlers.REGISTRY.getHandler(
                ResourceLocation.parse(packetBuffer.readUtf(32767)));
        Object craftingJob = handler.deserializeCraftingJobId(packetBuffer.readNbt().get("id"));
        return new CraftingJobGuiData(
                pos,
                side,
                channel,
                handler,
                craftingJob
        );
    }
}
