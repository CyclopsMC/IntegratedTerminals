package org.cyclops.integratedterminals.capability.ingredient;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.ingredient.storage.InconsistentIngredientInsertionException;
import org.cyclops.cyclopscore.ingredient.storage.IngredientStorageHelpers;
import org.cyclops.integrateddynamics.block.BlockEnergyBattery;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.api.ingredient.IIngredientInstanceSorter;
import org.cyclops.integratedterminals.client.gui.container.GuiTerminalStorage;
import org.cyclops.integratedterminals.client.gui.image.Images;
import org.cyclops.integratedterminals.core.terminalstorage.query.SearchMode;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

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
    public IngredientComponent<Integer, Boolean> getComponent() {
        return ingredientComponent;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(BlockEnergyBattery.getInstance());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInstance(Integer instance, long maxQuantity, @Nullable String label, GuiContainer gui,
                             GuiTerminalStorage.DrawLayer layer, float partialTick, int x, int y,
                             int mouseX, int mouseY, @Nullable List<String> additionalTooltipLines) {
        if (instance > 0) {
            if (layer == GuiTerminalStorage.DrawLayer.BACKGROUND){

                // Draw background
                GlStateManager.color(1, 1, 1, 1);
                RenderHelper.enableGUIStandardItemLighting();
                RenderHelpers.bindTexture(Images.ICONS);
                gui.drawTexturedModalRect(x, y, 0, 240, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER);

                // Draw progress
                GuiHelpers.renderProgressBar(gui, x, y, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER,
                        16, 240, GuiHelpers.ProgressDirection.UP, instance, (int) maxQuantity);

                // Draw amount
                RenderItemExtendedSlotCount.drawSlotText(Minecraft.getMinecraft().fontRenderer, label != null ? label : GuiHelpers.quantityToScaledString(instance), x, y);

                RenderHelper.disableStandardItemLighting();
            } else {
                GuiHelpers.renderTooltip(gui, x, y, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER,
                        mouseX, mouseY, () -> {
                            List<String> lines = Lists.newArrayList();
                            lines.add(L10NHelpers.localize("gui.integratedterminals.terminal_storage.tooltip.energy"));
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
    public String formatQuantity(Integer instance) {
        return L10NHelpers.localize("gui.integratedterminals.terminal_storage.tooltip.energy.amount",
                String.format("%,d", instance));
    }

    @Override
    public boolean isInstance(ItemStack itemStack) {
        return itemStack.hasCapability(CapabilityEnergy.ENERGY, null);
    }

    @Override
    public Integer getInstance(ItemStack itemStack) {
        IEnergyStorage energyStorage = itemStack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage != null) {
            return energyStorage.getEnergyStored();
        }
        return 0;
    }

    @Override
    public long getMaxQuantity(ItemStack itemStack) {
        IEnergyStorage energyStorage = itemStack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage != null) {
            return energyStorage.getMaxEnergyStored();
        }
        return 0;
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
    public Integer insertIntoContainer(IIngredientComponentStorage<Integer, Boolean> storage,
                                       Container container, int containerSlot, Integer maxInstance,
                                       @Nullable EntityPlayer player, boolean transferFullSelection) {
        ItemStack stack = container.getSlot(containerSlot).getStack();
        IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage != null) {
            IIngredientComponentStorage<Integer, Boolean> itemStorage = getEnergyStorage(storage.getComponent(), energyStorage);
            Integer ret = 0;
            try {
                ret = IngredientStorageHelpers.moveIngredientsIterative(storage, itemStorage, maxInstance, false);
            } catch (InconsistentIngredientInsertionException e) {
                // Ignore
            }
            container.detectAndSendChanges();
            return ret;
        }
        return 0;
    }

    @Override
    public void extractActiveStackFromPlayerInventory(IIngredientComponentStorage<Integer, Boolean> storage,
                                                      InventoryPlayer playerInventory, long moveQuantityPlayerSlot) {
        ItemStack playerStack = playerInventory.getItemStack();
        IEnergyStorage energyStorage = playerStack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage != null) {
            IIngredientComponentStorage<Integer, Boolean> itemStorage = getEnergyStorage(storage.getComponent(), energyStorage);
            try {
                IngredientStorageHelpers.moveIngredientsIterative(itemStorage, storage, moveQuantityPlayerSlot, false);
            } catch (InconsistentIngredientInsertionException e) {
                // Ignore
            }
        }
    }

    @Override
    public void extractMaxFromContainerSlot(IIngredientComponentStorage<Integer, Boolean> storage,
                                            Container container, int containerSlot) {
        ItemStack toMoveStack = container.getSlot(containerSlot).getStack();
        IEnergyStorage energyStorage = toMoveStack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage != null) {
            IIngredientComponentStorage<Integer, Boolean> itemStorage = getEnergyStorage(storage.getComponent(), energyStorage);
            try {
                IngredientStorageHelpers.moveIngredientsIterative(itemStorage, storage, Long.MAX_VALUE, false);
            } catch (InconsistentIngredientInsertionException e) {
                // Ignore
            }
        }
    }

    @Override
    public long getActivePlayerStackQuantity(InventoryPlayer playerInventory) {
        ItemStack toMoveStack = playerInventory.getItemStack();
        IEnergyStorage energyStorage = toMoveStack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage != null) {
            return energyStorage.getEnergyStored();
        }
        return 0;
    }

    @Override
    public void drainActivePlayerStackQuantity(InventoryPlayer playerInventory, long quantity) {
        ItemStack toMoveStack = playerInventory.getItemStack();
        IEnergyStorage energyStorage = toMoveStack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage != null) {
            // Drain
            while (quantity > 0) {
                int drained = energyStorage.extractEnergy((int) quantity, false);
                if (drained <= 0) {
                    break;
                }
                quantity -= drained;
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Predicate<Integer> getInstanceFilterPredicate(SearchMode searchMode, String query) {
        return integer -> true; // Searching does not make sense here, as at most one instance exists.
    }

    @Override
    public Collection<IIngredientInstanceSorter<Integer>> getInstanceSorters() {
        return Collections.emptyList();
    }
}
