package org.cyclops.integratedterminals.part;

import com.google.common.collect.Maps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartStateEmpty;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.core.part.PartTypeTerminal;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCrafting;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStoragePart;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A part that exposes a gui using which players can access storage indexes in the network.
 * @author rubensworks
 */
public class PartTypeTerminalStorage extends PartTypeTerminal<PartTypeTerminalStorage, PartTypeTerminalStorage.State> {

    public PartTypeTerminalStorage(String name) {
        super(name);
    }

    @Override
    public int getConsumptionRate(State state) {
        return GeneralConfig.terminalStorageBaseConsumption;
    }

    @Override
    protected PartTypeTerminalStorage.State constructDefaultState() {
        return new PartTypeTerminalStorage.State();
    }

    @Override
    public Optional<MenuProvider> getContainerProvider(PartPos pos) {
        return Optional.of(new MenuProvider() {

            @Override
            public Component getDisplayName() {
                return Component.translatable(getTranslationKey());
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(pos);
                PartTypeTerminalStorage.State state = (PartTypeTerminalStorage.State) data.getLeft()
                        .getPartState(data.getRight().getCenter().getSide());
                TerminalStorageState terminalStorageState = state.getPlayerStorageState(playerEntity);
                return new ContainerTerminalStoragePart(id, playerInventory,
                        data.getRight(), (PartTypeTerminalStorage) data.getMiddle(),
                        Optional.empty(), terminalStorageState);
            }
        });
    }

    @Override
    public void writeExtraGuiData(FriendlyByteBuf packetBuffer, PartPos pos, ServerPlayer player) {
        PacketCodec.write(packetBuffer, pos);

        super.writeExtraGuiData(packetBuffer, pos, player);

        // A false to indicate that there will follow no init data object
        packetBuffer.writeBoolean(false);

        PartTypeTerminalStorage.State state = (PartTypeTerminalStorage.State) PartHelpers
                .getPartContainerChecked(pos)
                .getPartState(pos.getSide());
        TerminalStorageState terminalStorageState = state.getPlayerStorageState(player);
        terminalStorageState.writeToPacketBuffer(packetBuffer);
    }

    @Override
    public void addDrops(PartTarget target, State state, List<ItemStack> itemStacks, boolean dropMainElement, boolean saveState) {
        for (Map.Entry<String, NonNullList<ItemStack>> entry : state.getNamedInventories().entrySet()) {
            // TODO: for now hardcoded on crafting tab
            if (entry.getKey().equals(TerminalStorageTabIngredientComponentItemStackCrafting.NAME.toString())) {
                entry.getValue().set(0, ItemStack.EMPTY);
            }
            for (ItemStack itemStack : entry.getValue()) {
                if (!itemStack.isEmpty()) {
                    itemStacks.add(itemStack);
                }
            }
        }
        state.clearNamedInventories();

        super.addDrops(target, state, itemStacks, dropMainElement, saveState);
    }

    public static class State extends PartStateEmpty<PartTypeTerminalStorage>
            implements ITerminalStorageTabCommon.IVariableInventory {

        private final Map<String, NonNullList<ItemStack>> namedInventories;
        private final Map<String, TerminalStorageState> playerStorageStates;

        public State() {
            this.namedInventories = Maps.newHashMap();
            this.playerStorageStates = Maps.newHashMap();
        }

        @Override
        public int getUpdateInterval() {
            return 1; // For enabling energy consumption
        }

        public void clearNamedInventories() {
            this.namedInventories.clear();
        }

        @Override
        public void setNamedInventory(String name, NonNullList<ItemStack> inventory) {
            this.namedInventories.put(name, inventory);
            this.onDirty();
        }

        public Map<String, NonNullList<ItemStack>> getNamedInventories() {
            return namedInventories;
        }

        @Override
        @Nullable
        public NonNullList<ItemStack> getNamedInventory(String name) {
            return this.namedInventories.get(name);
        }

        public TerminalStorageState getPlayerStorageState(Player player) {
            TerminalStorageState state = playerStorageStates.get(player.getUUID().toString());
            if (state == null) {
                state = TerminalStorageState.getPlayerDefault(player, this);
                playerStorageStates.put(player.getUUID().toString(), state);
                this.onDirty();
            }
            return state;
        }

        @Override
        public void writeToNBT(CompoundTag tag) {
            super.writeToNBT(tag);

            // Write namedInventories
            ListTag namedInventoriesList = new ListTag();
            for (Map.Entry<String, NonNullList<ItemStack>> entry : this.namedInventories.entrySet()) {
                CompoundTag listEntry = new CompoundTag();
                listEntry.putString("tabName", entry.getKey());
                listEntry.putInt("itemCount", entry.getValue().size());
                ContainerHelper.saveAllItems(listEntry, entry.getValue());
                namedInventoriesList.add(listEntry);
            }
            tag.put("namedInventories", namedInventoriesList);

            // Write playerStorageStates
            ListTag playerStorageStatesList = new ListTag();
            for (Map.Entry<String, TerminalStorageState> entry : this.playerStorageStates.entrySet()) {
                CompoundTag stateEntry = new CompoundTag();
                stateEntry.putString("player", entry.getKey());
                stateEntry.put("value", entry.getValue().getTag());
                playerStorageStatesList.add(stateEntry);
            }
            tag.put("playerStorageStates", playerStorageStatesList);
        }

        @Override
        public void readFromNBT(CompoundTag tag) {
            super.readFromNBT(tag);

            // Read namedInventories
            for (Tag listEntry : tag.getList("namedInventories", Tag.TAG_COMPOUND)) {
                NonNullList<ItemStack> list = NonNullList.withSize(((CompoundTag) listEntry).getInt("itemCount"), ItemStack.EMPTY);
                String tabName = ((CompoundTag) listEntry).getString("tabName");
                ContainerHelper.loadAllItems((CompoundTag) listEntry, list);
                this.namedInventories.put(tabName, list);
            }

            // Read playerStorageStates
            for (Tag listEntry : tag.getList("playerStorageStates", Tag.TAG_COMPOUND)) {
                String playerName = ((CompoundTag) listEntry).getString("player");
                TerminalStorageState state = new TerminalStorageState(((CompoundTag) listEntry).getCompound("value"), this);
                this.playerStorageStates.put(playerName, state);
            }
        }
    }

}
