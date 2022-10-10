package org.cyclops.integratedterminals.core.terminalstorage.button;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalRowColumnProvider;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.event.TerminalStorageScreenSizeEvent;
import org.cyclops.integratedterminals.client.gui.image.Images;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentCommon;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageState;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A button for configuring the scale of the gui.
 * @author rubensworks
 */
public class TerminalButtonScaleGui<T>
        implements ITerminalButton<TerminalStorageTabIngredientComponentClient<T, ?>,
        TerminalStorageTabIngredientComponentCommon<T, ?>, ButtonImage> {

    private final TerminalStorageState state;
    private final String buttonName;
    private final ITerminalStorageTabClient<?> clientTab;

    private GuiScale scale;

    public TerminalButtonScaleGui(TerminalStorageState state, ITerminalStorageTabClient<?> clientTab) {
        this.state = state;
        this.buttonName = "gui_scale";
        this.clientTab = clientTab;

        reloadFromState();
    }

    @Override
    public void reloadFromState() {
        if (state.hasButton("minecraft:itemstack", this.buttonName)) {
            CompoundTag data = (CompoundTag) state.getButton("minecraft:itemstack", this.buttonName);
            this.scale = GuiScale.values()[data.getInt("scale")];
        } else {
            this.scale = GuiScale.SCALE_XY;
        }
    }

    @Override
    public ButtonImage createButton(int x, int y) {
        return new ButtonImage(x, y,
                Component.translatable("gui.integratedterminals.terminal_storage.scale"),
                (b) -> {},
                scale == GuiScale.SCALE_XY ? Images.BUTTON_BACKGROUND_INACTIVE : Images.BUTTON_BACKGROUND_ACTIVE,
                scale.getImage());
    }

    @Override
    public void onClick(TerminalStorageTabIngredientComponentClient<T, ?> clientTab, @Nullable TerminalStorageTabIngredientComponentCommon<T, ?> commonTab, ButtonImage guiButton, int channel, int mouseButton) {
        this.scale = mouseButton == 0 ? GuiScale.values()[(this.scale.ordinal() + 1) % GuiScale.values().length] : GuiScale.SCALE_XY;

        CompoundTag data = new CompoundTag();
        data.putInt("scale", scale.ordinal());
        state.setButton(clientTab.getTabSettingsName().toString(), this.buttonName, data);

        clientTab.resetScale();
    }

    @Override
    public String getTranslationKey() {
        return "gui.integratedterminals.terminal_storage.scale";
    }

    @Override
    public void getTooltip(Player player, TooltipFlag tooltipFlag, List<Component> lines) {
        lines.add(Component.translatable("gui.integratedterminals.terminal_storage.scale.info").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable(scale.getLabel()));
    }

    public ITerminalRowColumnProvider getRowColumnProvider() {
        return this.scale.getRowColumnProvider();
    }

    public static enum GuiScale {
        SCALE_XY(Images.BUTTON_MIDDLE_SCALE_XY,
                "gui.integratedterminals.terminal_storage.scale.scalexy", () -> {
            Pair<Integer, Integer> widthHeight = TerminalStorageScreenSizeEvent.getWidthHeight();
            return new ITerminalRowColumnProvider.RowsAndColumns(
                    (int) Math.min(Math.max(1, Math.ceil((widthHeight.getRight() - 146) / GuiHelpers.SLOT_SIZE)), GeneralConfig.guiStorageScaleMaxRows),
                    (int) Math.min(Math.max(1, Math.ceil((widthHeight.getLeft() - 56) / GuiHelpers.SLOT_SIZE)), GeneralConfig.guiStorageScaleMaxColumns)
            );
        }),
        SCALE_Y(Images.BUTTON_MIDDLE_SCALE_Y,
                "gui.integratedterminals.terminal_storage.scale.scaley", () -> new ITerminalRowColumnProvider.RowsAndColumns(
                (int) Math.min(Math.max(1, Math.ceil((TerminalStorageScreenSizeEvent.getWidthHeight().getRight() - 146) / GuiHelpers.SLOT_SIZE)), GeneralConfig.guiStorageScaleMaxRows),
                GeneralConfig.guiStorageScaleHeightColumns
        )),
        SCALE_X(Images.BUTTON_MIDDLE_SCALE_X,
                "gui.integratedterminals.terminal_storage.scale.scalex", () -> new ITerminalRowColumnProvider.RowsAndColumns(
                GeneralConfig.guiStorageScaleWidthRows,
                (int) Math.min(Math.max(1, Math.ceil((TerminalStorageScreenSizeEvent.getWidthHeight().getLeft() - 56) / GuiHelpers.SLOT_SIZE)), GeneralConfig.guiStorageScaleMaxColumns)
        )),
        SMALL(Images.BUTTON_MIDDLE_SCALE_SMALL,
                "gui.integratedterminals.terminal_storage.scale.small", () -> new ITerminalRowColumnProvider.RowsAndColumns(GeneralConfig.guiStorageScaleSmallRows, GeneralConfig.guiStorageScaleSmallColumns)),
        MEDIUM(Images.BUTTON_MIDDLE_SCALE_MEDIUM,
                "gui.integratedterminals.terminal_storage.scale.medium", () -> new ITerminalRowColumnProvider.RowsAndColumns(GeneralConfig.guiStorageScaleMediumRows, GeneralConfig.guiStorageScaleMediumColumns)),
        LARGE(Images.BUTTON_MIDDLE_SCALE_LARGE,
                "gui.integratedterminals.terminal_storage.scale.large", () -> new ITerminalRowColumnProvider.RowsAndColumns(GeneralConfig.guiStorageScaleLargeRows, GeneralConfig.guiStorageScaleLargeColumns));

        @Nullable
        private final IImage image;
        private final String label;
        private final ITerminalRowColumnProvider rowColumnProvider;

        GuiScale(@Nullable IImage image, String label, ITerminalRowColumnProvider rowColumnProvider) {
            this.image = image;
            this.label = label;
            this.rowColumnProvider = rowColumnProvider;
        }

        @Nullable
        public IImage getImage() {
            return image;
        }

        public String getLabel() {
            return label;
        }

        public ITerminalRowColumnProvider getRowColumnProvider() {
            return rowColumnProvider;
        }
    }
}
