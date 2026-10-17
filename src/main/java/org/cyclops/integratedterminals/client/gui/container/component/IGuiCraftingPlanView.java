package org.cyclops.integratedterminals.client.gui.container.component;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.input.MouseButtonEvent;

/**
 * A gui component that visualizes a crafting plan.
 *
 * The using gui must call the following methods from its respective method:
 * * {@link #extractRenderState(GuiGraphicsExtractor, int, int, float)}
 * * {@link #drawGuiContainerBackgroundLayer(GuiGraphicsExtractor, float, int, int)}
 * * {@link #drawGuiContainerForegroundLayer(GuiGraphicsExtractor, int, int)}
 * * {@link #mouseScrolled(double, double, double, double)}
 * * {@link #mouseDragged(MouseButtonEvent, double, double)}
 * * {@link #mouseReleased(MouseButtonEvent)}
 *
 * @author rubensworks
 */
public interface IGuiCraftingPlanView extends GuiEventListener, Renderable, NarratableEntry {

    public void drawGuiContainerBackgroundLayer(GuiGraphicsExtractor guiGraphics, float partialTicks, int mouseX, int mouseY);

    public void drawGuiContainerForegroundLayer(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY);

    /**
     * @return If the visualized plan can be started.
     */
    public boolean isValid();

}
