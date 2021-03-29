package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageItem;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageState;
import org.cyclops.integratedterminals.item.ItemTerminalStoragePortable;

import java.util.Optional;

/**
 * Packet for telling the server that the storage terminal gui should be opened on a specific tab.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientItemOpenPacket extends PacketCodec {

	@CodecField
	private int slot;
	@CodecField
	private String handName;
	@CodecField
	private String tabName;
	@CodecField
	private int channel;

    public TerminalStorageIngredientItemOpenPacket() {

    }

	public TerminalStorageIngredientItemOpenPacket(Pair<Hand, Integer> location, String tabName, int channel) {
    	this.slot = location.getRight();
    	this.handName = location.getLeft().name();
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
		openServer(world, Pair.of(Hand.valueOf(handName), slot), player, tabName, channel);
	}

	public static void openServer(World world, Pair<Hand, Integer> location, ServerPlayerEntity player, String tabName, int channel) {
		// Create common data
		ContainerTerminalStorageBase.InitTabData initData = new ContainerTerminalStorageBase.InitTabData(tabName, channel);
		TerminalStorageState terminalStorageState = ItemTerminalStoragePortable.getTerminalStorageState(InventoryHelpers
				.getItemFromIndex(player, location.getRight(), location.getLeft()), player, location.getRight(), location.getLeft());

		// Create temporary container provider
		INamedContainerProvider containerProvider = new INamedContainerProvider() {
			@Override
			public ITextComponent getDisplayName() {
				return new StringTextComponent("");
			}

			@Override
			public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
				return new ContainerTerminalStorageItem(id, playerInventory,
						location.getRight(), location.getLeft(),
						Optional.of(initData), terminalStorageState);
			}
		};

		// Trigger gui opening
		NetworkHooks.openGui(player, containerProvider, packetBuffer -> {
			packetBuffer.writeInt(location.getRight());
			packetBuffer.writeBoolean(location.getLeft() == Hand.MAIN_HAND);

			packetBuffer.writeBoolean(true);
			initData.writeToPacketBuffer(packetBuffer);

			terminalStorageState.writeToPacketBuffer(packetBuffer);
		});
	}

	public static void send(Pair<Hand, Integer> location, String tabName, int channel) {
		IntegratedTerminals._instance.getPacketHandler().sendToServer(
				new TerminalStorageIngredientItemOpenPacket(location, tabName, channel));
	}
	
}