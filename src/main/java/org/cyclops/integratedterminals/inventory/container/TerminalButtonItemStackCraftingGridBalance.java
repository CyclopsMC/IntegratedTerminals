package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonImage;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.client.gui.image.Images;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientItemStackCraftingGridBalance;

import java.util.List;

/**
 * A button for balancing all slots in the crafting grid.
 * @author rubensworks
 */
public class TerminalButtonItemStackCraftingGridBalance<T>
        implements ITerminalButton<TerminalStorageTabIngredientComponentClient<T, ?>,
        TerminalStorageTabIngredientComponentCommontemStackCrafting, GuiButtonImage> {

    @Override
    public int getX(int guiLeft, int offset) {
        return guiLeft + 85;
    }

    @Override
    public int getY(int guiTop, int offset) {
        return guiTop + 67;
    }

    @Override
    public boolean isInLeftColumn() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiButtonImage createButton(int x, int y) {
        return new GuiButtonImage(0, x, y, Images.BUTTON_SMALL_BACKGROUND_INACTIVE, Images.BUTTON_SMALL_OVERLAY_SQUARE);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onClick(TerminalStorageTabIngredientComponentClient<T, ?> clientTab,
                        TerminalStorageTabIngredientComponentCommontemStackCrafting commomTab, GuiButtonImage guiButton,
                        int channel, int mouseButton) {
        IntegratedTerminals._instance.getPacketHandler().sendToServer(
                new TerminalStorageIngredientItemStackCraftingGridBalance(commomTab.getId()));
    }

    @Override
    public String getUnlocalizedName() {
        return "gui.integratedterminals.terminal_storage.craftinggrid.balance";
    }

    @Override
    public void getTooltip(EntityPlayer player, ITooltipFlag tooltipFlag, List<String> lines) {
        lines.add(L10NHelpers.localize("gui." + Reference.MOD_ID + ".terminal_storage.craftinggrid.balance.info"));
    }
}
