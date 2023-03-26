package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
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
                eConfig -> new ContainerTypeData<>(ContainerTerminalStorageCraftingPlanPart::new, FeatureFlags.VANILLA_SET));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerTerminalStorageCraftingPlanPart>> MenuScreens.ScreenConstructor<ContainerTerminalStorageCraftingPlanPart, U> getScreenFactory() {
        // Does not compile when simplified with lambdas
        return new ScreenFactorySafe<>(new MenuScreens.ScreenConstructor<ContainerTerminalStorageCraftingPlanPart, ContainerScreenTerminalStorageCraftingPlan<PartPos, ContainerTerminalStorageCraftingPlanPart>>() {
            @Override
            public ContainerScreenTerminalStorageCraftingPlan<PartPos, ContainerTerminalStorageCraftingPlanPart> create(ContainerTerminalStorageCraftingPlanPart p_create_1_, Inventory p_create_2_, Component p_create_3_) {
                return new ContainerScreenTerminalStorageCraftingPlan<>(p_create_1_, p_create_2_, p_create_3_);
            }
        });
    }

}
