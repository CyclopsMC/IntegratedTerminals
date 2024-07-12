package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.inventory.ItemLocation;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integratedterminals.RegistryEntries;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingPlanItem extends ContainerTerminalStorageCraftingPlanBase<Pair<InteractionHand, Integer>> {

    // Based on ItemInventoryContainer

    private final ItemLocation itemLocation;

    public ContainerTerminalStorageCraftingPlanItem(int id, Inventory playerInventory, RegistryFriendlyByteBuf packetBuffer) {
        this(id, playerInventory, ItemLocation.readFromPacketBuffer(packetBuffer),
                CraftingOptionGuiData.readFromPacketBuffer(packetBuffer));
    }

    public ContainerTerminalStorageCraftingPlanItem(int id, Inventory playerInventory,
                                                    ItemLocation itemLocation, CraftingOptionGuiData craftingOptionGuiData) {
        this(RegistryEntries.CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_PLAN_ITEM.get(), id, playerInventory,
                itemLocation, craftingOptionGuiData);
    }

    public ContainerTerminalStorageCraftingPlanItem(@Nullable MenuType<?> type, int id, Inventory playerInventory,
                                                    ItemLocation itemLocation,
                                                    CraftingOptionGuiData craftingOptionGuiData) {
        super(type, id, playerInventory, craftingOptionGuiData);
        this.itemLocation = itemLocation;
    }

    public ItemStack getItemStack(Player player) {
        return this.itemLocation.getItemStack(player);
    }

    @Override
    public Optional<INetwork> getNetwork() {
        return ContainerTerminalStorageItem.getNetworkFromItem(getItemStack(player));
    }
}
