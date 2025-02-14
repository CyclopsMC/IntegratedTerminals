package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integratedterminals.IntegratedTerminals;

/**
 * Config for {@link ContainerTerminalStorageItem}.
 * @author rubensworks
 */
public class ContainerTerminalStorageItemConfig extends GuiConfigCommon<ContainerTerminalStorageItem, IModBase> {

    public ContainerTerminalStorageItemConfig() {
        super(IntegratedTerminals._instance,
                "part_terminal_storage_item",
                eConfig -> new ContainerTypeData<>(ContainerTerminalStorageItem::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerTerminalStorageItem> getScreenFactoryProvider() {
        return new ContainerTerminalStorageItemConfigScreenFactoryProvider();
    }
}
