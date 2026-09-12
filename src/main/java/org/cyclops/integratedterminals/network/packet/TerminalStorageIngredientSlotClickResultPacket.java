package org.cyclops.integratedterminals.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

/**
 * Packet telling the client how much a storage slot click actually moved.
 *
 * The client predicts how much it expects to move, which can be too much when the storage
 * holds less than the client last heard of, or when it refuses what is offered to it.
 * This corrects such a prediction within one round trip, instead of leaving it to expire.
 *
 * @author rubensworks
 */
public class TerminalStorageIngredientSlotClickResultPacket extends PacketCodec<TerminalStorageIngredientSlotClickResultPacket> {

    public static final Type<TerminalStorageIngredientSlotClickResultPacket> ID = new Type<>(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "terminal_storage_ingredient_slot_click_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalStorageIngredientSlotClickResultPacket> CODEC = getCodec(TerminalStorageIngredientSlotClickResultPacket::new);

    @CodecField
    private String tabId;
    @CodecField
    private int clickId;
    @CodecField
    private long movedQuantity;

    public TerminalStorageIngredientSlotClickResultPacket() {
        super(ID);
    }

    public TerminalStorageIngredientSlotClickResultPacket(String tabId, int clickId, long movedQuantity) {
        super(ID);
        this.tabId = tabId;
        this.clickId = clickId;
        this.movedQuantity = movedQuantity;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(Level world, Player player) {
        if (player.containerMenu instanceof ContainerTerminalStorageBase container) {
            TerminalStorageTabIngredientComponentClient<?, ?> tab = (TerminalStorageTabIngredientComponentClient<?, ?>)
                    container.getTabClient(tabId);
            if (tab != null) {
                tab.handleClickResult(clickId, movedQuantity);
            }
        }
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {

    }

}
