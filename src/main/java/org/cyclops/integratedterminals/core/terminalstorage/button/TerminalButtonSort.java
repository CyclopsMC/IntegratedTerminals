package org.cyclops.integratedterminals.core.terminalstorage.button;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.ingredient.IIngredientInstanceSorter;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.client.gui.ButtonSort;
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
        ITerminalStorageTabCommon, ButtonSort> {

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
        this.buttonName = "sort_" + instanceSorter.getTranslationKey();

        if (state.hasButton(clientTab.getName().toString(), this.buttonName)) {
            CompoundNBT data = (CompoundNBT) state.getButton(clientTab.getName().toString(), this.buttonName);
            this.active = data.getBoolean("active");
            this.descending = data.getBoolean("descending");
        } else {
            this.active = false;
            this.descending = true;
        }
        updateSorter();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ButtonSort createButton(int x, int y) {
        return new ButtonSort(x, y, new TranslationTextComponent("gui.integratedterminals.terminal_storage.sort"), (b) -> {}, instanceSorter.getIcon(), active, descending);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClick(TerminalStorageTabIngredientComponentClient<T, ?> clientTab, ITerminalStorageTabCommon commonTab,
                        ButtonSort guiButton, int channel, int mouseButton) {
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

        CompoundNBT data = new CompoundNBT();
        data.putBoolean("active", active);
        data.putBoolean("descending", descending);
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
    public String getTranslationKey() {
        return instanceSorter.getTranslationKey();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getTooltip(PlayerEntity player, ITooltipFlag tooltipFlag, List<ITextComponent> lines) {
        instanceSorter.getTooltip(player, tooltipFlag, lines);
        if (active) {
            lines.add(new TranslationTextComponent("gui." + Reference.MOD_ID + ".terminal_storage.sort.order.label",
                    new TranslationTextComponent(descending
                            ? "gui." + Reference.MOD_ID + ".terminal_storage.sort.order.descending"
                            : "gui." + Reference.MOD_ID + ".terminal_storage.sort.order.ascending"))
                    .mergeStyle(TextFormatting.ITALIC));
        } else {
            lines.add(new TranslationTextComponent("general.cyclopscore.info.disabled")
                    .mergeStyle(TextFormatting.ITALIC));
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
