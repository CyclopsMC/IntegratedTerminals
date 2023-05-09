package org.cyclops.integratedterminals;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.NewRegistryEvent;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.infobook.IInfoBookRegistry;
import org.cyclops.cyclopscore.init.ModBaseVersionable;
import org.cyclops.cyclopscore.modcompat.ModCompatLoader;
import org.cyclops.cyclopscore.proxy.IClientProxy;
import org.cyclops.cyclopscore.proxy.ICommonProxy;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.event.IntegratedDynamicsSetupEvent;
import org.cyclops.integrateddynamics.infobook.OnTheDynamicsOfIntegrationBook;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabRegistry;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandlerRegistry;
import org.cyclops.integratedterminals.api.terminalstorage.location.ITerminalStorageLocationRegistry;
import org.cyclops.integratedterminals.block.BlockChorusGlassConfig;
import org.cyclops.integratedterminals.block.BlockMenrilGlassConfig;
import org.cyclops.integratedterminals.capability.ingredient.IngredientComponentTerminalStorageHandlerConfig;
import org.cyclops.integratedterminals.capability.ingredient.TerminalIngredientComponentCapabilities;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabRegistry;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabs;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.TerminalStorageTabIngredientCraftingHandlerRegistry;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.TerminalStorageTabIngredientCraftingHandlers;
import org.cyclops.integratedterminals.core.terminalstorage.location.TerminalStorageLocationRegistry;
import org.cyclops.integratedterminals.core.terminalstorage.location.TerminalStorageLocations;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalCraftingJobsConfig;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalCraftingJobsPlanConfig;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingOptionAmountItemConfig;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingOptionAmountPartConfig;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingPlanItemConfig;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingPlanPartConfig;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageItemConfig;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStoragePartConfig;
import org.cyclops.integratedterminals.item.ItemTerminalStoragePortableConfig;
import org.cyclops.integratedterminals.modcompat.integratedcrafting.IntegratedCraftingModCompat;
import org.cyclops.integratedterminals.part.PartTypes;
import org.cyclops.integratedterminals.proxy.ClientProxy;
import org.cyclops.integratedterminals.proxy.CommonProxy;

/**
 * The main mod class of this mod.
 * @author rubensworks (aka kroeserr)
 *
 */
@Mod(Reference.MOD_ID)
public class IntegratedTerminals extends ModBaseVersionable<IntegratedTerminals> {

    public static IntegratedTerminals _instance;

    public IntegratedTerminals() {
        super(Reference.MOD_ID, (instance) -> _instance = instance);

        // Registries
        getRegistryManager().addRegistry(ITerminalStorageTabRegistry.class, new TerminalStorageTabRegistry());
        getRegistryManager().addRegistry(ITerminalStorageTabIngredientCraftingHandlerRegistry.class, TerminalStorageTabIngredientCraftingHandlerRegistry.getInstance());
        getRegistryManager().addRegistry(ITerminalStorageLocationRegistry.class, new TerminalStorageLocationRegistry());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegistriesCreate);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onSetup);
        TerminalStorageTabs.load();
        TerminalStorageTabIngredientCraftingHandlers.load();
        TerminalStorageLocations.load();
    }

    public void onRegistriesCreate(NewRegistryEvent event) {
        PartTypes.load();
        TerminalIngredientComponentCapabilities.load();
    }

    @Override
    protected void loadModCompats(ModCompatLoader modCompatLoader) {
        super.loadModCompats(modCompatLoader);

        // Mod compats
        modCompatLoader.addModCompat(new IntegratedCraftingModCompat());
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        super.setup(event);
    }

    protected void onSetup(IntegratedDynamicsSetupEvent event) {
        // Initialize info book
        IntegratedDynamics._instance.getRegistryManager().getRegistry(IInfoBookRegistry.class)
                .registerSection(this,
                        OnTheDynamicsOfIntegrationBook.getInstance(), "info_book.integrateddynamics.manual",
                        "/data/" + Reference.MOD_ID + "/info/terminals_info.xml");
        IntegratedDynamics._instance.getRegistryManager().getRegistry(IInfoBookRegistry.class)
                .registerSection(this,
                        OnTheDynamicsOfIntegrationBook.getInstance(), "info_book.integrateddynamics.tutorials",
                        "/data/" + Reference.MOD_ID + "/info/terminals_tutorials.xml");
    }

    @Override
    protected CreativeModeTab.Builder constructDefaultCreativeModeTab(CreativeModeTab.Builder builder) {
        return super.constructDefaultCreativeModeTab(builder)
                .icon(() -> new ItemStack(RegistryEntries.ITEM_PART_TERMINAL_STORAGE));
    }

    @Override
    public void onConfigsRegister(ConfigHandler configHandler) {
        super.onConfigsRegister(configHandler);

        configHandler.addConfigurable(new GeneralConfig());

        configHandler.addConfigurable(new IngredientComponentTerminalStorageHandlerConfig());

        configHandler.addConfigurable(new BlockMenrilGlassConfig());
        configHandler.addConfigurable(new BlockChorusGlassConfig());

        configHandler.addConfigurable(new ItemTerminalStoragePortableConfig());

        configHandler.addConfigurable(new ContainerTerminalCraftingJobsConfig());
        configHandler.addConfigurable(new ContainerTerminalCraftingJobsPlanConfig());
        configHandler.addConfigurable(new ContainerTerminalStoragePartConfig());
        configHandler.addConfigurable(new ContainerTerminalStorageCraftingOptionAmountPartConfig());
        configHandler.addConfigurable(new ContainerTerminalStorageCraftingPlanPartConfig());
        configHandler.addConfigurable(new ContainerTerminalStorageItemConfig());
        configHandler.addConfigurable(new ContainerTerminalStorageCraftingOptionAmountItemConfig());
        configHandler.addConfigurable(new ContainerTerminalStorageCraftingPlanItemConfig());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected IClientProxy constructClientProxy() {
        return new ClientProxy();
    }

    @Override
    protected ICommonProxy constructCommonProxy() {
        return new CommonProxy();
    }

    /**
     * Log a new info message for this mod.
     * @param message The message to show.
     */
    public static void clog(String message) {
        clog(Level.INFO, message);
    }

    /**
     * Log a new message of the given level for this mod.
     * @param level The level in which the message must be shown.
     * @param message The message to show.
     */
    public static void clog(Level level, String message) {
        IntegratedTerminals._instance.getLoggerHelper().log(level, message);
    }

}
