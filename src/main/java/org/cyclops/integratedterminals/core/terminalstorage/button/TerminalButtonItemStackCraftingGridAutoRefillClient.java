package org.cyclops.integratedterminals.core.terminalstorage.button;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButtonClient;
import org.cyclops.integratedterminals.client.gui.image.Images;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;

/**
 * @author rubensworks
 */
public class TerminalButtonItemStackCraftingGridAutoRefillClient<T>
        implements ITerminalButtonClient<TerminalStorageTabIngredientComponentClient<T, ?>,
                TerminalStorageTabIngredientComponentItemStackCraftingCommon, ButtonImage> {

    private final TerminalButtonItemStackCraftingGridAutoRefill<T> button;

    public TerminalButtonItemStackCraftingGridAutoRefillClient(TerminalButtonItemStackCraftingGridAutoRefill<T> button) {
        this.button = button;
    }

    @Override
    public ButtonImage createButton(int x, int y) {
        return new ButtonImage(x, y,
                Component.translatable("gui.integratedterminals.terminal_storage.craftinggrid.autorefill"),
                (b) -> {},
                this.button.active == TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType.DISABLED ? Images.BUTTON_BACKGROUND_INACTIVE : Images.BUTTON_BACKGROUND_ACTIVE,
                this.button.active.getImage());
    }

    @Override
    public void onClick(TerminalStorageTabIngredientComponentClient<T, ?> clientTab,
                        TerminalStorageTabIngredientComponentItemStackCraftingCommon commomTab, ButtonImage guiButton,
                        int channel, MouseButtonEvent mouse, boolean isDoubleClick) {
        this.button.active = mouse.button() == 0 ? TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType.values()[(this.button.active.ordinal() + 1) % TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType.values().length] : TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType.DISABLED;

        CompoundTag data = new CompoundTag();
        data.putInt("active", this.button.active.ordinal());
        this.button.state.setButton(clientTab.getTabSettingsName().toString(), this.button.buttonName, data);

        this.button.notifyServer(clientTab);
    }

}
