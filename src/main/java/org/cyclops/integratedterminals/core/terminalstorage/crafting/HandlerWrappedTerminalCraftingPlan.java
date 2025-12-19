package org.cyclops.integratedterminals.core.terminalstorage.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import org.cyclops.cyclopscore.helper.IModHelpers;
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
        tag.put("flatPlan", IModHelpers.get().getMinecraftHelpers().valueOutputToNbt(o -> handler.serializeCraftingPlanFlat(o, craftingPlan.getCraftingPlanFlat()), lookupProvider));
        return tag;
    }

    public static HandlerWrappedTerminalCraftingPlan deserialize(HolderLookup.Provider lookupProvider, CompoundTag tag) {
        String handlerId = tag.getString("craftingPlanHandler").orElseThrow();
        ITerminalStorageTabIngredientCraftingHandler handler = TerminalStorageTabIngredientCraftingHandlers.REGISTRY
                .getHandler(Identifier.parse(handlerId));
        ITerminalCraftingPlanFlat craftingPlanFlat = IModHelpers.get().getMinecraftHelpers().valueInputFromNbt(tag.getCompound("flatPlan").orElseThrow(), lookupProvider, handler::deserializeCraftingPlanFlat);

        return new HandlerWrappedTerminalCraftingPlan(handler, craftingPlanFlat);
    }

}
