package org.cyclops.integratedterminals.core.terminalstorage.button;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
    private final ITerminalStorageTabClient<?> clientTab;

    private Comparator<T> effectiveSorter;
    private boolean active;
    private boolean descending;

    public TerminalButtonSort(IIngredientInstanceSorter<T> instanceSorter, TerminalStorageState state,
                              ITerminalStorageTabClient<?> clientTab) {
        this.instanceSorter = instanceSorter;
        this.state = state;
        this.buttonName = "sort_" + instanceSorter.getTranslationKey();
        this.clientTab = clientTab;

        reloadFromState();
    }

    @Override
    public void reloadFromState() {
        if (state.hasButton(clientTab.getTabSettingsName().toString(), this.buttonName)) {
            CompoundTag data = (CompoundTag) state.getButton(clientTab.getTabSettingsName().toString(), this.buttonName);
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
        return new ButtonSort(x, y, Component.translatable("gui.integratedterminals.terminal_storage.sort"), (b) -> {}, instanceSorter.getIcon(), active, descending);
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

        CompoundTag data = new CompoundTag();
        data.putBoolean("active", active);
        data.putBoolean("descending", descending);
        state.setButton(clientTab.getTabSettingsName().toString(), this.buttonName, data);

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
