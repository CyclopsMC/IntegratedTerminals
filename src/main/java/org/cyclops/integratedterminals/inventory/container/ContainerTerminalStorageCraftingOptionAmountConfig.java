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
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorageCraftingOptionAmount;

/**
 * Config for {@link ContainerTerminalStorageCraftingOptionAmount}.
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingOptionAmountConfig extends GuiConfig<ContainerTerminalStorageCraftingOptionAmount> {

    public ContainerTerminalStorageCraftingOptionAmountConfig() {
        super(IntegratedTerminals._instance,
                "part_terminal_storage_crafting_option_amount",
                eConfig -> new ContainerTypeData<>(ContainerTerminalStorageCraftingOptionAmount::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerTerminalStorageCraftingOptionAmount>> ScreenManager.IScreenFactory<ContainerTerminalStorageCraftingOptionAmount, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenTerminalStorageCraftingOptionAmount::new);
    }

}
