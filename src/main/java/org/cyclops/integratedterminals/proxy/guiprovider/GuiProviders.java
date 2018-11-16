package org.cyclops.integratedterminals.proxy.guiprovider;

import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.core.client.gui.ExtendedGuiHandler;

/**
 * @author rubensworks
 */
public class GuiProviders {

    public static int GUI_TERMINAL_STORAGE_CRAFTNG_OPTION_AMOUNT;
    /**
     * This is a variant of the default terminal storage gui constructor (which is register by ID).
     * This alternative allows additional init data to be passed to the constructor.
     */
    public static int GUI_TERMINAL_STORAGE_INIT;

    public static void register() {
        IntegratedTerminals._instance.getGuiHandler().registerGUI(
                new GuiProviderTerminalStorageCraftingOptionAmount(
                        GUI_TERMINAL_STORAGE_CRAFTNG_OPTION_AMOUNT = Helpers.getNewId(IntegratedTerminals._instance, Helpers.IDType.GUI),
                        IntegratedTerminals._instance), ExtendedGuiHandler.CRAFTING_OPTION);

        IntegratedTerminals._instance.getGuiHandler().registerGUI(
                new GuiProviderTerminalStorageInit(
                        GUI_TERMINAL_STORAGE_INIT = Helpers.getNewId(IntegratedTerminals._instance, Helpers.IDType.GUI),
                        IntegratedTerminals._instance), ExtendedGuiHandler.TERMINAL_STORAGE);
    }

}
