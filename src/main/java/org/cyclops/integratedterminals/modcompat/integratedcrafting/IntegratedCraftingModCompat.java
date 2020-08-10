package org.cyclops.integratedterminals.modcompat.integratedcrafting;

import org.cyclops.cyclopscore.modcompat.ICompatInitializer;
import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.cyclops.integratedterminals.Reference;

/**
 * @author rubensworks
 */
public class IntegratedCraftingModCompat implements IModCompat {

    @Override
    public String getId() {
        return Reference.MOD_INTEGRATECRAFTING;
    }

    @Override
    public boolean isEnabledDefault() {
        return true;
    }

    @Override
    public String getComment() {
        return "Crafting Terminal and Storage Terminal crafting actions.";
    }

    @Override
    public ICompatInitializer createInitializer() {
        return new IntegratedCraftingModCompatInitializer();
    }

}
