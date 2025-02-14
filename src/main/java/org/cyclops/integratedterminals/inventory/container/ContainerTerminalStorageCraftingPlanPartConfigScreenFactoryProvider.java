package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorageCraftingPlan;

/**
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingPlanPartConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerTerminalStorageCraftingPlanPart> {
    @Override
    public <U extends Screen & MenuAccess<ContainerTerminalStorageCraftingPlanPart>> MenuScreens.ScreenConstructor<ContainerTerminalStorageCraftingPlanPart, U> getScreenFactory() {
        // Does not compile when simplified with lambdas
        return new ScreenFactorySafe<>(new MenuScreens.ScreenConstructor<ContainerTerminalStorageCraftingPlanPart, ContainerScreenTerminalStorageCraftingPlan<PartPos, ContainerTerminalStorageCraftingPlanPart>>() {
            @Override
            public ContainerScreenTerminalStorageCraftingPlan<PartPos, ContainerTerminalStorageCraftingPlanPart> create(ContainerTerminalStorageCraftingPlanPart p_create_1_, Inventory p_create_2_, Component p_create_3_) {
                return new ContainerScreenTerminalStorageCraftingPlan<>(p_create_1_, p_create_2_, p_create_3_);
            }
        });
    }
}
