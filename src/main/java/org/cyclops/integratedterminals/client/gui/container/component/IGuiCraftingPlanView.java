package org.cyclops.integratedterminals.client.gui.container.component;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

/**
 * A gui component that visualizes a crafting plan.
 *
 * The using gui must call the following methods from its respective method:
 * * {@link #render(GuiGraphics, int, int, float)}
 * * {@link #drawGuiContainerBackgroundLayer(GuiGraphics, float, int, int)}
 * * {@link #drawGuiContainerForegroundLayer(GuiGraphics, int, int)}
 * * {@link #mouseScrolled(double, double, double, double)}
 * * {@link #mouseDragged(double, double, int, double, double)}
 * * {@link #mouseReleased(double, double, int)}
 *
 * @author rubensworks
 */
public interface IGuiCraftingPlanView extends GuiEventListener, Renderable, NarratableEntry {

    public void drawGuiContainerBackgroundLayer(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY);

    public void drawGuiContainerForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY);

    /**
     * @return If the visualized plan can be started.
     */
    public boolean isValid();

}
