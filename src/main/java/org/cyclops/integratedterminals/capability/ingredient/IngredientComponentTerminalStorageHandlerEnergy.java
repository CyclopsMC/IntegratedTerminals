package org.cyclops.integratedterminals.capability.ingredient;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.ingredient.storage.InconsistentIngredientInsertionException;
import org.cyclops.cyclopscore.ingredient.storage.IngredientStorageHelpers;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.RegistryEntries;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.api.ingredient.IIngredientInstanceSorter;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
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
        return new ItemStack(RegistryEntries.ITEM_ENERGY_BATTERY);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInstance(Integer instance, long maxQuantity, @Nullable String label, ContainerScreen gui,
                             ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int x, int y,
                             int mouseX, int mouseY, @Nullable List<ITextComponent> additionalTooltipLines) {
        if (instance > 0) {
            if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND){

                // Draw background
                GlStateManager.color4f(1, 1, 1, 1);
                RenderHelper.enableStandardItemLighting();
                RenderHelpers.bindTexture(Images.ICONS);
                gui.blit(x, y, 0, 240, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER);

                // Draw progress
                GuiHelpers.renderProgressBar(gui, x, y, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER,
                        16, 240, GuiHelpers.ProgressDirection.UP, instance, (int) maxQuantity);

                // Draw amount
                GlStateManager.disableLighting();
                RenderItemExtendedSlotCount.getInstance().drawSlotText(Minecraft.getInstance().fontRenderer, new MatrixStack(), label != null ? label : GuiHelpers.quantityToScaledString(instance), x, y);

                RenderHelper.disableStandardItemLighting();
            } else {
                GuiHelpers.renderTooltip(gui, x, y, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER,
                        mouseX, mouseY, () -> {
                            List<ITextComponent> lines = Lists.newArrayList();
                            lines.add(new TranslationTextComponent("gui.integratedterminals.terminal_storage.tooltip.energy"));
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
        return itemStack.getCapability(CapabilityEnergy.ENERGY).isPresent();
    }

    @Override
    public Integer getInstance(ItemStack itemStack) {
        return itemStack.getCapability(CapabilityEnergy.ENERGY)
                .map(IEnergyStorage::getEnergyStored)
                .orElse(0);
    }

    @Override
    public long getMaxQuantity(ItemStack itemStack) {
        return itemStack.getCapability(CapabilityEnergy.ENERGY)
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
    public int throwIntoWorld(IIngredientComponentStorage<Integer, Boolean> storage, Integer maxInstance,
                              PlayerEntity player) {
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
                                       @Nullable PlayerEntity player, boolean transferFullSelection) {
        ItemStack stack = container.getSlot(containerSlot).getStack();

        return stack.getCapability(CapabilityEnergy.ENERGY)
                .map(energyStorage -> {
                    IIngredientComponentStorage<Integer, Boolean> itemStorage = getEnergyStorage(storage.getComponent(), energyStorage);
                    Integer ret = 0;
                    try {
                        ret = IngredientStorageHelpers.moveIngredientsIterative(storage, itemStorage, maxInstance, false);
                    } catch (InconsistentIngredientInsertionException e) {
                        // Ignore
                    }
                    container.detectAndSendChanges();
                    return ret;
                })
                .orElse(0);
    }

    @Override
    public void extractActiveStackFromPlayerInventory(IIngredientComponentStorage<Integer, Boolean> storage,
                                                      PlayerInventory playerInventory, long moveQuantityPlayerSlot) {
        ItemStack playerStack = playerInventory.getItemStack();
        playerStack.getCapability(CapabilityEnergy.ENERGY)
                .ifPresent(energyStorage -> {
                    IIngredientComponentStorage<Integer, Boolean> itemStorage = getEnergyStorage(storage.getComponent(), energyStorage);
                    try {
                        IngredientStorageHelpers.moveIngredientsIterative(itemStorage, storage, moveQuantityPlayerSlot, false);
                    } catch (InconsistentIngredientInsertionException e) {
                        // Ignore
                    }
                });
    }

    @Override
    public void extractMaxFromContainerSlot(IIngredientComponentStorage<Integer, Boolean> storage,
                                            Container container, int containerSlot, PlayerInventory playerInventory) {
        ItemStack toMoveStack = container.getSlot(containerSlot).getStack();
        toMoveStack.getCapability(CapabilityEnergy.ENERGY)
                .ifPresent(energyStorage -> {
                    IIngredientComponentStorage<Integer, Boolean> itemStorage = getEnergyStorage(storage.getComponent(), energyStorage);
                    try {
                        IngredientStorageHelpers.moveIngredientsIterative(itemStorage, storage, Long.MAX_VALUE, false);
                    } catch (InconsistentIngredientInsertionException e) {
                        // Ignore
                    }
                });
    }

    @Override
    public long getActivePlayerStackQuantity(PlayerInventory playerInventory) {
        ItemStack toMoveStack = playerInventory.getItemStack();
        return toMoveStack.getCapability(CapabilityEnergy.ENERGY)
                .map(IEnergyStorage::getEnergyStored)
                .orElse(0);
    }

    @Override
    public void drainActivePlayerStackQuantity(PlayerInventory playerInventory, long quantityIn) {
        ItemStack toMoveStack = playerInventory.getItemStack();
        toMoveStack.getCapability(CapabilityEnergy.ENERGY)
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
    @OnlyIn(Dist.CLIENT)
    public Predicate<Integer> getInstanceFilterPredicate(SearchMode searchMode, String query) {
        return integer -> true; // Searching does not make sense here, as at most one instance exists.
    }

    @Override
    public Collection<IIngredientInstanceSorter<Integer>> getInstanceSorters() {
        return Collections.emptyList();
    }
}
