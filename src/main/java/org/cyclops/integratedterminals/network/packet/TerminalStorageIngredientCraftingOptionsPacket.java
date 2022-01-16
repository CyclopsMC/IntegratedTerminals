package org.cyclops.integratedterminals.network.packet;

import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCrafting;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

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
    private CompoundTag data;
    @CodecField
	private boolean reset;
	@CodecField
	private boolean firstChannel;

    public TerminalStorageIngredientCraftingOptionsPacket() {

    }

	public <T> TerminalStorageIngredientCraftingOptionsPacket(String tabId,
															  int channel,
															  List<HandlerWrappedTerminalCraftingOption<T>> craftingOptions,
															  boolean reset,
															  boolean firstChannel) {
		this.tabId = tabId;
		this.channel = channel;
		this.data = new CompoundTag();
		ListTag list = new ListTag();
		for (HandlerWrappedTerminalCraftingOption<?> option : craftingOptions) {
			list.add(HandlerWrappedTerminalCraftingOption.serialize(option));
		}
		this.data.put("craftingOptions", list);
		this.reset = reset;
		this.firstChannel = firstChannel;
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


			TerminalStorageTabIngredientComponentClient<?, ?> tab = (TerminalStorageTabIngredientComponentClient<?, ?>) container.getTabClient(tabId);
			IngredientComponent<?, ?> ingredientComponent = tab.getIngredientComponent();

			ListTag list = this.data.getList("craftingOptions", Tag.TAG_COMPOUND);
			List<HandlerWrappedTerminalCraftingOption<?>> craftingOptions = Lists.newArrayListWithExpectedSize(list.size());
			for (int i = 0; i < list.size(); i++) {
				HandlerWrappedTerminalCraftingOption<?> option = HandlerWrappedTerminalCraftingOption
						.deserialize(ingredientComponent, list.getCompound(i));
				craftingOptions.add(option);
			}

			tab.addCraftingOptions(channel, (List) craftingOptions, this.reset, this.firstChannel);

			// Hard-coded crafting tab
			// TODO: abstract this as "auxiliary" tabs
			if (tabId.equals(IngredientComponents.ITEMSTACK.getName().toString())) {
				TerminalStorageTabIngredientComponentClient<?, ?> tabCrafting = (TerminalStorageTabIngredientComponentClient<?, ?>) container
						.getTabClient(TerminalStorageTabIngredientComponentItemStackCrafting.NAME.toString());
				tabCrafting.addCraftingOptions(channel, (List) craftingOptions, this.reset, this.firstChannel);
			}

			container.refreshChannelStrings();
		}
	}

	@Override
	public void actionServer(Level world, ServerPlayer player) {

	}
	
}