package org.cyclops.integratedterminals.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStoragePart;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageState;
import org.cyclops.integratedterminals.part.PartTypeTerminalStorage;
import org.cyclops.integratedterminals.part.PartTypes;

import java.util.Optional;

/**
 * Packet for telling the server that the storage terminal gui should be opened on a specific tab.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientPartOpenPacket extends PacketCodec {

    @CodecField
    private BlockPos pos;
    @CodecField
    private Direction side;
    @CodecField
    private String tabName;
    @CodecField
    private int channel;

    public TerminalStorageIngredientPartOpenPacket() {

    }

    public TerminalStorageIngredientPartOpenPacket(BlockPos pos, Direction side, String tabName, int channel) {
        this.pos = pos;
        this.side = side;
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
        openServer(world, pos, side, player, tabName, channel);
    }

    public static void openServer(Level world, BlockPos pos, Direction side, ServerPlayer player, String tabName, int channel) {
        // Create common data
        ContainerTerminalStorageBase.InitTabData initData = new ContainerTerminalStorageBase.InitTabData(tabName, channel);
        PartPos partPos = PartPos.of(world, pos, side);
        Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(partPos);
        PartTypeTerminalStorage.State state = (PartTypeTerminalStorage.State) data.getLeft()
                .getPartState(data.getRight().getCenter().getSide());
        TerminalStorageState terminalStorageState = state.getPlayerStorageState(player);

        // Create temporary container provider
        MenuProvider containerProvider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                return new ContainerTerminalStoragePart(id, playerInventory,
                        data.getRight(), (PartTypeTerminalStorage) data.getMiddle(),
                        Optional.of(initData), terminalStorageState);
            }
        };

        // Trigger gui opening
        NetworkHooks.openScreen(player, containerProvider, packetBuffer -> {
            PacketCodec.write(packetBuffer, partPos);
            packetBuffer.writeUtf(PartTypes.TERMINAL_STORAGE.getUniqueName().toString());

            packetBuffer.writeBoolean(true);
            initData.writeToPacketBuffer(packetBuffer);

            terminalStorageState.writeToPacketBuffer(packetBuffer);
        });
    }

    public static void send(BlockPos pos, Direction side, String tabName, int channel) {
        IntegratedTerminals._instance.getPacketHandler().sendToServer(
                new TerminalStorageIngredientPartOpenPacket(pos, side, tabName, channel));
    }

}
