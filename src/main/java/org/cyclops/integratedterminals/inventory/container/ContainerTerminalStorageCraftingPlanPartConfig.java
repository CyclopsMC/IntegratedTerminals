package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integratedterminals.IntegratedTerminals;

/**
 * Config for {@link ContainerTerminalStorageCraftingPlanPart}.
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingPlanPartConfig extends GuiConfigCommon<ContainerTerminalStorageCraftingPlanPart, IModBase> {

    public ContainerTerminalStorageCraftingPlanPartConfig() {
        super(IntegratedTerminals._instance,
                "part_terminal_storage_crafting_plan_part",
                eConfig -> new ContainerTypeData<>(ContainerTerminalStorageCraftingPlanPart::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerTerminalStorageCraftingPlanPart> getScreenFactoryProvider() {
        return new ContainerTerminalStorageCraftingPlanPartConfigScreenFactoryProvider();
    }
}
