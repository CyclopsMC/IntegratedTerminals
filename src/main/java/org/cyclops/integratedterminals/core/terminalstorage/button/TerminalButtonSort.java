package org.cyclops.integratedterminals.core.terminalstorage.button;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.ingredient.IIngredientInstanceSorter;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.client.gui.GuiButtonSort;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageState;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

/**
 * A button for sorting based on a given {@link IIngredientInstanceSorter}.
 * @author rubensworks
 */
public class TerminalButtonSort<T> implements ITerminalButton<TerminalStorageTabIngredientComponentClient<T, ?>,
        ITerminalStorageTabCommon, GuiButtonSort> {

    private final IIngredientInstanceSorter<T> instanceSorter;
    private final TerminalStorageState state;
    private final String buttonName;

    private Comparator<T> effectiveSorter;
    private boolean active;
    private boolean descending;

    public TerminalButtonSort(IIngredientInstanceSorter<T> instanceSorter, TerminalStorageState state,
                              ITerminalStorageTabClient<?> clientTab) {
        this.instanceSorter = instanceSorter;
        this.state = state;
        this.buttonName = "sort_" + instanceSorter.getUnlocalizedName();

        if (state.hasButton(clientTab.getName().toString(), this.buttonName)) {
            NBTTagCompound data = (NBTTagCompound) state.getButton(clientTab.getName().toString(), this.buttonName);
            this.active = data.getBoolean("active");
            this.descending = data.getBoolean("descending");
        } else {
            this.active = false;
            this.descending = true;
        }
        updateSorter();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiButtonSort createButton(int x, int y) {
        return new GuiButtonSort(0, x, y, instanceSorter.getIcon(), active, descending);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onClick(TerminalStorageTabIngredientComponentClient<T, ?> clientTab, ITerminalStorageTabCommon commonTab,
                        GuiButtonSort guiButton, int channel, int mouseButton) {
        if (mouseButton == 0) {
            if (active) {
                if (descending) {
                    descending = false;
                } else {
                    active = false;
                }
            } else {
                active = true;
                descending = true;
            }
        } else {
            active = false;
            descending = true;
        }

        NBTTagCompound data = new NBTTagCompound();
        data.setBoolean("active", active);
        data.setBoolean("descending", descending);
        state.setButton(clientTab.getName().toString(), this.buttonName, data);

        updateSorter();
        clientTab.resetFilteredIngredientsViews(channel);
    }

    protected void updateSorter() {
        if (active) {
            if (descending) {
                this.effectiveSorter = this.instanceSorter.reversed();
            } else {
                this.effectiveSorter = this.instanceSorter;
            }
        } else {
            this.effectiveSorter = null;
        }
    }

    @Override
    public String getUnlocalizedName() {
        return instanceSorter.getUnlocalizedName();
    }

    @Override
    public void getTooltip(EntityPlayer player, ITooltipFlag tooltipFlag, List<String> lines) {
        instanceSorter.getTooltip(player, tooltipFlag, lines);
        if (active) {
            lines.add(TextFormatting.ITALIC + L10NHelpers.localize("gui." + Reference.MOD_ID + ".terminal_storage.sort.order.label",
                    L10NHelpers.localize(descending ? "gui." + Reference.MOD_ID + ".terminal_storage.sort.order.descending" : "gui." + Reference.MOD_ID + ".terminal_storage.sort.order.ascending")));
        } else {
            lines.add(TextFormatting.ITALIC + L10NHelpers.localize("general.cyclopscore.info.disabled"));
        }
    }

    /**
     * @return The comparator that should be used for sorting,
     *         this can change depending on the state of this button.
     */
    @Nullable
    public Comparator<T> getEffectiveSorter() {
        return effectiveSorter;
    }
}
