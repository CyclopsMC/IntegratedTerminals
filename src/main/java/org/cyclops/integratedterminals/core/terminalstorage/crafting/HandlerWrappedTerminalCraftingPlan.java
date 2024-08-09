package org.cyclops.integratedterminals.core.terminalstorage.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlanFlat;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;


/**
 * Data holder for {@link ITerminalCraftingPlan} wrapped with its handler.
 * @author rubensworks
 */
public class HandlerWrappedTerminalCraftingPlan {

    private final ITerminalStorageTabIngredientCraftingHandler handler;
    private final ITerminalCraftingPlanFlat craftingPlanFlat;

    public HandlerWrappedTerminalCraftingPlan(ITerminalStorageTabIngredientCraftingHandler handler,
                                              ITerminalCraftingPlanFlat craftingPlanFlat) {
        this.handler = handler;
        this.craftingPlanFlat = craftingPlanFlat;
    }

    public ITerminalStorageTabIngredientCraftingHandler getHandler() {
        return handler;
    }

    public ITerminalCraftingPlanFlat getCraftingPlanFlat() {
        return craftingPlanFlat;
    }

    public static CompoundTag serialize(HolderLookup.Provider lookupProvider, HandlerWrappedTerminalCraftingPlan craftingPlan) {
        ITerminalStorageTabIngredientCraftingHandler handler = craftingPlan.getHandler();
        CompoundTag tag = new CompoundTag();
        tag.putString("craftingPlanHandler", handler.getId().toString());
        tag.put("flatPlan", handler.serializeCraftingPlanFlat(lookupProvider, craftingPlan.getCraftingPlanFlat()));

        return tag;
    }

    public static HandlerWrappedTerminalCraftingPlan deserialize(HolderLookup.Provider lookupProvider, CompoundTag tag) {
        if (!tag.contains("craftingPlanHandler", Tag.TAG_STRING)) {
            throw new IllegalArgumentException("Could not find a craftingPlanHandler entry in the given tag");
        }
        String handlerId = tag.getString("craftingPlanHandler");
        ITerminalStorageTabIngredientCraftingHandler handler = TerminalStorageTabIngredientCraftingHandlers.REGISTRY
                .getHandler(ResourceLocation.parse(handlerId));
        ITerminalCraftingPlanFlat craftingPlanFlat = handler.deserializeCraftingPlanFlat(lookupProvider, tag.getCompound("flatPlan"));

        return new HandlerWrappedTerminalCraftingPlan(handler, craftingPlanFlat);
    }

}
