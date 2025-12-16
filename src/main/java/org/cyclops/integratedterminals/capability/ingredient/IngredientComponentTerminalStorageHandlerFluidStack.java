package org.cyclops.integratedterminals.capability.ingredient;

import com.google.common.collect.Lists;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.capability.item.ItemStackResourceHandlerContainerSlot;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.helper.IModHelpersNeoForge;
import org.cyclops.cyclopscore.ingredient.storage.InconsistentIngredientInsertionException;
import org.cyclops.cyclopscore.ingredient.storage.IngredientStorageHelpers;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandlerClient;
import org.cyclops.integratedterminals.api.ingredient.IIngredientInstanceSorter;
import org.cyclops.integratedterminals.capability.ingredient.sorter.FluidStackIdSorter;
import org.cyclops.integratedterminals.capability.ingredient.sorter.FluidStackNameSorter;
import org.cyclops.integratedterminals.capability.ingredient.sorter.FluidStackQuantitySorter;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

/**
 * Terminal storage handler for fluids.
 * @author rubensworks
 */
public class IngredientComponentTerminalStorageHandlerFluidStack implements IIngredientComponentTerminalStorageHandler<FluidStack, Integer> {

    private final IngredientComponent<FluidStack, Integer> ingredientComponent;

    public IngredientComponentTerminalStorageHandlerFluidStack(IngredientComponent<FluidStack, Integer> ingredientComponent) {
        this.ingredientComponent = ingredientComponent;
    }

    @Override
    public IIngredientComponentTerminalStorageHandlerClient<FluidStack, Integer> getClient() {
        return new IngredientComponentTerminalStorageHandlerFluidStackClient(this);
    }

