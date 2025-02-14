package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integratedterminals.IntegratedTerminals;

/**
 * Config for {@link ContainerTerminalStorageCraftingPlanItem}.
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingPlanItemConfig extends GuiConfigCommon<ContainerTerminalStorageCraftingPlanItem, IModBase> {

    public ContainerTerminalStorageCraftingPlanItemConfig() {
        super(IntegratedTerminals._instance,
                "part_terminal_storage_crafting_plan_item",
                eConfig -> new ContainerTypeData<>(ContainerTerminalStorageCraftingPlanItem::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerTerminalStorageCraftingPlanItem> getScreenFactoryProvider() {
        return new ContainerTerminalStorageCraftingPlanItemConfigScreenFactoryProvider();
    }
}
