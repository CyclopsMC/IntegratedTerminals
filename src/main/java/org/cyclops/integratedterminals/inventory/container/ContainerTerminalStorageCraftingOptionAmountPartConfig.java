package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integratedterminals.IntegratedTerminals;

/**
 * Config for {@link ContainerTerminalStorageCraftingOptionAmountPart}.
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingOptionAmountPartConfig extends GuiConfigCommon<ContainerTerminalStorageCraftingOptionAmountPart, IModBase> {

    public ContainerTerminalStorageCraftingOptionAmountPartConfig() {
        super(IntegratedTerminals._instance,
                "part_terminal_storage_crafting_option_amount_part",
                eConfig -> new ContainerTypeData<>(ContainerTerminalStorageCraftingOptionAmountPart::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerTerminalStorageCraftingOptionAmountPart> getScreenFactoryProvider() {
        return new ContainerTerminalStorageCraftingOptionAmountPartConfigScreenFactoryProvider();
    }
}
