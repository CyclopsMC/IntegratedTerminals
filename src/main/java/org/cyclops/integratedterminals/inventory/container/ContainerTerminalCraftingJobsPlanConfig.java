package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integratedterminals.IntegratedTerminals;

/**
 * Config for {@link ContainerTerminalCraftingJobsPlan}.
 * @author rubensworks
 */
public class ContainerTerminalCraftingJobsPlanConfig extends GuiConfigCommon<ContainerTerminalCraftingJobsPlan, IModBase> {

    public ContainerTerminalCraftingJobsPlanConfig() {
        super(IntegratedTerminals._instance,
                "part_terminal_crafting_jobs_plan",
                eConfig -> new ContainerTypeData<>(ContainerTerminalCraftingJobsPlan::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerTerminalCraftingJobsPlan> getScreenFactoryProvider() {
        return new ContainerTerminalCraftingJobsPlanConfigScreenFactoryProvider();
    }
}
