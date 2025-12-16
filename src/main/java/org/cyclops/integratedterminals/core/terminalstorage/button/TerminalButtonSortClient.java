package org.cyclops.integratedterminals.core.terminalstorage.button;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButtonClient;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.client.gui.ButtonSort;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;

/**
 * @author rubensworks
 */
public class TerminalButtonSortClient<T> implements ITerminalButtonClient<TerminalStorageTabIngredientComponentClient<T, ?>,
        ITerminalStorageTabCommon, ButtonSort> {

    private final TerminalButtonSort<T> button;

    public TerminalButtonSortClient(TerminalButtonSort<T> button) {
        this.button = button;
    }

    @Override
    public ButtonSort createButton(int x, int y) {
        return new ButtonSort(x, y, Component.translatable("gui.integratedterminals.terminal_storage.sort"), (b) -> {}, this.button.instanceSorter.getIcon(), this.button.active, this.button.descending);
    }

    @Override
    public void onClick(TerminalStorageTabIngredientComponentClient<T, ?> clientTab, ITerminalStorageTabCommon commonTab,
                        ButtonSort guiButton, int channel, MouseButtonEvent mouse, boolean isDoubleClick) {
        if (mouse.button() == 0) {
            if (this.button.active) {
                if (this.button.descending) {
                    this.button.descending = false;
                } else {
                    this.button.active = false;
                }
            } else {
                this.button.active = true;
                this.button.descending = true;
            }
        } else {
            this.button.active = false;
            this.button.descending = true;
        }

        CompoundTag data = new CompoundTag();
        data.putBoolean("active", this.button.active);
        data.putBoolean("descending", this.button.descending);
        this.button.state.setButton(clientTab.getTabSettingsName().toString(), this.button.buttonName, data);

        this.button.updateSorter();
        clientTab.resetFilteredIngredientsViews(channel);
    }
}
