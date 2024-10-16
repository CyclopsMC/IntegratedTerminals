package org.cyclops.integratedterminals.network.packet;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientSerializer;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.TerminalClickType;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

/**
 * Packet for sending a storage slot click event from client to server.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientSlotClickPacket<T> extends PacketCodec<TerminalStorageIngredientSlotClickPacket<T>> {

    public static final Type<TerminalStorageIngredientSlotClickPacket<?>> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "terminal_storage_ingredient_slot_click"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalStorageIngredientSlotClickPacket<?>> CODEC = (StreamCodec) getCodec(TerminalStorageIngredientSlotClickPacket::new);

    @CodecField
    private String tabId;
    @CodecField
    private String ingredientName;
    @CodecField
    private int clickType;
    @CodecField
    private int channel;
    @CodecField
    private CompoundTag hoveringStorageInstanceData;
    @CodecField
    private int hoveredContainerSlot;
    @CodecField
    private long moveQuantityPlayerSlot;
    @CodecField
    private CompoundTag activeStorageInstanceData;
    @CodecField
    private boolean transferFullSelection;

    public TerminalStorageIngredientSlotClickPacket() {
        super((Type) ID);
    }

    public TerminalStorageIngredientSlotClickPacket(HolderLookup.Provider lookupProvider, String tabId, IngredientComponent<T, ?> component,
                                                    TerminalClickType clickType,
                                                    int channel, T hoveringStorageInstance,
                                                    int hoveredContainerSlot, long moveQuantityPlayerSlot,
                                                    T activeStorageInstance, boolean transferFullSelection) {
        super((Type) ID);
        this.tabId = tabId;
        this.clickType = clickType.ordinal();
        this.ingredientName = component.getName().toString();
        this.channel = channel;
        this.hoveringStorageInstanceData = new CompoundTag();
        IIngredientSerializer<T, ?> serializer = getComponent().getSerializer();
        this.hoveringStorageInstanceData.put("i", serializer.serializeInstance(lookupProvider, hoveringStorageInstance));
        this.hoveredContainerSlot = hoveredContainerSlot;
        this.moveQuantityPlayerSlot = moveQuantityPlayerSlot;
        this.activeStorageInstanceData = new CompoundTag();
        this.activeStorageInstanceData.put("i", serializer.serializeInstance(lookupProvider, activeStorageInstance));
        this.transferFullSelection = transferFullSelection;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(Level world, Player player) {

    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
        if(player.containerMenu instanceof ContainerTerminalStorageBase) {
            ContainerTerminalStorageBase container = ((ContainerTerminalStorageBase) player.containerMenu);
            TerminalStorageTabIngredientComponentServer<T, ?> tab = (TerminalStorageTabIngredientComponentServer<T, ?>)
                    container.getTabServer(tabId);
            IIngredientSerializer<T, ?> serializer = getComponent().getSerializer();
            T hoveringStorageInstance = serializer.deserializeInstance(world.registryAccess(), this.hoveringStorageInstanceData.get("i"));
            T activeInstance = serializer.deserializeInstance(world.registryAccess(), this.activeStorageInstanceData.get("i"));
            tab.handleStorageSlotClick(container, player, getClickType(), getChannel(), hoveringStorageInstance,
                    hoveredContainerSlot, moveQuantityPlayerSlot, activeInstance, transferFullSelection);
        }
    }

    public TerminalClickType getClickType() {
        return TerminalClickType.values()[this.clickType];
    }

    public IngredientComponent<T, ?> getComponent() {
        IngredientComponent<T, ?> ingredientComponent = (IngredientComponent<T, ?>) IngredientComponent.REGISTRY.get(ResourceLocation.parse(this.ingredientName));
        if (ingredientComponent == null) {
            throw new IllegalArgumentException("No ingredient component with the given name was found: " + ingredientName);
        }
        return ingredientComponent;
    }

    public int getChannel() {
        return channel;
    }

}
