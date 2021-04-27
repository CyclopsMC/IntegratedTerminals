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
public class IngredientComponentTerminalStorageHandlerEnergy implements IIngredientComponentTerminalStorageHandler<Long, Boolean> {

    private final IngredientComponent<Long, Boolean> ingredientComponent;

    public IngredientComponentTerminalStorageHandlerEnergy(IngredientComponent<Long, Boolean> ingredientComponent) {
        this.ingredientComponent = ingredientComponent;
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
    @OnlyIn(Dist.CLIENT)
    public void drawInstance(MatrixStack matrixStack, Long instance, long maxQuantity, @Nullable String label, ContainerScreen gui,
                             ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int x, int y,
                             int mouseX, int mouseY, @Nullable List<ITextComponent> additionalTooltipLines) {
        if (instance > 0) {
            if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND){

                // Draw background
                GlStateManager.color4f(1, 1, 1, 1);
                RenderHelper.enableStandardItemLighting();
                RenderHelpers.bindTexture(Images.ICONS);
                gui.blit(matrixStack, x, y, 0, 240, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER);

                // Draw progress
                int progressScaled;
                int progressMaxScaled;
                if ((int)maxQuantity == maxQuantity) {
                    progressScaled = (int) (long) instance;
                    progressMaxScaled = (int) maxQuantity;
                } else {
                    progressScaled = (int) (long) (instance >> 16);
                    progressMaxScaled = (int) (maxQuantity >> 16);
                }
                GuiHelpers.renderProgressBar(gui, matrixStack, x, y, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER,
                        16, 240, GuiHelpers.ProgressDirection.UP, progressScaled, progressMaxScaled);

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
    public String formatQuantity(Long instance) {
        return L10NHelpers.localize("gui.integratedterminals.terminal_storage.tooltip.energy.amount",
                String.format("%,d", instance));
    }

    @Override
    public boolean isInstance(ItemStack itemStack) {
        return itemStack.getCapability(CapabilityEnergy.ENERGY).isPresent();
    }

    @Override
    public Long getInstance(ItemStack itemStack) {
        return itemStack.getCapability(CapabilityEnergy.ENERGY)
                .map(IEnergyStorage::getEnergyStored)
                .orElse(0)
                .longValue();
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
    public int throwIntoWorld(IIngredientComponentStorage<Long, Boolean> storage, Long maxInstance,
                              PlayerEntity player) {
        return 0; // Dropping energy in the world is not possible
    }

    protected IIngredientComponentStorage<Long, Boolean> getEnergyStorage(IngredientComponent<Long, Boolean> component,
                                                                             IEnergyStorage energyStorage) {
        return component
                .getStorageWrapperHandler(CapabilityEnergy.ENERGY)
                .wrapComponentStorage(energyStorage);
    }

    @Override
    public Long insertIntoContainer(IIngredientComponentStorage<Long, Boolean> storage,
                                       Container container, int containerSlot, Long maxInstance,
                                       @Nullable PlayerEntity player, boolean transferFullSelection) {
        ItemStack stack = container.getSlot(containerSlot).getStack();

        return stack.getCapability(CapabilityEnergy.ENERGY)
                .map(energyStorage -> {
                    IIngredientComponentStorage<Long, Boolean> itemStorage = getEnergyStorage(storage.getComponent(), energyStorage);
                    Long ret = 0L;
                    try {
                        ret = IngredientStorageHelpers.moveIngredientsIterative(storage, itemStorage, maxInstance, false);
                    } catch (InconsistentIngredientInsertionException e) {
                        // Ignore
                    }
                    container.detectAndSendChanges();
                    return ret;
                })
                .orElse(0L);
    }

    @Override
    public void extractActiveStackFromPlayerInventory(IIngredientComponentStorage<Long, Boolean> storage,
                                                      PlayerInventory playerInventory, long moveQuantityPlayerSlot) {
        ItemStack playerStack = playerInventory.getItemStack();
        playerStack.getCapability(CapabilityEnergy.ENERGY)
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
                                            Container container, int containerSlot, PlayerInventory playerInventory, int limit) {
        ItemStack toMoveStack = container.getSlot(containerSlot).getStack();
        toMoveStack.getCapability(CapabilityEnergy.ENERGY)
                .ifPresent(energyStorage -> {
                    IIngredientComponentStorage<Long, Boolean> itemStorage = getEnergyStorage(storage.getComponent(), energyStorage);
                    try {
                        IngredientStorageHelpers.moveIngredientsIterative(itemStorage, storage, limit == -1 ? Long.MAX_VALUE : limit, false);
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
    public Predicate<Long> getInstanceFilterPredicate(SearchMode searchMode, String query) {
        return integer -> true; // Searching does not make sense here, as at most one instance exists.
    }

    @Override
    public Collection<IIngredientInstanceSorter<Long>> getInstanceSorters() {
        return Collections.emptyList();
    }
}
