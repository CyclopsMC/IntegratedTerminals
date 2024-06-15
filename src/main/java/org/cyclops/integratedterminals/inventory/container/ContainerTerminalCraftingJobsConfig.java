package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalCraftingJobs;

/**
 * Config for {@link ContainerTerminalCraftingJobs}.
 * @author rubensworks
 */
public class ContainerTerminalCraftingJobsConfig extends GuiConfig<ContainerTerminalCraftingJobs> {

    public ContainerTerminalCraftingJobsConfig() {
        super(IntegratedTerminals._instance,
                "part_terminal_crafting_jobs",
                eConfig -> new ContainerTypeData<>(ContainerTerminalCraftingJobs::new, FeatureFlags.VANILLA_SET));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerTerminalCraftingJobs>> MenuScreens.ScreenConstructor<ContainerTerminalCraftingJobs, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenTerminalCraftingJobs::new);
    }

}
