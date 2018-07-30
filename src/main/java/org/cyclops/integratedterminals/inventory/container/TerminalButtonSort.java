package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.ingredient.IIngredientInstanceSorter;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.client.gui.GuiButtonSort;

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
    private Comparator<T> effectiveSorter;
    private boolean active;
    private boolean descending;

    public TerminalButtonSort(IIngredientInstanceSorter<T> instanceSorter) {
        this.instanceSorter = instanceSorter;
        this.effectiveSorter = null;
        this.active = false;
        this.descending = true;
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
                    this.effectiveSorter = instanceSorter.reversed();
                } else {
                    active = false;
                    this.effectiveSorter = null;
                }
            } else {
                active = true;
                descending = true;
                this.effectiveSorter = instanceSorter;
            }
        } else {
            active = false;
            descending = true;
            this.effectiveSorter = null;
        }
        clientTab.resetFilteredIngredientsViews(channel);
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
