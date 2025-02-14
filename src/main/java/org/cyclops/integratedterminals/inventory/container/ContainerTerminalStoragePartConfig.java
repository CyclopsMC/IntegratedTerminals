package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integratedterminals.IntegratedTerminals;

/**
 * Config for {@link ContainerTerminalStoragePart}.
 * @author rubensworks
 */
public class ContainerTerminalStoragePartConfig extends GuiConfigCommon<ContainerTerminalStoragePart, IModBase> {

    public ContainerTerminalStoragePartConfig() {
        super(IntegratedTerminals._instance,
                "part_terminal_storage_part",
                eConfig -> new ContainerTypeData<>(ContainerTerminalStoragePart::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerTerminalStoragePart> getScreenFactoryProvider() {
        return new ContainerTerminalStoragePartConfigScreenFactoryProvider();
    }
}
