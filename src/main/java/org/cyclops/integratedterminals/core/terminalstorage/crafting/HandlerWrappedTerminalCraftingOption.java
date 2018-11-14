package org.cyclops.integratedterminals.core.terminalstorage.crafting;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
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

    public static <T> NBTTagCompound serialize(HandlerWrappedTerminalCraftingOption<T> craftingOption) {
        ITerminalStorageTabIngredientCraftingHandler handler = craftingOption.getHandler();
        NBTTagCompound tag = handler.serializeCraftingOption(craftingOption.getCraftingOption());
        tag.setString("craftingOptionHandler", handler.getId().toString());
        return tag;
    }

    public static <T, M> HandlerWrappedTerminalCraftingOption<T> deserialize(IngredientComponent<T, M> ingredientComponent, NBTTagCompound tag) {
        if (!tag.hasKey("craftingOptionHandler", Constants.NBT.TAG_STRING)) {
            throw new IllegalArgumentException("Could not find a craftingOptionHandler entry in the given tag");
        }
        String handlerId = tag.getString("craftingOptionHandler");
        ITerminalStorageTabIngredientCraftingHandler handler = TerminalStorageTabIngredientCraftingHandlers.REGISTRY
                .getHandler(new ResourceLocation(handlerId));
        ITerminalCraftingOption<T> craftingOption = handler.deserializeCraftingOption(ingredientComponent, tag);
        return new HandlerWrappedTerminalCraftingOption<>(handler, craftingOption);
    }

}
