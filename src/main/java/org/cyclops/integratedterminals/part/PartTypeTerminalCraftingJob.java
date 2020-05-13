package org.cyclops.integratedterminals.part;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.core.part.PartStateEmpty;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.client.gui.container.GuiTerminalCraftingJobs;
import org.cyclops.integratedterminals.core.part.PartTypeTerminal;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.TerminalStorageTabIngredientCraftingHandlers;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalCraftingJobs;

import java.util.List;

/**
 * A part that exposes a gui using which players can view and manage the active crafting jobs in the network.
 * @author rubensworks
 */
public class PartTypeTerminalCraftingJob extends PartTypeTerminal<PartTypeTerminalCraftingJob, PartStateEmpty<PartTypeTerminalCraftingJob>> {

    public PartTypeTerminalCraftingJob(String name) {
        super(name);
    }

    @Override
    public int getConsumptionRate(PartStateEmpty<PartTypeTerminalCraftingJob> state) {
        return GeneralConfig.terminalCraftingBaseConsumption;
    }

    @Override
    protected PartStateEmpty<PartTypeTerminalCraftingJob> constructDefaultState() {
        return new PartStateEmpty<>();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Class<? extends GuiScreen> getGui() {
        return GuiTerminalCraftingJobs.class;
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerTerminalCraftingJobs.class;
    }

    @Override
    public void loadTooltip(ItemStack itemStack, List<String> lines) {
        super.loadTooltip(itemStack, lines);
        if (TerminalStorageTabIngredientCraftingHandlers.REGISTRY.getHandlers().isEmpty()) {
            lines.add(TextFormatting.GOLD + L10NHelpers.localize(
                    "parttype.parttypes.integratedterminals.terminal_crafting_job.tooltip.nohandlers"));
        }
    }
}
