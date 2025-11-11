package org.cyclops.integratedterminals;

import org.cyclops.cyclopscore.config.ConfigurablePropertyCommon;
import org.cyclops.cyclopscore.config.ModConfigLocation;
import org.cyclops.cyclopscore.config.extendedconfig.DummyConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * A config with general options for this mod.
 * @author rubensworks
 *
 */
public class GeneralConfig extends DummyConfigCommon<IModBase> {

    @ConfigurablePropertyCommon(category = "core", comment = "If an anonymous mod startup analytics request may be sent to our analytics service.")
    public static boolean analytics = true;

    @ConfigurablePropertyCommon(category = "core", comment = "If the version checker should be enabled.")
    public static boolean versionChecker = true;

    @ConfigurablePropertyCommon(category = "core", comment = "The maximum number of terminal storage instances that can be sent in a single packet. Reduce this when you have packet overflows.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int terminalStoragePacketMaxInstances = 512;
    @ConfigurablePropertyCommon(category = "core", comment = "The maximum number of terminal storage crafting recipes that can be sent in a single packet. Reduce this when you have packet overflows.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int terminalStoragePacketMaxRecipes = 128;
    @ConfigurablePropertyCommon(category = "core", comment = "If crafting plans should default to the tree-based view. If false, it will default to the flattened view.", isCommandable = true, configLocation = ModConfigLocation.COMMON)
    public static boolean terminalStorageDefaultToCraftingPlanTree = false;
    @ConfigurablePropertyCommon(category = "core", comment = "The limit for the number of leaves in a tree-based crafting plan after which it won't be sent to the client anymore.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int terminalStorageMaxTreePlanSize = 64;

    @ConfigurablePropertyCommon(category = "machine", comment = "The number of items that should be selected when clicking on an item in the storage terminal.", isCommandable = true)
    public static int guiStorageItemInitialQuantity = 64;

    @ConfigurablePropertyCommon(category = "machine", comment = "The number of items that should be removed when right-clicking when an item is selected in the storage terminal.", isCommandable = true)
    public static int guiStorageItemIncrementalQuantity = 1;

    @ConfigurablePropertyCommon(category = "machine", comment = "The number of items that should be selected when clicking on a fluid in the storage terminal.", isCommandable = true)
    public static int guiStorageFluidInitialQuantity = 100000;

    @ConfigurablePropertyCommon(category = "machine", comment = "The number of items that should be removed when right-clicking when a fluid is selected in the storage terminal.", isCommandable = true)
    public static int guiStorageFluidIncrementalQuantity = 1000;

    @ConfigurablePropertyCommon(category = "machine", comment = "The number of items that should be selected when clicking on energy in the storage terminal.", isCommandable = true)
    public static int guiStorageEnergyInitialQuantity = 100000;

    @ConfigurablePropertyCommon(category = "machine", comment = "The number of items that should be removed when right-clicking when energy is selected in the storage terminal.", isCommandable = true)
    public static int guiStorageEnergyIncrementalQuantity = 1000;

    @ConfigurablePropertyCommon(category = "machine", comment = "The update frequency in milliseconds for the crafting jobs gui.", isCommandable = true)
    public static int guiTerminalCraftingJobsUpdateFrequency = 1000;

    @ConfigurablePropertyCommon(category = "core", comment = "The number of threads that the crafting plan calculator can use.", minimalValue = 1, requiresMcRestart = true, configLocation = ModConfigLocation.SERVER)
    public static int craftingPlannerThreads = 2;

    @ConfigurablePropertyCommon(category = "core", comment = "If the crafting planners can work on separate thread.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static boolean craftingPlannerEnableMultithreading = false;
    @ConfigurablePropertyCommon(category = "core", comment = "If client-directed packets should be serialized in a separate thread.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static boolean packetSerializationEnableMultithreading = true;
    @ConfigurablePropertyCommon(category = "core", comment = "If client-received packets should be deserialized in a separate thread.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static boolean packetDeserializationEnableMultithreading = true;

    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the crafting terminal.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int terminalCraftingBaseConsumption = 1;
    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the storage terminal.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int terminalStorageBaseConsumption = 2;

    @ConfigurablePropertyCommon(category = "general", comment = "If the search box and button states should be synchronized between the item storage and crafting tabs.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static boolean syncItemStorageAndCraftingTabStates = true;

    @ConfigurablePropertyCommon(category = "general", comment = "If shift-clicking on the crafting terminal's crafting result slot should only produce a single result.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static boolean shiftClickCraftingResultLimit = false;

    @ConfigurablePropertyCommon(category = "general", comment = "The number of rows in the small scale of the storage terminal.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static int guiStorageScaleSmallRows = 5;
    @ConfigurablePropertyCommon(category = "general", comment = "The number of columns in the small scale of the storage terminal.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static int guiStorageScaleSmallColumns = 9;
    @ConfigurablePropertyCommon(category = "general", comment = "The number of rows in the medium scale of the storage terminal.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static int guiStorageScaleMediumRows = 7;
    @ConfigurablePropertyCommon(category = "general", comment = "The number of columns in the medium scale of the storage terminal.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static int guiStorageScaleMediumColumns = 10;
    @ConfigurablePropertyCommon(category = "general", comment = "The number of rows in the large scale of the storage terminal.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static int guiStorageScaleLargeRows = 9;
    @ConfigurablePropertyCommon(category = "general", comment = "The number of columns in the large scale of the storage terminal.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static int guiStorageScaleLargeColumns = 11;
    @ConfigurablePropertyCommon(category = "general", comment = "The number of columns in the height-based scale of the storage terminal.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static int guiStorageScaleHeightColumns = 9;
    @ConfigurablePropertyCommon(category = "general", comment = "The number of rows in the width-based scale of the storage terminal.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static int guiStorageScaleWidthRows = 5;
    @ConfigurablePropertyCommon(category = "general", comment = "The maximum number of rows in when scaling the storage terminal.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static int guiStorageScaleMaxRows = 20;
    @ConfigurablePropertyCommon(category = "general", comment = "The maximum number of columns in when scaling the storage terminal.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static int guiStorageScaleMaxColumns = 32;
    @ConfigurablePropertyCommon(category = "general", comment = "If the crafting grid should always be shown centrally, and not be responsive based on screen size.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static boolean guiStorageForceCraftingGridCenter = false;

    public GeneralConfig() {
        super(IntegratedTerminals._instance, "general");
    }

}
