package org.cyclops.integratedterminals.client.gui.tooltip;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Renders the machines of a {@link CraftingOptionMachinesTooltip} as a grid of icons.
 *
 * @author rubensworks
 */
public class ClientCraftingOptionMachinesTooltip extends ClientCraftingOptionSlotsTooltip {

    private final List<ItemStack> machines;

    public ClientCraftingOptionMachinesTooltip(CraftingOptionMachinesTooltip tooltip) {
        super(tooltip.machines().size());
        this.machines = tooltip.machines();
    }

    @Override
    protected void drawSlot(GuiGraphicsExtractor guiGraphics, int slot, int x, int y) {
        guiGraphics.item(this.machines.get(slot), x, y);
    }

}
