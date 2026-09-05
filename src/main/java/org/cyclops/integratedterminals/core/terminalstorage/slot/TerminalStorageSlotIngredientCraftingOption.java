package org.cyclops.integratedterminals.core.terminalstorage.slot;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.cyclopscore.client.gui.GuiGraphicsExtended;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlan;
import org.cyclops.integratedterminals.client.gui.tooltip.CraftingOptionIngredientsTooltip;
import org.cyclops.integratedterminals.client.gui.tooltip.TooltipRenderHelpers;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageChannels;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.PendingCraftingJobOutput;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.TerminalCraftingOptionInputs;

import javax.annotation.Nullable;
import java.util.List;
import java.util.OptionalInt;

/**
 * An ingredient slot for a crafting option.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class TerminalStorageSlotIngredientCraftingOption<T, M> extends TerminalStorageSlotIngredient<T, M> {

    private final HandlerWrappedTerminalCraftingOption<T> craftingOption;

    public TerminalStorageSlotIngredientCraftingOption(IIngredientComponentTerminalStorageHandler<T, M> ingredientComponentViewHandler, T instance, HandlerWrappedTerminalCraftingOption<T> craftingOption) {
        super(ingredientComponentViewHandler, instance);
        this.craftingOption = craftingOption;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawGuiContainerLayer(AbstractContainerScreen gui, GuiGraphics guiGraphics, ContainerScreenTerminalStorage.DrawLayer layer,
                                      float partialTick, int x, int y, int mouseX, int mouseY,
                                      ITerminalStorageTabClient tab, int channel, @Nullable String label) {
        IIngredientComponentTerminalStorageHandler<T, M> viewHandler = getIngredientComponentViewHandler();
        long maxQuantity = ((TerminalStorageTabIngredientComponentClient) tab).getMaxQuantity(channel);
        PendingCraftingJobOutput<T> pendingCraftingJobOutput = getPendingCraftingJobOutput(tab, channel, label);
        if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND) {
            viewHandler.drawInstance(guiGraphics, getInstance(), maxQuantity, null, gui, layer, partialTick, x, y, mouseX, mouseY, null);
            drawCraftLabel(guiGraphics, x, y);
        } else {
            // This is called for all visible slots on every frame,
            // so only determine the tooltip contents when they are actually going to be shown.
            boolean hovering = TooltipRenderHelpers.isHovering(gui, x, y,
                    GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER, mouseX, mouseY);
            List<List<IPrototypedIngredient<?, ?>>> inputs = hovering ? getInputs() : List.of();
            viewHandler.drawInstance(guiGraphics, getInstance(), maxQuantity, label, gui, layer, partialTick, x, y, mouseX, mouseY,
                    hovering ? getTooltipLines(pendingCraftingJobOutput, inputs, tab, channel, label) : null,
                    inputs.isEmpty() ? null : new CraftingOptionIngredientsTooltip(inputs));
        }
        drawCraftingJobOverlay(guiGraphics, layer, x, y, pendingCraftingJobOutput);
    }

    @OnlyIn(Dist.CLIENT)
    protected List<Component> getTooltipLines(@Nullable PendingCraftingJobOutput<T> pendingCraftingJobOutput,
                                              List<List<IPrototypedIngredient<?, ?>>> inputs,
                                              ITerminalStorageTabClient tab, int channel, @Nullable String label) {
        List<Component> tooltipLines = createTooltipLines(pendingCraftingJobOutput, tab, channel, label);
        // An unknown duration says nothing here, so it is left out rather than shown as a placeholder
        long estimatedTickDuration = getCraftingOption().getCraftingOption().getEstimatedTickDuration();
        if (estimatedTickDuration >= 0) {
            tooltipLines.add(Component.translatable("gui.integratedterminals.terminal_storage.tooltip.duration",
                    GuiCraftingPlan.getDurationValue(estimatedTickDuration)));
        }
        if (!inputs.isEmpty()) {
            tooltipLines.add(Component.translatable("gui.integratedterminals.terminal_storage.tooltip.requirements")
                    .withStyle(ChatFormatting.YELLOW));
        }
        return tooltipLines;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected List<Component> createChannelTooltipLines(ITerminalStorageTabClient tab) {
        // Contrary to stored ingredients, a crafting option is only available in a single channel.
        OptionalInt channel = ((TerminalStorageTabIngredientComponentClient<T, M>) tab)
                .getCraftingOptionChannel(getCraftingOption());
        return channel.isPresent()
                ? List.of(TerminalStorageChannels.createChannelLine(channel.getAsInt()))
                : List.of();
    }

    /**
     * @return The inputs that are required by this crafting option, with all their alternatives.
     */
    protected List<List<IPrototypedIngredient<?, ?>>> getInputs() {
        return TerminalCraftingOptionInputs.getGroupedInputs(getCraftingOption().getCraftingOption());
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    protected PendingCraftingJobOutput<T> getPendingCraftingJobOutput(ITerminalStorageTabClient tab, int channel,
                                                                      @Nullable String label) {
        // The same instance can also be shown as a stored ingredient.
        // In that case, only that slot indicates the running crafting jobs, to avoid indicating them twice.
        return ((TerminalStorageTabIngredientComponentClient<T, M>) tab).isShownAsStoredInstance(channel, getInstance())
                ? null : super.getPendingCraftingJobOutput(tab, channel, label);
    }

    public HandlerWrappedTerminalCraftingOption<T> getCraftingOption() {
        return craftingOption;
    }

    private void drawCraftLabel(GuiGraphics guiGraphics, int x, int y) {
        new GuiGraphicsExtended(guiGraphics).drawSlotText(Minecraft.getInstance().font,
                ChatFormatting.GOLD + L10NHelpers.localize("gui.integratedterminals.terminal_storage.craft"), x, y - 11);
    }

}
