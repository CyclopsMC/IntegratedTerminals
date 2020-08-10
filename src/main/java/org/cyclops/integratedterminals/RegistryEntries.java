package org.cyclops.integratedterminals;

import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraftforge.registries.ObjectHolder;
import org.cyclops.integratedcrafting.inventory.container.ContainerPartInterfaceCrafting;
import org.cyclops.integrateddynamics.core.item.ItemBlockEnergyContainer;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalCraftingJobs;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalCraftingJobsPlan;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingOptionAmount;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingPlan;

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
    @ObjectHolder("integratedterminals:part_terminal_storage")
    public static final ContainerType<ContainerTerminalStorage> CONTAINER_PART_TERMINAL_STORAGE = null;
    @ObjectHolder("integratedterminals:part_terminal_storage_crafting_option_amount")
    public static final ContainerType<ContainerTerminalStorageCraftingOptionAmount> CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_OPTION_AMOUNT = null;
    @ObjectHolder("integratedterminals:part_terminal_storage_crafting_plan")
    public static final ContainerType<ContainerTerminalStorageCraftingPlan> CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_PLAN = null;

}
