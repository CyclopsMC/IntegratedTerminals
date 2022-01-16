package org.cyclops.integratedterminals.network.packet;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.core.terminalstorage.button.TerminalButtonItemStackCraftingGridAutoRefill;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

/**
 * Packet for telling the server the new autoRefill value.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientItemStackCraftingGridSetAutoRefill extends PacketCodec {

    @CodecField
    private String tabId;
    @CodecField
    private int autoRefillType;

    public TerminalStorageIngredientItemStackCraftingGridSetAutoRefill() {

    }

    public TerminalStorageIngredientItemStackCraftingGridSetAutoRefill(String tabId,
                                                                       TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType autoRefill) {
        this.tabId = tabId;
        this.autoRefillType = autoRefill.ordinal();
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
            ITerminalStorageTabCommon tabCommon = container.getTabCommon(tabId);
            if (tabCommon instanceof TerminalStorageTabIngredientComponentItemStackCraftingCommon) {
                ((TerminalStorageTabIngredientComponentItemStackCraftingCommon) tabCommon)
                        .setAutoRefill(TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType.values()[this.autoRefillType]);
            }
        }
    }

}