    @Override
    public IngredientComponent<FluidStack, Integer> getComponent() {
        return ingredientComponent;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.WATER_BUCKET);
    }

    @Override
    public String formatQuantity(FluidStack instance) {
        return IModHelpers.get().getL10NHelpers().localize("gui.integratedterminals.terminal_storage.tooltip.fluid.amount",
                String.format(Locale.ROOT, "%,d", IModHelpersNeoForge.get().getFluidHelpers().getAmount(instance)));
    }

    @Override
    public boolean isInstance(ItemStack itemStack) {
        return itemStack.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forStack(itemStack)) != null;
    }

    @Override
    public FluidStack getInstance(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forStack(itemStack)))
                .map(fluidHandler -> fluidHandler.size() > 0 ? fluidHandler.getResource(0).toStack(fluidHandler.getAmountAsInt(0)) : FluidStack.EMPTY)
                .orElse(FluidStack.EMPTY);
    }

    @Override
    public long getMaxQuantity(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forStack(itemStack)))
                .map(fluidHandler -> fluidHandler.size() > 0 ? fluidHandler.getAmountAsLong(0) : 0)
                .orElse(0L);
    }

    @Override
    public int getInitialInstanceMovementQuantity() {
        return GeneralConfig.guiStorageFluidInitialQuantity;
    }

    @Override
    public int getIncrementalInstanceMovementQuantity() {
        return GeneralConfig.guiStorageFluidIncrementalQuantity;
    }

    @Override
    public int throwIntoWorld(IIngredientComponentStorage<FluidStack, Integer> storage, FluidStack maxInstance,
                              Player player) {
        return 0; // Dropping fluids in the world is not supported
    }

    @Override
    public FluidStack insertIntoContainer(IIngredientComponentStorage<FluidStack, Integer> storage,
                                          AbstractContainerMenu container, int containerSlot, FluidStack maxInstance,
                                          @Nullable Player player, boolean transferFullSelection) {
        return Optional.ofNullable(ItemStackResourceHandlerContainerSlot.asItemAccess(container, containerSlot).getCapability(Capabilities.Fluid.ITEM))
                .map(fluidHandler -> {
                    IIngredientComponentStorage<FluidStack, Integer> itemStorage = getFluidStorage(storage.getComponent(), fluidHandler);
                    FluidStack moved = FluidStack.EMPTY;
                    try {
                        moved = IngredientStorageHelpers.moveIngredientsIterative(storage, itemStorage, maxInstance,
                                ingredientComponent.getMatcher().getExactMatchNoQuantityCondition(), false);
                    } catch (InconsistentIngredientInsertionException e) {
                        // Ignore
                    }

                    container.broadcastChanges(); // TODO: can this be removed due to ItemAccess usage?
                    return moved;
                })
                .orElse(FluidStack.EMPTY);
    }

    protected IIngredientComponentStorage<FluidStack, Integer> getFluidStorage(IngredientComponent<FluidStack, Integer> component,
                                                                               ResourceHandler<FluidResource> fluidHandler) {
        return component
                .getStorageWrapperHandler(Capabilities.Fluid.ITEM)
                .wrapComponentStorage(fluidHandler);
    }

    @Override
    public void extractActiveStackFromPlayerInventory(IIngredientComponentStorage<FluidStack, Integer> storage,
                                                      AbstractContainerMenu container, Inventory playerInventory, long moveQuantityPlayerSlot) {
        Optional.ofNullable(ItemAccess.forPlayerCursor(playerInventory.player, container).getCapability(Capabilities.Fluid.ITEM))
                .ifPresent(fluidHandler -> {
                    IIngredientComponentStorage<FluidStack, Integer> itemStorage = getFluidStorage(storage.getComponent(), fluidHandler);
                    try {
                        IngredientStorageHelpers.moveIngredientsIterative(itemStorage, storage, moveQuantityPlayerSlot, false);
                    } catch (InconsistentIngredientInsertionException e) {
                        // Ignore
                    }
                });
    }

    @Override
    public void extractMaxFromContainerSlot(IIngredientComponentStorage<FluidStack, Integer> storage, AbstractContainerMenu container, int containerSlot, Inventory playerInventory, int limit) {
        Slot slot = container.getSlot(containerSlot);
        if (slot.mayPickup(playerInventory.player)) {
            Optional.ofNullable(ItemStackResourceHandlerContainerSlot.asItemAccess(container, containerSlot).getCapability(Capabilities.Fluid.ITEM))
                    .ifPresent(fluidHandler -> {
                        IIngredientComponentStorage<FluidStack, Integer> itemStorage = getFluidStorage(storage.getComponent(), fluidHandler);
                        try {
                            IngredientStorageHelpers.moveIngredientsIterative(itemStorage, storage, limit == -1 ? Long.MAX_VALUE : limit, false);
                        } catch (InconsistentIngredientInsertionException e) {
                            // Ignore
                        }

                        container.broadcastChanges(); // TODO: can this be removed due to ItemAccess usage?
                    });
        }
    }

    @Override
    public long getActivePlayerStackQuantity(Inventory playerInventory, AbstractContainerMenu container) {
        return Optional.ofNullable(ItemAccess.forPlayerCursor(playerInventory.player, container).getCapability(Capabilities.Fluid.ITEM))
                .map(fluidHandler -> fluidHandler.size() > 0 ? fluidHandler.getAmountAsLong(0) : 0)
                .orElse(0L);
    }

    @Override
    public void drainActivePlayerStackQuantity(Inventory playerInventory, AbstractContainerMenu container, long quantityIn) {
        Optional.ofNullable(ItemAccess.forPlayerCursor(playerInventory.player, container).getCapability(Capabilities.Fluid.ITEM))
                .ifPresent(fluidHandler -> {
                    long quantity = quantityIn;
                    while (quantity > 0) {
                        int drained;
                        try (var tx = Transaction.openRoot()) {
                            drained = fluidHandler.extract(fluidHandler.getResource(0), (int) quantity, tx);
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
    public Collection<IIngredientInstanceSorter<FluidStack>> getInstanceSorters() {
        return Lists.newArrayList(
                new FluidStackNameSorter(),
                new FluidStackIdSorter(),
                new FluidStackQuantitySorter()
        );
    }
}
