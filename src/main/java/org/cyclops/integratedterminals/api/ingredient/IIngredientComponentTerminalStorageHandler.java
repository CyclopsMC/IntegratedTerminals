package org.cyclops.integratedterminals.api.ingredient;

import com.google.common.collect.Iterables;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientCollapsedCollectionMutable;
import org.cyclops.cyclopscore.ingredient.collection.IngredientCollectionHelpers;
import org.cyclops.cyclopscore.ingredient.storage.IngredientComponentStorageCollectionWrapper;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.core.terminalstorage.query.SearchMode;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
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
     * @param guiGraphics The matrix stack.
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
    @Deprecated // TODO: remove in next major
    @OnlyIn(Dist.CLIENT)
    public default void drawInstance(GuiGraphics guiGraphics, T instance, long maxQuantity, @Nullable String label, AbstractContainerScreen gui, ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int x, int y,
                                     int mouseX, int mouseY, @Nullable List<Component> additionalTooltipLines) {
        drawInstance(guiGraphics, instance, maxQuantity, label, gui, layer, partialTick, x, y, mouseX, mouseY, additionalTooltipLines, null);
    }

    /**
     * Draw the given instance in the given gui.
     * @param guiGraphics The matrix stack.
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
     * @param additionalTooltipComponent An optional visual tooltip component to render below the tooltip lines.
     */
    @OnlyIn(Dist.CLIENT)
    public void drawInstance(GuiGraphics guiGraphics, T instance, long maxQuantity, @Nullable String label, AbstractContainerScreen gui, ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int x, int y,
                             int mouseX, int mouseY, @Nullable List<Component> additionalTooltipLines,
                             @Nullable TooltipComponent additionalTooltipComponent);

    /**
     * Show the quantity of the given instance on the second tooltip line.
     * @param lines Tooltip lines
     * @param instance An instance.
     */
    public default void addQuantityTooltip(List<Component> lines, T instance) {
        Component line = Component.translatable(
                "gui.integratedterminals.terminal_storage.tooltip.quantity",
                formatQuantity(instance))
                .withStyle(ChatFormatting.DARK_GRAY);
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
    public int throwIntoWorld(IIngredientComponentStorage<T, M> storage, T maxInstance, Player player);

    /**
     * Insert as much as possible from the given instance prototype into the container.
     * @param storage The storage to insert to.
     * @param container The container to extract from.
     * @param containerSlotStart The container slot to start from.
     * @param containerSlotEnd The container slot to end at (exclusive).
     * @param instance The instance to move.
     */
    public default void insertMaxIntoContainer(IIngredientComponentStorage<T, M> storage, AbstractContainerMenu container,
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
    public T insertIntoContainer(IIngredientComponentStorage<T, M> storage, AbstractContainerMenu container, int containerSlot, T maxInstance, @Nullable Player player, boolean transferFullSelection);

    /**
     * Simulate {@link #insertMaxIntoContainer(IIngredientComponentStorage, AbstractContainerMenu, int, int, Object)}
     * against the client-side container, so that its effect can be shown before the server confirms it.
     *
     * The container is modified, the storage is not, as the client has no storage to modify.
     *
     * This is part of this capability, and not a helper next to its only caller,
     * so that a handler whose movements can not run client-side can opt out of being predicted
     * by returning an empty instance here.
     *
     * @param container The client-side container to insert to.
     * @param containerSlotStart The container slot to start from.
     * @param containerSlotEnd The container slot to end at (exclusive).
     * @param instance The instance to move.
     * @param availableQuantity The quantity that is expected to be available in the storage.
     * @return The instance quantity that would be moved.
     */
    public default T predictInsertMaxIntoContainer(AbstractContainerMenu container, int containerSlotStart,
                                                   int containerSlotEnd, T instance, long availableQuantity) {
        return predictMovement(availableQuantity, instance, (storage, movedInstance) ->
                insertMaxIntoContainer(storage, container, containerSlotStart, containerSlotEnd, movedInstance));
    }

    /**
     * Simulate {@link #insertIntoContainer(IIngredientComponentStorage, AbstractContainerMenu, int, Object, Player, boolean)}
     * against the client-side container, so that its effect can be shown before the server confirms it.
     *
     * The container is modified, the storage is not, as the client has no storage to modify.
     * No player is passed, so that the container slot contents are never picked up by the prediction.
     * The server may still do so, in which case the prediction simply moves nothing.
     *
     * @param container The client-side container to insert to.
     * @param containerSlot The container slot to insert to.
     * @param maxInstance The instance to move.
     * @param transferFullSelection If the selected stack should be moved fully.
     * @param availableQuantity The quantity that is expected to be available in the storage.
     * @return The instance quantity that would be moved.
     */
    public default T predictInsertIntoContainer(AbstractContainerMenu container, int containerSlot, T maxInstance,
                                                boolean transferFullSelection, long availableQuantity) {
        return predictMovement(availableQuantity, maxInstance, (storage, movedInstance) ->
                insertIntoContainer(storage, container, containerSlot, movedInstance, null, transferFullSelection));
    }

    /**
     * Simulate {@link #extractMaxFromContainerSlot(IIngredientComponentStorage, AbstractContainerMenu, int, Inventory, int)}
     * against the client-side container, so that its effect can be shown before the server confirms it.
     *
     * The container is modified, the storage is not, as the client has no storage to modify.
     *
     * @param container The client-side container to extract from.
     * @param containerSlot The container slot to extract from.
     * @param playerInventory The active player inventory.
     * @param limit The max limit. -1 is no limit.
     * @return The instance quantity that would be moved.
     */
    public default T predictExtractMaxFromContainerSlot(AbstractContainerMenu container, int containerSlot,
                                                        Inventory playerInventory, int limit) {
        IIngredientCollapsedCollectionMutable<T, M> collection = IngredientCollectionHelpers
                .createCollapsedCollection(getComponent());
        extractMaxFromContainerSlot(new IngredientComponentStorageCollectionWrapper<>(collection),
                container, containerSlot, playerInventory, limit);
        return Iterables.getFirst(collection, getComponent().getMatcher().getEmptyInstance());
    }

    /**
     * Run the given movement against a storage that holds the given available quantity of the given instance,
     * and determine how much was taken out of it.
     *
     * The storage that is simulated has no rate limit, while the network may have one,
     * in which case the prediction moves more than the server will.
     * The client can not know that limit, so such a prediction is left to expire.
     */
    private T predictMovement(long availableQuantity, T instance,
                              BiConsumer<IIngredientComponentStorage<T, M>, T> movement) {
        IIngredientMatcher<T, M> matcher = getComponent().getMatcher();
        if (availableQuantity <= 0) {
            return matcher.getEmptyInstance();
        }
        IIngredientCollapsedCollectionMutable<T, M> collection = IngredientCollectionHelpers
                .createCollapsedCollection(getComponent());
        collection.add(matcher.withQuantity(instance, availableQuantity));
        // The movement may modify the instance it is given, so it never gets the caller's instance
        movement.accept(new IngredientComponentStorageCollectionWrapper<>(collection), matcher.copy(instance));
        return matcher.withQuantity(instance, availableQuantity - collection.getQuantity(instance));
    }

    /**
     * Move the ingredient in the active player stack to the storage.
     * @param storage The storage to insert to.
     * @param container
     * @param playerInventory The player inventory to extract from.
     * @param moveQuantityPlayerSlot The player stack quantity that should be extracted.
     */
    public void extractActiveStackFromPlayerInventory(IIngredientComponentStorage<T, M> storage, AbstractContainerMenu container, Inventory playerInventory, long moveQuantityPlayerSlot);

    /**
     * Move as much as possible from the given container slot into the storage.
     * @param storage The storage to insert to.
     * @param container The container to insert to.
     * @param containerSlot The container slot to insert to.
     * @param playerInventory The active player inventory.
     * @param limit The max limit. -1 is no limit.
     */
    public void extractMaxFromContainerSlot(IIngredientComponentStorage<T, M> storage, AbstractContainerMenu container, int containerSlot, Inventory playerInventory, int limit);

    /**
     * Get the quantity in the active player stack.
     * @param playerInventory The player inventory.
     * @param container
     * @return The quantity.
     */
    public long getActivePlayerStackQuantity(Inventory playerInventory, AbstractContainerMenu container);

    /**
     * Drain the given quantity from the active player stack.
     * This will typically only be called client-side, and later confirmed by the server.
     * @param playerInventory The player inventory.
     * @param container
     * @param quantity The quantity to drain.
     */
    public void drainActivePlayerStackQuantity(Inventory playerInventory, AbstractContainerMenu container, long quantity);

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
