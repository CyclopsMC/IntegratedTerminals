package org.cyclops.integratedterminals;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
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

    public static final DeferredHolder<Item, Item> ITEM_PART_TERMINAL_STORAGE = DeferredHolder.create(Registries.ITEM, new ResourceLocation("integratedterminals:part_terminal_storage"));
    public static final DeferredHolder<Item, ItemBlockEnergyContainer> ITEM_ENERGY_BATTERY = DeferredHolder.create(Registries.ITEM, new ResourceLocation("integrateddynamics:energy_battery"));
    public static final DeferredHolder<Item, Item> ITEM_TERMINAL_STORAGE_PORTABLE = DeferredHolder.create(Registries.ITEM, new ResourceLocation("integratedterminals:terminal_storage_portable"));

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerTerminalCraftingJobs>> CONTAINER_PART_TERMINAL_CRAFTING_JOBS = DeferredHolder.create(Registries.MENU, new ResourceLocation("integratedterminals:part_terminal_crafting_jobs"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerTerminalCraftingJobsPlan>> CONTAINER_PART_TERMINAL_CRAFTING_JOBS_PLAN = DeferredHolder.create(Registries.MENU, new ResourceLocation("integratedterminals:part_terminal_crafting_jobs_plan"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerTerminalStoragePart>> CONTAINER_PART_TERMINAL_STORAGE_PART = DeferredHolder.create(Registries.MENU, new ResourceLocation("integratedterminals:part_terminal_storage_part"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerTerminalStorageCraftingOptionAmountPart>> CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_OPTION_AMOUNT_PART = DeferredHolder.create(Registries.MENU, new ResourceLocation("integratedterminals:part_terminal_storage_crafting_option_amount_part"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerTerminalStorageCraftingPlanPart>> CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_PLAN_PART = DeferredHolder.create(Registries.MENU, new ResourceLocation("integratedterminals:part_terminal_storage_crafting_plan_part"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerTerminalStorageItem>> CONTAINER_PART_TERMINAL_STORAGE_ITEM = DeferredHolder.create(Registries.MENU, new ResourceLocation("integratedterminals:part_terminal_storage_item"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerTerminalStorageCraftingOptionAmountItem>> CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_OPTION_AMOUNT_ITEM = DeferredHolder.create(Registries.MENU, new ResourceLocation("integratedterminals:part_terminal_storage_crafting_option_amount_item"));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerTerminalStorageCraftingPlanItem>> CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_PLAN_ITEM = DeferredHolder.create(Registries.MENU, new ResourceLocation("integratedterminals:part_terminal_storage_crafting_plan_item"));

}
