package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
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
                eConfig -> new ContainerTypeData<>(ContainerTerminalStorageCraftingOptionAmountItem::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerTerminalStorageCraftingOptionAmountItem>> ScreenManager.IScreenFactory<ContainerTerminalStorageCraftingOptionAmountItem, U> getScreenFactory() {
        // Does not compile when simplified with lambdas
        return new ScreenFactorySafe<>(new ScreenManager.IScreenFactory<ContainerTerminalStorageCraftingOptionAmountItem, ContainerScreenTerminalStorageCraftingOptionAmount<Pair<Hand, Integer>, ContainerTerminalStorageCraftingOptionAmountItem>>() {
            @Override
            public ContainerScreenTerminalStorageCraftingOptionAmount<Pair<Hand, Integer>, ContainerTerminalStorageCraftingOptionAmountItem> create(ContainerTerminalStorageCraftingOptionAmountItem p_create_1_, PlayerInventory p_create_2_, ITextComponent p_create_3_) {
                return new ContainerScreenTerminalStorageCraftingOptionAmount<>(p_create_1_, p_create_2_, p_create_3_);
            }
        });
    }

}
