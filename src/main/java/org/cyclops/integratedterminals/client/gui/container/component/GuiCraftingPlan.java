package org.cyclops.integratedterminals.client.gui.container.component;

import net.minecraft.client.gui.Gui;
import org.cyclops.cyclopscore.client.gui.component.GuiScrollBar;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;

import java.io.IOException;

/**
 * A gui component for visualizing {@link CraftingOptionGuiData}.
 *
 * The using gui must call the following methods from its respective method:
 * * {@link #handleMouseInput()}
 * * {@link #drawCurrentScreen(int, int, float)}
 * * {@link #drawGuiContainerBackgroundLayer(float, int, int)}
 * * {@link #drawGuiContainerForegroundLayer(int, int)}
 *
 * @author rubensworks
 */
public class GuiCraftingPlan extends Gui {

    private final ITerminalCraftingPlan craftingPlan;
    private final GuiScrollBar scrollBar;

    private int firstRow;

    public GuiCraftingPlan(ITerminalCraftingPlan craftingPlan, int x, int y, int visibleRows) {
        this.craftingPlan = craftingPlan;
        this.scrollBar = new GuiScrollBar(x + 166, y + 0, 178, this::setFirstRow, visibleRows);
        // TODO: create craftingComponentsList (with expand and indents) -> visibleCraftingComponentsList
        //this.scrollBar.setTotalRows(visibleCraftingComponentsList.size() - 1); // TODO: make dynamic?
    }

    public void setFirstRow(int firstRow) {
        this.firstRow = firstRow;
    }

    public void drawCurrentScreen(int mouseX, int mouseY, float partialTicks) {
        scrollBar.drawCurrentScreen(mouseX, mouseY, partialTicks);
    }

    public void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        scrollBar.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        // TODO: draw crafting components
    }

    public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // TODO: draw crafting components
    }

    public void handleMouseInput() throws IOException {
        scrollBar.handleMouseInput();
    }
}
