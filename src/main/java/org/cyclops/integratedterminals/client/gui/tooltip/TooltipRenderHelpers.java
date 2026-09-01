package org.cyclops.integratedterminals.client.gui.tooltip;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.helper.IModHelpers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Helpers for rendering tooltips that can contain visual components next to text lines.
 * @author rubensworks
 */
public final class TooltipRenderHelpers {

    private TooltipRenderHelpers() {}

    /**
     * Render a tooltip if the mouse hovers over the given region.
     *
     * If no visual component is given, this is equivalent to
     * {@link org.cyclops.cyclopscore.helper.IGuiHelpers}'s tooltip rendering.
     * Otherwise, the visual component is rendered below the tooltip lines.
     *
     * This must be called while rendering the foreground layer of the given gui,
     * as the given position is expected to be relative to the gui.
     *
     * @param gui The gui to render in.
     * @param guiGraphics The gui graphics.
     * @param x The region X position, relative to the gui.
     * @param y The region Y position, relative to the gui.
     * @param width The region width.
     * @param height The region height.
     * @param mouseX The mouse X position.
     * @param mouseY The mouse Y position.
     * @param linesSupplier A supplier of the tooltip lines.
     * @param visualComponent An optional visual tooltip component.
     */
    public static void renderTooltip(AbstractContainerScreen gui, GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height,
                                     int mouseX, int mouseY, Supplier<List<Component>> linesSupplier,
                                     @Nullable TooltipComponent visualComponent) {
        if (visualComponent == null) {
            IModHelpers.get().getGuiHelpers().renderTooltip(gui, guiGraphics, x, y, width, height, mouseX, mouseY, linesSupplier);
        } else if (isHovering(gui, x, y, width, height, mouseX, mouseY)) {
            List<Either<FormattedText, TooltipComponent>> elements = Lists.newArrayList();
            for (Component line : linesSupplier.get()) {
                elements.add(Either.left(line));
            }
            elements.add(Either.right(visualComponent));

            guiGraphics.setComponentTooltipFromElementsForNextFrame(Minecraft.getInstance().font, elements,
                    mouseX, mouseY, ItemStack.EMPTY);
        }
    }

    /**
     * Check if the mouse hovers over the given region,
     * which is when {@link #renderTooltip} would show a tooltip for it.
     *
     * @param gui The gui that is being rendered in.
     * @param x The region X position, relative to the gui.
     * @param y The region Y position, relative to the gui.
     * @param width The region width.
     * @param height The region height.
     * @param mouseX The mouse X position.
     * @param mouseY The mouse Y position.
     * @return If the mouse hovers over the region.
     */
    public static boolean isHovering(AbstractContainerScreen gui, int x, int y, int width, int height,
                                     int mouseX, int mouseY) {
        return IModHelpers.get().getRenderHelpers()
                .isPointInRegion(x, y, width, height, mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop());
    }

}
