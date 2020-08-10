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
import org.cyclops.integratedterminals.api.terminalstorage.TerminalClickType;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;

/**
 * Packet for sending a storage slot click event from client to server.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientSlotClickPacket<T> extends PacketCodec {

    @CodecField
    private String tabId;
    @CodecField
    private String ingredientName;
    @CodecField
    private int clickType;
    @CodecField
    private int channel;
    @CodecField
    private CompoundNBT hoveringStorageInstanceData;
    @CodecField
    private int hoveredContainerSlot;
    @CodecField
    private long moveQuantityPlayerSlot;
    @CodecField
    private CompoundNBT activeStorageInstanceData;
    @CodecField
    private boolean transferFullSelection;

    public TerminalStorageIngredientSlotClickPacket() {

    }

    public TerminalStorageIngredientSlotClickPacket(String tabId, IngredientComponent<T, ?> component,
                                                    TerminalClickType clickType,
                                                    int channel, T hoveringStorageInstance,
                                                    int hoveredContainerSlot, long moveQuantityPlayerSlot,
                                                    T activeStorageInstance, boolean transferFullSelection) {
        this.tabId = tabId;
        this.clickType = clickType.ordinal();
        this.ingredientName = component.getName().toString();
        this.channel = channel;
        this.hoveringStorageInstanceData = new CompoundNBT();
        IIngredientSerializer<T, ?> serializer = getComponent().getSerializer();
        this.hoveringStorageInstanceData.put("i", serializer.serializeInstance(hoveringStorageInstance));
        this.hoveredContainerSlot = hoveredContainerSlot;
        this.moveQuantityPlayerSlot = moveQuantityPlayerSlot;
        this.activeStorageInstanceData = new CompoundNBT();
        this.activeStorageInstanceData.put("i", serializer.serializeInstance(activeStorageInstance));
        this.transferFullSelection = transferFullSelection;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(World world, PlayerEntity player) {

    }

    @Override
    public void actionServer(World world, ServerPlayerEntity player) {
        if(player.openContainer instanceof ContainerTerminalStorage) {
            ContainerTerminalStorage container = ((ContainerTerminalStorage) player.openContainer);
            TerminalStorageTabIngredientComponentServer<T, ?> tab = (TerminalStorageTabIngredientComponentServer<T, ?>)
                    container.getTabServer(tabId);
            IIngredientSerializer<T, ?> serializer = getComponent().getSerializer();
            T hoveringStorageInstance = serializer.deserializeInstance(this.hoveringStorageInstanceData.get("i"));
            T activeInstance = serializer.deserializeInstance(this.activeStorageInstanceData.get("i"));
            tab.handleStorageSlotClick(container, player, getClickType(), getChannel(), hoveringStorageInstance,
                    hoveredContainerSlot, moveQuantityPlayerSlot, activeInstance, transferFullSelection);
        }
    }

    public TerminalClickType getClickType() {
        return TerminalClickType.values()[this.clickType];
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