package org.cyclops.integratedterminals.core.terminalstorage.button;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButtonClient;
import org.cyclops.integratedterminals.client.gui.image.Images;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentCommon;

import javax.annotation.Nullable;

/**
 * @author rubensworks
 */
public class TerminalButtonScaleGuiClient<T>
        implements ITerminalButtonClient<TerminalStorageTabIngredientComponentClient<T, ?>,
                TerminalStorageTabIngredientComponentCommon<T, ?>, ButtonImage> {

    private final TerminalButtonScaleGui<T> button;

    public TerminalButtonScaleGuiClient(TerminalButtonScaleGui<T> button) {
        this.button = button;
    }

    @Override
    public ButtonImage createButton(int x, int y) {
        return new ButtonImage(x, y,
                Component.translatable("gui.integratedterminals.terminal_storage.scale"),
                (b) -> {},
                this.button.scale == TerminalButtonScaleGui.GuiScale.SCALE_XY ? Images.BUTTON_BACKGROUND_INACTIVE : Images.BUTTON_BACKGROUND_ACTIVE,
                this.button.scale.getImage());
    }

    @Override
    public void onClick(TerminalStorageTabIngredientComponentClient<T, ?> clientTab, @Nullable TerminalStorageTabIngredientComponentCommon<T, ?> commonTab, ButtonImage guiButton, int channel, MouseButtonEvent mouse, boolean isDoubleClick) {
        this.button.scale = mouse.button() == 0 ? TerminalButtonScaleGui.GuiScale.values()[(this.button.scale.ordinal() + 1) % TerminalButtonScaleGui.GuiScale.values().length] : TerminalButtonScaleGui.GuiScale.SCALE_XY;

        CompoundTag data = new CompoundTag();
        data.putInt("scale", this.button.scale.ordinal());
        this.button.state.setButton(clientTab.getTabSettingsName().toString(), this.button.buttonName, data);

        clientTab.resetScale();
    }

}
