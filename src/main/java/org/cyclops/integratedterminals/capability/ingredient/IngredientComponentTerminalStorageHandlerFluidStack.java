package org.cyclops.integratedterminals.capability.ingredient;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
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
    public void drawInstance(FluidStack instance, long maxQuantity, @Nullable String label, ContainerScreen gui,
                             ContainerScreenTerminalStorage.DrawLayer layer, float partialTick,
                             int x, int y, int mouseX, int mouseY,
                             @Nullable List<ITextComponent> additionalTooltipLines) {
        if (instance != null) {
            if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND) {
                // Draw fluid
                GuiHelpers.renderFluidSlot(gui, instance, x, y);

                // Draw amount
                RenderItemExtendedSlotCount.getInstance().drawSlotText(Minecraft.getInstance().fontRenderer, new MatrixStack(), label != null ? label : GuiHelpers.quantityToScaledString(instance.getAmount()), x, y);
                GlStateManager.disableLighting();
            } else {
                GuiHelpers.renderTooltip(gui, x, y, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER, mouseX, mouseY, () -> {
                    List<ITextComponent> lines = Lists.newArrayList();
                    lines.add(instance.getDisplayName()
                            .applyTextStyle(instance.getFluid().getAttributes().getRarity().color));
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
                String.format("%,d", FluidHelpers.getAmount(instance)));
    }

    @Override
    public boolean isInstance(ItemStack itemStack) {
        return itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent();
    }

    @Override
    public FluidStack getInstance(ItemStack itemStack) {
        return itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                .map(fluidHandler -> fluidHandler.getTanks() > 0 ? fluidHandler.getFluidInTank(0) : FluidStack.EMPTY)
                .orElse(FluidStack.EMPTY);
    }

    @Override
    public long getMaxQuantity(ItemStack itemStack) {
        return itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
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
                              PlayerEntity player) {
        return 0; // Dropping fluids in the world is not supported
    }

    @Override
    public FluidStack insertIntoContainer(IIngredientComponentStorage<FluidStack, Integer> storage,
                                          Container container, int containerSlot, FluidStack maxInstance,
                                          @Nullable PlayerEntity player, boolean transferFullSelection) {
        ItemStack stack = container.getSlot(containerSlot).getStack();
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                .map(fluidHandler -> {
                    IIngredientComponentStorage<FluidStack, Integer> itemStorage = getFluidStorage(storage.getComponent(), fluidHandler);
                    FluidStack moved = FluidStack.EMPTY;
                    try {
                        moved = IngredientStorageHelpers.moveIngredientsIterative(storage, itemStorage, maxInstance,
                                ingredientComponent.getMatcher().getExactMatchNoQuantityCondition(), false);
                    } catch (InconsistentIngredientInsertionException e) {
                        // Ignore
                    }

                    container.getSlot(containerSlot).putStack(fluidHandler.getContainer());
                    container.detectAndSendChanges();
                    return moved;
                })
                .orElse(FluidStack.EMPTY);
    }

    protected IIngredientComponentStorage<FluidStack, Integer> getFluidStorage(IngredientComponent<FluidStack, Integer> component,
                                                                               IFluidHandlerItem fluidHandler) {
        return component
                .getStorageWrapperHandler(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                .wrapComponentStorage(fluidHandler);
    }

    @Override
    public void extractActiveStackFromPlayerInventory(IIngredientComponentStorage<FluidStack, Integer> storage,
                                                      PlayerInventory playerInventory, long moveQuantityPlayerSlot) {
        ItemStack playerStack = playerInventory.getItemStack();
        playerStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                .ifPresent(fluidHandler -> {
                    IIngredientComponentStorage<FluidStack, Integer> itemStorage = getFluidStorage(storage.getComponent(), fluidHandler);
                    try {
                        IngredientStorageHelpers.moveIngredientsIterative(itemStorage, storage, moveQuantityPlayerSlot, false);
                    } catch (InconsistentIngredientInsertionException e) {
                        // Ignore
                    }

                    playerInventory.setItemStack(fluidHandler.getContainer());
                });
    }

    @Override
    public void extractMaxFromContainerSlot(IIngredientComponentStorage<FluidStack, Integer> storage, Container container, int containerSlot, PlayerInventory playerInventory) {
        ItemStack toMoveStack = container.getSlot(containerSlot).getStack();
        toMoveStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                .ifPresent(fluidHandler -> {
                    IIngredientComponentStorage<FluidStack, Integer> itemStorage = getFluidStorage(storage.getComponent(), fluidHandler);
                    try {
                        IngredientStorageHelpers.moveIngredientsIterative(itemStorage, storage, Long.MAX_VALUE, false);
                    } catch (InconsistentIngredientInsertionException e) {
                        // Ignore
                    }

                    container.getSlot(containerSlot).putStack(fluidHandler.getContainer());
                    container.detectAndSendChanges();
                });
    }

    @Override
    public long getActivePlayerStackQuantity(PlayerInventory playerInventory) {
        ItemStack toMoveStack = playerInventory.getItemStack();
        return toMoveStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                .map(fluidHandler -> fluidHandler.getTanks() > 0 ? fluidHandler.getFluidInTank(0).getAmount() : 0)
                .orElse(0);
    }

    @Override
    public void drainActivePlayerStackQuantity(PlayerInventory playerInventory, long quantityIn) {
        ItemStack toMoveStack = playerInventory.getItemStack();
        toMoveStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                .ifPresent(fluidHandler -> {
                    long quantity = quantityIn;
                    while (quantity > 0) {
                        int drained = fluidHandler.drain((int) quantity, IFluidHandler.FluidAction.EXECUTE).getAmount();
                        if (drained <= 0) {
                            break;
                        }
                        quantity -= drained;
                    }
                    playerInventory.setItemStack(fluidHandler.getContainer());
                });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Predicate<FluidStack> getInstanceFilterPredicate(SearchMode searchMode, String query) {
        switch (searchMode) {
            case MOD:
                return i -> i.getFluid().getRegistryName().getNamespace()
                        .toLowerCase(Locale.ENGLISH).matches(".*" + query + ".*");
            case TOOLTIP:
                return i -> false; // Fluids have no tooltip
            case TAG:
                return i -> false; // There is no fluid dictionary
            case DEFAULT:
                return i -> i != null && i.getDisplayName().getString().toLowerCase(Locale.ENGLISH).matches(".*" + query + ".*");
        }
        return null;
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
