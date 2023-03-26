package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorageCraftingOptionAmount;

/**
 * Config for {@link ContainerTerminalStorageCraftingOptionAmountItem}.
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingOptionAmountItemConfig extends GuiConfig<ContainerTerminalStorageCraftingOptionAmountItem> {

    public ContainerTerminalStorageCraftingOptionAmountItemConfig() {
        super(IntegratedTerminals._instance,
                "part_terminal_storage_crafting_option_amount_item",
                eConfig -> new ContainerTypeData<>(ContainerTerminalStorageCraftingOptionAmountItem::new, FeatureFlags.VANILLA_SET));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerTerminalStorageCraftingOptionAmountItem>> MenuScreens.ScreenConstructor<ContainerTerminalStorageCraftingOptionAmountItem, U> getScreenFactory() {
        // Does not compile when simplified with lambdas
        return new ScreenFactorySafe<>(new MenuScreens.ScreenConstructor<ContainerTerminalStorageCraftingOptionAmountItem, ContainerScreenTerminalStorageCraftingOptionAmount<Pair<InteractionHand, Integer>, ContainerTerminalStorageCraftingOptionAmountItem>>() {
            @Override
            public ContainerScreenTerminalStorageCraftingOptionAmount<Pair<InteractionHand, Integer>, ContainerTerminalStorageCraftingOptionAmountItem> create(ContainerTerminalStorageCraftingOptionAmountItem p_create_1_, Inventory p_create_2_, Component p_create_3_) {
                return new ContainerScreenTerminalStorageCraftingOptionAmount<>(p_create_1_, p_create_2_, p_create_3_);
            }
        });
    }

}
