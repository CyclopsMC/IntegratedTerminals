package org.cyclops.integratedterminals.capability.ingredient;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
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
    public void drawInstance(PoseStack matrixStack, ItemStack instance, long maxQuantity, @Nullable String label, AbstractContainerScreen gui,
                             ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int x, int y,
                             int mouseX, int mouseY, @Nullable List<Component> additionalTooltipLines) {
        RenderItemExtendedSlotCount renderItem = RenderItemExtendedSlotCount.getInstance();
        matrixStack.pushPose();
        GlStateManager._enableBlend();
        GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Lighting.setupFor3DItems();
        //GlStateManager._enableRescaleNormal();
        GlStateManager._enableDepthTest();
        GL11.glEnable(GL11.GL_DEPTH_TEST); // Needed, as the line above doesn't always seem to work...
        if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND) {
            Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(matrixStack, instance, x, y);
            renderItem.renderGuiItemDecorations(matrixStack, Minecraft.getInstance().font, instance, x, y, label);
        } else {
            GuiHelpers.renderTooltip(gui, matrixStack, x, y, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER, mouseX, mouseY, () -> {
                List<Component> lines = instance.getTooltipLines(
                        Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips
                                ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
                if (additionalTooltipLines != null) {
                    lines.addAll(additionalTooltipLines);
                }
                addQuantityTooltip(lines, instance);
                return lines;
            });
        }
        Lighting.setupForFlatItems();
        matrixStack.popPose();
    }

    @Override
    public String formatQuantity(ItemStack instance) {
        return String.format(Locale.ROOT, "%,d", instance.getCount());
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
                              Player player) {
        ItemStack extracted = storage.extract(maxInstance, ItemMatch.EXACT, false);
        if (!extracted.isEmpty()) {
            player.drop(extracted, true);
        }
        return extracted.getCount();
    }

    @Override
    public ItemStack insertIntoContainer(IIngredientComponentStorage<ItemStack, Integer> storage,
                                         AbstractContainerMenu container, int containerSlotIndex, ItemStack maxInstance,
                                         @Nullable Player player, boolean transferFullSelection) {
        IIngredientMatcher<ItemStack, Integer> matcher = IngredientComponent.ITEMSTACK.getMatcher();

        // Limit transfer to 64 at a time
        if (maxInstance.getCount() > 64) {
            maxInstance.setCount(64);
        }

        Slot containerSlot = container.getSlot(containerSlotIndex);
        if (transferFullSelection && player != null && container.getCarried().isEmpty()) {
            // Pick up container slot contents if not empty
            ItemStack containerStack = containerSlot.getItem();
            if (!containerStack.isEmpty()
                    && !matcher.matches(containerStack, maxInstance, matcher.getExactMatchNoQuantityCondition())
                    && containerSlot.mayPickup(player)) {
                container.setCarried(containerStack);
                containerSlot.onTake(player, containerStack);
                containerSlot.set(ItemStack.EMPTY);
            }
        }

        long requiredQuantity = matcher.getQuantity(maxInstance);
        long movedTotal = 0;
        while (movedTotal < requiredQuantity) {
            ItemStack extracted = storage.extract(maxInstance, matcher.getExactMatchNoQuantityCondition(), true);
            if (extracted.isEmpty()) {
                break;
            }
            ItemStack playerStack = containerSlot.getItem();
            if ((playerStack.isEmpty() || ItemHandlerHelper.canItemStacksStack(extracted, playerStack))
                    && containerSlot.mayPlace(extracted)) {
                int newCount = Math.min(playerStack.getCount() + extracted.getCount(), extracted.getMaxStackSize());
                int inserted = newCount - playerStack.getCount();
                ItemStack moved = storage.extract(matcher.withQuantity(maxInstance, inserted),
                        matcher.getExactMatchNoQuantityCondition(), false);
                if (moved.isEmpty()) {
                    break;
                }
                movedTotal += moved.getCount();

                containerSlot.set(matcher.withQuantity(maxInstance, containerSlot.getItem().getCount() + moved.getCount()).copy());
                container.broadcastChanges();
            } else {
                break;
            }
        }
        return matcher.withQuantity(maxInstance, movedTotal);
    }

    @Override
    public void extractActiveStackFromPlayerInventory(IIngredientComponentStorage<ItemStack, Integer> storage,
                                                      AbstractContainerMenu container, Inventory playerInventory,
                                                      long moveQuantityPlayerSlot) {
        ItemStack playerStack = IngredientComponent.ITEMSTACK.getMatcher().withQuantity(container.getCarried(),
                moveQuantityPlayerSlot);
        int remaining = storage.insert(playerStack.copy(), false).getCount();
        int moved = (int) (moveQuantityPlayerSlot - remaining);
        container.getCarried().shrink(moved);
    }

    @Override
    public void extractMaxFromContainerSlot(IIngredientComponentStorage<ItemStack, Integer> storage,
                                            AbstractContainerMenu container, int containerSlot, Inventory playerInventory, int limit) {
        Slot slot = container.getSlot(containerSlot);
        if (slot.mayPickup(playerInventory.player)) {
            ItemStack toMove = slot.remove(limit == -1 ? Integer.MAX_VALUE : limit);
            if (!toMove.isEmpty()) {
                // The following code is a bit convoluted to handle cases where the container and the storage point to the same inventory.
                // See https://github.com/CyclopsMC/IntegratedTerminals/issues/47
                ItemStack remainingStack = storage.insert(toMove, false);
                if (!remainingStack.isEmpty()) {
                    // Check if the slot is still empty, because the storage may be linked to the container in some exotic cases (e.g. player interfaces).
                    if (!slot.hasItem()) {
                        slot.set(remainingStack);
                    } else {
                        // Simply add the remainder to the player's container
                        playerInventory.add(remainingStack);
                    }
                }
                container.broadcastChanges();
            }
        }
    }

    @Override
    public long getActivePlayerStackQuantity(Inventory playerInventory, AbstractContainerMenu container) {
        return container.getCarried().getCount();
    }

    @Override
    public void drainActivePlayerStackQuantity(Inventory playerInventory, AbstractContainerMenu container, long quantity) {
        container.getCarried().shrink((int) quantity);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Predicate<ItemStack> getInstanceFilterPredicate(SearchMode searchMode, String query) {
        return switch (searchMode) {
            case MOD -> i -> Optional.ofNullable(i.getItem().getCreatorModId(i))
                    .orElse("minecraft").toLowerCase(Locale.ENGLISH)
                    .matches(".*" + query + ".*");
            case TOOLTIP -> i -> i.getTooltipLines(Minecraft.getInstance().player, TooltipFlag.Default.NORMAL).stream()
                    .anyMatch(s -> s.getString().toLowerCase(Locale.ENGLISH).matches(".*" + query + ".*"));
            case TAG -> i -> ForgeRegistries.ITEMS.tags().getReverseTag(i.getItem())
                    .map(reverseTag -> reverseTag.getTagKeys()
                            .filter(tag -> tag.location().toString().toLowerCase(Locale.ENGLISH).matches(".*" + query + ".*"))
                            .anyMatch(tag -> !ForgeRegistries.ITEMS.tags().getTag(tag).isEmpty()))
                    .orElse(false);
            case DEFAULT -> i -> i.getHoverName().getString().toLowerCase(Locale.ENGLISH).matches(".*" + query + ".*");
        };
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
