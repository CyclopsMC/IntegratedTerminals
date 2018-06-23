package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.client.gui.inventory.GuiContainer;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentViewHandler;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageSlot;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;

/**
 * An ingredient slot.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class TerminalStorageSlotIngredient<T, M> implements ITerminalStorageSlot {

    private final IIngredientComponentViewHandler<T, M> ingredientComponentViewHandler;
    private final T instance;

    public TerminalStorageSlotIngredient(IIngredientComponentViewHandler<T, M> ingredientComponentViewHandler, T instance) {
        this.ingredientComponentViewHandler = ingredientComponentViewHandler;
        this.instance = instance;
    }

    @Override
    public void drawGuiContainerLayer(GuiContainer gui, IIngredientComponentViewHandler.DrawLayer layer,
                                      float partialTick, int x, int y, int mouseX, int mouseY,
                                      ITerminalStorageTabClient tab, int channel) {
        ingredientComponentViewHandler.drawInstanceSlot(instance, gui, layer, partialTick, x, y, mouseX, mouseY, tab, channel);
    }
}
