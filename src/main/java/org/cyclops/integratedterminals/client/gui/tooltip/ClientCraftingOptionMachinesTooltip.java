package org.cyclops.integratedterminals.client.gui.tooltip;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Renders the machines of a {@link CraftingOptionMachinesTooltip} as a grid of icons.
 *
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class ClientCraftingOptionMachinesTooltip extends ClientCraftingOptionSlotsTooltip {

    private final List<ItemStack> machines;

    public ClientCraftingOptionMachinesTooltip(CraftingOptionMachinesTooltip tooltip) {
        super(tooltip.machines().size());
        this.machines = tooltip.machines();
    }

    @Override
    protected void drawSlot(GuiGraphics guiGraphics, int slot, int x, int y) {
        guiGraphics.renderItem(this.machines.get(slot), x, y);
    }

}
