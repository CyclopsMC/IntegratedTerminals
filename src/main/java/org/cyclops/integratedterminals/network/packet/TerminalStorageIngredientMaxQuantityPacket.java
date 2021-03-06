package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCrafting;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

/**
 * Packet for sending a storage's quantity from server to client.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientMaxQuantityPacket extends PacketCodec {

	@CodecField
	private String tabId;
	@CodecField
	private String ingredientName;
    @CodecField
    private long maxQuantity;
	@CodecField
	private int channel;

    public TerminalStorageIngredientMaxQuantityPacket() {

    }

    public TerminalStorageIngredientMaxQuantityPacket(String tabId, IngredientComponent<?, ?> ingredientComponent,
													  long maxQuantity, int channel) {
		this.tabId = tabId;
    	this.ingredientName = ingredientComponent.getName().toString();
		this.maxQuantity = maxQuantity;
		this.channel = channel;
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void actionClient(World world, PlayerEntity player) {
		if(player.openContainer instanceof ContainerTerminalStorageBase) {
			ContainerTerminalStorageBase container = ((ContainerTerminalStorageBase) player.openContainer);
			IngredientComponent<?, ?> ingredientComponent = IngredientComponent.REGISTRY.getValue(new ResourceLocation(this.ingredientName));
			if (ingredientComponent == null) {
				throw new IllegalArgumentException("No ingredient component with the given name was found: " + ingredientName);
			}
			TerminalStorageTabIngredientComponentClient<?, ?> tab = (TerminalStorageTabIngredientComponentClient<?, ?>) container.getTabClient(tabId);
			tab.setMaxQuantity(channel, maxQuantity);

			// Hard-coded crafting tab
			// TODO: abstract this as "auxiliary" tabs
			if (tabId.equals(IngredientComponents.ITEMSTACK.getName().toString())) {
				TerminalStorageTabIngredientComponentClient<?, ?> tabCrafting = (TerminalStorageTabIngredientComponentClient<?, ?>) container
						.getTabClient(TerminalStorageTabIngredientComponentItemStackCrafting.NAME.toString());
				tabCrafting.setMaxQuantity(channel, maxQuantity);
			}
		}
	}

	@Override
	public void actionServer(World world, ServerPlayerEntity player) {

	}
	
}