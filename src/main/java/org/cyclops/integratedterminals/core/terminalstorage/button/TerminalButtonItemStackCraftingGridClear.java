package org.cyclops.integratedterminals.core.terminalstorage.button;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.client.gui.image.Images;
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
    public void reloadFromState() {

    }

    @Override
    public int getX(int guiLeft, int offset) {
        return guiLeft + 85;
    }

    @Override
    public int getY(int guiTop, int offset) {
        return guiTop + 57;
    }

    @Override
    public boolean isInLeftColumn() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ButtonImage createButton(int x, int y) {
        return new ButtonImage(x, y,
                new TranslationTextComponent("gui.integratedterminals.terminal_storage.craftinggrid.clear"),
                (b) -> {},
                Images.BUTTON_SMALL_BACKGROUND_INACTIVE,
                Images.BUTTON_SMALL_OVERLAY_CROSS);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClick(TerminalStorageTabIngredientComponentClient<T, ?> clientTab,
                        TerminalStorageTabIngredientComponentItemStackCraftingCommon commomTab, ButtonImage guiButton,
                        int channel, int mouseButton) {
        boolean toStorage = !MinecraftHelpers.isShifted();
        TerminalButtonItemStackCraftingGridClear.clearGrid(commomTab, channel, toStorage);
    }

    @Override
    public String getTranslationKey() {
        return "gui.integratedterminals.terminal_storage.craftinggrid.clear";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getTooltip(PlayerEntity player, ITooltipFlag tooltipFlag, List<ITextComponent> lines) {
        lines.add(new TranslationTextComponent("gui." + Reference.MOD_ID + ".terminal_storage.craftinggrid.clear.info"));
    }

    public static void clearGrid(TerminalStorageTabIngredientComponentItemStackCraftingCommon commomTab,
                                 int channel, boolean toStorage) {
        IntegratedTerminals._instance.getPacketHandler().sendToServer(
                new TerminalStorageIngredientItemStackCraftingGridClear(commomTab.getName().toString(), channel, toStorage));
        commomTab.getInventoryCraftResult().setInventorySlotContents(0, ItemStack.EMPTY);
    }
}
