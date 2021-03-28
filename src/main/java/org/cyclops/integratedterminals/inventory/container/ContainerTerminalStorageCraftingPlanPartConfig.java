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
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorageCraftingPlan;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorageCraftingPlan;

/**
 * Config for {@link ContainerTerminalStorageCraftingPlanPart}.
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingPlanPartConfig extends GuiConfig<ContainerTerminalStorageCraftingPlanPart> {

    public ContainerTerminalStorageCraftingPlanPartConfig() {
        super(IntegratedTerminals._instance,
                "part_terminal_storage_crafting_plan_part",
                eConfig -> new ContainerTypeData<>(ContainerTerminalStorageCraftingPlanPart::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerTerminalStorageCraftingPlanPart>> ScreenManager.IScreenFactory<ContainerTerminalStorageCraftingPlanPart, U> getScreenFactory() {
        // Does not compile when simplified with lambdas
        return new ScreenFactorySafe<>(new ScreenManager.IScreenFactory<ContainerTerminalStorageCraftingPlanPart, ContainerScreenTerminalStorageCraftingPlan<PartPos, ContainerTerminalStorageCraftingPlanPart>>() {
            @Override
            public ContainerScreenTerminalStorageCraftingPlan<PartPos, ContainerTerminalStorageCraftingPlanPart> create(ContainerTerminalStorageCraftingPlanPart p_create_1_, PlayerInventory p_create_2_, ITextComponent p_create_3_) {
                return new ContainerScreenTerminalStorageCraftingPlan<>(p_create_1_, p_create_2_, p_create_3_);
            }
        });
    }

}
