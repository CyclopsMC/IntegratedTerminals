package org.cyclops.integratedterminals.proxy.guiprovider;

import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.core.client.gui.ExtendedGuiHandler;

/**
 * @author rubensworks
 */
public class GuiProviders {

    public static int GUI_TERMINAL_STORAGE_CRAFTNG_OPTION_AMOUNT;

    public static void register() {
        IntegratedTerminals._instance.getGuiHandler().registerGUI(
                new GuiProviderTerminalStorageCraftingOptionAmount(
                        GUI_TERMINAL_STORAGE_CRAFTNG_OPTION_AMOUNT = Helpers.getNewId(IntegratedTerminals._instance, Helpers.IDType.GUI),
                        IntegratedTerminals._instance), ExtendedGuiHandler.CRAFTING_OPTION);
    }

}
