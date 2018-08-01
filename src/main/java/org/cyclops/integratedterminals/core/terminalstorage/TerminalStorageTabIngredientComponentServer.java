package org.cyclops.integratedterminals.core.terminalstorage;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.ResourceLocation;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientCollection;
import org.cyclops.cyclopscore.ingredient.collection.IngredientArrayList;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentStorageObservable;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabServer;
import org.cyclops.integratedterminals.api.terminalstorage.TerminalClickType;
import org.cyclops.integratedterminals.capability.ingredient.IngredientComponentTerminalStorageHandlerConfig;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientChangeEventPacket;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientMaxQuantityPacket;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientUpdateActiveStorageIngredientPacket;

import javax.annotation.Nullable;

/**
 * A server-side storage terminal ingredient tab.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientComponentServer<T, M> implements ITerminalStorageTabServer,
        IIngredientComponentStorageObservable.IIndexChangeObserver<T, M> {

    private final ResourceLocation name;
    private final IngredientComponent<T, M> ingredientComponent;
    private final IPositionedAddonsNetworkIngredients<T, M> ingredientNetwork;
    private final PartPos pos;
    private final EntityPlayerMP player;

    public TerminalStorageTabIngredientComponentServer(ResourceLocation name, IngredientComponent<T, M> ingredientComponent,
                                                       IPositionedAddonsNetworkIngredients<T, M> ingredientNetwork,
                                                       PartPos pos,
                                                       EntityPlayerMP player) {
        this.name = name;
        this.ingredientComponent = ingredientComponent;
        this.ingredientNetwork = ingredientNetwork;
        this.pos = pos;
        this.player = player;
    }

    @Override
    public ResourceLocation getName() {
        return name;
    }

    @Override
    public void init() {
        // Send _all_ current network contents
        for (int channel : this.ingredientNetwork.getChannels()) {
            initChannel(channel);
        }

        // Listen to future network changes
        this.ingredientNetwork.addObserver(this);
    }

    protected void initChannel(int channel) {
        IIngredientComponentStorage<T, M> channelInstance = this.ingredientNetwork.getChannel(channel);
        IIngredientCollection<T, M> ingredientCollection = new IngredientArrayList<>(ingredientComponent,
                channelInstance);
        sendToClient(new IIngredientComponentStorageObservable.StorageChangeEvent<>(channel, pos,
                IIngredientComponentStorageObservable.Change.ADDITION, false,
                ingredientCollection));
    }

    @Override
    public void deInit() {
        this.ingredientNetwork.removeObserver(this);
    }

    @Override
    public boolean isForChannel(int channel) {
        return true;
    }

    @Override
    public void onChange(IIngredientComponentStorageObservable.StorageChangeEvent<T, M> event) {
        sendToClient(event);
    }

    protected void sendToClient(IIngredientComponentStorageObservable.StorageChangeEvent<T, M> event) {
        long maxQuantity = this.ingredientNetwork.getChannel(event.getChannel()).getMaxQuantity();

        // Only allow ingredient collection of a max given size to be sent in a packet
        if (event.getInstances().size() <= GeneralConfig.terminalStoragePacketMaxInstances) {
            IntegratedTerminals._instance.getPacketHandler().sendToPlayer(
                    new TerminalStorageIngredientChangeEventPacket(this.getName().toString(), event, this.ingredientNetwork.hasPositions()), player);
            IntegratedTerminals._instance.getPacketHandler().sendToPlayer(
                    new TerminalStorageIngredientMaxQuantityPacket(this.getName().toString(), event.getInstances().getComponent(), maxQuantity, event.getChannel()), player);
        } else {
            IngredientArrayList<T, M> buffer = new IngredientArrayList<>(event.getInstances().getComponent(),
                    GeneralConfig.terminalStoragePacketMaxInstances);
            for (T instance : event.getInstances()) {
                buffer.add(instance);

                // If our buffer reaches its capacity,
                // flush it, and create a new buffer
                if (buffer.size() == GeneralConfig.terminalStoragePacketMaxInstances) {
                    sendToClient(new IIngredientComponentStorageObservable.StorageChangeEvent<>(
                            event.getChannel(), event.getPos(), event.getChangeType(), event.isCompleteChange(), buffer
                    ));
                    buffer = new IngredientArrayList<>(event.getInstances().getComponent(),
                            GeneralConfig.terminalStoragePacketMaxInstances);
                }
            }

            // Our buffer can contain some remaining instances, make sure to flush them as well.
            if (!buffer.isEmpty()) {
                sendToClient(new IIngredientComponentStorageObservable.StorageChangeEvent<>(
                        event.getChannel(), event.getPos(), event.getChangeType(), event.isCompleteChange(), buffer
                ));
            }
        }
    }

    public IPositionedAddonsNetworkIngredients<T, M> getIngredientNetwork() {
        return ingredientNetwork;
    }

    @Nullable
    public void handleStorageSlotClick(Container container, EntityPlayerMP player, TerminalClickType clickType,
                                       int channel, T hoveringStorageInstance, int hoveredContainerSlot,
                                       long moveQuantityPlayerSlot, T activeStorageInstance) {
        IIngredientComponentTerminalStorageHandler<T, M> viewHandler = ingredientComponent.getCapability(IngredientComponentTerminalStorageHandlerConfig.CAPABILITY);
        IIngredientComponentStorage<T, M> storage = ingredientNetwork.getChannel(channel);

        boolean updateActivePlayerStack = false;

        switch (clickType) {
            case STORAGE_QUICK_MOVE:
                viewHandler.insertMaxIntoContainer(storage, container, hoveringStorageInstance);
                break;
            case STORAGE_PLACE_WORLD:
                viewHandler.throwIntoWorld(storage, activeStorageInstance, player);
                break;
            case STORAGE_PLACE_PLAYER:
                T movedInstance = viewHandler.insertIntoContainer(storage, container, hoveredContainerSlot, activeStorageInstance);
                IIngredientMatcher<T, M> matcher = this.ingredientComponent.getMatcher();
                T remainingInstance = matcher.withQuantity(movedInstance,
                        matcher.getQuantity(activeStorageInstance) - matcher.getQuantity(movedInstance));
                IntegratedTerminals._instance.getPacketHandler().sendToPlayer(
                        new TerminalStorageIngredientUpdateActiveStorageIngredientPacket(this.getName().toString(),
                                this.ingredientComponent, channel, remainingInstance), player);
                break;
            case PLAYER_PLACE_STORAGE:
                viewHandler.extractActiveStackFromPlayerInventory(storage, player.inventory, moveQuantityPlayerSlot);
                updateActivePlayerStack = true;
                break;
            case PLAYER_QUICK_MOVE:
                viewHandler.extractMaxFromContainerSlot(storage, container, hoveredContainerSlot);
                break;
        }

        // Notify the client that the currently hovering player stack has changed.
        if (updateActivePlayerStack) {
            player.connection.sendPacket(new SPacketSetSlot(-1, 0, player.inventory.getItemStack()));
        }
    }
}
