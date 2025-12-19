package org.cyclops.integratedterminals.network.packet;

import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientSerializer;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.item.TagPathElement;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.TerminalClickType;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;
import org.slf4j.Logger;

/**
 * Packet for sending a storage slot click event from client to server.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientSlotClickPacket<T> extends PacketCodec<TerminalStorageIngredientSlotClickPacket<T>> {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Type<TerminalStorageIngredientSlotClickPacket<?>> ID = new Type<>(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "terminal_storage_ingredient_slot_click"));
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
        IIngredientSerializer<T, ?> serializer = getComponent().getSerializer();
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new TagPathElement(new CompoundTag()), LOGGER)) {
            TagValueOutput valueOutput = TagValueOutput.createWithContext(scopedCollector, lookupProvider);
            serializer.serializeInstance(valueOutput, hoveringStorageInstance);
            this.hoveringStorageInstanceData = valueOutput.buildResult();
        }
        this.hoveredContainerSlot = hoveredContainerSlot;
        this.moveQuantityPlayerSlot = moveQuantityPlayerSlot;
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new TagPathElement(new CompoundTag()), LOGGER)) {
            TagValueOutput valueOutput = TagValueOutput.createWithContext(scopedCollector, lookupProvider);
            serializer.serializeInstance(valueOutput, activeStorageInstance);
            this.activeStorageInstanceData = valueOutput.buildResult();
        }
        this.transferFullSelection = transferFullSelection;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(Level world, Player player) {

    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
        if(player.containerMenu instanceof ContainerTerminalStorageBase) {
            ContainerTerminalStorageBase container = ((ContainerTerminalStorageBase) player.containerMenu);
            TerminalStorageTabIngredientComponentServer<T, ?> tab = (TerminalStorageTabIngredientComponentServer<T, ?>)
                    container.getTabServer(tabId);
            IIngredientSerializer<T, ?> serializer = getComponent().getSerializer();
            T hoveringStorageInstance;
            T activeInstance;
            try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new TagPathElement(this.hoveringStorageInstanceData), LOGGER)) {
                ValueInput input = TagValueInput.create(scopedCollector, world.registryAccess(), this.hoveringStorageInstanceData);
                hoveringStorageInstance = serializer.deserializeInstance(input);
            }
            try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new TagPathElement(this.activeStorageInstanceData), LOGGER)) {
                ValueInput input = TagValueInput.create(scopedCollector, world.registryAccess(), this.activeStorageInstanceData);
                activeInstance = serializer.deserializeInstance(input);
            }
            tab.handleStorageSlotClick(container, player, getClickType(), getChannel(), hoveringStorageInstance,
                    hoveredContainerSlot, moveQuantityPlayerSlot, activeInstance, transferFullSelection);
        }
    }

    public TerminalClickType getClickType() {
        return TerminalClickType.values()[this.clickType];
    }

    public IngredientComponent<T, ?> getComponent() {
        IngredientComponent<T, ?> ingredientComponent = (IngredientComponent<T, ?>) IngredientComponent.REGISTRY.getValue(Identifier.parse(this.ingredientName));
        if (ingredientComponent == null) {
            throw new IllegalArgumentException("No ingredient component with the given name was found: " + ingredientName);
        }
        return ingredientComponent;
    }

    public int getChannel() {
        return channel;
    }

}
