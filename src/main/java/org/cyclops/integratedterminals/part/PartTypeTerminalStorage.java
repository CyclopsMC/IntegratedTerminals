package org.cyclops.integratedterminals.part;

import com.google.common.collect.Maps;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartStateEmpty;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.core.part.PartTypeTerminal;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCrafting;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;

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
    public Optional<INamedContainerProvider> getContainerProvider(PartPos pos) {
        return Optional.of(new INamedContainerProvider() {

            @Override
            public ITextComponent getDisplayName() {
                return new TranslationTextComponent(getTranslationKey());
            }

            @Override
            public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(pos);
                return new ContainerTerminalStorage(id, playerInventory,
                        data.getRight(), (PartTypeTerminalStorage) data.getMiddle(),
                        Optional.empty());
            }
        });
    }

    @Override
    public void writeExtraGuiData(PacketBuffer packetBuffer, PartPos pos, ServerPlayerEntity player) {
        PacketCodec.write(packetBuffer, pos);

        super.writeExtraGuiData(packetBuffer, pos, player);

        // A false to indicate that there will follow no init data object
        packetBuffer.writeBoolean(false);
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

    public static class State extends PartStateEmpty<PartTypeTerminalStorage> {

        private final Map<String, NonNullList<ItemStack>> namedInventories;

        public State() {
            this.namedInventories = Maps.newHashMap();
        }

        public void clearNamedInventories() {
            this.namedInventories.clear();
        }

        public void setNamedInventory(String name, NonNullList<ItemStack> inventory) {
            this.namedInventories.put(name, inventory);
            this.onDirty();
        }

        public Map<String, NonNullList<ItemStack>> getNamedInventories() {
            return namedInventories;
        }

        @Nullable
        public NonNullList<ItemStack> getNamedInventory(String name) {
            return this.namedInventories.get(name);
        }

        public void loadNamedInventory(String name, IInventory inventory) {
            NonNullList<ItemStack> tabItems = this.getNamedInventory(name);
            if (tabItems != null) {
                for (int i = 0; i < tabItems.size(); i++) {
                    inventory.setInventorySlotContents(i, tabItems.get(i));
                }
            }
        }

        public void saveNamedInventory(String name, IInventory inventory) {
            NonNullList<ItemStack> latestItems = NonNullList.create();
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                latestItems.add(inventory.getStackInSlot(i));
            }
            this.setNamedInventory(name, latestItems);
        }

        @Override
        public void writeToNBT(CompoundNBT tag) {
            super.writeToNBT(tag);
            ListNBT list = new ListNBT();
            for (Map.Entry<String, NonNullList<ItemStack>> entry : this.namedInventories.entrySet()) {
                CompoundNBT listEntry = new CompoundNBT();
                listEntry.putString("tabName", entry.getKey());
                listEntry.putInt("itemCount", entry.getValue().size());
                ItemStackHelper.saveAllItems(listEntry, entry.getValue());
                list.add(listEntry);
            }
            tag.put("namedInventories", list);
        }

        @Override
        public void readFromNBT(CompoundNBT tag) {
            super.readFromNBT(tag);
            for (INBT listEntry : tag.getList("namedInventories", Constants.NBT.TAG_COMPOUND)) {
                NonNullList<ItemStack> list = NonNullList.withSize(((CompoundNBT) listEntry).getInt("itemCount"), ItemStack.EMPTY);
                String tabName = ((CompoundNBT) listEntry).getString("tabName");
                ItemStackHelper.loadAllItems((CompoundNBT) listEntry, list);
                this.namedInventories.put(tabName, list);
            }
        }
    }

}
