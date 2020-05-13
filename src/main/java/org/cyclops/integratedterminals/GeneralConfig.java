package org.cyclops.integratedterminals;

import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableType;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.extendedconfig.DummyConfig;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.tracking.Analytics;
import org.cyclops.cyclopscore.tracking.Versions;

/**
 * A config with general options for this mod.
 * @author rubensworks
 *
 */
public class GeneralConfig extends DummyConfig {

    /**
     * The current mod version, will be used to check if the player's config isn't out of date and
     * warn the player accordingly.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "Config version for " + Reference.MOD_NAME +".\nDO NOT EDIT MANUALLY!")
    public static String version = Reference.MOD_VERSION;

    /**
     * If the debug mode should be enabled. @see Debug
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "Set 'true' to enable development debug mode. This will result in a lower performance!", requiresMcRestart = true)
    public static boolean debug = false;

    /**
     * If the recipe loader should crash when finding invalid recipes.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "If the recipe loader should crash when finding invalid recipes.", requiresMcRestart = true)
    public static boolean crashOnInvalidRecipe = false;

    /**
     * If mod compatibility loader should crash hard if errors occur in that process.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "If mod compatibility loader should crash hard if errors occur in that process.", requiresMcRestart = true)
    public static boolean crashOnModCompatCrash = false;

    /**
     * If an anonymous mod startup analytics request may be sent to our analytics service.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "If an anonymous mod startup analytics request may be sent to our analytics service.")
    public static boolean analytics = true;

    /**
     * If the version checker should be enabled.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "If the version checker should be enabled.")
    public static boolean versionChecker = true;

    /**
     * The maximum number of terminal storage instances that can be sent in a single packet. Reduce this when you have packet overflows.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "The maximum number of terminal storage instances that can be sent in a single packet. Reduce this when you have packet overflows.", isCommandable = true)
    public static int terminalStoragePacketMaxInstances = 1024;

    /**
     * The number that should be selected when clicking on an item in the storage terminal.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The number of items that should be selected when clicking on an item in the storage terminal.", isCommandable = true)
    public static int guiStorageItemInitialQuantity = 64;

    /**
     * The number that should be removed when right-clicking when an item is selected in the storage terminal.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The number of items that should be removed when right-clicking when an item is selected in the storage terminal.", isCommandable = true)
    public static int guiStorageItemIncrementalQuantity = 1;

    /**
     * The number that should be selected when clicking on a fluid in the storage terminal.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The number of items that should be selected when clicking on a fluid in the storage terminal.", isCommandable = true)
    public static int guiStorageFluidInitialQuantity = 100000;

    /**
     * The number that should be removed when right-clicking when a fluid is selected in the storage terminal.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The number of items that should be removed when right-clicking when a fluid is selected in the storage terminal.", isCommandable = true)
    public static int guiStorageFluidIncrementalQuantity = 1000;

    /**
     * The number that should be selected when clicking on energy in the storage terminal.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The number of items that should be selected when clicking on energy in the storage terminal.", isCommandable = true)
    public static int guiStorageEnergyInitialQuantity = 100000;

    /**
     * The number that should be removed when right-clicking when energy is selected in the storage terminal.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The number of items that should be removed when right-clicking when energy is selected in the storage terminal.", isCommandable = true)
    public static int guiStorageEnergyIncrementalQuantity = 1000;

    /**
     * The update frequency in milliseconds for the crafting jobs gui.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The update frequency in milliseconds for the crafting jobs gui.", isCommandable = true)
    public static int guiTerminalCraftingJobsUpdateFrequency = 1000;

    /**
     * The number of threads that the crafting plan calculator can use.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "The number of threads that the crafting plan calculator can use.", minimalValue = 1, requiresMcRestart = true)
    public static int craftingPlannerThreads = 2;

    /**
     * If the crafting planners can work on separate thread.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "If the crafting planners can work on separate thread.", isCommandable = true)
    public static boolean craftingPlannerEnableMultithreading = true;
    
    /**
     * The base energy usage for the crafting terminal.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the crafting terminal.", minimalValue = 0)
    public static int terminalCraftingBaseConsumption = 1;
    
    /**
     * The base energy usage for the storage terminal.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "The base energy usage for the storage terminal.", minimalValue = 0)
    public static int terminalStorageBaseConsumption = 2;

    /**
     * The type of this config.
     */
    public static ConfigurableType TYPE = ConfigurableType.DUMMY;

    /**
     * Create a new instance.
     */
    public GeneralConfig() {
        super(IntegratedTerminals._instance, true, "general", null, GeneralConfig.class);
    }

    @Override
    public void onRegistered() {
        getMod().putGenericReference(ModBase.REFKEY_CRASH_ON_INVALID_RECIPE, GeneralConfig.crashOnInvalidRecipe);
        getMod().putGenericReference(ModBase.REFKEY_DEBUGCONFIG, GeneralConfig.debug);
        getMod().putGenericReference(ModBase.REFKEY_CRASH_ON_MODCOMPAT_CRASH, GeneralConfig.crashOnModCompatCrash);

        if(analytics) {
            Analytics.registerMod(getMod(), Reference.GA_TRACKING_ID);
        }
        if(versionChecker) {
            Versions.registerMod(getMod(), IntegratedTerminals._instance, Reference.VERSION_URL);
        }
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}
