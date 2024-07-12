package org.cyclops.integratedterminals.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.inventory.ItemLocation;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.RegistryEntries;
import org.cyclops.integratedterminals.item.ItemTerminalStoragePortable;

/**
 * Packet for telling the server that the storage terminal gui should be opened without a specific tab.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientItemOpenGenericPacket extends PacketCodec<TerminalStorageIngredientItemOpenGenericPacket> {

    public static final Type<TerminalStorageIngredientItemOpenGenericPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "terminal_storage_ingredient_item_open_generic"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalStorageIngredientItemOpenGenericPacket> CODEC = getCodec(TerminalStorageIngredientItemOpenGenericPacket::new);

    @CodecField
    private ItemLocation itemLocation;

    public TerminalStorageIngredientItemOpenGenericPacket() {
        super(ID);
    }

    public TerminalStorageIngredientItemOpenGenericPacket(ItemLocation itemLocation) {
        super(ID);
        this.itemLocation = itemLocation;
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
        openServer(world, itemLocation, player);
    }

    public static void openServer(Level world, ItemLocation itemLocation, ServerPlayer player) {
        ((ItemTerminalStoragePortable) RegistryEntries.ITEM_TERMINAL_STORAGE_PORTABLE.get())
                .openGuiForItemIndex(world, player, itemLocation);
    }

    public static void send(ItemLocation itemLocation) {
        IntegratedTerminals._instance.getPacketHandler().sendToServer(
                new TerminalStorageIngredientItemOpenGenericPacket(itemLocation));
    }

}
