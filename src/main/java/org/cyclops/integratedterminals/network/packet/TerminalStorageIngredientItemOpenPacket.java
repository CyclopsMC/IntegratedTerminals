package org.cyclops.integratedterminals.network.packet;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.IntegratedTerminals;
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
public class TerminalStorageIngredientItemOpenPacket extends PacketCodec {

    @CodecField
    private int slot;
    @CodecField
    private String handName;
    @CodecField
    private String tabName;
    @CodecField
    private int channel;

    public TerminalStorageIngredientItemOpenPacket() {

    }

    public TerminalStorageIngredientItemOpenPacket(Pair<InteractionHand, Integer> location, String tabName, int channel) {
        this.slot = location.getRight();
        this.handName = location.getLeft().name();
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
        openServer(world, Pair.of(InteractionHand.valueOf(handName), slot), player, tabName, channel);
    }

    public static void openServer(Level world, Pair<InteractionHand, Integer> location, ServerPlayer player, String tabName, int channel) {
        // Create common data
        ContainerTerminalStorageBase.InitTabData initData = new ContainerTerminalStorageBase.InitTabData(tabName, channel);
        TerminalStorageState terminalStorageState = ItemTerminalStoragePortable.getTerminalStorageState(InventoryHelpers
                .getItemFromIndex(player, location.getRight(), location.getLeft()), player, location.getRight(), location.getLeft());

        // Create temporary container provider
        MenuProvider containerProvider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return new TextComponent("");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                return new ContainerTerminalStorageItem(id, playerInventory,
                        location.getRight(), location.getLeft(),
                        Optional.of(initData), terminalStorageState);
            }
        };

        // Trigger gui opening
        NetworkHooks.openGui(player, containerProvider, packetBuffer -> {
            packetBuffer.writeInt(location.getRight());
            packetBuffer.writeBoolean(location.getLeft() == InteractionHand.MAIN_HAND);

            packetBuffer.writeBoolean(true);
            initData.writeToPacketBuffer(packetBuffer);

            terminalStorageState.writeToPacketBuffer(packetBuffer);
        });
    }

    public static void send(Pair<InteractionHand, Integer> location, String tabName, int channel) {
        IntegratedTerminals._instance.getPacketHandler().sendToServer(
                new TerminalStorageIngredientItemOpenPacket(location, tabName, channel));
    }

}
