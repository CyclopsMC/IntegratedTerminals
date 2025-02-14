package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integratedterminals.IntegratedTerminals;

/**
 * Config for {@link ContainerTerminalStorageCraftingOptionAmountItem}.
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingOptionAmountItemConfig extends GuiConfigCommon<ContainerTerminalStorageCraftingOptionAmountItem, IModBase> {

    public ContainerTerminalStorageCraftingOptionAmountItemConfig() {
        super(IntegratedTerminals._instance,
                "part_terminal_storage_crafting_option_amount_item",
                eConfig -> new ContainerTypeData<>(ContainerTerminalStorageCraftingOptionAmountItem::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerTerminalStorageCraftingOptionAmountItem> getScreenFactoryProvider() {
        return new ContainerTerminalStorageCraftingOptionAmountItemConfigScreenFactoryProvider();
    }
}
