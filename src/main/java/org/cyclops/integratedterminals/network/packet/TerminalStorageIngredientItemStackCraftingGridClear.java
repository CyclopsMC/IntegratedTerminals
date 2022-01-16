package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

/**
 * Packet for telling the server that the crafting grid must be cleared.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientItemStackCraftingGridClear extends PacketCodec {

    @CodecField
    private String tabId;
    @CodecField
    private int channel;
    @CodecField
    private boolean toStorage;

    public TerminalStorageIngredientItemStackCraftingGridClear() {

    }

    public TerminalStorageIngredientItemStackCraftingGridClear(String tabId, int channel, boolean toStorage) {
        this.tabId = tabId;
        this.channel = channel;
        this.toStorage = toStorage;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(World world, PlayerEntity player) {

    }

    @Override
    public void actionServer(World world, ServerPlayerEntity player) {
        if(player.containerMenu instanceof ContainerTerminalStorageBase) {
            ContainerTerminalStorageBase container = ((ContainerTerminalStorageBase) player.containerMenu);
            if (container.getTabServer(tabId) instanceof TerminalStorageTabIngredientComponentServer) {
                TerminalStorageTabIngredientComponentServer<ItemStack, Integer> tabServer =
                        (TerminalStorageTabIngredientComponentServer<ItemStack, Integer>) container.getTabServer(tabId);
                TerminalStorageTabIngredientComponentItemStackCraftingCommon tabCommon =
                        (TerminalStorageTabIngredientComponentItemStackCraftingCommon) container.getTabCommon(tabId);
                clearGrid(tabCommon, tabServer, channel, toStorage, player);
            }
        }
    }

    public static void clearGrid(TerminalStorageTabIngredientComponentItemStackCraftingCommon tabCommon,
                                 TerminalStorageTabIngredientComponentServer<ItemStack, Integer> tabServer,
                                 int channel, boolean toStorage, PlayerEntity player) {
        tabCommon.getInventoryCraftResult().setItem(0, ItemStack.EMPTY);
        CraftingInventory inventoryCrafting = tabCommon.getInventoryCrafting();
        for (int i = 0; i < inventoryCrafting.getContainerSize(); i++) {
            ItemStack itemStack = inventoryCrafting.removeItemNoUpdate(i);
            if (!itemStack.isEmpty()) {
                if (toStorage) {
                    // To storage
                    ItemStack remainder = tabServer.getIngredientNetwork().getChannel(channel).insert(itemStack, false);
                    // Place the remainder back into the grid, so we don't loose it.
                    inventoryCrafting.setItem(i, remainder);
                } else {
                    // To player inventory
                    player.inventory.placeItemBackInInventory(player.level, itemStack);
                }
            }
        }
    }

}