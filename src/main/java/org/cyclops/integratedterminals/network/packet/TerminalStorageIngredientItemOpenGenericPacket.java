package org.cyclops.integratedterminals.network.packet;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.RegistryEntries;
import org.cyclops.integratedterminals.item.ItemTerminalStoragePortable;

/**
 * Packet for telling the server that the storage terminal gui should be opened without a specific tab.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientItemOpenGenericPacket extends PacketCodec {

	@CodecField
	private int slot;
	@CodecField
	private String handName;

    public TerminalStorageIngredientItemOpenGenericPacket() {

    }

	public TerminalStorageIngredientItemOpenGenericPacket(Pair<InteractionHand, Integer> location) {
    	this.slot = location.getRight();
    	this.handName = location.getLeft().name();
	}

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void actionClient(Level world, Player player) {

	}

	@Override
	public void actionServer(Level world, ServerPlayer player) {
		openServer(world, Pair.of(InteractionHand.valueOf(handName), slot), player);
	}

	public static void openServer(Level world, Pair<InteractionHand, Integer> location, ServerPlayer player) {
		((ItemTerminalStoragePortable) RegistryEntries.ITEM_TERMINAL_STORAGE_PORTABLE)
				.openGuiForItemIndex(world, player, location.getRight(), location.getLeft());
	}

	public static void send(Pair<InteractionHand, Integer> location) {
		IntegratedTerminals._instance.getPacketHandler().sendToServer(
				new TerminalStorageIngredientItemOpenGenericPacket(location));
	}
	
}