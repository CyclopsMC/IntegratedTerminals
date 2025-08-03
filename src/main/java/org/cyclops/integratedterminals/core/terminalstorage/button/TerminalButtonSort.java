package org.cyclops.integratedterminals.core.terminalstorage.button;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.ingredient.IIngredientInstanceSorter;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButtonClient;
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

    protected final IIngredientInstanceSorter<T> instanceSorter;
    protected final TerminalStorageState state;
    protected final String buttonName;
    protected final ITerminalStorageTabClient<?> clientTab;

    protected Comparator<T> effectiveSorter;
    protected boolean active;
    protected boolean descending;

    public TerminalButtonSort(IIngredientInstanceSorter<T> instanceSorter, TerminalStorageState state,
                              ITerminalStorageTabClient<?> clientTab) {
        this.instanceSorter = instanceSorter;
        this.state = state;
        this.buttonName = "sort_" + instanceSorter.getTranslationKey();
        this.clientTab = clientTab;

        reloadFromState();
    }

    @Override
    public ITerminalButtonClient<TerminalStorageTabIngredientComponentClient<T, ?>, ITerminalStorageTabCommon, ButtonSort> getClient() {
        return new TerminalButtonSortClient<>(this);
    }

    @Override
    public void reloadFromState() {
        if (state.hasButton(clientTab.getTabSettingsName().toString(), this.buttonName)) {
            CompoundTag data = (CompoundTag) state.getButton(clientTab.getTabSettingsName().toString(), this.buttonName);
            this.active = data.getBoolean("active").orElseThrow();
            this.descending = data.getBoolean("descending").orElseThrow();
        } else {
            this.active = false;
            this.descending = true;
        }
        updateSorter();
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
    public void getTooltip(Player player, TooltipFlag tooltipFlag, List<Component> lines) {
        instanceSorter.getTooltip(player, tooltipFlag, lines);
        if (active) {
            lines.add(Component.translatable("gui." + Reference.MOD_ID + ".terminal_storage.sort.order.label",
                    Component.translatable(descending
                            ? "gui." + Reference.MOD_ID + ".terminal_storage.sort.order.descending"
                            : "gui." + Reference.MOD_ID + ".terminal_storage.sort.order.ascending"))
                    .withStyle(ChatFormatting.ITALIC));
        } else {
            lines.add(Component.translatable("general.cyclopscore.info.disabled")
                    .withStyle(ChatFormatting.ITALIC));
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
