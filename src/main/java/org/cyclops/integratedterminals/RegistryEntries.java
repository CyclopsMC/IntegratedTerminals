package org.cyclops.integratedterminals;

import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraftforge.registries.ObjectHolder;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalCraftingJobs;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalCraftingJobsPlan;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingOptionAmountPart;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingPlanPart;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStoragePart;

/**
 * Referenced registry entries.
 * @author rubensworks
 */
public class RegistryEntries {

    @ObjectHolder("integratedterminals:part_terminal_storage")
    public static final Item ITEM_PART_TERMINAL_STORAGE = null;
    @ObjectHolder("integrateddynamics:energy_battery")
    public static final ItemBlockEnergyContainer ITEM_ENERGY_BATTERY = null;

    @ObjectHolder("integratedterminals:part_terminal_crafting_jobs")
    public static final ContainerType<ContainerTerminalCraftingJobs> CONTAINER_PART_TERMINAL_CRAFTING_JOBS = null;
    @ObjectHolder("integratedterminals:part_terminal_crafting_jobs_plan")
    public static final ContainerType<ContainerTerminalCraftingJobsPlan> CONTAINER_PART_TERMINAL_CRAFTING_JOBS_PLAN = null;
    @ObjectHolder("integratedterminals:part_terminal_storage_part")
    public static final ContainerType<ContainerTerminalStoragePart> CONTAINER_PART_TERMINAL_STORAGE_PART = null;
    @ObjectHolder("integratedterminals:part_terminal_storage_crafting_option_amount_part")
    public static final ContainerType<ContainerTerminalStorageCraftingOptionAmountPart> CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_OPTION_AMOUNT_PART = null;
    @ObjectHolder("integratedterminals:part_terminal_storage_crafting_plan_part")
    public static final ContainerType<ContainerTerminalStorageCraftingPlanPart> CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_PLAN_PART = null;

}
