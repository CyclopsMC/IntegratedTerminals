package org.cyclops.integratedterminals.network.packet;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

import java.util.List;

/**
 * Packet for telling the server that the crafting grid must be balanced.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientItemStackCraftingGridBalance extends PacketCodec {

    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "terminal_storage_ingredient_itemstack_crafting_grid_balance");

    @CodecField
    private String tabId;

    public TerminalStorageIngredientItemStackCraftingGridBalance() {
        super(ID);
    }

    public TerminalStorageIngredientItemStackCraftingGridBalance(String tabId) {
        super(ID);
        this.tabId = tabId;
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
            if (container.getTabServer(tabId) instanceof TerminalStorageTabIngredientComponentServer) {
                TerminalStorageTabIngredientComponentItemStackCraftingCommon tabCommon =
                        (TerminalStorageTabIngredientComponentItemStackCraftingCommon) container.getTabCommon(tabId);
                tabCommon.getInventoryCraftResult().setItem(0, ItemStack.EMPTY);
                balanceGrid(tabCommon.getInventoryCrafting());
            }
        }
    }

    public static void balanceGrid(CraftingContainer craftingGrid) {
        // Init bins
        List<Pair<ItemStack, List<Pair<Integer, Integer>>>> bins = Lists.newArrayListWithExpectedSize(craftingGrid.getContainerSize());
        for(int slot = 0; slot < craftingGrid.getContainerSize(); slot++) {
            ItemStack itemStack = craftingGrid.getItem(slot);
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
                craftingGrid.setItem(slot.getKey(), itemStack);
            }
        }
    }

}
