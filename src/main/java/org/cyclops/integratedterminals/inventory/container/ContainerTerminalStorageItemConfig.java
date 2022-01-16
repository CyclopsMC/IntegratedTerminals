package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;

/**
 * Config for {@link ContainerTerminalStorageItem}.
 * @author rubensworks
 */
public class ContainerTerminalStorageItemConfig extends GuiConfig<ContainerTerminalStorageItem> {

    public ContainerTerminalStorageItemConfig() {
        super(IntegratedTerminals._instance,
                "part_terminal_storage_item",
                eConfig -> new ContainerTypeData<>(ContainerTerminalStorageItem::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerTerminalStorageItem>> MenuScreens.ScreenConstructor<ContainerTerminalStorageItem, U> getScreenFactory() {
        // Does not compile when simplified with lambdas
        return new ScreenFactorySafe<>(new MenuScreens.ScreenConstructor<ContainerTerminalStorageItem, ContainerScreenTerminalStorage<Pair<InteractionHand, Integer>, ContainerTerminalStorageItem>>() {
            @Override
            public ContainerScreenTerminalStorage<Pair<InteractionHand, Integer>, ContainerTerminalStorageItem> create(ContainerTerminalStorageItem p_create_1_, Inventory p_create_2_, Component p_create_3_) {
                return new ContainerScreenTerminalStorage<>(p_create_1_, p_create_2_, p_create_3_);
            }
        });
    }

}
