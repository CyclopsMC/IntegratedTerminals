package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;

/**
 * Packet for sending a storage slot click event from client to server.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientCalculateCraftingJobPacket<T, M> extends TerminalStorageIngredientCraftingOptionDataPacketAbstract<T, M> {

    public TerminalStorageIngredientCalculateCraftingJobPacket() {

    }

    public TerminalStorageIngredientCalculateCraftingJobPacket(CraftingOptionGuiData<T, M> craftingOptionData) {
        super(craftingOptionData);
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        if(player.openContainer instanceof ContainerTerminalStorage) {
            ContainerTerminalStorage container = ((ContainerTerminalStorage) player.openContainer);
            TerminalStorageTabIngredientComponentServer<T, M> tab = (TerminalStorageTabIngredientComponentServer<T, M>)
                    container.getTabServer(getTabName());
            HandlerWrappedTerminalCraftingOption<T> craftingOption = getCraftingOption(tab.getIngredientNetwork().getComponent());

            // TODO: calculate crafing job, and send back to client
            System.out.println("Request crafting of " + craftingOption.getCraftingOption().getOutputs().next() + " for " + getAmount()); // TODO
        }
    }

}