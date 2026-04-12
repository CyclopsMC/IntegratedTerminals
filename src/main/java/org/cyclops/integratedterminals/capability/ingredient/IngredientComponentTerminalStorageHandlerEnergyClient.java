package org.cyclops.integratedterminals.capability.ingredient;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.cyclops.cyclopscore.helper.IGuiHelpers;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandlerClient;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.client.gui.image.Images;
import org.cyclops.integratedterminals.core.terminalstorage.query.SearchMode;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author rubensworks
 */
public class IngredientComponentTerminalStorageHandlerEnergyClient implements IIngredientComponentTerminalStorageHandlerClient<Long, Boolean> {

    private final IngredientComponentTerminalStorageHandlerEnergy handler;

    public IngredientComponentTerminalStorageHandlerEnergyClient(IngredientComponentTerminalStorageHandlerEnergy handler) {
        this.handler = handler;
    }

    @Override
    public void drawInstance(GuiGraphicsExtractor guiGraphics, Long instance, long maxQuantity, @Nullable String label, AbstractContainerScreen gui,
                             ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int x, int y,
                             int mouseX, int mouseY, @Nullable List<Component> additionalTooltipLines) {
        if (instance > 0) {
            if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND){

                // Draw background
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Images.ICONS, x, y, 0, 240, IModHelpers.get().getGuiHelpers().getSlotSizeInner(), IModHelpers.get().getGuiHelpers().getSlotSizeInner(), 256, 256);

                // Draw progress
                int progressScaled;
                int progressMaxScaled;
                if ((int)maxQuantity == maxQuantity) {
                    progressScaled = (int) (long) instance;
                    progressMaxScaled = (int) maxQuantity;
                } else {
                    progressScaled = (int) (long) (instance >> 16);
                    progressMaxScaled = (int) (maxQuantity >> 16);
                }
                IModHelpers.get().getGuiHelpers().renderProgressBar(guiGraphics, Images.ICONS, x, y, IModHelpers.get().getGuiHelpers().getSlotSizeInner(), IModHelpers.get().getGuiHelpers().getSlotSizeInner(),
                        16, 240, IGuiHelpers.ProgressDirection.UP, progressScaled, progressMaxScaled);

                // Draw amount
                float scale = 0.5f;
                guiGraphics.pose().pushMatrix();
                guiGraphics.pose().scale(scale, scale);
                String amountLabel = label != null ? label : IModHelpers.get().getGuiHelpers().quantityToScaledString(instance);
                guiGraphics.text(Minecraft.getInstance().font, amountLabel, (int) ((x + 17) / scale - Minecraft.getInstance().font.width(amountLabel)), (int) ((y + 12) / scale), -1, true);
                guiGraphics.pose().popMatrix();
            } else {
                IModHelpers.get().getGuiHelpers().renderTooltip(gui, guiGraphics, x, y, IModHelpers.get().getGuiHelpers().getSlotSizeInner(), IModHelpers.get().getGuiHelpers().getSlotSizeInner(),
                        mouseX, mouseY, () -> {
                            List<Component> lines = Lists.newArrayList();
                            lines.add(Component.translatable("gui.integratedterminals.terminal_storage.tooltip.energy"));
                            this.handler.addQuantityTooltip(lines, instance);
                            if (additionalTooltipLines != null) {
                                lines.addAll(additionalTooltipLines);
                            }
                            return lines;
                        });
            }
        }
    }

    @Override
    public Predicate<Long> getInstanceFilterPredicate(SearchMode searchMode, String query) {
        return integer -> true; // Searching does not make sense here, as at most one instance exists.
    }

}
