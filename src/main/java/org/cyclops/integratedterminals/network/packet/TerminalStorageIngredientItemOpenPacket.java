package org.cyclops.integratedterminals.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.inventory.ItemLocation;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageItem;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageState;
import org.cyclops.integratedterminals.item.ItemTerminalStoragePortable;

import java.util.Optional;

/**
 * Packet for telling the server that the storage terminal gui should be opened on a specific tab.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientItemOpenPacket extends PacketCodec<TerminalStorageIngredientItemOpenPacket> {

    public static final Type<TerminalStorageIngredientItemOpenPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "terminal_storage_ingredient_item_open"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalStorageIngredientItemOpenPacket> CODEC = getCodec(TerminalStorageIngredientItemOpenPacket::new);

    @CodecField
    private ItemLocation itemLocation;
    @CodecField
    private String tabName;
    @CodecField
    private int channel;

    public TerminalStorageIngredientItemOpenPacket() {
        super(ID);
    }

    public TerminalStorageIngredientItemOpenPacket(ItemLocation itemLocation, String tabName, int channel) {
        super(ID);
        this.itemLocation = itemLocation;
        this.tabName = tabName;
        this.channel = channel;
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
        openServer(world, itemLocation, player, tabName, channel);
    }

    public static void openServer(Level world, ItemLocation itemLocation, ServerPlayer player, String tabName, int channel) {
        // Create common data
        ContainerTerminalStorageBase.InitTabData initData = new ContainerTerminalStorageBase.InitTabData(tabName, channel);
        TerminalStorageState terminalStorageState = ItemTerminalStoragePortable.getTerminalStorageState(itemLocation.getItemStack(player), player, itemLocation);

        // Create temporary container provider
        MenuProvider containerProvider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                return new ContainerTerminalStorageItem(id, playerInventory,
                        itemLocation, Optional.of(initData), terminalStorageState);
            }
        };

        // Trigger gui opening
        player.openMenu(    containerProvider, packetBuffer -> {
            ItemLocation.writeToPacketBuffer(packetBuffer, itemLocation);

            packetBuffer.writeBoolean(true);
            initData.writeToPacketBuffer(packetBuffer);

            terminalStorageState.writeToPacketBuffer(packetBuffer);
        });
    }

    public static void send(ItemLocation itemLocation, String tabName, int channel) {
        IntegratedTerminals._instance.getPacketHandler().sendToServer(
                new TerminalStorageIngredientItemOpenPacket(itemLocation, tabName, channel));
    }

}
