package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.inventory.container.ItemInventoryContainer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.part.PartTypeConnectorOmniDirectional;
import org.cyclops.integratedterminals.RegistryEntries;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.api.terminalstorage.location.ITerminalStorageLocation;
import org.cyclops.integratedterminals.core.terminalstorage.location.TerminalStorageLocations;
import org.cyclops.integratedterminals.item.ItemTerminalStoragePortable;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @author rubensworks
 */
public class ContainerTerminalStorageItem extends ContainerTerminalStorageBase<Pair<InteractionHand, Integer>> {

    // Based on ItemInventoryContainer

    private final int itemIndex;
    private final InteractionHand hand;

    public ContainerTerminalStorageItem(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id, playerInventory, ItemInventoryContainer.readItemIndex(packetBuffer),
                ItemInventoryContainer.readHand(packetBuffer),
                packetBuffer.readBoolean() ? Optional.of(InitTabData.readFromPacketBuffer(packetBuffer)) : Optional.empty(),
                TerminalStorageState.readFromPacketBuffer(packetBuffer));
        getGuiState().setDirtyMarkListener(this::sendGuiStateToServer);
    }

    public ContainerTerminalStorageItem(int id, Inventory playerInventory, int itemIndex, InteractionHand hand,
                                        Optional<InitTabData> initTabData, TerminalStorageState terminalStorageState) {
        this(RegistryEntries.CONTAINER_PART_TERMINAL_STORAGE_ITEM, id, playerInventory,
                itemIndex, hand, initTabData, terminalStorageState);
    }

    public ContainerTerminalStorageItem(@Nullable MenuType<?> type, int id, Inventory playerInventory,
                                        int itemIndex, InteractionHand hand,
                                        Optional<InitTabData> initTabData,
                                        TerminalStorageState terminalStorageState) {
        super(type, id, playerInventory, initTabData, terminalStorageState,
                getNetworkFromItem(InventoryHelpers.getItemFromIndex(playerInventory.player, itemIndex, hand)),
                getVariableInventoryFromItem(InventoryHelpers.getItemFromIndex(playerInventory.player, itemIndex, hand)));
        this.itemIndex = itemIndex;
        this.hand = hand;
    }

    public static Optional<INetwork> getNetworkFromItem(ItemStack itemStack) {
        if (MinecraftHelpers.isClientSideThread()) {
            return Optional.empty();
        }
        int groupId = ItemTerminalStoragePortable.getGroupId(itemStack);
        if (groupId < 0) {
            return Optional.empty();
        }
        for (PartPos pos : PartTypeConnectorOmniDirectional.LOADED_GROUPS.getPositions(groupId)) {
            LazyOptional<INetwork> network = NetworkHelpers.getNetwork(pos);
            if (network.isPresent()) {
                return network.map(a -> a);
            }
        }
        return Optional.empty();
    }

    public static Optional<ITerminalStorageTabCommon.IVariableInventory> getVariableInventoryFromItem(ItemStack itemStack) {
        return Optional.of(ItemTerminalStoragePortable.getVariableInventory(itemStack));
    }

    public ItemStack getItemStack(Player player) {
        return InventoryHelpers.getItemFromIndex(player, itemIndex, hand);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        ItemStack item = getItemStack(player);
        return item != null && item.getItem() == RegistryEntries.ITEM_TERMINAL_STORAGE_PORTABLE;
    }

    @Override
    public ITerminalStorageLocation<Pair<InteractionHand, Integer>> getLocation() {
        return TerminalStorageLocations.ITEM;
    }

    @Override
    public Pair<InteractionHand, Integer> getLocationInstance() {
        return Pair.of(hand, itemIndex);
    }

    @Override
    public void onVariableContentsUpdated(INetwork network, IVariable<?> variable) {
        // We don't have a real part, so don't emit anything here
    }

    @Override
    protected Slot createNewSlot(Container inventory, int index, int x, int y) {
        return new Slot(inventory, index, x, y) {
            @Override
            public boolean mayPickup(Player playerIn) {
                return super.mayPickup(playerIn) && itemIndex != index;
            }
        };
    }
}
