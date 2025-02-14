package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalCraftingJobsPlan;

/**
 * @author rubensworks
 */
public class ContainerTerminalCraftingJobsPlanConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerTerminalCraftingJobsPlan> {
    @Override
    public <U extends Screen & MenuAccess<ContainerTerminalCraftingJobsPlan>> MenuScreens.ScreenConstructor<ContainerTerminalCraftingJobsPlan, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenTerminalCraftingJobsPlan::new);
    }
}
