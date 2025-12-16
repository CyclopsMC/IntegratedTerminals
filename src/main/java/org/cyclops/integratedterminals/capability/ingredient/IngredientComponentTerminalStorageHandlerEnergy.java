package org.cyclops.integratedterminals.capability.ingredient;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.capability.item.ItemStackResourceHandlerContainerSlot;
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
        return itemStack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(itemStack)) != null;
    }

    @Override
    public Long getInstance(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(itemStack)))
                .map(EnergyHandler::getAmountAsLong)
                .orElse(0L)
                .longValue();
    }

    @Override
    public long getMaxQuantity(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(itemStack)))
                .map(EnergyHandler::getCapacityAsLong)
                .orElse(0L);
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
                                                                             EnergyHandler energyStorage) {
        return component
                .getStorageWrapperHandler(Capabilities.Energy.ITEM)
                .wrapComponentStorage(energyStorage);
    }

    @Override
    public Long insertIntoContainer(IIngredientComponentStorage<Long, Boolean> storage,
                                       AbstractContainerMenu container, int containerSlot, Long maxInstance,
                                       @Nullable Player player, boolean transferFullSelection) {
        return Optional.ofNullable(ItemStackResourceHandlerContainerSlot.asItemAccess(container, containerSlot).getCapability(Capabilities.Energy.ITEM))
                .map(energyStorage -> {
                    IIngredientComponentStorage<Long, Boolean> itemStorage = getEnergyStorage(storage.getComponent(), energyStorage);
                    Long ret = 0L;
                    try {
                        ret = IngredientStorageHelpers.moveIngredientsIterative(storage, itemStorage, maxInstance, false);
                    } catch (InconsistentIngredientInsertionException e) {
                        // Ignore
                    }
                    container.broadcastChanges(); // TODO: can this be removed due to ItemAccess usage?
                    return ret;
                })
                .orElse(0L);
    }

    @Override
    public void extractActiveStackFromPlayerInventory(IIngredientComponentStorage<Long, Boolean> storage,
                                                      AbstractContainerMenu container, Inventory playerInventory, long moveQuantityPlayerSlot) {
        Optional.ofNullable(ItemAccess.forPlayerCursor(playerInventory.player, container).getCapability(Capabilities.Energy.ITEM))
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
            Optional.ofNullable(ItemStackResourceHandlerContainerSlot.asItemAccess(container, containerSlot).getCapability(Capabilities.Energy.ITEM))
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
        return Optional.ofNullable(ItemAccess.forPlayerCursor(playerInventory.player, container).getCapability(Capabilities.Energy.ITEM))
                .map(EnergyHandler::getAmountAsLong)
                .orElse(0L);
    }

    @Override
    public void drainActivePlayerStackQuantity(Inventory playerInventory, AbstractContainerMenu container, long quantityIn) {
        Optional.ofNullable(ItemAccess.forPlayerCursor(playerInventory.player, container).getCapability(Capabilities.Energy.ITEM))
                .ifPresent(energyStorage -> {
                    // Drain
                    long quantity = quantityIn;
                    while (quantity > 0) {
                        int drained;
                        try (var tx = Transaction.openRoot()) {
                            drained = energyStorage.extract((int) quantity, tx);
                            tx.commit();
                        }
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
