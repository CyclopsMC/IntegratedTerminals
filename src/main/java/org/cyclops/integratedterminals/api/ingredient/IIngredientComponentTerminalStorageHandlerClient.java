package org.cyclops.integratedterminals.api.ingredient;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.core.terminalstorage.query.SearchMode;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author rubensworks
 */
public interface IIngredientComponentTerminalStorageHandlerClient<T, M> {

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
    public void drawInstance(GuiGraphicsExtractor guiGraphics, T instance, long maxQuantity, @Nullable String label, AbstractContainerScreen gui, ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int x, int y,
                             int mouseX, int mouseY, @Nullable List<Component> additionalTooltipLines);

    /**
     * Get a predicate for matching instances that apply to the given query string.
     * @param searchMode The mode to search under
     * @param query A query string.
     * @return An instance matcher.
     */
    public Predicate<T> getInstanceFilterPredicate(SearchMode searchMode, String query);

}
