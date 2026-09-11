package org.cyclops.integratedterminals.client.gui.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * A tooltip component holding the machines that a crafting option is crafted in.
 *
 * @param machines The crafting machines.
 * @author rubensworks
 */
public record CraftingOptionMachinesTooltip(List<ItemStack> machines) implements TooltipComponent {
}
