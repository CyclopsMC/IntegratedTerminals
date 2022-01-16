package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;

import javax.annotation.Nullable;

/**
 * A container for setting the amount for a given crafting option.
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingOptionAmountBase<L> extends InventoryContainer {

    private final CraftingOptionGuiData<?, ?, L> craftingOptionGuiData;

    public ContainerTerminalStorageCraftingOptionAmountBase(@Nullable MenuType<?> type, int id, Inventory playerInventory,
                                                            CraftingOptionGuiData craftingOptionGuiData) {
        super(type, id, playerInventory, new SimpleContainer());

        addPlayerInventory(player.getInventory(), 9, 80);

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
    public boolean stillValid(Player playerIn) {
        return true;
    }

}
