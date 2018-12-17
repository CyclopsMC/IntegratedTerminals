package org.cyclops.integratedterminals.proxy.guiprovider;

import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.core.client.gui.ExtendedGuiHandler;

/**
 * @author rubensworks
 */
public class GuiProviders {

    public static int ID_GUI_TERMINAL_STORAGE_CRAFTNG_OPTION_AMOUNT;
    public static IGuiContainerProvider GUI_TERMINAL_STORAGE_CRAFTNG_OPTION_AMOUNT;
    public static int ID_GUI_TERMINAL_STORAGE_CRAFTNG_PLAN;
    public static IGuiContainerProvider GUI_TERMINAL_STORAGE_CRAFTNG_PLAN;
    /**
     * This is a variant of the default terminal storage gui constructor (which is register by ID).
     * This alternative allows additional init data to be passed to the constructor.
     */
    public static int ID_GUI_TERMINAL_STORAGE_INIT;

    public static void register() {
        IntegratedTerminals._instance.getGuiHandler().registerGUI(
                GUI_TERMINAL_STORAGE_CRAFTNG_OPTION_AMOUNT = new GuiProviderTerminalStorageCraftingOptionAmount(
                        ID_GUI_TERMINAL_STORAGE_CRAFTNG_OPTION_AMOUNT = Helpers.getNewId(IntegratedTerminals._instance, Helpers.IDType.GUI),
                        IntegratedTerminals._instance), ExtendedGuiHandler.CRAFTING_OPTION);

        IntegratedTerminals._instance.getGuiHandler().registerGUI(
                GUI_TERMINAL_STORAGE_CRAFTNG_PLAN = new GuiProviderTerminalStorageCraftingPlan(
                        ID_GUI_TERMINAL_STORAGE_CRAFTNG_PLAN = Helpers.getNewId(IntegratedTerminals._instance, Helpers.IDType.GUI),
                        IntegratedTerminals._instance), ExtendedGuiHandler.CRAFTING_OPTION);

        IntegratedTerminals._instance.getGuiHandler().registerGUI(
                new GuiProviderTerminalStorageInit(
                        ID_GUI_TERMINAL_STORAGE_INIT = Helpers.getNewId(IntegratedTerminals._instance, Helpers.IDType.GUI),
                        IntegratedTerminals._instance), ExtendedGuiHandler.TERMINAL_STORAGE);
    }

}
