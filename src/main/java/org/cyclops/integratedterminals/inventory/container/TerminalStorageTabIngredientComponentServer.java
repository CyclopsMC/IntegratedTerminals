package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.entity.player.EntityPlayerMP;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientCollection;
import org.cyclops.cyclopscore.ingredient.collection.IngredientArrayList;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentStorageObservable;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabServer;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientChangeEventPacket;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientMaxQuantityPacket;

/**
 * A server-side storage terminal ingredient tab.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientComponentServer<T, M> implements ITerminalStorageTabServer, IIngredientComponentStorageObservable.IIndexChangeObserver<T, M> {

    private final IngredientComponent<T, M> ingredientComponent;
    private final IPositionedAddonsNetworkIngredients<T, M> ingredientNetwork;
    private final PartPos pos;
    private final EntityPlayerMP player;

    public TerminalStorageTabIngredientComponentServer(IngredientComponent<T, M> ingredientComponent,
                                                       IPositionedAddonsNetworkIngredients<T, M> ingredientNetwork,
                                                       PartPos pos,
                                                       EntityPlayerMP player) {
        this.ingredientComponent = ingredientComponent;
        this.ingredientNetwork = ingredientNetwork;
        this.pos = pos;
        this.player = player;
    }

    @Override
    public String getId() {
        return ingredientComponent.getName().toString();
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
                    new TerminalStorageIngredientChangeEventPacket(event), player);
            IntegratedTerminals._instance.getPacketHandler().sendToPlayer(
                    new TerminalStorageIngredientMaxQuantityPacket(event.getInstances().getComponent(), maxQuantity, event.getChannel()), player);
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
}
