package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorageCraftingOptionAmount;

/**
 * Config for {@link ContainerTerminalStorageCraftingOptionAmountPart}.
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingOptionAmountPartConfig extends GuiConfig<ContainerTerminalStorageCraftingOptionAmountPart> {

    public ContainerTerminalStorageCraftingOptionAmountPartConfig() {
        super(IntegratedTerminals._instance,
                "part_terminal_storage_crafting_option_amount_part",
                eConfig -> new ContainerTypeData<>(ContainerTerminalStorageCraftingOptionAmountPart::new, FeatureFlags.VANILLA_SET));
    }

    @OnlyIn(Dist.CLIENT)
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
