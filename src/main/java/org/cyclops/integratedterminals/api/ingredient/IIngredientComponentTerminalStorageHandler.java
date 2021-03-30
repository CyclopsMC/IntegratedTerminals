package org.cyclops.integratedterminals.api.ingredient;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.core.terminalstorage.query.SearchMode;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Capability for displaying and interacting with ingredient components of a certain type in the storage terminal.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public interface IIngredientComponentTerminalStorageHandler<T, M> {

    /**
     * @return The ingredient component.
     */
    public IngredientComponent<T, M> getComponent();

    /**
     * @return The item that can be used to visually represent this ingredient component type.
     */
    public ItemStack getIcon();

    /**
     * Draw the given instance in the given gui.
     * @param matrixStack The matrix stack.
     * @param instance An instance.
     * @param maxQuantity The maximum allowed quantity of the given instance.
     * @param label An optional label that should be rendered instead of the quantity.
     * @param gui A gui to render in.
     * @param layer The layer to render in.
     * @param partialTick The partial tick.
     * @param x The slot X position.
     * @param y The slot Y position.
     * @param mouseX The mouse X position.
     * @param mouseY The mouse Y position.
     * @param additionalTooltipLines The additional tooltip lines to add.
     */
    @OnlyIn(Dist.CLIENT)
    public void drawInstance(MatrixStack matrixStack, T instance, long maxQuantity, @Nullable String label, ContainerScreen gui, ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int x, int y,
                             int mouseX, int mouseY, @Nullable List<ITextComponent> additionalTooltipLines);

    /**
     * Show the quantity of the given instance on the second tooltip line.
     * @param lines Tooltip lines
     * @param instance An instance.
     */
    public default void addQuantityTooltip(List<ITextComponent> lines, T instance) {
        ITextComponent line = new TranslationTextComponent(
                "gui.integratedterminals.terminal_storage.tooltip.quantity",
                formatQuantity(instance))
                .mergeStyle(TextFormatting.DARK_GRAY);
        if (lines.size() <= 1) {
            lines.add(line);
        } else {
            lines.add(1, line);
        }
    }

    /**
     * Get a display string for the quantity of the given instance.
     * @param instance An instance.
     * @return The formatted quantity.
     */
    public String formatQuantity(T instance);

    /**
     * Get the ingredient instance from the given item.
     * @param itemStack An item.
     * @return If the stack represents an ingredient.
     */
    public boolean isInstance(ItemStack itemStack);

    /**
     * Get the ingredient instance from the given item.
     * @param itemStack An item.
     * @return An ingredient.
     */
    public T getInstance(ItemStack itemStack);

    /**
     * Get the max instance quantity in the given item.
     * @param itemStack An item.
     * @return The max quantity.
     */
    public long getMaxQuantity(ItemStack itemStack);

    /**
     * @return The number that should be selected when clicking on an instance in the storage terminal.
     */
    public int getInitialInstanceMovementQuantity();

    /**
     * @return The number that should be removed when right-clicking when an instance is selected in the storage terminal.
     */
    public int getIncrementalInstanceMovementQuantity();

    /**
     * Throw the given instance into the world.
     * @param storage The storage to extract from.
     * @param maxInstance The instance to throw.
     * @param player The throwing player.
     * @return The instance quantity that was thrown.
     */
    public int throwIntoWorld(IIngredientComponentStorage<T, M> storage, T maxInstance, PlayerEntity player);

    /**
     * Insert as much as possible from the given instance prototype into the container.
     * @param storage The storage to insert to.
     * @param container The container to extract from.
     * @param containerSlotStart The container slot to start from.
     * @param containerSlotEnd The container slot to end at (exclusive).
     * @param instance The instance to move.
     */
    public default void insertMaxIntoContainer(IIngredientComponentStorage<T, M> storage, Container container,
                                               int containerSlotStart, int containerSlotEnd, T instance) {
        IIngredientMatcher<T, M> matcher = storage.getComponent().getMatcher();
        T toAdd = instance;
        int slot = containerSlotStart;
        while (!matcher.isEmpty(toAdd) && slot < containerSlotEnd) {
            T moved = insertIntoContainer(storage, container, slot++, toAdd, null, true);
            if (!matcher.isEmpty(moved)) {
                toAdd = matcher.withQuantity(toAdd, Math.max(0, matcher.getQuantity(toAdd) - matcher.getQuantity(moved)));
            }
        }
    }

    /**
     * Insert the given instance into the container.
     * @param storage The storage to extract from.
     * @param container The container to insert to.
     * @param containerSlot The container slot to insert to.
     * @param maxInstance The instance to move.
     * @param player The player. If null, the container slot will not be picked up by the player if not empty.
     * @param transferFullSelection If the selected stack should be moved fully.
     * @return The instance quantity that was moved.
     */
    public T insertIntoContainer(IIngredientComponentStorage<T, M> storage, Container container, int containerSlot, T maxInstance, @Nullable PlayerEntity player, boolean transferFullSelection);

    /**
     * Move the ingredient in the active player stack to the storage.
     * @param storage The storage to insert to.
     * @param playerInventory The player inventory to extract from.
     * @param moveQuantityPlayerSlot The player stack quantity that should be extracted.
     */
    public void extractActiveStackFromPlayerInventory(IIngredientComponentStorage<T, M> storage, PlayerInventory playerInventory, long moveQuantityPlayerSlot);

    /**
     * Move as much as possible from the given container slot into the storage.
     * @param storage The storage to insert to.
     * @param container The container to insert to.
     * @param containerSlot The container slot to insert to.
     * @param playerInventory The active player inventory.
     * @param limit The max limit. -1 is no limit.
     */
    public void extractMaxFromContainerSlot(IIngredientComponentStorage<T, M> storage, Container container, int containerSlot, PlayerInventory playerInventory, int limit);

    /**
     * Get the quantity in the active player stack.
     * @param playerInventory The player inventory.
     * @return The quantity.
     */
    public long getActivePlayerStackQuantity(PlayerInventory playerInventory);

    /**
     * Drain the given quantity from the active player stack.
     * This will typically only be called client-side, and later confirmed by the server.
     * @param playerInventory The player inventory.
     * @param quantity The quantity to drain.
     */
    public void drainActivePlayerStackQuantity(PlayerInventory playerInventory, long quantity);

    /**
     * Get a predicate for matching instances that apply to the given query string.
     * @param searchMode The mode to search under
     * @param query A query string.
     * @return An instance matcher.
     */
    @OnlyIn(Dist.CLIENT)
    public Predicate<T> getInstanceFilterPredicate(SearchMode searchMode, String query);

    /**
     * @return The available sorters.
     */
    public Collection<IIngredientInstanceSorter<T>> getInstanceSorters();

}
