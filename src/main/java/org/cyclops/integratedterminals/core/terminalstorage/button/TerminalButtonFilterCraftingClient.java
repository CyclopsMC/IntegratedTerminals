package org.cyclops.integratedterminals.core.terminalstorage.button;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButtonClient;
import org.cyclops.integratedterminals.client.gui.image.Images;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentCommon;

/**
 * @author rubensworks
 */
public class TerminalButtonFilterCraftingClient<T> implements ITerminalButtonClient<TerminalStorageTabIngredientComponentClient<T, ?>,
        TerminalStorageTabIngredientComponentCommon<T, ?>, ButtonImage> {

    private final TerminalButtonFilterCrafting<T> button;

    public TerminalButtonFilterCraftingClient(TerminalButtonFilterCrafting<T> button) {
        this.button = button;
    }

    @Override
    public ButtonImage createButton(int x, int y) {
        return new ButtonImage(x, y,
                Component.translatable("gui.integratedterminals.terminal_storage.craftinggrid.clear"),
                (b) -> {},
                this.button.active == TerminalButtonFilterCrafting.FilterType.ALL ? Images.BUTTON_BACKGROUND_INACTIVE : Images.BUTTON_BACKGROUND_ACTIVE,
                this.button.active.getImage());
    }

    @Override
    public void onClick(TerminalStorageTabIngredientComponentClient<T, ?> clientTab,
                        TerminalStorageTabIngredientComponentCommon<T, ?> commomTab, ButtonImage guiButton,
                        int channel, MouseButtonEvent mouse, boolean isDoubleClick) {
        this.button.active = mouse.button() == 0 ? TerminalButtonFilterCrafting.FilterType.values()[(this.button.active.ordinal() + 1) % TerminalButtonFilterCrafting.FilterType.values().length] : TerminalButtonFilterCrafting.FilterType.ALL;

        CompoundTag data = new CompoundTag();
        data.putInt("active", this.button.active.ordinal());
        this.button.state.setButton(clientTab.getTabSettingsName().toString(), this.button.buttonName, data);

        clientTab.resetFilteredIngredientsViews(channel);
    }

}
