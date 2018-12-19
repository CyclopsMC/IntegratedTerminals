package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.network.INetwork;
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
    private EnumFacing side;
    @CodecField
    private int channel;
    @CodecField
    private String craftingPlanHandler;
    @CodecField
    private NBTTagCompound craftingJobId;

    public CancelCraftingJobPacket() {

    }

    public CancelCraftingJobPacket(CraftingJobGuiData craftingPlanGuiData) {
        this.pos = craftingPlanGuiData.getPos();
        this.side = craftingPlanGuiData.getSide();
        this.channel = craftingPlanGuiData.getChannel();
        this.craftingPlanHandler = craftingPlanGuiData.getHandler().getId().toString();
        this.craftingJobId = new NBTTagCompound();
        this.craftingJobId.setTag("id", craftingPlanGuiData.getHandler().serializeCraftingJobId(craftingPlanGuiData.getCraftingJob()));
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
        INetwork network = NetworkHelpers.getNetwork(world, pos, side);
        ITerminalStorageTabIngredientCraftingHandler handler = getHandler();
        Object craftingJobId = handler.deserializeCraftingJobId(this.craftingJobId.getTag("id"));
        handler.cancelCraftingJob(network, channel, craftingJobId);
    }

    protected ITerminalStorageTabIngredientCraftingHandler getHandler() {
        return TerminalStorageTabIngredientCraftingHandlers.REGISTRY.getHandler(
                new ResourceLocation(this.craftingPlanHandler));
    }

}