package org.cyclops.integratedterminals.network.packet;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageState;

/**
 * Packet for updating the gui state server-side.
 * @author rubensworks
 *
 */
public class TerminalStorageChangeGuiState extends PacketCodec {

    @CodecField
    private CompoundTag state;

    public TerminalStorageChangeGuiState() {

    }

    public TerminalStorageChangeGuiState(TerminalStorageState state) {
        this.state = state.getTag();
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
            container.getGuiState().setTag(this.state);
        }
    }

}