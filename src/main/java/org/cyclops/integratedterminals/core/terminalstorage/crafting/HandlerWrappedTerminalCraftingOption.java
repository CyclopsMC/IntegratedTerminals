package org.cyclops.integratedterminals.core.terminalstorage.crafting;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingOption;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;

/**
 * Data holder for {@link ITerminalCraftingOption} wrapped with its handler.
 * @param <T> The instance type.
 * @author rubensworks
 */
public class HandlerWrappedTerminalCraftingOption<T> {

    private final ITerminalStorageTabIngredientCraftingHandler handler;
    private final ITerminalCraftingOption<T> craftingOption;

    public HandlerWrappedTerminalCraftingOption(ITerminalStorageTabIngredientCraftingHandler handler,
                                                ITerminalCraftingOption<T> craftingOption) {
        this.handler = handler;
        this.craftingOption = craftingOption;
    }

    public ITerminalStorageTabIngredientCraftingHandler getHandler() {
        return handler;
    }

    public ITerminalCraftingOption<T> getCraftingOption() {
        return craftingOption;
    }

    public static <T> void serialize(ValueOutput valueOutput, HandlerWrappedTerminalCraftingOption<T> craftingOption) {
        ITerminalStorageTabIngredientCraftingHandler handler = craftingOption.getHandler();
        handler.serializeCraftingOption(valueOutput, craftingOption.getCraftingOption());
        valueOutput.putString("craftingOptionHandler", handler.getId().toString());
    }

    public static <T, M> HandlerWrappedTerminalCraftingOption<T> deserialize(ValueInput valueInput, IngredientComponent<T, M> ingredientComponent) {
        String handlerId = valueInput.getString("craftingOptionHandler").orElseThrow();
        ITerminalStorageTabIngredientCraftingHandler handler = TerminalStorageTabIngredientCraftingHandlers.REGISTRY
                .getHandler(Identifier.parse(handlerId));
        ITerminalCraftingOption<T> craftingOption = handler.deserializeCraftingOption(valueInput, ingredientComponent);
        return new HandlerWrappedTerminalCraftingOption<>(handler, craftingOption);
    }

}
