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
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorageCraftingPlan;

/**
 * Config for {@link ContainerTerminalStorageCraftingPlan}.
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingPlanConfig extends GuiConfig<ContainerTerminalStorageCraftingPlan> {

    public ContainerTerminalStorageCraftingPlanConfig() {
        super(IntegratedTerminals._instance,
                "part_terminal_storage_crafting_plan",
                eConfig -> new ContainerTypeData<>(ContainerTerminalStorageCraftingPlan::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerTerminalStorageCraftingPlan>> ScreenManager.IScreenFactory<ContainerTerminalStorageCraftingPlan, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenTerminalStorageCraftingPlan::new);
    }

}
