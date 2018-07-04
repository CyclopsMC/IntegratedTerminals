package org.cyclops.integratedterminals.capability.ingredient;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.ingredient.storage.IngredientStorageHelpers;
import org.cyclops.integrateddynamics.block.BlockEnergyBattery;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.client.gui.container.GuiTerminalStorage;

import javax.annotation.Nullable;

/**
 * Terminal storage handler for energy.
 * @author rubensworks
 */
public class IngredientComponentTerminalStorageHandlerEnergy implements IIngredientComponentTerminalStorageHandler<Integer, Boolean> {

    private final IngredientComponent<Integer, Boolean> ingredientComponent;

    public IngredientComponentTerminalStorageHandlerEnergy(IngredientComponent<Integer, Boolean> ingredientComponent) {
        this.ingredientComponent = ingredientComponent;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(BlockEnergyBattery.getInstance());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInstance(Integer instance, long maxQuantity, @Nullable String label, GuiContainer gui,
                             GuiTerminalStorage.DrawLayer layer, float partialTick, int x, int y, int mouseX, int mouseY, int channel) {
        if (instance > 0) {
            if (layer == GuiTerminalStorage.DrawLayer.BACKGROUND){
                // Draw background
                gui.drawTexturedModalRect(x, y, 48, 225, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER);

                // Draw progress
                GuiHelpers.renderProgressBar(gui, x, y, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER,
                        64, 225, GuiHelpers.ProgressDirection.UP, instance, (int) maxQuantity);

                // Draw amount
                RenderItemExtendedSlotCount.drawSlotText(Minecraft.getMinecraft().fontRenderer, label != null ? label : GuiHelpers.quantityToScaledString(instance), x, y);
            } else {
                GuiHelpers.renderTooltip(gui, x, y, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER,
                        mouseX, mouseY, () -> Lists.newArrayList(label != null ? label : TextFormatting.GRAY.toString() + instance + " FE"));
            }
        }
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
    public int throwIntoWorld(IIngredientComponentStorage<Integer, Boolean> storage, Integer maxInstance,
                              EntityPlayer player) {
        return 0; // Dropping energy in the world is not possible
    }

    protected IIngredientComponentStorage<Integer, Boolean> getEnergyStorage(IngredientComponent<Integer, Boolean> component,
                                                                             IEnergyStorage energyStorage) {
        return component
                .getStorageWrapperHandler(CapabilityEnergy.ENERGY)
                .wrapComponentStorage(energyStorage);
    }

    @Override
    public int insertIntoPlayerInventory(IIngredientComponentStorage<Integer, Boolean> storage,
                                         InventoryPlayer playerInventory, int playerSlot, Integer maxInstance) {
        PlayerMainInvWrapper inv = new PlayerMainInvWrapper(playerInventory);
        ItemStack stack = inv.getStackInSlot(playerSlot);
        IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage != null) {
            IIngredientComponentStorage<Integer, Boolean> itemStorage = getEnergyStorage(storage.getComponent(), energyStorage);
            return IngredientStorageHelpers.moveIngredients(storage, itemStorage, maxInstance, false);
        }
        return 0;
    }

    @Override
    public void extractActiveStackFromPlayerInventory(IIngredientComponentStorage<Integer, Boolean> storage,
                                                      InventoryPlayer playerInventory) {
        ItemStack playerStack = playerInventory.getItemStack();
        IEnergyStorage energyStorage = playerStack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage != null) {
            IIngredientComponentStorage<Integer, Boolean> itemStorage = getEnergyStorage(storage.getComponent(), energyStorage);
            IngredientStorageHelpers.moveIngredients(itemStorage, storage, Long.MAX_VALUE, false);
        }
    }

    @Override
    public void extractMaxFromPlayerInventorySlot(IIngredientComponentStorage<Integer, Boolean> storage, InventoryPlayer playerInventory, int playerSlot) {
        ItemStack toMoveStack = playerInventory.getStackInSlot(playerSlot);
        IEnergyStorage energyStorage = toMoveStack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage != null) {
            IIngredientComponentStorage<Integer, Boolean> itemStorage = getEnergyStorage(storage.getComponent(), energyStorage);
            IngredientStorageHelpers.moveIngredients(itemStorage, storage, Long.MAX_VALUE, false);
        }
    }
}
