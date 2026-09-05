package org.cyclops.integratedterminals.core.terminalstorage.slot;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageSlot;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.client.gui.image.Images;
import org.cyclops.integratedterminals.client.gui.tooltip.TooltipRenderHelpers;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageChannels;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.PendingCraftingJobOutput;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

/**
 * An ingredient slot.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class TerminalStorageSlotIngredient<T, M> implements ITerminalStorageSlot {

    /**
     * The duration in milliseconds of a single frame of the crafting spinner.
     */
    private static final long SPINNER_FRAME_DURATION = 100;

    private final IIngredientComponentTerminalStorageHandler<T, M> ingredientComponentViewHandler;
    private final T instance;

    public TerminalStorageSlotIngredient(IIngredientComponentTerminalStorageHandler<T, M> ingredientComponentViewHandler, T instance) {
        this.ingredientComponentViewHandler = ingredientComponentViewHandler;
        this.instance = instance;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawGuiContainerLayer(AbstractContainerScreen gui, GuiGraphics guiGraphics, ContainerScreenTerminalStorage.DrawLayer layer,
                                      float partialTick, int x, int y, int mouseX, int mouseY,
                                      ITerminalStorageTabClient tab, int channel, @Nullable String label) {
        long maxQuantity = ((TerminalStorageTabIngredientComponentClient) tab).getMaxQuantity(channel);
        PendingCraftingJobOutput<T> pendingCraftingJobOutput = getPendingCraftingJobOutput(tab, channel, label);
        // This is called for all visible slots on every frame,
        // so only determine the tooltip lines when they are actually going to be shown.
        List<Component> tooltipLines = layer == ContainerScreenTerminalStorage.DrawLayer.FOREGROUND
                && TooltipRenderHelpers.isHovering(gui, x, y, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER, mouseX, mouseY)
                ? createTooltipLines(pendingCraftingJobOutput, tab, channel, label) : null;
        ingredientComponentViewHandler.drawInstance(guiGraphics, instance, maxQuantity, label, gui, layer, partialTick, x, y, mouseX, mouseY,
                tooltipLines);
        drawCraftingJobOverlay(guiGraphics, layer, x, y, pendingCraftingJobOutput);
    }

    public IIngredientComponentTerminalStorageHandler<T, M> getIngredientComponentViewHandler() {
        return ingredientComponentViewHandler;
    }

    public T getInstance() {
        return instance;
    }

    /**
     * Get the pending output of the running crafting jobs that will produce this slot's instance.
     * @param tab The tab this slot is being rendered in.
     * @param channel The channel this slot is being rendered in.
     * @param label An optional label that is rendered instead of the quantity.
     *              Slots with such a label are not part of the storage overview,
     *              such as the instance that is being moved around by the player,
     *              so they don't get a crafting indication.
     * @return The pending crafting job output, or null if this slot's instance is not being crafted.
     */
    @Nullable
    @OnlyIn(Dist.CLIENT)
    protected PendingCraftingJobOutput<T> getPendingCraftingJobOutput(ITerminalStorageTabClient tab, int channel,
                                                                      @Nullable String label) {
        return label == null
                ? ((TerminalStorageTabIngredientComponentClient<T, M>) tab).getPendingCraftingJobOutput(channel, getInstance())
                : null;
    }

    @OnlyIn(Dist.CLIENT)
    protected List<Component> createTooltipLines(@Nullable PendingCraftingJobOutput<T> pendingCraftingJobOutput,
                                                 ITerminalStorageTabClient tab, int channel, @Nullable String label) {
        List<Component> tooltipLines = Lists.newArrayList();
        if (pendingCraftingJobOutput != null) {
            addCraftingJobTooltipLines(tooltipLines, pendingCraftingJobOutput);
        }
        addChannelTooltipLines(tooltipLines, tab, channel, label);
        return tooltipLines;
    }

    /**
     * Add the tooltip lines that indicate in which channels this slot's instance is available.
     *
     * These are only shown when all channels are shown at once,
     * as the channel is already known when a single channel is shown.
     *
     * @param tooltipLines The tooltip lines to append to.
     * @param tab The tab this slot is being rendered in.
     * @param channel The channel this slot is being rendered in.
     * @param label An optional label that is rendered instead of the quantity.
     *              Slots with such a label are not part of the storage overview,
     *              so they don't get a channel indication.
     */
    @OnlyIn(Dist.CLIENT)
    protected void addChannelTooltipLines(List<Component> tooltipLines, ITerminalStorageTabClient tab,
                                          int channel, @Nullable String label) {
        if (GeneralConfig.guiStorageTooltipChannels && label == null
                && channel == IPositionedAddonsNetwork.WILDCARD_CHANNEL) {
            tooltipLines.addAll(createChannelTooltipLines(tab));
        }
    }

    /**
     * @param tab The tab this slot is being rendered in.
     * @return The tooltip lines indicating the channels in which this slot's instance is available.
     */
    @OnlyIn(Dist.CLIENT)
    protected List<Component> createChannelTooltipLines(ITerminalStorageTabClient tab) {
        return TerminalStorageChannels.createChannelTooltipLines(getIngredientComponentViewHandler(), getInstance(),
                ((TerminalStorageTabIngredientComponentClient<T, M>) tab).getInstanceQuantitiesPerChannel(getInstance()));
    }

    @OnlyIn(Dist.CLIENT)
    protected void addCraftingJobTooltipLines(List<Component> tooltipLines,
                                              PendingCraftingJobOutput<T> pendingCraftingJobOutput) {
        tooltipLines.add(Component.translatable("gui.integratedterminals.terminal_storage.tooltip.crafting",
                        getIngredientComponentViewHandler().formatQuantity(pendingCraftingJobOutput.getInstance()))
                .withStyle(ChatFormatting.AQUA));
        String unlocalizedStatus = "gui.integratedterminals.craftingplan.status."
                + pendingCraftingJobOutput.getStatus().name().toLowerCase(Locale.ENGLISH);
        tooltipLines.add(Component.translatable("gui.integratedterminals.craftingplan.status",
                        Component.translatable(unlocalizedStatus))
                .withStyle(ChatFormatting.GRAY));
        tooltipLines.add(Component.translatable(unlocalizedStatus + ".desc").withStyle(ChatFormatting.DARK_GRAY));
    }

    /**
     * Draw a spinner over this slot when its instance is being crafted.
     * The spinner is colored based on the status of the crafting jobs.
     */
    @OnlyIn(Dist.CLIENT)
    protected void drawCraftingJobOverlay(GuiGraphics guiGraphics, ContainerScreenTerminalStorage.DrawLayer layer,
                                          int x, int y, @Nullable PendingCraftingJobOutput<T> pendingCraftingJobOutput) {
        if (layer != ContainerScreenTerminalStorage.DrawLayer.BACKGROUND || pendingCraftingJobOutput == null) {
            return;
        }

        Triple<Float, Float, Float> color = Helpers.intToRGB(pendingCraftingJobOutput.getStatus().getColor());
        int frame = (int) ((System.currentTimeMillis() / SPINNER_FRAME_DURATION) % Images.SPINNER.length);

        guiGraphics.pose().pushPose();
        // Draw in front of the instance, which is rendered as a 3D item for some ingredient components.
        guiGraphics.pose().translate(0, 0, 300);
        GlStateManager._enableBlend();
        Images.SPINNER[frame].drawWithColor(guiGraphics, x, y,
                color.getLeft(), color.getMiddle(), color.getRight(), 1F);
        guiGraphics.pose().popPose();
    }

}
