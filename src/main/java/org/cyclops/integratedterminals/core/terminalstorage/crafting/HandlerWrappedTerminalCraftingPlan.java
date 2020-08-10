package org.cyclops.integratedterminals.core.terminalstorage.crafting;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
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

    public static CompoundNBT serialize(HandlerWrappedTerminalCraftingPlan craftingPlan) {
        ITerminalStorageTabIngredientCraftingHandler handler = craftingPlan.getHandler();
        CompoundNBT tag = handler.serializeCraftingPlan(craftingPlan.getCraftingPlan());
        tag.putString("craftingPlanHandler", handler.getId().toString());
        return tag;
    }

    public static HandlerWrappedTerminalCraftingPlan deserialize(CompoundNBT tag) {
        if (!tag.contains("craftingPlanHandler", Constants.NBT.TAG_STRING)) {
            throw new IllegalArgumentException("Could not find a craftingPlanHandler entry in the given tag");
        }
        String handlerId = tag.getString("craftingPlanHandler");
        ITerminalStorageTabIngredientCraftingHandler handler = TerminalStorageTabIngredientCraftingHandlers.REGISTRY
                .getHandler(new ResourceLocation(handlerId));
        ITerminalCraftingPlan craftingPlan = handler.deserializeCraftingPlan(tag);
        return new HandlerWrappedTerminalCraftingPlan(handler, craftingPlan);
    }

}
