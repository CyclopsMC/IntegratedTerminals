package org.cyclops.integratedterminals.core.terminalstorage.crafting;

import net.minecraft.nbt.NBTTagCompound;
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

    public static NBTTagCompound serialize(HandlerWrappedTerminalCraftingPlan craftingPlan) {
        ITerminalStorageTabIngredientCraftingHandler handler = craftingPlan.getHandler();
        NBTTagCompound tag = handler.serializeCraftingPlan(craftingPlan.getCraftingPlan());
        tag.setString("craftingPlanHandler", handler.getId().toString());
        return tag;
    }

    public static HandlerWrappedTerminalCraftingPlan deserialize(NBTTagCompound tag) {
        if (!tag.hasKey("craftingPlanHandler", Constants.NBT.TAG_STRING)) {
            throw new IllegalArgumentException("Could not find a craftingPlanHandler entry in the given tag");
        }
        String handlerId = tag.getString("craftingPlanHandler");
        ITerminalStorageTabIngredientCraftingHandler handler = TerminalStorageTabIngredientCraftingHandlers.REGISTRY
                .getHandler(new ResourceLocation(handlerId));
        ITerminalCraftingPlan craftingPlan = handler.deserializeCraftingPlan(tag);
        return new HandlerWrappedTerminalCraftingPlan(handler, craftingPlan);
    }

}
