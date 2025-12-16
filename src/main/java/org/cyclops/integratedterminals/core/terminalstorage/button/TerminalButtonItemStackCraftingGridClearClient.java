package org.cyclops.integratedterminals.core.terminalstorage.button;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButtonClient;
import org.cyclops.integratedterminals.client.gui.image.Images;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;

/**
 * @author rubensworks
 */
public class TerminalButtonItemStackCraftingGridClearClient<T>
        implements ITerminalButtonClient<TerminalStorageTabIngredientComponentClient<T, ?>,
                TerminalStorageTabIngredientComponentItemStackCraftingCommon, ButtonImage> {

    private final TerminalButtonItemStackCraftingGridClear<T> button;

    public TerminalButtonItemStackCraftingGridClearClient(TerminalButtonItemStackCraftingGridClear<T> button) {
        this.button = button;
    }

    @Override
    public ButtonImage createButton(int x, int y) {
        return new ButtonImage(x, y,
                Component.translatable("gui.integratedterminals.terminal_storage.craftinggrid.clear"),
                (b) -> {},
                Images.BUTTON_SMALL_BACKGROUND_INACTIVE,
                Images.BUTTON_SMALL_OVERLAY_CROSS);
    }

    @Override
    public void onClick(TerminalStorageTabIngredientComponentClient<T, ?> clientTab,
                        TerminalStorageTabIngredientComponentItemStackCraftingCommon commomTab, ButtonImage guiButton,
                        int channel, MouseButtonEvent mouse, boolean isDoubleClick) {
        boolean toStorage = !IModHelpers.get().getMinecraftClientHelpers().isShifted();
        TerminalButtonItemStackCraftingGridClear.clearGrid(commomTab, channel, toStorage);
    }
}
