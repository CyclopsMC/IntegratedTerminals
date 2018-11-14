package org.cyclops.integratedterminals.network.packet;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCrafting;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;

import java.util.List;

/**
 * Packet for sending a storage change event from server to client.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientCraftingOptionsPacket extends PacketCodec {

	@CodecField
	private String tabId;
	@CodecField
	private int channel;
    @CodecField
    private NBTTagCompound data;

    public TerminalStorageIngredientCraftingOptionsPacket() {

    }

	public <T> TerminalStorageIngredientCraftingOptionsPacket(String tabId,
															  int channel,
															  List<HandlerWrappedTerminalCraftingOption<T>> craftingOptions) {
		this.tabId = tabId;
		this.channel = channel;
		this.data = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (HandlerWrappedTerminalCraftingOption<?> option : craftingOptions) {
			list.appendTag(HandlerWrappedTerminalCraftingOption.serialize(option));
		}
		this.data.setTag("craftingOptions", list);
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


			TerminalStorageTabIngredientComponentClient<?, ?> tab = (TerminalStorageTabIngredientComponentClient<?, ?>) container.getTabClient(tabId);
			IngredientComponent<?, ?> ingredientComponent = tab.getIngredientComponent();

			NBTTagList list = this.data.getTagList("craftingOptions", Constants.NBT.TAG_COMPOUND);
			List<HandlerWrappedTerminalCraftingOption<?>> craftingOptions = Lists.newArrayListWithExpectedSize(list.tagCount());
			for (int i = 0; i < list.tagCount(); i++) {
				HandlerWrappedTerminalCraftingOption<?> option = HandlerWrappedTerminalCraftingOption
						.deserialize(ingredientComponent, list.getCompoundTagAt(i));
				craftingOptions.add(option);
			}

			tab.addCraftingOptions(channel, (List) craftingOptions);

			// Hard-coded crafting tab
			// TODO: abstract this as "auxiliary" tabs
			if (tabId.equals(IngredientComponents.ITEMSTACK.getName().toString())) {
				TerminalStorageTabIngredientComponentClient<?, ?> tabCrafting = (TerminalStorageTabIngredientComponentClient<?, ?>) container
						.getTabClient(TerminalStorageTabIngredientComponentItemStackCrafting.NAME.toString());
				tabCrafting.addCraftingOptions(channel, (List) craftingOptions);
			}

			container.refreshChannelStrings();
		}
	}

	@Override
	public void actionServer(World world, EntityPlayerMP player) {

	}
	
}