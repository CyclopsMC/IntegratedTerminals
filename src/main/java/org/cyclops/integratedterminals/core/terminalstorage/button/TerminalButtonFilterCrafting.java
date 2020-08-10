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
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.client.gui.image.Images;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentCommon;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * A button for clearing the crafting grid.
 * @author rubensworks
 */
public class TerminalButtonFilterCrafting<T>
        implements ITerminalButton<TerminalStorageTabIngredientComponentClient<T, ?>,
        TerminalStorageTabIngredientComponentCommon<T, ?>, ButtonImage> {

    private final TerminalStorageState state;
    private final String buttonName;

    private FilterType active;

    public TerminalButtonFilterCrafting(TerminalStorageState state, ITerminalStorageTabClient<?> clientTab) {
        this.state = state;
        this.buttonName = "filter_crafting";

        if (state.hasButton(clientTab.getName().toString(), this.buttonName)) {
            CompoundNBT data = (CompoundNBT) state.getButton(clientTab.getName().toString(), this.buttonName);
            this.active = FilterType.values()[data.getInt("active")];
        } else {
            this.active = FilterType.ALL;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ButtonImage createButton(int x, int y) {
        return new ButtonImage(x, y,
                L10NHelpers.localize("gui.integratedterminals.terminal_storage.craftinggrid.clear"),
                (b) -> {},
                active == FilterType.ALL ? Images.BUTTON_BACKGROUND_INACTIVE : Images.BUTTON_BACKGROUND_ACTIVE,
                active.getImage());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClick(TerminalStorageTabIngredientComponentClient<T, ?> clientTab,
                        TerminalStorageTabIngredientComponentCommon<T, ?> commomTab, ButtonImage guiButton,
                        int channel, int mouseButton) {
        this.active = mouseButton == 0 ? FilterType.values()[(this.active.ordinal() + 1) % FilterType.values().length] : FilterType.ALL;

        CompoundNBT data = new CompoundNBT();
        data.putInt("active", active.ordinal());
        state.setButton(clientTab.getName().toString(), this.buttonName, data);

        clientTab.resetFilteredIngredientsViews(channel);
    }

    @Override
    public String getTranslationKey() {
        return "gui." + Reference.MOD_ID + ".terminal_storage.crafting.filter";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getTooltip(PlayerEntity player, ITooltipFlag tooltipFlag, List<ITextComponent> lines) {
        lines.add(new TranslationTextComponent("gui." + Reference.MOD_ID + ".terminal_storage.crafting.filter.info"));
        lines.add(new TranslationTextComponent(active.getLabel()));
    }

    public Predicate<TerminalStorageTabIngredientComponentClient.InstanceWithMetadata<T>> getEffectiveFilter() {
        return (Predicate) active.getFilter();
    }

    public static enum FilterType {
        ALL(Images.BUTTON_MIDDLE_FILTER_CRAFTING_ALL,
                "gui.integratedterminals.terminal_storage.crafting.filter.type.all",
                i -> true),
        STORAGE(Images.BUTTON_MIDDLE_FILTER_CRAFTING_STORAGE,
                "gui.integratedterminals.terminal_storage.crafting.filter.type.storage",
                i -> i.getCraftingOption() == null),
        CRAFTABLE(Images.BUTTON_MIDDLE_FILTER_CRAFTING_CRAFTABLE,
                "gui.integratedterminals.terminal_storage.crafting.filter.type.craftable",
                i -> i.getCraftingOption() != null);

        @Nullable
        private final IImage image;
        private final String label;
        private final Predicate<TerminalStorageTabIngredientComponentClient.InstanceWithMetadata<?>> filter;

        FilterType(@Nullable IImage image, String label, Predicate<TerminalStorageTabIngredientComponentClient.InstanceWithMetadata<?>> filter) {
            this.image = image;
            this.label = label;
            this.filter = filter;
        }

        @Nullable
        public IImage getImage() {
            return image;
        }

        public String getLabel() {
            return label;
        }

        public Predicate<TerminalStorageTabIngredientComponentClient.InstanceWithMetadata<?>> getFilter() {
            return filter;
        }
    }
}
