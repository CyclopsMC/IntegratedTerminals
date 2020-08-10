package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
                eConfig -> new ContainerTypeData<>(ContainerTerminalCraftingJobs::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerTerminalCraftingJobs>> ScreenManager.IScreenFactory<ContainerTerminalCraftingJobs, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenTerminalCraftingJobs::new);
    }

}
