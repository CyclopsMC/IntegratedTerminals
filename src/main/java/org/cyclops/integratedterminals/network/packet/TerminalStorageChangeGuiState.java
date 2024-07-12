package org.cyclops.integratedterminals.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageState;

/**
 * Packet for updating the gui state server-side.
 * @author rubensworks
 *
 */
public class TerminalStorageChangeGuiState extends PacketCodec<TerminalStorageChangeGuiState> {

    public static final Type<TerminalStorageChangeGuiState> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "terminal_storage_change_gui_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalStorageChangeGuiState> CODEC = getCodec(TerminalStorageChangeGuiState::new);

    @CodecField
    private CompoundTag state;

    public TerminalStorageChangeGuiState() {
        super(ID);
    }

    public TerminalStorageChangeGuiState(TerminalStorageState state) {
        super(ID);
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
