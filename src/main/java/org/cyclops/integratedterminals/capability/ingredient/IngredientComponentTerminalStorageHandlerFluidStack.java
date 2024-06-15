package org.cyclops.integratedterminals.capability.ingredient;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.client.gui.GuiGraphicsExtended;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.ingredient.storage.InconsistentIngredientInsertionException;
import org.cyclops.cyclopscore.ingredient.storage.IngredientStorageHelpers;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.api.ingredient.IIngredientInstanceSorter;
import org.cyclops.integratedterminals.capability.ingredient.sorter.FluidStackIdSorter;
import org.cyclops.integratedterminals.capability.ingredient.sorter.FluidStackNameSorter;
import org.cyclops.integratedterminals.capability.ingredient.sorter.FluidStackQuantitySorter;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.core.terminalstorage.query.SearchMode;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

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
    public IngredientComponent<FluidStack, Integer> getComponent() {
        return ingredientComponent;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.WATER_BUCKET);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInstance(GuiGraphics guiGraphics, FluidStack instance, long maxQuantity, @Nullable String label, AbstractContainerScreen gui,
                             ContainerScreenTerminalStorage.DrawLayer layer, float partialTick,
                             int x, int y, int mouseX, int mouseY,
                             @Nullable List<Component> additionalTooltipLines) {
        if (instance != null) {
            if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND) {
                // Draw fluid
                GuiHelpers.renderFluidSlot(guiGraphics, instance, x, y);

                // Draw amount
                GuiGraphicsExtended renderItem = new GuiGraphicsExtended(guiGraphics);
                renderItem.drawSlotText(Minecraft.getInstance().font, label != null ? label : GuiHelpers.quantityToScaledString(instance.getAmount()), x, y);
            } else {
                GuiHelpers.renderTooltip(gui, guiGraphics.pose(), x, y, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER, mouseX, mouseY, () -> {
                    List<Component> lines = Lists.newArrayList();
                    lines.add(((MutableComponent) instance.getDisplayName())
                            .withStyle(instance.getFluid().getFluidType().getRarity().color));
                    addQuantityTooltip(lines, instance);
                    if (additionalTooltipLines != null) {
                        lines.addAll(additionalTooltipLines);
                    }
                    return lines;
                });
            }
        }
    }

    @Override
    public String formatQuantity(FluidStack instance) {
        return L10NHelpers.localize("gui.integratedterminals.terminal_storage.tooltip.fluid.amount",
                String.format(Locale.ROOT, "%,d", FluidHelpers.getAmount(instance)));
    }

    @Override
    public boolean isInstance(ItemStack itemStack) {
        return itemStack.getCapability(Capabilities.FluidHandler.ITEM) != null;
    }

    @Override
    public FluidStack getInstance(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getCapability(Capabilities.FluidHandler.ITEM))
                .map(fluidHandler -> fluidHandler.getTanks() > 0 ? fluidHandler.getFluidInTank(0) : FluidStack.EMPTY)
                .orElse(FluidStack.EMPTY);
    }

    @Override
    public long getMaxQuantity(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getCapability(Capabilities.FluidHandler.ITEM))
                .map(fluidHandler -> fluidHandler.getTanks() > 0 ? fluidHandler.getTankCapacity(0) : 0)
                .orElse(0);
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
        ItemStack stack = container.getSlot(containerSlot).getItem();
        return Optional.ofNullable(stack.getCapability(Capabilities.FluidHandler.ITEM))
                .map(fluidHandler -> {
                    IIngredientComponentStorage<FluidStack, Integer> itemStorage = getFluidStorage(storage.getComponent(), fluidHandler);
                    FluidStack moved = FluidStack.EMPTY;
                    try {
                        moved = IngredientStorageHelpers.moveIngredientsIterative(storage, itemStorage, maxInstance,
                                ingredientComponent.getMatcher().getExactMatchNoQuantityCondition(), false);
                    } catch (InconsistentIngredientInsertionException e) {
                        // Ignore
                    }

                    container.getSlot(containerSlot).set(fluidHandler.getContainer());
                    container.broadcastChanges();
                    return moved;
                })
                .orElse(FluidStack.EMPTY);
    }

    protected IIngredientComponentStorage<FluidStack, Integer> getFluidStorage(IngredientComponent<FluidStack, Integer> component,
                                                                               IFluidHandlerItem fluidHandler) {
        return component
                .getStorageWrapperHandler(Capabilities.FluidHandler.ITEM)
                .wrapComponentStorage(fluidHandler);
    }

    @Override
    public void extractActiveStackFromPlayerInventory(IIngredientComponentStorage<FluidStack, Integer> storage,
                                                      AbstractContainerMenu container, Inventory playerInventory, long moveQuantityPlayerSlot) {
        ItemStack playerStack = container.getCarried();
        Optional.ofNullable(playerStack.getCapability(Capabilities.FluidHandler.ITEM))
                .ifPresent(fluidHandler -> {
                    IIngredientComponentStorage<FluidStack, Integer> itemStorage = getFluidStorage(storage.getComponent(), fluidHandler);
                    try {
                        IngredientStorageHelpers.moveIngredientsIterative(itemStorage, storage, moveQuantityPlayerSlot, false);
                    } catch (InconsistentIngredientInsertionException e) {
                        // Ignore
                    }

                    container.setCarried(fluidHandler.getContainer());
                });
    }

    @Override
    public void extractMaxFromContainerSlot(IIngredientComponentStorage<FluidStack, Integer> storage, AbstractContainerMenu container, int containerSlot, Inventory playerInventory, int limit) {
        Slot slot = container.getSlot(containerSlot);
        if (slot.mayPickup(playerInventory.player)) {
            ItemStack toMoveStack = slot.getItem();
            Optional.ofNullable(toMoveStack.getCapability(Capabilities.FluidHandler.ITEM))
                    .ifPresent(fluidHandler -> {
                        IIngredientComponentStorage<FluidStack, Integer> itemStorage = getFluidStorage(storage.getComponent(), fluidHandler);
                        try {
                            IngredientStorageHelpers.moveIngredientsIterative(itemStorage, storage, limit == -1 ? Long.MAX_VALUE : limit, false);
                        } catch (InconsistentIngredientInsertionException e) {
                            // Ignore
                        }

                        container.getSlot(containerSlot).set(fluidHandler.getContainer());
                        container.broadcastChanges();
                    });
        }
    }

    @Override
    public long getActivePlayerStackQuantity(Inventory playerInventory, AbstractContainerMenu container) {
        ItemStack toMoveStack = container.getCarried();
        return Optional.ofNullable(toMoveStack.getCapability(Capabilities.FluidHandler.ITEM))
                .map(fluidHandler -> fluidHandler.getTanks() > 0 ? fluidHandler.getFluidInTank(0).getAmount() : 0)
                .orElse(0);
    }

    @Override
    public void drainActivePlayerStackQuantity(Inventory playerInventory, AbstractContainerMenu container, long quantityIn) {
        ItemStack toMoveStack = container.getCarried();
        Optional.ofNullable(toMoveStack.getCapability(Capabilities.FluidHandler.ITEM))
                .ifPresent(fluidHandler -> {
                    long quantity = quantityIn;
                    while (quantity > 0) {
                        int drained = fluidHandler.drain((int) quantity, IFluidHandler.FluidAction.EXECUTE).getAmount();
                        if (drained <= 0) {
                            break;
                        }
                        quantity -= drained;
                    }
                    container.setCarried(fluidHandler.getContainer());
                });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Predicate<FluidStack> getInstanceFilterPredicate(SearchMode searchMode, String query) {
        return switch (searchMode) {
            case MOD -> i -> BuiltInRegistries.FLUID.getKey(i.getFluid()).getNamespace()
                    .toLowerCase(Locale.ENGLISH).matches(".*" + query + ".*");
            case TOOLTIP -> i -> false; // Fluids have no tooltip
            case TAG -> i -> i.getFluid().builtInRegistryHolder().tags()
                    .filter(tag -> tag.location().toString().toLowerCase(Locale.ENGLISH).matches(".*" + query + ".*"))
                    .anyMatch(tag -> !BuiltInRegistries.FLUID.getTag(tag).isEmpty());
            case DEFAULT -> i -> i != null && i.getDisplayName().getString().toLowerCase(Locale.ENGLISH).matches(".*" + query + ".*");
        };
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
