package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageTabIngredientComponentCommontemStackCrafting;

/**
 * Packet for telling the server the new autoRefill value.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientItemStackCraftingGridSetAutoRefill extends PacketCodec {

    @CodecField
    private String tabId;
    @CodecField
    private boolean autoRefill;

    public TerminalStorageIngredientItemStackCraftingGridSetAutoRefill() {

    }

    public TerminalStorageIngredientItemStackCraftingGridSetAutoRefill(String tabId, boolean autoRefill) {
        this.tabId = tabId;
        this.autoRefill = autoRefill;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void actionClient(World world, EntityPlayer player) {

    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        if(player.openContainer instanceof ContainerTerminalStorage) {
            ContainerTerminalStorage container = ((ContainerTerminalStorage) player.openContainer);
            ITerminalStorageTabCommon tabCommon = container.getTabCommon(tabId);
            if (tabCommon instanceof TerminalStorageTabIngredientComponentCommontemStackCrafting) {
                ((TerminalStorageTabIngredientComponentCommontemStackCrafting) tabCommon).setAutoRefill(this.autoRefill);
            }
        }
    }

}