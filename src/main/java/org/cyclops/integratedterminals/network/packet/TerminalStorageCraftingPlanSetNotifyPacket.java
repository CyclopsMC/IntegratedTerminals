package org.cyclops.integratedterminals.network.packet;

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
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingPlanBase;

/**
 * Packet for telling the server if the player wants to be notified when the planned crafting job is completed.
 * @author rubensworks
 */
public class TerminalStorageCraftingPlanSetNotifyPacket extends PacketCodec<TerminalStorageCraftingPlanSetNotifyPacket> {

    public static final Type<TerminalStorageCraftingPlanSetNotifyPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "terminal_storage_crafting_plan_set_notify"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalStorageCraftingPlanSetNotifyPacket> CODEC = getCodec(TerminalStorageCraftingPlanSetNotifyPacket::new);

    @CodecField
    private boolean notify;

    public TerminalStorageCraftingPlanSetNotifyPacket() {
        super(ID);
    }

    public TerminalStorageCraftingPlanSetNotifyPacket(boolean notify) {
        super(ID);
        this.notify = notify;
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
        if (player.containerMenu instanceof ContainerTerminalStorageCraftingPlanBase<?> container) {
            container.setNotifyOnCompletion(this.notify);
        }
    }

}
