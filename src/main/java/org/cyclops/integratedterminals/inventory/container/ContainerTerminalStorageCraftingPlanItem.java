package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
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
public class ContainerTerminalStorageCraftingPlanItem extends ContainerTerminalStorageCraftingPlanBase<Pair<InteractionHand, Integer>> {

    // Based on ItemInventoryContainer

    private final int itemIndex;
    private final InteractionHand hand;

    public ContainerTerminalStorageCraftingPlanItem(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id, playerInventory, ItemInventoryContainer.readItemIndex(packetBuffer),
                ItemInventoryContainer.readHand(packetBuffer),
                CraftingOptionGuiData.readFromPacketBuffer(packetBuffer));
    }

    public ContainerTerminalStorageCraftingPlanItem(int id, Inventory playerInventory,
                                                    int itemIndex, InteractionHand hand, CraftingOptionGuiData craftingOptionGuiData) {
        this(RegistryEntries.CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_PLAN_ITEM, id, playerInventory,
                itemIndex, hand, craftingOptionGuiData);
    }

    public ContainerTerminalStorageCraftingPlanItem(@Nullable MenuType<?> type, int id, Inventory playerInventory,
                                                    int itemIndex, InteractionHand hand,
                                                    CraftingOptionGuiData craftingOptionGuiData) {
        super(type, id, playerInventory, craftingOptionGuiData);
        this.itemIndex = itemIndex;
        this.hand = hand;
    }

    public ItemStack getItemStack(Player player) {
        return InventoryHelpers.getItemFromIndex(player, itemIndex, hand);
    }

    @Override
    public Optional<INetwork> getNetwork() {
        return ContainerTerminalStorageItem.getNetworkFromItem(getItemStack(player));
    }
}
