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
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;

/**
 * Config for {@link ContainerTerminalStorage}.
 * @author rubensworks
 */
public class ContainerTerminalStorageConfig extends GuiConfig<ContainerTerminalStorage> {

    public ContainerTerminalStorageConfig() {
        super(IntegratedTerminals._instance,
                "part_terminal_storage",
                eConfig -> new ContainerTypeData<>(ContainerTerminalStorage::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerTerminalStorage>> ScreenManager.IScreenFactory<ContainerTerminalStorage, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenTerminalStorage::new);
    }

}
