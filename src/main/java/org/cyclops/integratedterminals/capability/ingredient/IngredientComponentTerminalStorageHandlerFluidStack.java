package org.cyclops.integratedterminals.capability.ingredient;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
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
import org.cyclops.integratedterminals.client.gui.container.GuiTerminalStorage;

import javax.annotation.Nullable;
import java.util.List;

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
                        lines.add(TextFormatting.GRAY.toString() + instance.amount + " mB");
                    }
                    return lines;
                });
            }
        }
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
    public int insertIntoPlayerInventory(IIngredientComponentStorage<FluidStack, Integer> storage,
                                         InventoryPlayer playerInventory, int playerSlot, FluidStack maxInstance) {
        PlayerMainInvWrapper inv = new PlayerMainInvWrapper(playerInventory);
        ItemStack stack = inv.getStackInSlot(playerSlot);
        IFluidHandlerItem fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandler != null) {
            IIngredientComponentStorage<FluidStack, Integer> itemStorage = getFluidStorage(storage.getComponent(), fluidHandler);
            FluidStack moved = IngredientStorageHelpers.moveIngredients(storage, itemStorage, maxInstance,
                    FluidMatch.EXACT, false);
            inv.setStackInSlot(playerSlot, fluidHandler.getContainer());
            return FluidHelpers.getAmount(moved);
        }
        return 0;
    }

    protected IIngredientComponentStorage<FluidStack, Integer> getFluidStorage(IngredientComponent<FluidStack, Integer> component,
                                                                               IFluidHandlerItem fluidHandler) {
        return component
                .getStorageWrapperHandler(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                .wrapComponentStorage(fluidHandler);
    }

    @Override
    public void extractActiveStackFromPlayerInventory(IIngredientComponentStorage<FluidStack, Integer> storage,
                                                      InventoryPlayer playerInventory) {
        ItemStack playerStack = playerInventory.getItemStack();
        IFluidHandlerItem fluidHandler = playerStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandler != null) {
            IIngredientComponentStorage<FluidStack, Integer> itemStorage = getFluidStorage(storage.getComponent(), fluidHandler);
            IngredientStorageHelpers.moveIngredientsIterative(itemStorage, storage, Long.MAX_VALUE, false);

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
}
