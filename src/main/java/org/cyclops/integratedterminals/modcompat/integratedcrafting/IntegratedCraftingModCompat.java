package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.cyclops.integratedterminals.Reference;

/**
 * @author rubensworks
 */
public class IntegratedCraftingModCompat implements IModCompat {

    @Override
    public void onInit(Step initStep) {
        if(initStep == Step.PREINIT) {
            TerminalStorageCraftingHooks.register();
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
