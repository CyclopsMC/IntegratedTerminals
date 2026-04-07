package org.cyclops.integratedterminals.capability.ingredient;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.cyclopscore.client.gui.GuiGraphicsExtended;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.helper.IModHelpersNeoForge;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandlerClient;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.core.terminalstorage.query.SearchMode;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * @author rubensworks
 */
public class IngredientComponentTerminalStorageHandlerFluidStackClient implements IIngredientComponentTerminalStorageHandlerClient<FluidStack, Integer> {

    private final IngredientComponentTerminalStorageHandlerFluidStack handler;

    public IngredientComponentTerminalStorageHandlerFluidStackClient(IngredientComponentTerminalStorageHandlerFluidStack handler) {
        this.handler = handler;
    }

    @Override
    public void drawInstance(GuiGraphicsExtractor guiGraphics, FluidStack instance, long maxQuantity, @Nullable String label, AbstractContainerScreen gui,
                             ContainerScreenTerminalStorage.DrawLayer layer, float partialTick,
                             int x, int y, int mouseX, int mouseY,
                             @Nullable List<Component> additionalTooltipLines) {
        if (instance != null) {
            if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND) {
                // Draw fluid
                IModHelpersNeoForge.get().getGuiHelpers().renderFluidSlot(guiGraphics, instance, x, y);

                // Draw amount
                GuiGraphicsExtended renderItem = new GuiGraphicsExtended(guiGraphics);
                renderItem.drawSlotText(Minecraft.getInstance().font, label != null ? label : IModHelpers.get().getGuiHelpers().quantityToScaledString(instance.getAmount()), x, y);
            } else {
                IModHelpers.get().getGuiHelpers().renderTooltip(gui, guiGraphics, x, y, IModHelpers.get().getGuiHelpers().getSlotSizeInner(), IModHelpers.get().getGuiHelpers().getSlotSizeInner(), mouseX, mouseY, () -> {
                    List<Component> lines = Lists.newArrayList();
                    lines.add(((MutableComponent) instance.getHoverName()).withStyle(instance.getFluid().getFluidType().getRarity().getStyleModifier().apply(Style.EMPTY)));
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
    public Predicate<FluidStack> getInstanceFilterPredicate(SearchMode searchMode, String query) {
        return switch (searchMode) {
            case MOD -> i -> BuiltInRegistries.FLUID.getKey(i.getFluid()).getNamespace()
                    .toLowerCase(Locale.ENGLISH).matches(".*" + query + ".*");
            case TOOLTIP -> i -> false; // Fluids have no tooltip
            case TAG -> i -> i.getFluid().builtInRegistryHolder().tags()
                    .filter(tag -> tag.location().toString().toLowerCase(Locale.ENGLISH).matches(".*" + query + ".*"))
                    .anyMatch(tag -> BuiltInRegistries.FLUID.get(tag).isPresent());
            case DEFAULT -> i -> i != null && i.getHoverName().getString().toLowerCase(Locale.ENGLISH).matches(".*" + query + ".*");
        };
    }

}
