package org.cyclops.integratedterminals.network.packet;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageTabIngredientComponentCommontemStackCrafting;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageTabIngredientComponentServer;

import java.util.List;

/**
 * Packet for sending a storage slot click event from client to server.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientItemStackCraftingGridBalance<T> extends PacketCodec {

    @CodecField
    private String tabId;
    @CodecField
    private int channel;

    public TerminalStorageIngredientItemStackCraftingGridBalance() {

    }

    public TerminalStorageIngredientItemStackCraftingGridBalance(String tabId, int channel) {
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
            if (container.getTabServer(tabId) instanceof TerminalStorageTabIngredientComponentServer) {
                TerminalStorageTabIngredientComponentServer<ItemStack, Integer> tabServer =
                        (TerminalStorageTabIngredientComponentServer<ItemStack, Integer>) container.getTabServer(tabId);
                TerminalStorageTabIngredientComponentCommontemStackCrafting tabCommon =
                        (TerminalStorageTabIngredientComponentCommontemStackCrafting) container.getTabCommon(tabId);
                tabCommon.getInventoryCraftResult().setInventorySlotContents(0, ItemStack.EMPTY);
                balanceGrid(tabCommon.getInventoryCrafting());
            }
        }
    }

    public static void balanceGrid(InventoryCrafting craftingGrid) {
        // Init bins
        List<Pair<ItemStack, List<Pair<Integer, Integer>>>> bins = Lists.newArrayListWithExpectedSize(craftingGrid.getSizeInventory());
        for(int slot = 0; slot < craftingGrid.getSizeInventory(); slot++) {
            ItemStack itemStack = craftingGrid.getStackInSlot(slot);
            if(!itemStack.isEmpty()) {
                int amount = itemStack.getCount();
                itemStack = itemStack.copy();
                itemStack.setCount(1);
                int bin = 0;
                boolean addedToBin = false;
                while(bin < bins.size() && !addedToBin) {
                    Pair<ItemStack, List<Pair<Integer, Integer>>> pair = bins.get(bin);
                    ItemStack original = pair.getLeft().copy();
                    original.setCount(1);
                    if(ItemHandlerHelper.canItemStacksStackRelaxed(original, itemStack)) {
                        pair.getLeft().grow(amount);
                        pair.getRight().add(new MutablePair<>(slot, 0));
                        addedToBin = true;
                    }
                    bin++;
                }

                if(!addedToBin) {
                    itemStack.setCount(amount);
                    bins.add(new MutablePair<>(itemStack,
                            Lists.newArrayList((Pair<Integer, Integer>) new MutablePair<>(slot, 0))));
                }
            }
        }

        // Balance bins
        for(Pair<ItemStack, List<Pair<Integer, Integer>>> pair : bins) {
            int division = pair.getLeft().getCount() / pair.getRight().size();
            int modulus = pair.getLeft().getCount() % pair.getRight().size();
            for(Pair<Integer, Integer> slot : pair.getRight()) {
                slot.setValue(division + Math.max(0, Math.min(1, modulus--)));
            }
        }

        // Set bins to slots
        for(Pair<ItemStack, List<Pair<Integer, Integer>>> pair : bins) {
            for(Pair<Integer, Integer> slot : pair.getRight()) {
                ItemStack itemStack = pair.getKey().copy();
                itemStack.setCount(slot.getRight());
                craftingGrid.setInventorySlotContents(slot.getKey(), itemStack);
            }
        }
    }

}