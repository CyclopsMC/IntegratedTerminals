package org.cyclops.integratedterminals.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientCollection;
import org.cyclops.cyclopscore.ingredient.collection.IngredientArrayList;
import org.cyclops.cyclopscore.ingredient.collection.IngredientCollections;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentStorageObservable;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCrafting;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

import java.util.function.Function;

/**
 * Packet for sending a storage change event from server to client.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientChangeEventPacket extends PacketCodec<TerminalStorageIngredientChangeEventPacket> {

    public static final Type<TerminalStorageIngredientChangeEventPacket> ID = new Type<>(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "terminal_storage_ingredient_change_event"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalStorageIngredientChangeEventPacket> CODEC = getCodec(TerminalStorageIngredientChangeEventPacket::new);

    @CodecField
    private String tabId;
    @CodecField
    private CompoundTag changeData;
    @CodecField
    private int channel;
    @CodecField
    private boolean enabled;

    public TerminalStorageIngredientChangeEventPacket() {
        super(ID);
    }

    public TerminalStorageIngredientChangeEventPacket(HolderLookup.Provider lookupProvider, String tabId,
                                                      IIngredientComponentStorageObservable.StorageChangeEvent<?, ?> event,
                                                      boolean enabled) {
        super(ID);
        this.tabId = tabId;
        IIngredientComponentStorageObservable.Change changeType = event.getChangeType();
        IIngredientCollection<?, ?> instances = event.getInstances();
        CompoundTag serialized = IModHelpers.get().getMinecraftHelpers().valueOutputToNbt(o ->IngredientCollections.serialize(o, instances), lookupProvider);
        serialized.putInt("changeType", changeType.ordinal());
        this.changeData = serialized;
        this.channel = event.getChannel();
        this.enabled = enabled;
    }

    @Override
    public boolean isAsync() {
        return GeneralConfig.packetDeserializationEnableMultithreading;
    }

    @Override
    public void actionClient(Level world, Player player) {
        IIngredientComponentStorageObservable.Change changeType = IIngredientComponentStorageObservable.Change.values()[changeData.getInt("changeType").orElseThrow()];
        IngredientArrayList ingredients = IModHelpers.get().getMinecraftHelpers().valueInputFromNbt(changeData, world.registryAccess(), (Function<ValueInput, IngredientArrayList>) IngredientCollections::deserialize);

        // Run the following code in the render thread, since this packet runs in a different thread. (isAsync is true)
        Minecraft.getInstance().execute(() -> {
            if(player.containerMenu instanceof ContainerTerminalStorageBase container) {
                TerminalStorageTabIngredientComponentClient<?, ?> tab = (TerminalStorageTabIngredientComponentClient<?, ?>) container.getTabClient(tabId);
                tab.onChange(channel, changeType, ingredients, enabled);

                // Hard-coded crafting tab
                // TODO: abstract this as "auxiliary" tabs
                if (tabId.equals(IngredientComponents.ITEMSTACK.getName().toString())) {
                    TerminalStorageTabIngredientComponentClient<?, ?> tabCrafting = (TerminalStorageTabIngredientComponentClient<?, ?>) container
                            .getTabClient(TerminalStorageTabIngredientComponentItemStackCrafting.NAME.toString());
                    tabCrafting.onChange(channel, changeType, ingredients, enabled);
                }

                container.refreshChannelStrings();
            }
        });
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {

    }

}
