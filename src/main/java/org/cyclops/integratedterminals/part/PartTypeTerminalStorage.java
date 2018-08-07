package org.cyclops.integratedterminals.part;

import com.google.common.collect.Maps;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.PartStateEmpty;
import org.cyclops.integratedterminals.client.gui.container.GuiTerminalStorage;
import org.cyclops.integratedterminals.core.part.PartTypeTerminal;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCrafting;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * A part that exposes a gui using which players can access storage indexes in the network.
 * @author rubensworks
 */
public class PartTypeTerminalStorage extends PartTypeTerminal<PartTypeTerminalStorage, PartTypeTerminalStorage.State> {

    public PartTypeTerminalStorage(String name) {
        super(name);
    }

    @Override
    protected PartTypeTerminalStorage.State constructDefaultState() {
        return new PartTypeTerminalStorage.State();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Class<? extends GuiScreen> getGui() {
        return GuiTerminalStorage.class;
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerTerminalStorage.class;
    }

    @Override
    public void addDrops(PartTarget target, State state, List<ItemStack> itemStacks, boolean dropMainElement, boolean saveState) {
        super.addDrops(target, state, itemStacks, dropMainElement, saveState);
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
        public void writeToNBT(NBTTagCompound tag) {
            super.writeToNBT(tag);
            NBTTagList list = new NBTTagList();
            for (Map.Entry<String, NonNullList<ItemStack>> entry : this.namedInventories.entrySet()) {
                NBTTagCompound listEntry = new NBTTagCompound();
                listEntry.setString("tabName", entry.getKey());
                listEntry.setInteger("itemCount", entry.getValue().size());
                ItemStackHelper.saveAllItems(listEntry, entry.getValue());
                list.appendTag(listEntry);
            }
            tag.setTag("namedInventories", list);
        }

        @Override
        public void readFromNBT(NBTTagCompound tag) {
            super.readFromNBT(tag);
            for (NBTBase listEntry : tag.getTagList("namedInventories", Constants.NBT.TAG_COMPOUND)) {
                NonNullList<ItemStack> list = NonNullList.withSize(((NBTTagCompound) listEntry).getInteger("itemCount"), ItemStack.EMPTY);
                String tabName = ((NBTTagCompound) listEntry).getString("tabName");
                ItemStackHelper.loadAllItems((NBTTagCompound) listEntry, list);
                this.namedInventories.put(tabName, list);
            }
        }
    }

}
