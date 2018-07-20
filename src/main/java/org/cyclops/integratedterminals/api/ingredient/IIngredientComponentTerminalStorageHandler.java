package org.cyclops.integratedterminals.api.ingredient;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integratedterminals.client.gui.container.GuiTerminalStorage;
import org.cyclops.integratedterminals.inventory.container.query.SearchMode;

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
     * @return The item that can be used to visually represent this ingredient component type.
     */
    public ItemStack getIcon();

    /**
     * Draw the given instance in the given gui.
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
     * @param channel The current channel.
     */
    @SideOnly(Side.CLIENT)
    public void drawInstance(T instance, long maxQuantity, @Nullable String label, GuiContainer gui, GuiTerminalStorage.DrawLayer layer, float partialTick, int x, int y,
                             int mouseX, int mouseY, int channel);

    /**
     * Show the quantity of the given instance on the second tooltip line.
     * @param lines Tooltip lines
     * @param instance An instance.
     */
    public default void addQuantityTooltip(List<String> lines, T instance) {
        String line = TextFormatting.DARK_GRAY.toString() + L10NHelpers.localize(
                "gui.integratedterminals.terminal_storage.tooltip.quantity",
                formatQuantity(instance));
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
     * @return An ingredient.
     */
    public T getInstance(ItemStack itemStack);

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
    public int throwIntoWorld(IIngredientComponentStorage<T, M> storage, T maxInstance, EntityPlayer player);

    /**
     * Insert as much as possible from the given instance prototype into the player inventory.
     * @param storage The storage to insert to.
     * @param playerInventory The player storage to extract from.
     * @param instance The instance to move.
     */
    public default void insertMaxIntoPlayerInventory(IIngredientComponentStorage<T, M> storage, InventoryPlayer playerInventory, T instance) {
        IIngredientMatcher<T, M> matcher = storage.getComponent().getMatcher();
        T toAdd = instance;
        PlayerMainInvWrapper inv = new PlayerMainInvWrapper(playerInventory);
        int slot = 0;
        while (!matcher.isEmpty(toAdd) && slot < inv.getSlots()) {
            insertIntoPlayerInventory(storage, playerInventory, slot++, toAdd);
        }
    }

    /**
     * Insert the given instance into the player inventory.
     * @param storage The storage to extract from.
     * @param playerInventory The player inventory to insert to.
     * @param playerSlot The player slot to insert to.
     * @param maxInstance The instance to move.
     * @return The instance quantity that was moved.
     */
    public T insertIntoPlayerInventory(IIngredientComponentStorage<T, M> storage, InventoryPlayer playerInventory, int playerSlot, T maxInstance);

    /**
     * Move the ingredient in the active player stack to the storage.
     * @param storage The storage to insert to.
     * @param playerInventory The player inventory to extract from.
     * @param moveQuantityPlayerSlot The player stack quantity that should be extracted.
     */
    public void extractActiveStackFromPlayerInventory(IIngredientComponentStorage<T, M> storage, InventoryPlayer playerInventory, long moveQuantityPlayerSlot);

    /**
     * Move as much as possible from the given player slot into the storage.
     * @param storage The storage to insert to.
     * @param playerInventory The player inventory to extract from.
     * @param playerSlot The player slot to extract from.
     */
    public void extractMaxFromPlayerInventorySlot(IIngredientComponentStorage<T, M> storage, InventoryPlayer playerInventory, int playerSlot);

    /**
     * Get the quantity in the active player stack.
     * @param playerInventory The player inventory.
     * @return The quantity.
     */
    public long getActivePlayerStackQuantity(InventoryPlayer playerInventory);

    /**
     * Drain the given quantity from the active player stack.
     * This will typically only be called client-side, and later confirmed by the server.
     * @param playerInventory The player inventory.
     * @param quantity The quantity to drain.
     */
    public void drainActivePlayerStackQuantity(InventoryPlayer playerInventory, long quantity);

    /**
     * Get a predicate for matching instances that apply to the given query string.
     * @param searchMode The mode to search under
     * @param query A query string.
     * @return An instance matcher.
     */
    public Predicate<T> getInstanceFilterPredicate(SearchMode searchMode, String query);

    /**
     * @return The available sorters.
     */
    public Collection<IIngredientInstanceSorter<T>> getInstanceSorters();

}
