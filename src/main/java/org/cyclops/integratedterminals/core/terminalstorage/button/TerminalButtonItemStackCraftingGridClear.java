package org.cyclops.integratedterminals.core.terminalstorage.button;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButtonClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientItemStackCraftingGridClear;

import java.util.List;

/**
 * A button for clearing the crafting grid.
 * @author rubensworks
 */
public class TerminalButtonItemStackCraftingGridClear<T>
        implements ITerminalButton<TerminalStorageTabIngredientComponentClient<T, ?>,
        TerminalStorageTabIngredientComponentItemStackCraftingCommon, ButtonImage> {

    @Override
    public ITerminalButtonClient<TerminalStorageTabIngredientComponentClient<T, ?>, TerminalStorageTabIngredientComponentItemStackCraftingCommon, ButtonImage> getClient() {
        return new TerminalButtonItemStackCraftingGridClearClient<>(this);
    }

    @Override
    public void reloadFromState() {

    }

    @Override
    public int getX(int guiLeft, int offset, int gridXSize, int gridYSize, int playerInventoryOffsetX, int playerInventoryOffsetY) {
        return guiLeft + (gridXSize / 2) + 32 - (playerInventoryOffsetX > 0 ? 107 : 0);
    }

    @Override
    public int getY(int guiTop, int offset, int gridXSize, int gridYSize, int playerInventoryOffsetX, int playerInventoryOffsetY) {
        return guiTop + gridYSize + 59;
    }

    @Override
    public boolean isInLeftColumn() {
        return false;
    }

    @Override
    public String getTranslationKey() {
        return "gui.integratedterminals.terminal_storage.craftinggrid.clear";
    }

    @Override
    public void getTooltip(Player player, TooltipFlag tooltipFlag, List<Component> lines) {
        lines.add(Component.translatable("gui." + Reference.MOD_ID + ".terminal_storage.craftinggrid.clear.info").withStyle(ChatFormatting.GRAY));
    }

    public static void clearGrid(TerminalStorageTabIngredientComponentItemStackCraftingCommon commomTab,
                                 int channel, boolean toStorage) {
        IntegratedTerminals._instance.getPacketHandler().sendToServer(
                new TerminalStorageIngredientItemStackCraftingGridClear(commomTab.getName().toString(), channel, toStorage));
        commomTab.getInventoryCraftResult().setItem(0, ItemStack.EMPTY);
    }
}
