package org.cyclops.integratedterminals.capability.ingredient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.cyclops.cyclopscore.client.gui.GuiGraphicsExtended;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandlerClient;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.core.terminalstorage.query.SearchMode;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author rubensworks
 */
public class IngredientComponentTerminalStorageHandlerItemStackClient implements IIngredientComponentTerminalStorageHandlerClient<ItemStack, Integer> {

    private final IngredientComponentTerminalStorageHandlerItemStack handler;

    public IngredientComponentTerminalStorageHandlerItemStackClient(IngredientComponentTerminalStorageHandlerItemStack handler) {
        this.handler = handler;
    }

    @Override
    public void drawInstance(GuiGraphicsExtractor guiGraphics, ItemStack instance, long maxQuantity, @Nullable String label, AbstractContainerScreen gui,
                             ContainerScreenTerminalStorage.DrawLayer layer, float partialTick, int x, int y,
                             int mouseX, int mouseY, @Nullable List<Component> additionalTooltipLines) {
        // Make a copy of the item to make sure that any changes in the NBT tag that the mod may make during rendering
        // does not propagate into our client-side index. Otherwise, the client may think it has different items than
        // the server, which will cause these items not to be extractable by the client from the terminal.
        // See https://github.com/CyclopsMC/IntegratedTerminals/issues/106
        final ItemStack instanceCopy = instance.copy();

        GuiGraphicsExtended renderItem = new GuiGraphicsExtended(guiGraphics);
        if (layer == ContainerScreenTerminalStorage.DrawLayer.BACKGROUND) {
            guiGraphics.item(instanceCopy, x, y);
            renderItem.itemDecorations(Minecraft.getInstance().font, instanceCopy, x, y, label);
        } else {
            IModHelpers.get().getGuiHelpers().renderTooltip(gui, guiGraphics, x, y, IModHelpers.get().getGuiHelpers().getSlotSizeInner(), IModHelpers.get().getGuiHelpers().getSlotSizeInner(), mouseX, mouseY, () -> {
                List<Component> lines = instanceCopy.getTooltipLines(
                        Item.TooltipContext.of(Minecraft.getInstance().player.registryAccess()),
                        Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips
                                ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
                if (additionalTooltipLines != null) {
                    lines.addAll(additionalTooltipLines);
                }
                this.handler.addQuantityTooltip(lines, instanceCopy);
                return lines;
            });
        }
    }

    @Override
    public Predicate<ItemStack> getInstanceFilterPredicate(SearchMode searchMode, String query) {
        return switch (searchMode) {
            case MOD -> i -> Optional.ofNullable(i.getItem().getCreatorModId(Minecraft.getInstance().getConnection().registryAccess(), i))
                    .orElse("minecraft").toLowerCase(Locale.ENGLISH)
                    .matches(".*" + query + ".*");
            case TOOLTIP -> i -> i.getTooltipLines(Item.TooltipContext.of(Minecraft.getInstance().player.registryAccess()), Minecraft.getInstance().player, TooltipFlag.Default.NORMAL).stream()
                    .anyMatch(s -> s.getString().toLowerCase(Locale.ENGLISH).matches(".*" + query + ".*"));
            case TAG -> i -> i.getItem().builtInRegistryHolder().tags()
                    .filter(tag -> tag.location().toString().toLowerCase(Locale.ENGLISH).matches(".*" + query + ".*"))
                    .anyMatch(tag -> BuiltInRegistries.ITEM.get(tag).isPresent());
            case DEFAULT -> i -> i.getHoverName().getString().toLowerCase(Locale.ENGLISH).matches(".*" + query + ".*");
        };
    }

}
