package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.inventory.container.ItemInventoryContainer;
import org.cyclops.integratedterminals.RegistryEntries;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;

import javax.annotation.Nullable;

/**
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingOptionAmountItem extends ContainerTerminalStorageCraftingOptionAmountBase<Pair<InteractionHand, Integer>> {

    // Based on ItemInventoryContainer

    private final int itemIndex;
    private final InteractionHand hand;

    public ContainerTerminalStorageCraftingOptionAmountItem(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id, playerInventory, ItemInventoryContainer.readItemIndex(packetBuffer),
                ItemInventoryContainer.readHand(packetBuffer),
                CraftingOptionGuiData.readFromPacketBuffer(packetBuffer));
    }

    public ContainerTerminalStorageCraftingOptionAmountItem(int id, Inventory playerInventory,
                                                            int itemIndex, InteractionHand hand, CraftingOptionGuiData craftingOptionGuiData) {
        this(RegistryEntries.CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_OPTION_AMOUNT_ITEM, id, playerInventory,
                itemIndex, hand, craftingOptionGuiData);
    }

    public ContainerTerminalStorageCraftingOptionAmountItem(@Nullable MenuType<?> type, int id, Inventory playerInventory,
                                                            int itemIndex, InteractionHand hand,
                                                            CraftingOptionGuiData craftingOptionGuiData) {
        super(type, id, playerInventory, craftingOptionGuiData);
        this.itemIndex = itemIndex;
        this.hand = hand;
    }

}
