package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ContainerType;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;

import javax.annotation.Nullable;

/**
 * A container for setting the amount for a given crafting option.
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingOptionAmountBase<L> extends InventoryContainer {

    private final CraftingOptionGuiData<?, ?, L> craftingOptionGuiData;

    public ContainerTerminalStorageCraftingOptionAmountBase(@Nullable ContainerType<?> type, int id, PlayerInventory playerInventory,
                                                            CraftingOptionGuiData craftingOptionGuiData) {
        super(type, id, playerInventory, new Inventory());

        addPlayerInventory(player.inventory, 9, 80);

        this.craftingOptionGuiData = craftingOptionGuiData;
    }

    public <T, M> CraftingOptionGuiData<T, M, L> getCraftingOptionGuiData() {
        return (CraftingOptionGuiData<T, M, L>) craftingOptionGuiData;
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

}
