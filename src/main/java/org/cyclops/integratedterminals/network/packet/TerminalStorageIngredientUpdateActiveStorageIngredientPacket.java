package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientSerializer;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

/**
 * Packet for sending the currently active storage stack from server to client.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientUpdateActiveStorageIngredientPacket<T> extends PacketCodec {

    @CodecField
    private String tabId;
    @CodecField
    private String ingredientName;
    @CodecField
    private int channel;
    @CodecField
    private CompoundNBT activeStorageInstanceData;

    public TerminalStorageIngredientUpdateActiveStorageIngredientPacket() {

    }

    public TerminalStorageIngredientUpdateActiveStorageIngredientPacket(String tabId,
                                                                        IngredientComponent<T, ?> component,
                                                                        int channel, T activeStorageInstance) {
        this.tabId = tabId;
        this.ingredientName = component.getName().toString();
        this.channel = channel;
        IIngredientSerializer<T, ?> serializer = getComponent().getSerializer();
        this.activeStorageInstanceData = new CompoundNBT();
        this.activeStorageInstanceData.put("i", serializer.serializeInstance(activeStorageInstance));
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(World world, PlayerEntity player) {
        if(player.containerMenu instanceof ContainerTerminalStorageBase) {
            ContainerTerminalStorageBase container = ((ContainerTerminalStorageBase) player.containerMenu);
            TerminalStorageTabIngredientComponentClient<T, ?> tab = (TerminalStorageTabIngredientComponentClient<T, ?>)
                    container.getTabClient(tabId);
            IIngredientSerializer<T, ?> serializer = getComponent().getSerializer();
            T activeInstance = serializer.deserializeInstance(this.activeStorageInstanceData.get("i"));
            tab.handleActiveIngredientUpdate(getChannel(), activeInstance);
        }
    }

    @Override
    public void actionServer(World world, ServerPlayerEntity player) {

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