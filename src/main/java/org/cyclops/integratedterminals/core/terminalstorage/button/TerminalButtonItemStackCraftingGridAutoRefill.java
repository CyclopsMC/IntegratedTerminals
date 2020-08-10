package org.cyclops.integratedterminals.core.terminalstorage.button;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.client.gui.image.Images;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageState;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientItemStackCraftingGridSetAutoRefill;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A button for clearing the crafting grid.
 * @author rubensworks
 */
public class TerminalButtonItemStackCraftingGridAutoRefill<T>
        implements ITerminalButton<TerminalStorageTabIngredientComponentClient<T, ?>,
        TerminalStorageTabIngredientComponentItemStackCraftingCommon, ButtonImage> {

    private final TerminalStorageState state;
    private final String buttonName;

    private AutoRefillType active;

    public TerminalButtonItemStackCraftingGridAutoRefill(TerminalStorageState state, ITerminalStorageTabClient<?> clientTab) {
        this.state = state;
        this.buttonName = "itemstack_grid_autorefill";

        if (state.hasButton(clientTab.getName().toString(), this.buttonName)) {
            CompoundNBT data = (CompoundNBT) state.getButton(clientTab.getName().toString(), this.buttonName);
            this.active = AutoRefillType.values()[data.getInt("active")];
        } else {
            this.active = AutoRefillType.STORAGE;
        }

        notifyServer((TerminalStorageTabIngredientComponentClient<T, ?>) clientTab);
    }

    protected void notifyServer(TerminalStorageTabIngredientComponentClient<T, ?> clientTab) {
        IntegratedTerminals._instance.getPacketHandler().sendToServer(
                new TerminalStorageIngredientItemStackCraftingGridSetAutoRefill(clientTab.getName().toString(), this.active));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ButtonImage createButton(int x, int y) {
        return new ButtonImage(x, y,
                L10NHelpers.localize("gui.integratedterminals.terminal_storage.craftinggrid.autorefill"),
                (b) -> {},
                active == AutoRefillType.DISABLED ? Images.BUTTON_BACKGROUND_INACTIVE : Images.BUTTON_BACKGROUND_ACTIVE,
                active.getImage());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClick(TerminalStorageTabIngredientComponentClient<T, ?> clientTab,
                        TerminalStorageTabIngredientComponentItemStackCraftingCommon commomTab, ButtonImage guiButton,
                        int channel, int mouseButton) {
        this.active = mouseButton == 0 ? AutoRefillType.values()[(this.active.ordinal() + 1) % AutoRefillType.values().length] : AutoRefillType.DISABLED;

        CompoundNBT data = new CompoundNBT();
        data.putInt("active", active.ordinal());
        state.setButton(clientTab.getName().toString(), this.buttonName, data);

        notifyServer(clientTab);
    }

    @Override
    public String getTranslationKey() {
        return "gui.integratedterminals.terminal_storage.craftinggrid.autorefill";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getTooltip(PlayerEntity player, ITooltipFlag tooltipFlag, List<ITextComponent> lines) {
        lines.add(new TranslationTextComponent("gui." + Reference.MOD_ID + ".terminal_storage.craftinggrid.autorefill.info"));
        lines.add(new TranslationTextComponent(active.getLabel()));
    }

    public static enum AutoRefillType {
        DISABLED(Images.BUTTON_MIDDLE_AUTOREFILL_DISABLED,
                "gui.integratedterminals.terminal_storage.craftinggrid.autorefill.type.disabled"),
        STORAGE(Images.BUTTON_MIDDLE_AUTOREFILL_STORAGE,
                "gui.integratedterminals.terminal_storage.craftinggrid.autorefill.type.storage"),
        PLAYER(Images.BUTTON_MIDDLE_AUTOREFILL_PLAYER,
                "gui.integratedterminals.terminal_storage.craftinggrid.autorefill.type.player"),
        STORAGE_PLAYER(Images.BUTTON_MIDDLE_AUTOREFILL_STORAGEPLAYER,
                "gui.integratedterminals.terminal_storage.craftinggrid.autorefill.type.storage_player"),
        PLAYER_STORAGE(Images.BUTTON_MIDDLE_AUTOREFILL_PLAYERSTORAGE,
                "gui.integratedterminals.terminal_storage.craftinggrid.autorefill.type.player_storage");

        @Nullable
        private final IImage image;
        private final String label;

        AutoRefillType(@Nullable IImage image, String label) {
            this.image = image;
            this.label = label;
        }

        @Nullable
        public IImage getImage() {
            return image;
        }

        public String getLabel() {
            return label;
        }
    }
}
