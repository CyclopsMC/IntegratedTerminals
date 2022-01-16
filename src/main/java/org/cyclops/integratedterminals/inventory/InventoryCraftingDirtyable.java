package org.cyclops.integratedterminals.inventory;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;

/**
 * An extended {@link CraftingContainer} that has a {@link IDirtyMarkListener}.
 * @author rubensworks
 */
public class InventoryCraftingDirtyable extends CraftingContainer {

    private final IDirtyMarkListener dirtyMarkListener;

    public InventoryCraftingDirtyable(AbstractContainerMenu eventHandlerIn, int width, int height, IDirtyMarkListener dirtyMarkListener) {
        super(eventHandlerIn, width, height);
        this.dirtyMarkListener = dirtyMarkListener;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack decreased = super.removeItem(index, count);
        if (!decreased.isEmpty()) {
            this.setChanged();
        }
        return decreased;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        super.setItem(index, stack);
        this.setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.dirtyMarkListener.onDirty();
    }
}
