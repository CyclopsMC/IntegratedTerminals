package org.cyclops.integratedterminals.network.packet;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientSerializer;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

/**
 * Packet for sending the currently active storage stack from server to client.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientUpdateActiveStorageIngredientPacket<T> extends PacketCodec {

    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "terminal_storage_ingredient_update_active_storage_ingredient");

    @CodecField
    private String tabId;
    @CodecField
    private String ingredientName;
    @CodecField
    private int channel;
    @CodecField
    private CompoundTag activeStorageInstanceData;

    public TerminalStorageIngredientUpdateActiveStorageIngredientPacket() {
        super(ID);
    }

    public TerminalStorageIngredientUpdateActiveStorageIngredientPacket(String tabId,
                                                                        IngredientComponent<T, ?> component,
                                                                        int channel, T activeStorageInstance) {
        super(ID);
        this.tabId = tabId;
        this.ingredientName = component.getName().toString();
        this.channel = channel;
        IIngredientSerializer<T, ?> serializer = getComponent().getSerializer();
        this.activeStorageInstanceData = new CompoundTag();
        this.activeStorageInstanceData.put("i", serializer.serializeInstance(activeStorageInstance));
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
            TerminalStorageTabIngredientComponentClient<T, ?> tab = (TerminalStorageTabIngredientComponentClient<T, ?>)
                    container.getTabClient(tabId);
            IIngredientSerializer<T, ?> serializer = getComponent().getSerializer();
            T activeInstance = serializer.deserializeInstance(this.activeStorageInstanceData.get("i"));
            tab.handleActiveIngredientUpdate(getChannel(), activeInstance);
        }
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {

    }

    public IngredientComponent<T, ?> getComponent() {
        IngredientComponent<T, ?> ingredientComponent = (IngredientComponent<T, ?>) IngredientComponent.REGISTRY.get(new ResourceLocation(this.ingredientName));
        if (ingredientComponent == null) {
            throw new IllegalArgumentException("No ingredient component with the given name was found: " + ingredientName);
        }
        return ingredientComponent;
    }

    public int getChannel() {
        return channel;
    }

}
