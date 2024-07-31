package org.cyclops.integratedterminals.core.terminalstorage.crafting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlanFlat;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Data holder for {@link ITerminalCraftingPlan} wrapped with its handler.
 * @author rubensworks
 */
public class HandlerWrappedTerminalCraftingPlan {

    private final ITerminalStorageTabIngredientCraftingHandler handler;
    @Nullable
    private final ITerminalCraftingPlan craftingPlan;
    private final ITerminalCraftingPlanFlat craftingPlanFlat;

    public HandlerWrappedTerminalCraftingPlan(ITerminalStorageTabIngredientCraftingHandler handler,
                                              @Nullable ITerminalCraftingPlan craftingPlan,
                                              ITerminalCraftingPlanFlat craftingPlanFlat) {
        this.handler = handler;
        this.craftingPlan = craftingPlan;
        this.craftingPlanFlat = craftingPlanFlat;
    }

    public ITerminalStorageTabIngredientCraftingHandler getHandler() {
        return handler;
    }

    @Nullable
    public ITerminalCraftingPlan getCraftingPlan() {
        return craftingPlan;
    }

    public ITerminalCraftingPlanFlat getCraftingPlanFlat() {
        return craftingPlanFlat;
    }

    public static CompoundTag serialize(HandlerWrappedTerminalCraftingPlan craftingPlan) {
        ITerminalStorageTabIngredientCraftingHandler handler = craftingPlan.getHandler();
        ITerminalCraftingPlan plan = craftingPlan.getCraftingPlan();
        CompoundTag tag = new CompoundTag();
        tag.putString("craftingPlanHandler", handler.getId().toString());

        if (plan != null && !isPlanTooLarge(plan)) {
            tag.put("treePlan", handler.serializeCraftingPlan(craftingPlan.getCraftingPlan()));
        }
        tag.put("flatPlan", handler.serializeCraftingPlanFlat(craftingPlan.getCraftingPlanFlat()));

        return tag;
    }

    public static HandlerWrappedTerminalCraftingPlan deserialize(CompoundTag tag) {
        if (!tag.contains("craftingPlanHandler", Tag.TAG_STRING)) {
            throw new IllegalArgumentException("Could not find a craftingPlanHandler entry in the given tag");
        }
        String handlerId = tag.getString("craftingPlanHandler");
        ITerminalStorageTabIngredientCraftingHandler handler = TerminalStorageTabIngredientCraftingHandlers.REGISTRY
                .getHandler(new ResourceLocation(handlerId));

        ITerminalCraftingPlan craftingPlan = null;
        if (tag.contains("treePlan", Tag.TAG_COMPOUND)) {
            craftingPlan = handler.deserializeCraftingPlan(tag.getCompound("treePlan"));
        }
        ITerminalCraftingPlanFlat craftingPlanFlat = handler.deserializeCraftingPlanFlat(tag.getCompound("flatPlan"));

        return new HandlerWrappedTerminalCraftingPlan(handler, craftingPlan, craftingPlanFlat);
    }

    public static boolean isPlanTooLarge(ITerminalCraftingPlan craftingPlan) {
        return getPlanSize(craftingPlan) > GeneralConfig.terminalStorageMaxTreePlanSize;
    }

    public static int getPlanSize(ITerminalCraftingPlan craftingPlan) {
        List<ITerminalCraftingPlan<?>> deps = craftingPlan.getDependencies();
        if (deps.isEmpty()) {
            return 1;
        } else {
            return deps.stream()
                    .mapToInt(HandlerWrappedTerminalCraftingPlan::getPlanSize)
                    .sum();
        }
    }

}
