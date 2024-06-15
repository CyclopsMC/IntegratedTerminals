package org.cyclops.integratedterminals.network.packet;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.part.PartTypes;

/**
 * Packet for opening a live crafting plan gui.
 * @author rubensworks
 *
 */
public class OpenCraftingJobsGuiPacket extends PacketCodec {

    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "open_crafting_jobs_gui");

    @CodecField
    private BlockPos pos;
    @CodecField
    private Direction side;

    public OpenCraftingJobsGuiPacket() {
        super(ID);
    }

    public OpenCraftingJobsGuiPacket(BlockPos pos, Direction side) {
        super(ID);
        this.pos = pos;
        this.side = side;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(Level world, Player player) {

    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
        PartHelpers.openContainerPart(player, PartPos.of(DimPos.of(world, pos), side), PartTypes.TERMINAL_CRAFTING_JOB);
    }

    public static void send(BlockPos pos, Direction side) {
        IntegratedTerminals._instance.getPacketHandler().sendToServer(new OpenCraftingJobsGuiPacket(pos, side));
    }

}
