package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.inventory.container.ItemInventoryContainer;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integratedterminals.RegistryEntries;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingPlanItem extends ContainerTerminalStorageCraftingPlanBase<Pair<Hand, Integer>> {

    // Based on ItemInventoryContainer

    private final int itemIndex;
    private final Hand hand;

    public ContainerTerminalStorageCraftingPlanItem(int id, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
        this(id, playerInventory, ItemInventoryContainer.readItemIndex(packetBuffer),
                ItemInventoryContainer.readHand(packetBuffer),
                CraftingOptionGuiData.readFromPacketBuffer(packetBuffer));
    }

    public ContainerTerminalStorageCraftingPlanItem(int id, PlayerInventory playerInventory,
                                                    int itemIndex, Hand hand, CraftingOptionGuiData craftingOptionGuiData) {
        this(RegistryEntries.CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_PLAN_ITEM, id, playerInventory,
                itemIndex, hand, craftingOptionGuiData);
    }

    public ContainerTerminalStorageCraftingPlanItem(@Nullable ContainerType<?> type, int id, PlayerInventory playerInventory,
                                                    int itemIndex, Hand hand,
                                                    CraftingOptionGuiData craftingOptionGuiData) {
        super(type, id, playerInventory, craftingOptionGuiData);
        this.itemIndex = itemIndex;
        this.hand = hand;
    }

    public ItemStack getItemStack(PlayerEntity player) {
        return InventoryHelpers.getItemFromIndex(player, itemIndex, hand);
    }

    @Override
    public Optional<INetwork> getNetwork() {
        return ContainerTerminalStorageItem.getNetworkFromItem(getItemStack(player));
    }
}
