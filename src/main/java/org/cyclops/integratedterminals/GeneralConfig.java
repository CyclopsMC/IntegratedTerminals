package org.cyclops.integratedterminals;

import net.neoforged.fml.config.ModConfig;
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
    public static int terminalStoragePacketMaxInstances = 512;
    @ConfigurableProperty(category = "core", comment = "The maximum number of terminal storage crafting recipes that can be sent in a single packet. Reduce this when you have packet overflows.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static int terminalStoragePacketMaxRecipes = 128;
    @ConfigurableProperty(category = "core", comment = "If crafting plans should default to the tree-based view. If false, it will default to the flattened view.", isCommandable = true, configLocation = ModConfig.Type.COMMON)
    public static boolean terminalStorageDefaultToCraftingPlanTree = false;
    @ConfigurableProperty(category = "core", comment = "The limit for the number of leaves in a tree-based crafting plan after which it won't be sent to the client anymore.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static int terminalStorageMaxTreePlanSize = 64;

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
    public static boolean craftingPlannerEnableMultithreading = false;

    @ConfigurableProperty(category = "general", comment = "The base energy usage for the crafting terminal.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int terminalCraftingBaseConsumption = 1;
    @ConfigurableProperty(category = "general", comment = "The base energy usage for the storage terminal.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int terminalStorageBaseConsumption = 2;

    @ConfigurableProperty(category = "general", comment = "If the search box and button states should be synchronized between the item storage and crafting tabs.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static boolean syncItemStorageAndCraftingTabStates = true;

    @ConfigurableProperty(category = "general", comment = "If shift-clicking on the crafting terminal's crafting result slot should only produce a single result.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static boolean shiftClickCraftingResultLimit = false;

    @ConfigurableProperty(category = "general", comment = "The number of rows in the small scale of the storage terminal.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static int guiStorageScaleSmallRows = 5;
    @ConfigurableProperty(category = "general", comment = "The number of columns in the small scale of the storage terminal.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static int guiStorageScaleSmallColumns = 9;
    @ConfigurableProperty(category = "general", comment = "The number of rows in the medium scale of the storage terminal.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static int guiStorageScaleMediumRows = 7;
    @ConfigurableProperty(category = "general", comment = "The number of columns in the medium scale of the storage terminal.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static int guiStorageScaleMediumColumns = 10;
    @ConfigurableProperty(category = "general", comment = "The number of rows in the large scale of the storage terminal.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static int guiStorageScaleLargeRows = 9;
    @ConfigurableProperty(category = "general", comment = "The number of columns in the large scale of the storage terminal.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static int guiStorageScaleLargeColumns = 11;
    @ConfigurableProperty(category = "general", comment = "The number of columns in the height-based scale of the storage terminal.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static int guiStorageScaleHeightColumns = 9;
    @ConfigurableProperty(category = "general", comment = "The number of rows in the width-based scale of the storage terminal.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static int guiStorageScaleWidthRows = 5;
    @ConfigurableProperty(category = "general", comment = "The maximum number of rows in when scaling the storage terminal.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static int guiStorageScaleMaxRows = 20;
    @ConfigurableProperty(category = "general", comment = "The maximum number of columns in when scaling the storage terminal.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static int guiStorageScaleMaxColumns = 32;
    @ConfigurableProperty(category = "general", comment = "If the crafting grid should always be shown centrally, and not be responsive based on screen size.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static boolean guiStorageForceCraftingGridCenter = false;

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
