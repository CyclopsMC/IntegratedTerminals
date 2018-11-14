package org.cyclops.integratedterminals.core.terminalstorage.crafting;

import com.google.common.collect.Maps;
import net.minecraft.util.ResourceLocation;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandlerRegistry;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

/**
 * Implementation of {@link ITerminalStorageTabIngredientCraftingHandlerRegistry}.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientCraftingHandlerRegistry
        implements ITerminalStorageTabIngredientCraftingHandlerRegistry {

    private static TerminalStorageTabIngredientCraftingHandlerRegistry INSTANCE = new TerminalStorageTabIngredientCraftingHandlerRegistry();

    private final Map<ResourceLocation, ITerminalStorageTabIngredientCraftingHandler> handlers = Maps.newHashMap();

    private TerminalStorageTabIngredientCraftingHandlerRegistry() {

    }

    public static TerminalStorageTabIngredientCraftingHandlerRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public <T extends ITerminalStorageTabIngredientCraftingHandler> T register(T handler) {
        handlers.put(handler.getId(), handler);
        return handler;
    }

    @Override
    public Collection<ITerminalStorageTabIngredientCraftingHandler> getHandlers() {
        return handlers.values();
    }

    @Nullable
    @Override
    public ITerminalStorageTabIngredientCraftingHandler getHandler(ResourceLocation id) {
        return handlers.get(id);
    }
}
