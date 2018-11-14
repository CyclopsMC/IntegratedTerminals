package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;

/**
 * Packet for sending a storage slot click event from client to server.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientCalculateCraftingJob<T> extends PacketCodec {

    @CodecField
    private String tabId;
    @CodecField
    private String ingredientName;
    @CodecField
    private int channel;
    @CodecField
    private NBTTagCompound craftingOption;

    public TerminalStorageIngredientCalculateCraftingJob() {

    }

    public TerminalStorageIngredientCalculateCraftingJob(String tabId, IngredientComponent<T, ?> component,
                                                         int channel, HandlerWrappedTerminalCraftingOption<T> craftingOption) {
        this.tabId = tabId;
        this.ingredientName = component.getName().toString();
        this.channel = channel;
        this.craftingOption = HandlerWrappedTerminalCraftingOption.serialize(craftingOption);
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
        if(player.openContainer instanceof ContainerTerminalStorage) {
            ContainerTerminalStorage container = ((ContainerTerminalStorage) player.openContainer);
            TerminalStorageTabIngredientComponentServer<T, ?> tab = (TerminalStorageTabIngredientComponentServer<T, ?>)
                    container.getTabServer(tabId);
            HandlerWrappedTerminalCraftingOption<T> craftingOption = HandlerWrappedTerminalCraftingOption
                    .deserialize(tab.getIngredientNetwork().getComponent(), this.craftingOption);

            // TODO: calculate crafing job, and send back to client
            System.out.println("Request crafting of " + craftingOption.getCraftingOption().getOutputs().next()); // TODO
        }
    }

    public IngredientComponent<T, ?> getComponent() {
        IngredientComponent<T, ?> ingredientComponent = (IngredientComponent<T, ?>) IngredientComponent.REGISTRY.getValue(new ResourceLocation(this.ingredientName));
        if (ingredientComponent == null) {
            throw new IllegalArgumentException("No ingredient component with the given name was found: " + ingredientName);
        }
        return ingredientComponent;
    }

    public int getChannel() {
        return channel;
    }

}