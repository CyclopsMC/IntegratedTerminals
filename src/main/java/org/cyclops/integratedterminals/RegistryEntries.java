package org.cyclops.integratedterminals;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ObjectHolder;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalCraftingJobs;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalCraftingJobsPlan;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingOptionAmountItem;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingOptionAmountPart;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingPlanItem;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingPlanPart;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageItem;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStoragePart;

/**
 * Referenced registry entries.
 * @author rubensworks
 */
public class RegistryEntries {

    @ObjectHolder(registryName = "item", value = "integratedterminals:part_terminal_storage")
    public static final Item ITEM_PART_TERMINAL_STORAGE = null;
    @ObjectHolder(registryName = "item", value = "integrateddynamics:energy_battery")
    public static final ItemBlockEnergyContainer ITEM_ENERGY_BATTERY = null;
    @ObjectHolder(registryName = "item", value = "integratedterminals:terminal_storage_portable")
    public static final Item ITEM_TERMINAL_STORAGE_PORTABLE = null;

    @ObjectHolder(registryName = "menu", value = "integratedterminals:part_terminal_crafting_jobs")
    public static final MenuType<ContainerTerminalCraftingJobs> CONTAINER_PART_TERMINAL_CRAFTING_JOBS = null;
    @ObjectHolder(registryName = "menu", value = "integratedterminals:part_terminal_crafting_jobs_plan")
    public static final MenuType<ContainerTerminalCraftingJobsPlan> CONTAINER_PART_TERMINAL_CRAFTING_JOBS_PLAN = null;
    @ObjectHolder(registryName = "menu", value = "integratedterminals:part_terminal_storage_part")
    public static final MenuType<ContainerTerminalStoragePart> CONTAINER_PART_TERMINAL_STORAGE_PART = null;
    @ObjectHolder(registryName = "menu", value = "integratedterminals:part_terminal_storage_crafting_option_amount_part")
    public static final MenuType<ContainerTerminalStorageCraftingOptionAmountPart> CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_OPTION_AMOUNT_PART = null;
    @ObjectHolder(registryName = "menu", value = "integratedterminals:part_terminal_storage_crafting_plan_part")
    public static final MenuType<ContainerTerminalStorageCraftingPlanPart> CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_PLAN_PART = null;
    @ObjectHolder(registryName = "menu", value = "integratedterminals:part_terminal_storage_item")
    public static final MenuType<ContainerTerminalStorageItem> CONTAINER_PART_TERMINAL_STORAGE_ITEM = null;
    @ObjectHolder(registryName = "menu", value = "integratedterminals:part_terminal_storage_crafting_option_amount_item")
    public static final MenuType<ContainerTerminalStorageCraftingOptionAmountItem> CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_OPTION_AMOUNT_ITEM = null;
    @ObjectHolder(registryName = "menu", value = "integratedterminals:part_terminal_storage_crafting_plan_item")
    public static final MenuType<ContainerTerminalStorageCraftingPlanItem> CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_PLAN_ITEM = null;

}
