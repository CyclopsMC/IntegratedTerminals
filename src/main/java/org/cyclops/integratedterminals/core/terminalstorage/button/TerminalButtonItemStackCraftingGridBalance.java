package org.cyclops.integratedterminals.core.terminalstorage.button;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.client.gui.image.Images;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientItemStackCraftingGridBalance;

import java.util.List;

/**
 * A button for balancing all slots in the crafting grid.
 * @author rubensworks
 */
public class TerminalButtonItemStackCraftingGridBalance<T>
        implements ITerminalButton<TerminalStorageTabIngredientComponentClient<T, ?>,
        TerminalStorageTabIngredientComponentItemStackCraftingCommon, ButtonImage> {

    @Override
    public void reloadFromState() {

    }

    @Override
    public int getX(int guiLeft, int offset, int gridXSize, int gridYSize, int playerInventoryOffsetX, int playerInventoryOffsetY) {
        return guiLeft + (gridXSize / 2) + 32 - (playerInventoryOffsetX > 0 ? 107 : 0);
    }

    @Override
    public int getY(int guiTop, int offset, int gridXSize, int gridYSize, int playerInventoryOffsetX, int playerInventoryOffsetY) {
        return guiTop + gridYSize + 69;
    }

    @Override
    public boolean isInLeftColumn() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ButtonImage createButton(int x, int y) {
        return new ButtonImage(x, y,
                new TranslatableComponent("gui.integratedterminals.terminal_storage.craftinggrid.balance"),
                (b) -> {},
                Images.BUTTON_SMALL_BACKGROUND_INACTIVE,
                Images.BUTTON_SMALL_OVERLAY_SQUARE);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClick(TerminalStorageTabIngredientComponentClient<T, ?> clientTab,
                        TerminalStorageTabIngredientComponentItemStackCraftingCommon commomTab, ButtonImage guiButton,
                        int channel, int mouseButton) {
        IntegratedTerminals._instance.getPacketHandler().sendToServer(
                new TerminalStorageIngredientItemStackCraftingGridBalance(commomTab.getName().toString()));
    }

    @Override
    public String getTranslationKey() {
        return "gui.integratedterminals.terminal_storage.craftinggrid.balance";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getTooltip(Player player, TooltipFlag tooltipFlag, List<Component> lines) {
        lines.add(new TranslatableComponent("gui." + Reference.MOD_ID + ".terminal_storage.craftinggrid.balance.info").withStyle(ChatFormatting.GRAY));
    }
}
