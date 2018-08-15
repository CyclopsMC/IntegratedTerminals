package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;
import org.cyclops.integratedterminals.part.PartTypeTerminalStorage;

/**
 * Packet for sending a storage slot click event from client to server.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientItemStackCraftingGridShiftClickOutput extends PacketCodec {

    @CodecField
    private String tabId;
    @CodecField
    private int channel;

    public TerminalStorageIngredientItemStackCraftingGridShiftClickOutput() {

    }

    public TerminalStorageIngredientItemStackCraftingGridShiftClickOutput(String tabId, int channel) {
        this.tabId = tabId;
        this.channel = channel;
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
            if (tabCommon instanceof TerminalStorageTabIngredientComponentItemStackCraftingCommon) {
                TerminalStorageTabIngredientComponentItemStackCraftingCommon tabCommonCrafting =
                        (TerminalStorageTabIngredientComponentItemStackCraftingCommon) tabCommon;
                PartTypeTerminalStorage.State partState = container.getPartState();

                // Loop until the result slot is empty
                SlotCrafting slotCrafting = tabCommonCrafting.getSlotCrafting();
                ItemStack currentCraftingItem = slotCrafting.getStack().copy();
                ItemStack resultStack;
                int craftedAmount = 0;
                do {
                    // Break the loop once we can not add the result into the player inventory anymore
                    if (!ItemHandlerHelper.insertItem(new PlayerMainInvWrapper(player.inventory),
                            slotCrafting.getStack(), true).isEmpty()) {
                        break;
                    }

                    // Break the loop if we are crafting something else
                    if (!ItemHandlerHelper.canItemStacksStackRelaxed(currentCraftingItem, slotCrafting.getStack())) {
                        break;
                    }

                    // Remove the current result stack and properly call all events
                    resultStack = slotCrafting.onTake(player, slotCrafting.decrStackSize(64));
                    craftedAmount += resultStack.getCount();

                    if (!resultStack.isEmpty()) {
                        // Move result into player inventory
                        player.inventory.placeItemBackInInventory(world, resultStack.copy());

                        // Re-calculate recipe
                        tabCommonCrafting.updateCraftingResult(player, player.openContainer, partState);
                    }
                } while(!resultStack.isEmpty() && craftedAmount < resultStack.getMaxStackSize());
            }
        }
    }

}