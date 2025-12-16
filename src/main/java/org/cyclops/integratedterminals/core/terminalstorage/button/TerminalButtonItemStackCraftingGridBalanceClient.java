package org.cyclops.integratedterminals.core.terminalstorage.button;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButtonClient;
import org.cyclops.integratedterminals.client.gui.image.Images;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientItemStackCraftingGridBalance;

/**
 * @author rubensworks
 */
public class TerminalButtonItemStackCraftingGridBalanceClient<T>
        implements ITerminalButtonClient<TerminalStorageTabIngredientComponentClient<T, ?>,
                TerminalStorageTabIngredientComponentItemStackCraftingCommon, ButtonImage> {

    private final TerminalButtonItemStackCraftingGridBalance<T> button;

    public TerminalButtonItemStackCraftingGridBalanceClient(TerminalButtonItemStackCraftingGridBalance<T> button) {
        this.button = button;
    }

    @Override
    public ButtonImage createButton(int x, int y) {
        return new ButtonImage(x, y,
                Component.translatable("gui.integratedterminals.terminal_storage.craftinggrid.balance"),
                (b) -> {},
                Images.BUTTON_SMALL_BACKGROUND_INACTIVE,
                Images.BUTTON_SMALL_OVERLAY_SQUARE);
    }

    @Override
    public void onClick(TerminalStorageTabIngredientComponentClient<T, ?> clientTab,
                        TerminalStorageTabIngredientComponentItemStackCraftingCommon commonTab, ButtonImage guiButton,
                        int channel, MouseButtonEvent mouse, boolean isDoubleClick) {
        IntegratedTerminals._instance.getPacketHandler().sendToServer(
                new TerminalStorageIngredientItemStackCraftingGridBalance(commonTab.getName().toString()));
    }
}
