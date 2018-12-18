package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.client.gui.ExtendedGuiHandler;
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
    private EnumFacing side;

    public OpenCraftingJobsGuiPacket() {

    }

    public OpenCraftingJobsGuiPacket(BlockPos pos, EnumFacing side) {
        this.pos = pos;
        this.side = side;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {

    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        IntegratedDynamics._instance.getGuiHandler().setTemporaryData(org.cyclops.integrateddynamics.core.client.gui.ExtendedGuiHandler.PART, side);
        player.openGui(IntegratedDynamics._instance, PartTypes.TERMINAL_CRAFTING_JOB.getGuiID(),
                world, pos.getX(), pos.getY(), pos.getZ());
    }

    public static void send(BlockPos pos, EnumFacing side) {
        IntegratedDynamics._instance.getGuiHandler().setTemporaryData(ExtendedGuiHandler.PART, side);
        IntegratedTerminals._instance.getPacketHandler().sendToServer(new OpenCraftingJobsGuiPacket(pos, side));
    }

}