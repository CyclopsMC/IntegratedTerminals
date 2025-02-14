package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integratedterminals.IntegratedTerminals;

/**
 * Config for {@link ContainerTerminalCraftingJobs}.
 * @author rubensworks
 */
public class ContainerTerminalCraftingJobsConfig extends GuiConfigCommon<ContainerTerminalCraftingJobs, IModBase> {

    public ContainerTerminalCraftingJobsConfig() {
        super(IntegratedTerminals._instance,
                "part_terminal_crafting_jobs",
                eConfig -> new ContainerTypeData<>(ContainerTerminalCraftingJobs::new, FeatureFlags.VANILLA_SET));
    }


    @Override
    public GuiConfigScreenFactoryProvider<ContainerTerminalCraftingJobs> getScreenFactoryProvider() {
        return new ContainerTerminalCraftingJobsConfigScreenFactoryProvider();
    }
}
