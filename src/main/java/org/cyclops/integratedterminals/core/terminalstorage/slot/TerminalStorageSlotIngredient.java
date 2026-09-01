package org.cyclops.integratedterminals.core.terminalstorage.slot;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageSlot;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.client.gui.image.Images;
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
    public void drawGuiContainerLayer(AbstractContainerScreen gui, GuiGraphicsExtractor guiGraphics, ContainerScreenTerminalStorage.DrawLayer layer,
                                      float partialTick, int x, int y, int mouseX, int mouseY,
                                      ITerminalStorageTabClient tab, int channel, @Nullable String label) {
        long maxQuantity = ((TerminalStorageTabIngredientComponentClient) tab).getMaxQuantity(channel);
        PendingCraftingJobOutput<T> pendingCraftingJobOutput = getPendingCraftingJobOutput(tab, channel, label);
        ingredientComponentViewHandler.getClient().drawInstance(guiGraphics, instance, maxQuantity, label, gui, layer, partialTick, x, y, mouseX, mouseY,
                createCraftingJobTooltipLines(pendingCraftingJobOutput), null);
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
    protected PendingCraftingJobOutput<T> getPendingCraftingJobOutput(ITerminalStorageTabClient tab, int channel,
                                                                     @Nullable String label) {
        return label == null
                ? ((TerminalStorageTabIngredientComponentClient<T, M>) tab).getPendingCraftingJobOutput(channel, getInstance())
                : null;
    }

    @Nullable
    protected List<Component> createCraftingJobTooltipLines(@Nullable PendingCraftingJobOutput<T> pendingCraftingJobOutput) {
        if (pendingCraftingJobOutput == null) {
            return null;
        }
        List<Component> tooltipLines = Lists.newArrayList();
        addCraftingJobTooltipLines(tooltipLines, pendingCraftingJobOutput);
        return tooltipLines;
    }

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
    protected void drawCraftingJobOverlay(GuiGraphicsExtractor guiGraphics, ContainerScreenTerminalStorage.DrawLayer layer,
                                          int x, int y, @Nullable PendingCraftingJobOutput<T> pendingCraftingJobOutput) {
        if (layer != ContainerScreenTerminalStorage.DrawLayer.BACKGROUND || pendingCraftingJobOutput == null) {
            return;
        }

        Triple<Float, Float, Float> color = IModHelpers.get().getBaseHelpers()
                .intToRGB(pendingCraftingJobOutput.getStatus().getColor());
        int frame = (int) ((System.currentTimeMillis() / SPINNER_FRAME_DURATION) % Images.SPINNER.length);

        // Drawn after the instance, so that it is layered in front of it.
        Images.SPINNER[frame].drawWithColor(guiGraphics, x, y,
                color.getLeft(), color.getMiddle(), color.getRight(), 1F);
    }

}
