package org.cyclops.integratedterminals.core.terminalstorage.slot;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.cyclopscore.client.gui.GuiGraphicsExtended;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.client.gui.tooltip.CraftingOptionIngredientsTooltip;
import org.cyclops.integratedterminals.client.gui.tooltip.TooltipRenderHelpers;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.PendingCraftingJobOutput;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.TerminalCraftingOptionInputs;

import javax.annotation.Nullable;
import java.util.List;

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
    public void drawGuiContainerLayer(AbstractContainerScreen gui, GuiGraphicsExtractor guiGraphics, ContainerScreenTerminalStorage.DrawLayer layer,
                                      float partialTick, int x, int y, int mouseX, int mouseY,
                                      ITerminalStorageTabClient tab, int channel, @Nullable String label) {
        IIngredientComponentTerminalStorageHandler<T, M> viewHandler = getIngredientComponentViewHandler();
        long maxQuantity = ((TerminalStorageTabIngredientComponentClient) tab).getMaxQuantity(channel);
        PendingCraftingJobOutput<T> pendingCraftingJobOutput = getPendingCraftingJobOutput(tab, channel, label);
        if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND) {
            viewHandler.getClient().drawInstance(guiGraphics, getInstance(), maxQuantity, null, gui, layer, partialTick, x, y, mouseX, mouseY, null, null);
            drawCraftLabel(guiGraphics, x, y);
        } else {
            // This is called for all visible slots on every frame,
            // so only determine the requirements when they are actually going to be shown.
            int slotSizeInner = IModHelpers.get().getGuiHelpers().getSlotSizeInner();
            List<List<IPrototypedIngredient<?, ?>>> inputs = TooltipRenderHelpers.isHovering(gui, x, y,
                    slotSizeInner, slotSizeInner, mouseX, mouseY)
                    ? getInputs() : List.of();
            viewHandler.getClient().drawInstance(guiGraphics, getInstance(), maxQuantity, label, gui, layer, partialTick, x, y, mouseX, mouseY,
                    getTooltipLines(pendingCraftingJobOutput, inputs),
                    inputs.isEmpty() ? null : new CraftingOptionIngredientsTooltip(inputs));
        }
        drawCraftingJobOverlay(guiGraphics, layer, x, y, pendingCraftingJobOutput);
    }

    protected List<Component> getTooltipLines(@Nullable PendingCraftingJobOutput<T> pendingCraftingJobOutput,
                                              List<List<IPrototypedIngredient<?, ?>>> inputs) {
        List<Component> tooltipLines = Lists.newArrayList();
        if (pendingCraftingJobOutput != null) {
            addCraftingJobTooltipLines(tooltipLines, pendingCraftingJobOutput);
        }
        if (!inputs.isEmpty()) {
            tooltipLines.add(Component.translatable("gui.integratedterminals.terminal_storage.tooltip.requirements")
                    .withStyle(ChatFormatting.YELLOW));
        }
        return tooltipLines;
    }

    /**
     * @return The inputs that are required by this crafting option, with all their alternatives.
     */
    protected List<List<IPrototypedIngredient<?, ?>>> getInputs() {
        return TerminalCraftingOptionInputs.getGroupedInputs(getCraftingOption().getCraftingOption());
    }

    @Nullable
    @Override
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

    private void drawCraftLabel(GuiGraphicsExtractor guiGraphics, int x, int y) {
        new GuiGraphicsExtended(guiGraphics).drawSlotText(Minecraft.getInstance().font,
                ChatFormatting.GOLD + IModHelpers.get().getL10NHelpers().localize("gui.integratedterminals.terminal_storage.craft"), x, y - 11);
    }

}
