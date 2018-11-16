package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.core.client.gui.ExtendedGuiHandler;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;
import org.cyclops.integratedterminals.proxy.guiprovider.GuiProviders;

/**
 * Packet for telling the server that the storage terminal gui should be opened on a specific tab.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientOpenPacket extends PacketCodec {

	@CodecField
	private BlockPos pos;
	@CodecField
	private EnumFacing side;
	@CodecField
	private String tabName;
	@CodecField
	private int channel;

    public TerminalStorageIngredientOpenPacket() {

    }

	public TerminalStorageIngredientOpenPacket(BlockPos pos, EnumFacing side, String tabName, int channel) {
    	this.pos = pos;
    	this.side = side;
		this.tabName = tabName;
		this.channel = channel;
	}

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void actionClient(World world, EntityPlayer player) {

	}

	@Override
	public void actionServer(World world, EntityPlayerMP player) {
		IntegratedTerminals._instance.getGuiHandler().setTemporaryData(ExtendedGuiHandler.TERMINAL_STORAGE,
				Pair.of(side, new ContainerTerminalStorage.InitTabData(tabName, channel)));
		player.openGui(IntegratedTerminals._instance, GuiProviders.GUI_TERMINAL_STORAGE_INIT,
				world, pos.getX(), pos.getY(), pos.getZ());
	}
	
}