package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.TerminalStorageTabIngredientCraftingHandlers;

/**
 * @author rubensworks
 */
public class IntegratedCraftingModCompat implements IModCompat {

    @Override
    public void onInit(Step initStep) {
        if(initStep == Step.INIT) {
            TerminalStorageTabIngredientCraftingHandlers.REGISTRY.register(
                    new TerminalStorageTabIngredientCraftingHandlerCraftingNetwork());
        }
    }

    @Override
    public String getModID() {
        return Reference.MOD_INTEGRATECRAFTING;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getComment() {
        return "Crafting Terminal and Storage Terminal crafting actions.";
    }

}
