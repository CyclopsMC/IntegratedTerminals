package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientCollection;
import org.cyclops.cyclopscore.ingredient.collection.IngredientArrayList;
import org.cyclops.cyclopscore.ingredient.collection.IngredientCollections;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentStorageObservable;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCrafting;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;

/**
 * Packet for sending a storage change event from server to client.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientChangeEventPacket extends PacketCodec {

	@CodecField
	private String tabId;
    @CodecField
    private NBTTagCompound changeData;
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
		NBTTagCompound serialized = IngredientCollections.serialize(instances);
		serialized.setInteger("changeType", changeType.ordinal());
		this.changeData = serialized;
		this.channel = event.getChannel();
		this.enabled = enabled;
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void actionClient(World world, EntityPlayer player) {
		if(player.openContainer instanceof ContainerTerminalStorage) {
			ContainerTerminalStorage container = ((ContainerTerminalStorage) player.openContainer);
			IIngredientComponentStorageObservable.Change changeType = IIngredientComponentStorageObservable.Change.values()[changeData.getInteger("changeType")];
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
	public void actionServer(World world, EntityPlayerMP player) {

	}
	
}