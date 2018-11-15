package org.cyclops.integratedterminals.core.client.gui;

import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;

/**
 * @author rubensworks
 */
public class CraftingOptionGuiData<T, M> {

    private final IngredientComponent<T, M> component;
    private final int channel;
    private final HandlerWrappedTerminalCraftingOption<T> craftingOption;
    private final int amount;

    public CraftingOptionGuiData(IngredientComponent<T, M> component, int channel, HandlerWrappedTerminalCraftingOption<T> craftingOption, int amount) {
        this.component = component;
        this.channel = channel;
        this.craftingOption = craftingOption;
        this.amount = amount;
    }

    public IngredientComponent<T, M> getComponent() {
        return component;
    }

    public int getChannel() {
        return channel;
    }

    public HandlerWrappedTerminalCraftingOption<T> getCraftingOption() {
        return craftingOption;
    }

    public int getAmount() {
        return amount;
    }
}
