package org.cyclops.integratedterminals.capability.ingredient;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.ingredient.storage.IngredientStorageHelpers;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.api.ingredient.IIngredientInstanceSorter;
import org.cyclops.integratedterminals.capability.ingredient.sorter.FluidStackIdSorter;
import org.cyclops.integratedterminals.capability.ingredient.sorter.FluidStackNameSorter;
import org.cyclops.integratedterminals.capability.ingredient.sorter.FluidStackQuantitySorter;
import org.cyclops.integratedterminals.client.gui.container.GuiTerminalStorage;
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
    @SideOnly(Side.CLIENT)
    public void drawInstance(FluidStack instance, long maxQuantity, @Nullable String label, GuiContainer gui,
                             GuiTerminalStorage.DrawLayer layer, float partialTick,
                             int x, int y, int mouseX, int mouseY,
                             @Nullable List<String> additionalTooltipLines) {
        if (instance != null) {
            if (layer == GuiTerminalStorage.DrawLayer.BACKGROUND) {
                // Draw fluid
                GuiHelpers.renderFluidSlot(gui, instance, x, y);

                // Draw amount
                RenderItemExtendedSlotCount.drawSlotText(Minecraft.getMinecraft().fontRenderer, label != null ? label : GuiHelpers.quantityToScaledString(instance.amount), x, y);
                GlStateManager.disableLighting();
            } else {
                GuiHelpers.renderTooltip(gui, x, y, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER, mouseX, mouseY, () -> {
                    List<String> lines = Lists.newArrayList();
                    lines.add(instance.getFluid().getRarity().rarityColor + instance.getLocalizedName());
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
        return itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
    }

    @Override
    public FluidStack getInstance(ItemStack itemStack) {
        IFluidHandlerItem fluidHandler = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandler != null) {
            IFluidTankProperties[] props = fluidHandler.getTankProperties();
            if (props.length > 0) {
                return props[0].getContents();
            }
        }
        return null;
    }

    @Override
    public long getMaxQuantity(ItemStack itemStack) {
        IFluidHandlerItem fluidHandler = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandler != null) {
            IFluidTankProperties[] props = fluidHandler.getTankProperties();
            if (props.length > 0) {
                return props[0].getCapacity();
            }
        }
        return 0;
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
                              EntityPlayer player) {
        return 0; // Dropping fluids in the world is not supported
    }

    @Override
    public FluidStack insertIntoContainer(IIngredientComponentStorage<FluidStack, Integer> storage,
                                          Container container, int containerSlot, FluidStack maxInstance) {
        ItemStack stack = container.getSlot(containerSlot).getStack();
        IFluidHandlerItem fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandler != null) {
            IIngredientComponentStorage<FluidStack, Integer> itemStorage = getFluidStorage(storage.getComponent(), fluidHandler);
            FluidStack moved = IngredientStorageHelpers.moveIngredientsIterative(storage, itemStorage, maxInstance,
                    ingredientComponent.getMatcher().getExactMatchNoQuantityCondition(), false);

            container.getSlot(containerSlot).putStack(fluidHandler.getContainer());
            container.detectAndSendChanges();
            return moved;
        }
        return null;
    }

    protected IIngredientComponentStorage<FluidStack, Integer> getFluidStorage(IngredientComponent<FluidStack, Integer> component,
                                                                               IFluidHandlerItem fluidHandler) {
        return component
                .getStorageWrapperHandler(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                .wrapComponentStorage(fluidHandler);
    }

    @Override
    public void extractActiveStackFromPlayerInventory(IIngredientComponentStorage<FluidStack, Integer> storage,
                                                      InventoryPlayer playerInventory, long moveQuantityPlayerSlot) {
        ItemStack playerStack = playerInventory.getItemStack();
        IFluidHandlerItem fluidHandler = playerStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandler != null) {
            IIngredientComponentStorage<FluidStack, Integer> itemStorage = getFluidStorage(storage.getComponent(), fluidHandler);
            IngredientStorageHelpers.moveIngredientsIterative(itemStorage, storage, moveQuantityPlayerSlot, false);

            playerInventory.setItemStack(fluidHandler.getContainer());
        }
    }

    @Override
    public void extractMaxFromContainerSlot(IIngredientComponentStorage<FluidStack, Integer> storage, Container container, int containerSlot) {
        ItemStack toMoveStack = container.getSlot(containerSlot).getStack();
        IFluidHandlerItem fluidHandler = toMoveStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandler != null) {
            IIngredientComponentStorage<FluidStack, Integer> itemStorage = getFluidStorage(storage.getComponent(), fluidHandler);
            IngredientStorageHelpers.moveIngredientsIterative(itemStorage, storage, Long.MAX_VALUE, false);

            container.getSlot(containerSlot).putStack(fluidHandler.getContainer());
            container.detectAndSendChanges();
        }
    }

    @Override
    public long getActivePlayerStackQuantity(InventoryPlayer playerInventory) {
        ItemStack toMoveStack = playerInventory.getItemStack();
        IFluidHandlerItem fluidHandler = toMoveStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandler != null) {
            IFluidTankProperties[] props = fluidHandler.getTankProperties();
            if (props.length > 0) {
                return FluidHelpers.getAmount(props[0].getContents());
            }
        }
        return 0;
    }

    @Override
    public void drainActivePlayerStackQuantity(InventoryPlayer playerInventory, long quantity) {
        ItemStack toMoveStack = playerInventory.getItemStack();
        IFluidHandlerItem fluidHandler = toMoveStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandler != null) {
            while (quantity > 0) {
                int drained = FluidHelpers.getAmount(fluidHandler.drain((int) quantity, true));
                if (drained <= 0) {
                    break;
                }
                quantity -= drained;
            }
            playerInventory.setItemStack(fluidHandler.getContainer());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Predicate<FluidStack> getInstanceFilterPredicate(SearchMode searchMode, String query) {
        switch (searchMode) {
            case MOD:
                return i -> Optional.ofNullable(FluidRegistry.getModId(i)).orElse("minecraft")
                        .toLowerCase(Locale.ENGLISH).matches(".*" + query + ".*");
            case TOOLTIP:
                return i -> false; // Fluids have no tooltip
            case DICT:
                return i -> false; // There is no fluid dictionary
            case DEFAULT:
                return i -> i.getLocalizedName().toLowerCase(Locale.ENGLISH).matches(".*" + query + ".*");
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
