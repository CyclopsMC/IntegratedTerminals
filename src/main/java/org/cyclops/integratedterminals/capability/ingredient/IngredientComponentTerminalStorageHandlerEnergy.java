package org.cyclops.integratedterminals.capability.ingredient;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.ingredient.storage.InconsistentIngredientInsertionException;
import org.cyclops.cyclopscore.ingredient.storage.IngredientStorageHelpers;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.RegistryEntries;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandlerClient;
import org.cyclops.integratedterminals.api.ingredient.IIngredientInstanceSorter;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

/**
 * Terminal storage handler for energy.
 * @author rubensworks
 */
public class IngredientComponentTerminalStorageHandlerEnergy implements IIngredientComponentTerminalStorageHandler<Long, Boolean> {

    private final IngredientComponent<Long, Boolean> ingredientComponent;

    public IngredientComponentTerminalStorageHandlerEnergy(IngredientComponent<Long, Boolean> ingredientComponent) {
        this.ingredientComponent = ingredientComponent;
    }

    @Override
    public IIngredientComponentTerminalStorageHandlerClient<Long, Boolean> getClient() {
        return new IngredientComponentTerminalStorageHandlerEnergyClient(this);
    }

    @Override
    public IngredientComponent<Long, Boolean> getComponent() {
        return ingredientComponent;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(RegistryEntries.ITEM_ENERGY_BATTERY);
    }

    @Override
    public String formatQuantity(Long instance) {
        return IModHelpers.get().getL10NHelpers().localize("gui.integratedterminals.terminal_storage.tooltip.energy.amount",
                String.format(Locale.ROOT, "%,d", instance));
    }

    @Override
    public boolean isInstance(ItemStack itemStack) {
        return itemStack.getCapability(Capabilities.EnergyStorage.ITEM) != null;
    }

    @Override
    public Long getInstance(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getCapability(Capabilities.EnergyStorage.ITEM))
                .map(IEnergyStorage::getEnergyStored)
                .orElse(0)
                .longValue();
    }

    @Override
    public long getMaxQuantity(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getCapability(Capabilities.EnergyStorage.ITEM))
                .map(IEnergyStorage::getMaxEnergyStored)
                .orElse(0);
    }

    @Override
    public int getInitialInstanceMovementQuantity() {
        return GeneralConfig.guiStorageEnergyInitialQuantity;
    }

    @Override
    public int getIncrementalInstanceMovementQuantity() {
        return GeneralConfig.guiStorageEnergyIncrementalQuantity;
    }

    @Override
    public int throwIntoWorld(IIngredientComponentStorage<Long, Boolean> storage, Long maxInstance,
                              Player player) {
        return 0; // Dropping energy in the world is not possible
    }

    protected IIngredientComponentStorage<Long, Boolean> getEnergyStorage(IngredientComponent<Long, Boolean> component,
                                                                             IEnergyStorage energyStorage) {
        return component
                .getStorageWrapperHandler(Capabilities.EnergyStorage.ITEM)
                .wrapComponentStorage(energyStorage);
    }

    @Override
    public Long insertIntoContainer(IIngredientComponentStorage<Long, Boolean> storage,
                                       AbstractContainerMenu container, int containerSlot, Long maxInstance,
                                       @Nullable Player player, boolean transferFullSelection) {
        ItemStack stack = container.getSlot(containerSlot).getItem();

        return Optional.ofNullable(stack.getCapability(Capabilities.EnergyStorage.ITEM))
                .map(energyStorage -> {
                    IIngredientComponentStorage<Long, Boolean> itemStorage = getEnergyStorage(storage.getComponent(), energyStorage);
                    Long ret = 0L;
                    try {
                        ret = IngredientStorageHelpers.moveIngredientsIterative(storage, itemStorage, maxInstance, false);
                    } catch (InconsistentIngredientInsertionException e) {
                        // Ignore
                    }
                    container.broadcastChanges();
                    return ret;
                })
                .orElse(0L);
    }

    @Override
    public void extractActiveStackFromPlayerInventory(IIngredientComponentStorage<Long, Boolean> storage,
                                                      AbstractContainerMenu container, Inventory playerInventory, long moveQuantityPlayerSlot) {
        ItemStack playerStack = container.getCarried();
        Optional.ofNullable(playerStack.getCapability(Capabilities.EnergyStorage.ITEM))
                .ifPresent(energyStorage -> {
                    IIngredientComponentStorage<Long, Boolean> itemStorage = getEnergyStorage(storage.getComponent(), energyStorage);
                    try {
                        IngredientStorageHelpers.moveIngredientsIterative(itemStorage, storage, moveQuantityPlayerSlot, false);
                    } catch (InconsistentIngredientInsertionException e) {
                        // Ignore
                    }
                });
    }

    @Override
    public void extractMaxFromContainerSlot(IIngredientComponentStorage<Long, Boolean> storage,
                                            AbstractContainerMenu container, int containerSlot, Inventory playerInventory, int limit) {
        Slot slot = container.getSlot(containerSlot);
        if (slot.mayPickup(playerInventory.player)) {
            ItemStack toMoveStack = slot.getItem();
            Optional.ofNullable(toMoveStack.getCapability(Capabilities.EnergyStorage.ITEM))
                    .ifPresent(energyStorage -> {
                        IIngredientComponentStorage<Long, Boolean> itemStorage = getEnergyStorage(storage.getComponent(), energyStorage);
                        try {
                            IngredientStorageHelpers.moveIngredientsIterative(itemStorage, storage, limit == -1 ? Long.MAX_VALUE : limit, false);
                        } catch (InconsistentIngredientInsertionException e) {
                            // Ignore
                        }
                    });
        }
    }

    @Override
    public long getActivePlayerStackQuantity(Inventory playerInventory, AbstractContainerMenu container) {
        ItemStack toMoveStack = container.getCarried();
        return Optional.ofNullable(toMoveStack.getCapability(Capabilities.EnergyStorage.ITEM))
                .map(IEnergyStorage::getEnergyStored)
                .orElse(0);
    }

    @Override
    public void drainActivePlayerStackQuantity(Inventory playerInventory, AbstractContainerMenu container, long quantityIn) {
        ItemStack toMoveStack = container.getCarried();
        Optional.ofNullable(toMoveStack.getCapability(Capabilities.EnergyStorage.ITEM))
                .ifPresent(energyStorage -> {
                    // Drain
                    long quantity = quantityIn;
                    while (quantity > 0) {
                        int drained = energyStorage.extractEnergy((int) quantity, false);
                        if (drained <= 0) {
                            break;
                        }
                        quantity -= drained;
                    }
                });
    }

    @Override
    public Collection<IIngredientInstanceSorter<Long>> getInstanceSorters() {
        return Collections.emptyList();
    }
}
