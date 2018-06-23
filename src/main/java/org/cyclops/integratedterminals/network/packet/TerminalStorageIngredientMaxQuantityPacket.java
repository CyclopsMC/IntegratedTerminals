package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageTabIngredientComponentClient;

/**
 * Packet for sending a storage's quantity from server to client.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientMaxQuantityPacket extends PacketCodec {

	@CodecField
	private String ingredientName;
    @CodecField
    private long maxQuantity;
	@CodecField
	private int channel;

    public TerminalStorageIngredientMaxQuantityPacket() {

    }

    public TerminalStorageIngredientMaxQuantityPacket(IngredientComponent<?, ?> ingredientComponent, long maxQuantity, int channel) {
    	this.ingredientName = ingredientComponent.getName().toString();
		this.maxQuantity = maxQuantity;
		this.channel = channel;
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void actionClient(World world, EntityPlayer player) {
		if(player.openContainer instanceof ContainerTerminalStorage) {
			IngredientComponent<?, ?> ingredientComponent = IngredientComponent.REGISTRY.getValue(new ResourceLocation(this.ingredientName));
			if (ingredientComponent == null) {
				throw new IllegalArgumentException("No ingredient component with the given name was found: " + ingredientName);
			}
			TerminalStorageTabIngredientComponentClient<?, ?> tab = (TerminalStorageTabIngredientComponentClient<?, ?>) ((ContainerTerminalStorage) player.openContainer).getTabClient(ingredientComponent);
			tab.setMaxQuantity(channel, maxQuantity);
		}
	}

	@Override
	public void actionServer(World world, EntityPlayerMP player) {

	}
	
}