package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalCraftingJobs;

/**
 * @author rubensworks
 */
public class ContainerTerminalCraftingJobsConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerTerminalCraftingJobs> {
    @Override
    public <U extends Screen & MenuAccess<ContainerTerminalCraftingJobs>> MenuScreens.ScreenConstructor<ContainerTerminalCraftingJobs, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenTerminalCraftingJobs::new);
    }
}
