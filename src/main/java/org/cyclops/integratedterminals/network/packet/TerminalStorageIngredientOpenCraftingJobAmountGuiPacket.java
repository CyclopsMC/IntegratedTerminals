package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.core.client.gui.ExtendedGuiHandler;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;
import org.cyclops.integratedterminals.proxy.guiprovider.GuiProviders;

/**
 * Packet for opening the crafting job amount gui.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientOpenCraftingJobAmountGuiPacket<T, M> extends TerminalStorageIngredientCraftingOptionDataPacketAbstract<T, M> {

    public TerminalStorageIngredientOpenCraftingJobAmountGuiPacket() {

    }

    public TerminalStorageIngredientOpenCraftingJobAmountGuiPacket(String tabId, CraftingOptionGuiData<T, M> craftingOptionData) {
        super(tabId, craftingOptionData);
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        if(player.openContainer instanceof ContainerTerminalStorage) {
            ContainerTerminalStorage container = ((ContainerTerminalStorage) player.openContainer);
            TerminalStorageTabIngredientComponentServer<T, M> tab = (TerminalStorageTabIngredientComponentServer<T, M>)
                    container.getTabServer(getTabId());
            CraftingOptionGuiData<T, M> craftingOptionData = getCraftingOptionData(tab.getIngredientNetwork().getComponent());

            IntegratedTerminals._instance.getGuiHandler().setTemporaryData(ExtendedGuiHandler.CRAFTING_OPTION,
                    Pair.of(container.getTarget().getCenter().getSide(), craftingOptionData)); // Pass the side as extra data to the gui
            BlockPos cPos = container.getTarget().getCenter().getPos().getBlockPos();
            player.openGui(IntegratedTerminals._instance, GuiProviders.GUI_TERMINAL_STORAGE_CRAFTNG_OPTION_AMOUNT,
                    world, cPos.getX(), cPos.getY(), cPos.getZ());
        }
    }

}