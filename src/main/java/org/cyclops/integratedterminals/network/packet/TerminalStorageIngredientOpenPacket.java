package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageState;
import org.cyclops.integratedterminals.part.PartTypeTerminalStorage;
import org.cyclops.integratedterminals.part.PartTypes;

import java.util.Optional;

/**
 * Packet for telling the server that the storage terminal gui should be opened on a specific tab.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientOpenPacket extends PacketCodec {

	@CodecField
	private BlockPos pos;
	@CodecField
	private Direction side;
	@CodecField
	private String tabName;
	@CodecField
	private int channel;

    public TerminalStorageIngredientOpenPacket() {

    }

	public TerminalStorageIngredientOpenPacket(BlockPos pos, Direction side, String tabName, int channel) {
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
	@OnlyIn(Dist.CLIENT)
	public void actionClient(World world, PlayerEntity player) {

	}

	@Override
	public void actionServer(World world, ServerPlayerEntity player) {
		openServer(world, pos, side, player, tabName, channel);
	}

	public static void openServer(World world, BlockPos pos, Direction side, ServerPlayerEntity player, String tabName, int channel) {
		// Create common data
		ContainerTerminalStorage.InitTabData initData = new ContainerTerminalStorage.InitTabData(tabName, channel);
		PartPos partPos = PartPos.of(world, pos, side);
		Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(partPos);
		PartTypeTerminalStorage.State state = (PartTypeTerminalStorage.State) data.getLeft()
				.getPartState(data.getRight().getCenter().getSide());
		TerminalStorageState terminalStorageState = state.getPlayerStorageState(player);

		// Create temporary container provider
		INamedContainerProvider containerProvider = new INamedContainerProvider() {
			@Override
			public ITextComponent getDisplayName() {
				return new StringTextComponent("");
			}

			@Override
			public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
				return new ContainerTerminalStorage(id, playerInventory,
						data.getRight(), (PartTypeTerminalStorage) data.getMiddle(),
						Optional.of(initData), terminalStorageState);
			}
		};

		// Trigger gui opening
		NetworkHooks.openGui(player, containerProvider, packetBuffer -> {
			PacketCodec.write(packetBuffer, partPos);
			packetBuffer.writeString(PartTypes.TERMINAL_STORAGE.getUniqueName().toString());

			packetBuffer.writeBoolean(true);
			initData.writeToPacketBuffer(packetBuffer);

			terminalStorageState.writeToPacketBuffer(packetBuffer);
		});
	}

	public static void send(BlockPos pos, Direction side, String tabName, int channel) {
		IntegratedTerminals._instance.getPacketHandler().sendToServer(
				new TerminalStorageIngredientOpenPacket(pos, side, tabName, channel));
	}
	
}