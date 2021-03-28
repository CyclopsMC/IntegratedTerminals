package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;

/**
 * Config for {@link ContainerTerminalStoragePart}.
 * @author rubensworks
 */
public class ContainerTerminalStoragePartConfig extends GuiConfig<ContainerTerminalStoragePart> {

    public ContainerTerminalStoragePartConfig() {
        super(IntegratedTerminals._instance,
                "part_terminal_storage_part",
                eConfig -> new ContainerTypeData<>(ContainerTerminalStoragePart::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerTerminalStoragePart>> ScreenManager.IScreenFactory<ContainerTerminalStoragePart, U> getScreenFactory() {
        // Does not compile when simplified with lambdas
        return new ScreenFactorySafe<>(new ScreenManager.IScreenFactory<ContainerTerminalStoragePart, ContainerScreenTerminalStorage<PartPos, ContainerTerminalStoragePart>>() {
            @Override
            public ContainerScreenTerminalStorage<PartPos, ContainerTerminalStoragePart> create(ContainerTerminalStoragePart p_create_1_, PlayerInventory p_create_2_, ITextComponent p_create_3_) {
                return new ContainerScreenTerminalStorage<>(p_create_1_, p_create_2_, p_create_3_);
            }
        });
    }

}
