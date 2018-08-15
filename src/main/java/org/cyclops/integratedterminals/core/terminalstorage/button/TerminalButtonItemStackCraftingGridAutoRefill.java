package org.cyclops.integratedterminals.core.terminalstorage.button;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonImage;
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
        TerminalStorageTabIngredientComponentItemStackCraftingCommon, GuiButtonImage> {

    private final TerminalStorageState state;
    private final String buttonName;

    private AutoRefillType active;

    public TerminalButtonItemStackCraftingGridAutoRefill(TerminalStorageState state, ITerminalStorageTabClient<?> clientTab) {
        this.state = state;
        this.buttonName = "itemstack_grid_autorefill";

        if (state.hasButton(clientTab.getName().toString(), this.buttonName)) {
            NBTTagCompound data = (NBTTagCompound) state.getButton(clientTab.getName().toString(), this.buttonName);
            this.active = AutoRefillType.values()[data.getInteger("active")];
        } else {
            this.active = AutoRefillType.STORAGE;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiButtonImage createButton(int x, int y) {
        return new GuiButtonImage(0, x, y,
                active == AutoRefillType.DISABLED ? Images.BUTTON_BACKGROUND_INACTIVE : Images.BUTTON_BACKGROUND_ACTIVE,
                active.getImage());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onClick(TerminalStorageTabIngredientComponentClient<T, ?> clientTab,
                        TerminalStorageTabIngredientComponentItemStackCraftingCommon commomTab, GuiButtonImage guiButton,
                        int channel, int mouseButton) {
        this.active = mouseButton == 0 ? AutoRefillType.values()[(this.active.ordinal() + 1) % AutoRefillType.values().length] : AutoRefillType.DISABLED;

        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("active", active.ordinal());
        state.setButton(clientTab.getName().toString(), this.buttonName, data);

        IntegratedTerminals._instance.getPacketHandler().sendToServer(
                new TerminalStorageIngredientItemStackCraftingGridSetAutoRefill(clientTab.getName().toString(), this.active));
    }

    @Override
    public String getUnlocalizedName() {
        return "gui.integratedterminals.terminal_storage.craftinggrid.autorefill";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getTooltip(EntityPlayer player, ITooltipFlag tooltipFlag, List<String> lines) {
        lines.add(L10NHelpers.localize("gui." + Reference.MOD_ID + ".terminal_storage.craftinggrid.autorefill.info"));
        lines.add(L10NHelpers.localize(active.getLabel()));
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
