package org.cyclops.integratedterminals;

import net.minecraftforge.fml.config.ModConfig;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.DummyConfig;
import org.cyclops.cyclopscore.tracking.Analytics;
import org.cyclops.cyclopscore.tracking.Versions;

/**
 * A config with general options for this mod.
 * @author rubensworks
 *
 */
public class GeneralConfig extends DummyConfig {

    @ConfigurableProperty(category = "core", comment = "If an anonymous mod startup analytics request may be sent to our analytics service.")
    public static boolean analytics = true;

    @ConfigurableProperty(category = "core", comment = "If the version checker should be enabled.")
    public static boolean versionChecker = true;

    @ConfigurableProperty(category = "core", comment = "The maximum number of terminal storage instances that can be sent in a single packet. Reduce this when you have packet overflows.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static int terminalStoragePacketMaxInstances = 1024;

    @ConfigurableProperty(category = "machine", comment = "The number of items that should be selected when clicking on an item in the storage terminal.", isCommandable = true)
    public static int guiStorageItemInitialQuantity = 64;

    @ConfigurableProperty(category = "machine", comment = "The number of items that should be removed when right-clicking when an item is selected in the storage terminal.", isCommandable = true)
    public static int guiStorageItemIncrementalQuantity = 1;

    @ConfigurableProperty(category = "machine", comment = "The number of items that should be selected when clicking on a fluid in the storage terminal.", isCommandable = true)
    public static int guiStorageFluidInitialQuantity = 100000;

    @ConfigurableProperty(category = "machine", comment = "The number of items that should be removed when right-clicking when a fluid is selected in the storage terminal.", isCommandable = true)
    public static int guiStorageFluidIncrementalQuantity = 1000;

    @ConfigurableProperty(category = "machine", comment = "The number of items that should be selected when clicking on energy in the storage terminal.", isCommandable = true)
    public static int guiStorageEnergyInitialQuantity = 100000;

    @ConfigurableProperty(category = "machine", comment = "The number of items that should be removed when right-clicking when energy is selected in the storage terminal.", isCommandable = true)
    public static int guiStorageEnergyIncrementalQuantity = 1000;

    @ConfigurableProperty(category = "machine", comment = "The update frequency in milliseconds for the crafting jobs gui.", isCommandable = true)
    public static int guiTerminalCraftingJobsUpdateFrequency = 1000;

    @ConfigurableProperty(category = "core", comment = "The number of threads that the crafting plan calculator can use.", minimalValue = 1, requiresMcRestart = true, configLocation = ModConfig.Type.SERVER)
    public static int craftingPlannerThreads = 2;

    @ConfigurableProperty(category = "core", comment = "If the crafting planners can work on separate thread.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static boolean craftingPlannerEnableMultithreading = true;

    @ConfigurableProperty(category = "general", comment = "The base energy usage for the crafting terminal.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int terminalCraftingBaseConsumption = 1;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the storage terminal.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int terminalStorageBaseConsumption = 2;

    @ConfigurableProperty(category = "general", comment = "If the search box and button states should be synchronized between the item storage and crafting tabs.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static boolean syncItemStorageAndCraftingTabStates = true;

    @ConfigurableProperty(category = "general", comment = "If shift-clicking on the crafting terminal's crafting result slot should only produce a single result.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static boolean shiftClickCraftingResultLimit = false;

    public GeneralConfig() {
        super(IntegratedTerminals._instance, "general");
    }

    @Override
    public void onRegistered() {
        if(analytics) {
            Analytics.registerMod(getMod(), Reference.GA_TRACKING_ID);
        }
        if(versionChecker) {
            Versions.registerMod(getMod(), IntegratedTerminals._instance, Reference.VERSION_URL);
        }
    }

}
