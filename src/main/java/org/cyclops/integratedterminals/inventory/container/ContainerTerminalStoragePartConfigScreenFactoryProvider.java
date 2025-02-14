package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;

/**
 * @author rubensworks
 */
public class ContainerTerminalStoragePartConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerTerminalStoragePart> {
    @Override
    public <U extends Screen & MenuAccess<ContainerTerminalStoragePart>> MenuScreens.ScreenConstructor<ContainerTerminalStoragePart, U> getScreenFactory() {
        // Does not compile when simplified with lambdas
        return new ScreenFactorySafe<>(new MenuScreens.ScreenConstructor<ContainerTerminalStoragePart, ContainerScreenTerminalStorage<PartPos, ContainerTerminalStoragePart>>() {
            @Override
            public ContainerScreenTerminalStorage<PartPos, ContainerTerminalStoragePart> create(ContainerTerminalStoragePart p_create_1_, Inventory p_create_2_, Component p_create_3_) {
                return new ContainerScreenTerminalStorage<>(p_create_1_, p_create_2_, p_create_3_);
            }
        });
    }
}
