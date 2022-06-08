package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.inventory.ItemLocation;
import org.cyclops.integratedterminals.RegistryEntries;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;

import javax.annotation.Nullable;

/**
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingOptionAmountItem extends ContainerTerminalStorageCraftingOptionAmountBase<Pair<InteractionHand, Integer>> {

    // Based on ItemInventoryContainer

    private final ItemLocation location;

    public ContainerTerminalStorageCraftingOptionAmountItem(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id, playerInventory, ItemLocation.readFromPacketBuffer(packetBuffer),
                CraftingOptionGuiData.readFromPacketBuffer(packetBuffer));
    }

    public ContainerTerminalStorageCraftingOptionAmountItem(int id, Inventory playerInventory,
                                                            ItemLocation location, CraftingOptionGuiData craftingOptionGuiData) {
        this(RegistryEntries.CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_OPTION_AMOUNT_ITEM, id, playerInventory,
                location, craftingOptionGuiData);
    }

    public ContainerTerminalStorageCraftingOptionAmountItem(@Nullable MenuType<?> type, int id, Inventory playerInventory,
                                                            ItemLocation location,
                                                            CraftingOptionGuiData craftingOptionGuiData) {
        super(type, id, playerInventory, craftingOptionGuiData);
        this.location = location;
    }

}
