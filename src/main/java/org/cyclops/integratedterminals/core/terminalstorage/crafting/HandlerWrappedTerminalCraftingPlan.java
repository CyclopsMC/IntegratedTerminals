package org.cyclops.integratedterminals.core.terminalstorage.crafting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;

/**
 * Data holder for {@link ITerminalCraftingPlan} wrapped with its handler.
 * @author rubensworks
 */
public class HandlerWrappedTerminalCraftingPlan {

    private final ITerminalStorageTabIngredientCraftingHandler handler;
    private final ITerminalCraftingPlan craftingPlan;

    public HandlerWrappedTerminalCraftingPlan(ITerminalStorageTabIngredientCraftingHandler handler,
                                              ITerminalCraftingPlan craftingPlan) {
        this.handler = handler;
        this.craftingPlan = craftingPlan;
    }

    public ITerminalStorageTabIngredientCraftingHandler getHandler() {
        return handler;
    }

    public ITerminalCraftingPlan getCraftingPlan() {
        return craftingPlan;
    }

    public static CompoundTag serialize(HandlerWrappedTerminalCraftingPlan craftingPlan) {
        ITerminalStorageTabIngredientCraftingHandler handler = craftingPlan.getHandler();
        CompoundTag tag = handler.serializeCraftingPlan(craftingPlan.getCraftingPlan());
        tag.putString("craftingPlanHandler", handler.getId().toString());
        return tag;
    }

    public static HandlerWrappedTerminalCraftingPlan deserialize(CompoundTag tag) {
        if (!tag.contains("craftingPlanHandler", Tag.TAG_STRING)) {
            throw new IllegalArgumentException("Could not find a craftingPlanHandler entry in the given tag");
        }
        String handlerId = tag.getString("craftingPlanHandler");
        ITerminalStorageTabIngredientCraftingHandler handler = TerminalStorageTabIngredientCraftingHandlers.REGISTRY
                .getHandler(ResourceLocation.parse(handlerId));
        ITerminalCraftingPlan craftingPlan = handler.deserializeCraftingPlan(tag);
        return new HandlerWrappedTerminalCraftingPlan(handler, craftingPlan);
    }

}
