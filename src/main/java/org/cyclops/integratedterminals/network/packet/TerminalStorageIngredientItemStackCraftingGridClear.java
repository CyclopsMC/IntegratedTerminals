package org.cyclops.integratedterminals.network.packet;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentServer;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

/**
 * Packet for telling the server that the crafting grid must be cleared.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientItemStackCraftingGridClear extends PacketCodec {

    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "terminal_storage_ingredient_itemstack_crafting_grid_clear");

    @CodecField
    private String tabId;
    @CodecField
    private int channel;
    @CodecField
    private boolean toStorage;

    public TerminalStorageIngredientItemStackCraftingGridClear() {
        super(ID);
    }

    public TerminalStorageIngredientItemStackCraftingGridClear(String tabId, int channel, boolean toStorage) {
        super(ID);
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
    public void actionClient(Level world, Player player) {

    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
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
                                 int channel, boolean toStorage, Player player) {
        tabCommon.getInventoryCraftResult().setItem(0, ItemStack.EMPTY);
        CraftingContainer inventoryCrafting = tabCommon.getInventoryCrafting();
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
                    player.getInventory().placeItemBackInInventory(itemStack, true);
                    inventoryCrafting.setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

}
