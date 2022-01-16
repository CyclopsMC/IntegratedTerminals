package org.cyclops.integratedterminals.network.packet;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientCollection;
import org.cyclops.cyclopscore.ingredient.collection.IngredientArrayList;
import org.cyclops.cyclopscore.ingredient.collection.IngredientCollections;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentStorageObservable;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCrafting;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

/**
 * Packet for sending a storage change event from server to client.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientChangeEventPacket extends PacketCodec {

	@CodecField
	private String tabId;
    @CodecField
    private CompoundTag changeData;
	@CodecField
	private int channel;
	@CodecField
	private boolean enabled;

    public TerminalStorageIngredientChangeEventPacket() {

    }

    public TerminalStorageIngredientChangeEventPacket(String tabId,
													  IIngredientComponentStorageObservable.StorageChangeEvent<?, ?> event,
													  boolean enabled) {
    	this.tabId = tabId;
		IIngredientComponentStorageObservable.Change changeType = event.getChangeType();
		IIngredientCollection<?, ?> instances = event.getInstances();
		CompoundTag serialized = IngredientCollections.serialize(instances);
		serialized.putInt("changeType", changeType.ordinal());
		this.changeData = serialized;
		this.channel = event.getChannel();
		this.enabled = enabled;
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void actionClient(Level world, Player player) {
		if(player.containerMenu instanceof ContainerTerminalStorageBase) {
			ContainerTerminalStorageBase container = ((ContainerTerminalStorageBase) player.containerMenu);
			IIngredientComponentStorageObservable.Change changeType = IIngredientComponentStorageObservable.Change.values()[changeData.getInt("changeType")];
			IngredientArrayList ingredients = IngredientCollections.deserialize(changeData);

			TerminalStorageTabIngredientComponentClient<?, ?> tab = (TerminalStorageTabIngredientComponentClient<?, ?>) container.getTabClient(tabId);
			tab.onChange(channel, changeType, ingredients, enabled);

			// Hard-coded crafting tab
			// TODO: abstract this as "auxiliary" tabs
			if (tabId.equals(IngredientComponents.ITEMSTACK.getName().toString())) {
				TerminalStorageTabIngredientComponentClient<?, ?> tabCrafting = (TerminalStorageTabIngredientComponentClient<?, ?>) container
						.getTabClient(TerminalStorageTabIngredientComponentItemStackCrafting.NAME.toString());
				tabCrafting.onChange(channel, changeType, ingredients, enabled);
			}

			container.refreshChannelStrings();
		}
	}

	@Override
	public void actionServer(Level world, ServerPlayer player) {

	}
	
}