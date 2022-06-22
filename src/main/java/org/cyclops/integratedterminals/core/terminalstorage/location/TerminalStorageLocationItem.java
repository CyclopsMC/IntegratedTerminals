package org.cyclops.integratedterminals.core.terminalstorage.location;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.cyclops.cyclopscore.inventory.ItemLocation;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.location.ITerminalStorageLocation;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingOptionAmountItem;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingPlanItem;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientItemOpenPacket;

/**
 * @author rubensworks
 */
public class TerminalStorageLocationItem implements ITerminalStorageLocation<ItemLocation> {

    @Override
    public ResourceLocation getName() {
        return new ResourceLocation(Reference.MOD_ID, "item");
    }

    @Override
    public <T, M> void openContainerFromClient(CraftingOptionGuiData<T, M, ItemLocation> craftingOptionGuiData) {
        ItemLocation slot = craftingOptionGuiData.getLocationInstance();
        TerminalStorageIngredientItemOpenPacket.send(slot,
                craftingOptionGuiData.getTabName(), craftingOptionGuiData.getChannel());
    }

    @Override
    public <T, M> void openContainerFromServer(CraftingOptionGuiData<T, M, ItemLocation> craftingOptionGuiData, Level world, ServerPlayer player) {
        ItemLocation slot = craftingOptionGuiData.getLocationInstance();
        TerminalStorageIngredientItemOpenPacket.openServer(
                world,
                slot,
                player,
                craftingOptionGuiData.getTabName(),
                craftingOptionGuiData.getChannel()
        );
    }

    @Override
    public <T, M> void openContainerCraftingPlan(CraftingOptionGuiData<T, M, ItemLocation> craftingOptionGuiData, Level world, ServerPlayer player) {
        ItemLocation location = craftingOptionGuiData.getLocationInstance();

        // Create temporary container provider
        MenuProvider containerProvider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                return new ContainerTerminalStorageCraftingPlanItem(id, playerInventory,
                        location, craftingOptionGuiData);
            }
        };

        // Trigger gui opening
        NetworkHooks.openGui(player, containerProvider, packetBuffer -> {
            ItemLocation.writeToPacketBuffer(packetBuffer, location);

            craftingOptionGuiData.writeToPacketBuffer(packetBuffer);
        });
    }

    @Override
    public <T, M> void openContainerCraftingOptionAmount(CraftingOptionGuiData<T, M, ItemLocation> craftingOptionGuiData, Level world, ServerPlayer player) {
        ItemLocation location = craftingOptionGuiData.getLocationInstance();

        // Create temporary container provider
        MenuProvider containerProvider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                return new ContainerTerminalStorageCraftingOptionAmountItem(id, playerInventory,
                        location, craftingOptionGuiData);
            }
        };

        // Trigger gui opening
        NetworkHooks.openGui(player, containerProvider, packetBuffer -> {
            ItemLocation.writeToPacketBuffer(packetBuffer, location);

            craftingOptionGuiData.writeToPacketBuffer(packetBuffer);
        });
    }

    @Override
    public void writeToPacketBuffer(FriendlyByteBuf packetBuffer, ItemLocation location) {
        ItemLocation.writeToPacketBuffer(packetBuffer, location);
    }

    @Override
    public ItemLocation readFromPacketBuffer(FriendlyByteBuf packetBuffer) {
        return ItemLocation.readFromPacketBuffer(packetBuffer);
    }
}
