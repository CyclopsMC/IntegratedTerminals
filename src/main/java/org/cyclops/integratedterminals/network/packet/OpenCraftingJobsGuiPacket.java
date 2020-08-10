package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.part.PartTypes;

/**
 * Packet for opening a live crafting plan gui.
 * @author rubensworks
 *
 */
public class OpenCraftingJobsGuiPacket extends PacketCodec {

    @CodecField
    private BlockPos pos;
    @CodecField
    private Direction side;

    public OpenCraftingJobsGuiPacket() {

    }

    public OpenCraftingJobsGuiPacket(BlockPos pos, Direction side) {
        this.pos = pos;
        this.side = side;
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
        PartHelpers.openContainerPart(player, PartPos.of(DimPos.of(world, pos), side), PartTypes.TERMINAL_CRAFTING_JOB);
    }

    public static void send(BlockPos pos, Direction side) {
        IntegratedTerminals._instance.getPacketHandler().sendToServer(new OpenCraftingJobsGuiPacket(pos, side));
    }

}