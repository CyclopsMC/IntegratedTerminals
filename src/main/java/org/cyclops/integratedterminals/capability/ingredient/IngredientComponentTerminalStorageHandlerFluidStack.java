package org.cyclops.integratedterminals.capability.ingredient;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.cyclops.commoncapabilities.api.capability.fluidhandler.FluidMatch;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.ingredient.storage.IngredientStorageHelpers;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.api.ingredient.IIngredientInstanceSorter;
import org.cyclops.integratedterminals.capability.ingredient.sorter.FluidStackIdSorter;
import org.cyclops.integratedterminals.capability.ingredient.sorter.FluidStackNameSorter;
import org.cyclops.integratedterminals.capability.ingredient.sorter.FluidStackQuantitySorter;
import org.cyclops.integratedterminals.client.gui.container.GuiTerminalStorage;
import org.cyclops.integratedterminals.inventory.container.query.SearchMode;

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
    public ItemStack getIcon() {
        return new ItemStack(Items.WATER_BUCKET);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInstance(FluidStack instance, long maxQuantity, @Nullable String label, GuiContainer gui, GuiTerminalStorage.DrawLayer layer, float partialTick,
                             int x, int y, int mouseX, int mouseY, int channel) {
        if (instance != null) {
            if (layer == GuiTerminalStorage.DrawLayer.BACKGROUND) {
                // Draw fluid
                GuiHelpers.renderFluidSlot(gui, instance, x, y);

                // Draw amount
                RenderItemExtendedSlotCount.drawSlotText(Minecraft.getMinecraft().fontRenderer, label != null ? label : GuiHelpers.quantityToScaledString(instance.amount), x, y);
            } else {
                GuiHelpers.renderTooltip(gui, x, y, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER, mouseX, mouseY, () -> {
                    List<String> lines = Lists.newArrayList();
                    lines.add(instance.getFluid().getRarity().rarityColor + instance.getLocalizedName());
                    if (label != null) {
                        lines.add(label);
                    } else {
                        addQuantityTooltip(lines, instance);
                    }
                    return lines;
                });
            }
        }
    }

    @Override
    public String formatQuantity(FluidStack instance) {
        return String.format("%,d", FluidHelpers.getAmount(instance)) + " mB";
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
    public FluidStack insertIntoPlayerInventory(IIngredientComponentStorage<FluidStack, Integer> storage,
                                         InventoryPlayer playerInventory, int playerSlot, FluidStack maxInstance) {
        PlayerMainInvWrapper inv = new PlayerMainInvWrapper(playerInventory);
        ItemStack stack = inv.getStackInSlot(playerSlot);
        IFluidHandlerItem fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandler != null) {
            IIngredientComponentStorage<FluidStack, Integer> itemStorage = getFluidStorage(storage.getComponent(), fluidHandler);
            FluidStack moved = IngredientStorageHelpers.moveIngredients(storage, itemStorage, maxInstance,
                    FluidMatch.FLUID | FluidMatch.NBT, false);
            inv.setStackInSlot(playerSlot, fluidHandler.getContainer());
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
    public void extractMaxFromPlayerInventorySlot(IIngredientComponentStorage<FluidStack, Integer> storage, InventoryPlayer playerInventory, int playerSlot) {
        ItemStack toMoveStack = playerInventory.getStackInSlot(playerSlot);
        IFluidHandlerItem fluidHandler = toMoveStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandler != null) {
            IIngredientComponentStorage<FluidStack, Integer> itemStorage = getFluidStorage(storage.getComponent(), fluidHandler);
            IngredientStorageHelpers.moveIngredientsIterative(itemStorage, storage, Long.MAX_VALUE, false);

            playerInventory.setInventorySlotContents(playerSlot, fluidHandler.getContainer());
            playerInventory.player.openContainer.detectAndSendChanges();
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
