package org.cyclops.integratedterminals.inventory;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;

/**
 * An extended {@link CraftingInventory} that has a {@link IDirtyMarkListener}.
 * @author rubensworks
 */
public class InventoryCraftingDirtyable extends CraftingInventory {

    private final IDirtyMarkListener dirtyMarkListener;

    public InventoryCraftingDirtyable(Container eventHandlerIn, int width, int height, IDirtyMarkListener dirtyMarkListener) {
        super(eventHandlerIn, width, height);
        this.dirtyMarkListener = dirtyMarkListener;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack decreased = super.decrStackSize(index, count);
        if (!decreased.isEmpty()) {
            this.markDirty();
        }
        return decreased;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        super.setInventorySlotContents(index, stack);
        this.markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.dirtyMarkListener.onDirty();
    }
}
