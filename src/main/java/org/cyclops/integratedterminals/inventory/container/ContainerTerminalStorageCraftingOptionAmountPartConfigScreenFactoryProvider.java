package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorageCraftingOptionAmount;

/**
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingOptionAmountPartConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerTerminalStorageCraftingOptionAmountPart> {
    @Override
    public <U extends Screen & MenuAccess<ContainerTerminalStorageCraftingOptionAmountPart>> MenuScreens.ScreenConstructor<ContainerTerminalStorageCraftingOptionAmountPart, U> getScreenFactory() {
        // Does not compile when simplified with lambdas
        return new ScreenFactorySafe<>(new MenuScreens.ScreenConstructor<ContainerTerminalStorageCraftingOptionAmountPart, ContainerScreenTerminalStorageCraftingOptionAmount<PartPos, ContainerTerminalStorageCraftingOptionAmountPart>>() {
            @Override
            public ContainerScreenTerminalStorageCraftingOptionAmount<PartPos, ContainerTerminalStorageCraftingOptionAmountPart> create(ContainerTerminalStorageCraftingOptionAmountPart p_create_1_, Inventory p_create_2_, Component p_create_3_) {
                return new ContainerScreenTerminalStorageCraftingOptionAmount<>(p_create_1_, p_create_2_, p_create_3_);
            }
        });
    }
}
