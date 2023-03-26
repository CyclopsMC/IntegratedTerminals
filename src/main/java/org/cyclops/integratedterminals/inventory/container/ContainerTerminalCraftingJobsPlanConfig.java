package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalCraftingJobsPlan;

/**
 * Config for {@link ContainerTerminalCraftingJobsPlan}.
 * @author rubensworks
 */
public class ContainerTerminalCraftingJobsPlanConfig extends GuiConfig<ContainerTerminalCraftingJobsPlan> {

    public ContainerTerminalCraftingJobsPlanConfig() {
        super(IntegratedTerminals._instance,
                "part_terminal_crafting_jobs_plan",
                eConfig -> new ContainerTypeData<>(ContainerTerminalCraftingJobsPlan::new, FeatureFlags.VANILLA_SET));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerTerminalCraftingJobsPlan>> MenuScreens.ScreenConstructor<ContainerTerminalCraftingJobsPlan, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenTerminalCraftingJobsPlan::new);
    }

}
