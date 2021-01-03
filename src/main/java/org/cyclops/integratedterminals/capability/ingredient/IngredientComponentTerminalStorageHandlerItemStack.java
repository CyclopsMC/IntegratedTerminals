package org.cyclops.integratedterminals.capability.ingredient;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.items.ItemHandlerHelper;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.api.ingredient.IIngredientInstanceSorter;
import org.cyclops.integratedterminals.capability.ingredient.sorter.ItemStackIdSorter;
import org.cyclops.integratedterminals.capability.ingredient.sorter.ItemStackNameSorter;
import org.cyclops.integratedterminals.capability.ingredient.sorter.ItemStackQuantitySorter;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.core.terminalstorage.query.SearchMode;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Terminal storage handler for items.
 * @author rubensworks
 */
public class IngredientComponentTerminalStorageHandlerItemStack implements IIngredientComponentTerminalStorageHandler<ItemStack, Integer> {

    private final IngredientComponent<ItemStack, Integer> ingredientComponent;

    public IngredientComponentTerminalStorageHandlerItemStack(IngredientComponent<ItemStack, Integer> ingredientComponent) {
        this.ingredientComponent = ingredientComponent;
    }

    @Override
    public IngredientComponent<ItemStack, Integer> getComponent() {
        return ingredientComponent;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Blocks.CHEST);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInstance(MatrixStack matrixStack, ItemStack instance, long maxQuantity, @Nullable String label, ContainerScreen gui,
                             ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int x, int y,
                             int mouseX, int mouseY, @Nullable List<ITextComponent> additionalTooltipLines) {
        RenderItemExtendedSlotCount renderItem = RenderItemExtendedSlotCount.getInstance();
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableDepthTest();
        GL11.glEnable(GL11.GL_DEPTH_TEST); // Needed, as the line above doesn't always seem to work...
        if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND) {
            Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(instance, x, y);
            renderItem.renderItemOverlayIntoGUI(Minecraft.getInstance().fontRenderer, instance, x, y, label);
        } else {
            GuiUtils.preItemToolTip(instance);
            GuiHelpers.renderTooltip(gui, x, y, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER, mouseX, mouseY, () -> {
                List<ITextComponent> lines = instance.getTooltip(
                        Minecraft.getInstance().player, Minecraft.getInstance().gameSettings.advancedItemTooltips
                                ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
                if (additionalTooltipLines != null) {
                    lines.addAll(additionalTooltipLines);
                }
                addQuantityTooltip(lines, instance);
                return lines;
            });
            GuiUtils.postItemToolTip();
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public String formatQuantity(ItemStack instance) {
        return String.format("%,d", instance.getCount());
    }

    @Override
    public boolean isInstance(ItemStack itemStack) {
        return itemStack != null;
    }

    @Override
    public ItemStack getInstance(ItemStack itemStack) {
        return itemStack;
    }

    @Override
    public long getMaxQuantity(ItemStack itemStack) {
        return itemStack.getMaxStackSize();
    }

    @Override
    public int getInitialInstanceMovementQuantity() {
        return GeneralConfig.guiStorageItemInitialQuantity;
    }

    @Override
    public int getIncrementalInstanceMovementQuantity() {
        return GeneralConfig.guiStorageItemIncrementalQuantity;
    }

    @Override
    public int throwIntoWorld(IIngredientComponentStorage<ItemStack, Integer> storage, ItemStack maxInstance,
                              PlayerEntity player) {
        ItemStack extracted = storage.extract(maxInstance, ItemMatch.EXACT, false);
        if (!extracted.isEmpty()) {
            player.dropItem(extracted, true);
        }
        return extracted.getCount();
    }

    @Override
    public ItemStack insertIntoContainer(IIngredientComponentStorage<ItemStack, Integer> storage,
                                         Container container, int containerSlotIndex, ItemStack maxInstance,
                                         @Nullable PlayerEntity player, boolean transferFullSelection) {
        IIngredientMatcher<ItemStack, Integer> matcher = IngredientComponent.ITEMSTACK.getMatcher();

        // Limit transfer to 64 at a time
        if (maxInstance.getCount() > 64) {
            maxInstance.setCount(64);
        }

        Slot containerSlot = container.getSlot(containerSlotIndex);
        if (transferFullSelection && player != null && player.inventory.getItemStack().isEmpty()) {
            // Pick up container slot contents if not empty
            ItemStack containerStack = containerSlot.getStack();
            if (!containerStack.isEmpty()
                    && !matcher.matches(containerStack, maxInstance, matcher.getExactMatchNoQuantityCondition())
                    && containerSlot.canTakeStack(player)) {
                player.inventory.setItemStack(containerStack);
                containerSlot.putStack(ItemStack.EMPTY);
            }
        }

        long requiredQuantity = matcher.getQuantity(maxInstance);
        long movedTotal = 0;
        while (movedTotal < requiredQuantity) {
            ItemStack extracted = storage.extract(maxInstance, matcher.getExactMatchNoQuantityCondition(), true);
            if (extracted.isEmpty()) {
                break;
            }
            ItemStack playerStack = containerSlot.getStack();
            if ((playerStack.isEmpty() || ItemHandlerHelper.canItemStacksStack(extracted, playerStack))
                    && containerSlot.isItemValid(extracted)) {
                int newCount = Math.min(playerStack.getCount() + extracted.getCount(), extracted.getMaxStackSize());
                int inserted = newCount - playerStack.getCount();
                ItemStack moved = storage.extract(matcher.withQuantity(maxInstance, inserted),
                        matcher.getExactMatchNoQuantityCondition(), false);
                if (moved.isEmpty()) {
                    break;
                }
                movedTotal += moved.getCount();

                containerSlot.putStack(matcher.withQuantity(maxInstance, containerSlot.getStack().getCount() + moved.getCount()).copy());
                container.detectAndSendChanges();
            } else {
                break;
            }
        }
        return matcher.withQuantity(maxInstance, movedTotal);
    }

    @Override
    public void extractActiveStackFromPlayerInventory(IIngredientComponentStorage<ItemStack, Integer> storage,
                                                      PlayerInventory playerInventory, long moveQuantityPlayerSlot) {
        ItemStack playerStack = IngredientComponent.ITEMSTACK.getMatcher().withQuantity(playerInventory.getItemStack(),
                moveQuantityPlayerSlot);
        int remaining = storage.insert(playerStack.copy(), false).getCount();
        int moved = (int) (moveQuantityPlayerSlot - remaining);
        playerInventory.getItemStack().shrink(moved);
    }

    @Override
    public void extractMaxFromContainerSlot(IIngredientComponentStorage<ItemStack, Integer> storage,
                                            Container container, int containerSlot, PlayerInventory playerInventory) {
        Slot slot = container.getSlot(containerSlot);
        ItemStack toMove = slot.getStack();
        if (!toMove.isEmpty()) {
            // The following code is a bit convoluted to handle cases where the container and the storage point to the same inventory.
            // See https://github.com/CyclopsMC/IntegratedTerminals/issues/47
            slot.putStack(ItemStack.EMPTY);
            ItemStack remainingStack = storage.insert(toMove, false);
            if (!remainingStack.isEmpty()) {
                // Check if the slot is still empty, because the storage may be linked to the container in some exotic cases (e.g. player interfaces).
                if (!slot.getHasStack()) {
                    slot.putStack(remainingStack);
                } else {
                    // Simply add the remainder to the player's container
                    playerInventory.addItemStackToInventory(remainingStack);
                }
            }
            container.detectAndSendChanges();
        }
    }

    @Override
    public long getActivePlayerStackQuantity(PlayerInventory playerInventory) {
        return playerInventory.getItemStack().getCount();
    }

    @Override
    public void drainActivePlayerStackQuantity(PlayerInventory playerInventory, long quantity) {
        playerInventory.getItemStack().shrink((int) quantity);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Predicate<ItemStack> getInstanceFilterPredicate(SearchMode searchMode, String query) {
        switch (searchMode) {
            case MOD:
                return i -> Optional.ofNullable(i.getItem().getCreatorModId(i))
                        .orElse("minecraft").toLowerCase(Locale.ENGLISH)
                        .matches(".*" + query + ".*");
            case TOOLTIP:
                return i -> i.getTooltip(Minecraft.getInstance().player, ITooltipFlag.TooltipFlags.NORMAL).stream()
                        .anyMatch(s -> s.getString().toLowerCase(Locale.ENGLISH).matches(".*" + query + ".*"));
            case TAG:
                return i -> ItemTags.getCollection().getOwningTags(i.getItem()).stream()
                        .filter(id -> id.toString().toLowerCase(Locale.ENGLISH).matches(".*" + query + ".*"))
                        .map(r -> ItemTags.getCollection().get(r))
                        .anyMatch(Objects::nonNull);
            case DEFAULT:
                return i -> i.getDisplayName().getString().toLowerCase(Locale.ENGLISH).matches(".*" + query + ".*");
        }
        return null;
    }

    @Override
    public Collection<IIngredientInstanceSorter<ItemStack>> getInstanceSorters() {
        return Lists.newArrayList(
                new ItemStackNameSorter(),
                new ItemStackIdSorter(),
                new ItemStackQuantitySorter()
        );
    }
}
