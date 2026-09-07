package org.cyclops.integratedterminals.client.gui.tooltip;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Helpers for rendering tooltips that can contain visual components next to text lines.
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public final class TooltipRenderHelpers {

    private TooltipRenderHelpers() {}

    /**
     * Render a tooltip if the mouse hovers over the given region.
     *
     * If no additional elements are given, this is equivalent to {@link GuiHelpers}'s tooltip rendering.
     * Otherwise, the tooltip is rendered by vanilla,
     * so that visual components can be rendered below the tooltip lines.
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
     * @param additionalElements Optional elements to append below the tooltip lines,
     *                           which can mix text and visual components.
     */
    public static void renderTooltip(AbstractContainerScreen gui, GuiGraphics guiGraphics, int x, int y, int width, int height,
                                     int mouseX, int mouseY, Supplier<List<Component>> linesSupplier,
                                     @Nullable List<Either<FormattedText, TooltipComponent>> additionalElements) {
        if (additionalElements == null || additionalElements.isEmpty()) {
            GuiHelpers.renderTooltip(gui, guiGraphics.pose(), x, y, width, height, mouseX, mouseY, linesSupplier);
        } else if (isHovering(gui, x, y, width, height, mouseX, mouseY)) {
            List<Either<FormattedText, TooltipComponent>> elements = Lists.newArrayList();
            for (Component line : linesSupplier.get()) {
                elements.add(Either.left(line));
            }
            elements.addAll(additionalElements);

            // Just like GuiHelpers#renderTooltip, don't write to the depth buffer,
            // so that anything that is drawn after this tooltip is not occluded by it.
            RenderSystem.disableDepthTest();

            // Tooltips are positioned in screen space,
            // while the foreground layer is translated to the position of the gui.
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(-gui.getGuiLeft(), -gui.getGuiTop(), 0);
            guiGraphics.renderComponentTooltipFromElements(Minecraft.getInstance().font, elements, mouseX, mouseY, ItemStack.EMPTY);
            guiGraphics.pose().popPose();

            RenderSystem.enableDepthTest();
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
        return RenderHelpers.isPointInRegion(x, y, width, height, mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop());
    }

}
