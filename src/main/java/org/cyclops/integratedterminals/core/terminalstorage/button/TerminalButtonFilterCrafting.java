package org.cyclops.integratedterminals.core.terminalstorage.button;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.client.gui.image.Images;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentCommon;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageState;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * A button for filtering and ordering stored and craftable ingredients.
 * @author rubensworks
 */
public class TerminalButtonFilterCrafting<T>
        implements ITerminalButton<TerminalStorageTabIngredientComponentClient<T, ?>,
        TerminalStorageTabIngredientComponentCommon<T, ?>, ButtonImage> {

    private final TerminalStorageState state;
    private final String buttonName;
    private final ITerminalStorageTabClient<?> clientTab;

    private FilterType active;

    public TerminalButtonFilterCrafting(TerminalStorageState state, ITerminalStorageTabClient<?> clientTab) {
        this.state = state;
        this.buttonName = "filter_crafting";
        this.clientTab = clientTab;

        reloadFromState();
    }

    @Override
    public void reloadFromState() {
        if (state.hasButton(clientTab.getTabSettingsName().toString(), this.buttonName)) {
            CompoundTag data = (CompoundTag) state.getButton(clientTab.getTabSettingsName().toString(), this.buttonName);
            this.active = FilterType.read(data);
        } else {
            this.active = FilterType.getDefault();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ButtonImage createButton(int x, int y) {
        return new ButtonImage(x, y,
                Component.translatable(getTranslationKey()),
                (b) -> {},
                active == FilterType.getDefault() ? Images.BUTTON_BACKGROUND_INACTIVE : Images.BUTTON_BACKGROUND_ACTIVE,
                active.getImage());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClick(TerminalStorageTabIngredientComponentClient<T, ?> clientTab,
                        TerminalStorageTabIngredientComponentCommon<T, ?> commomTab, ButtonImage guiButton,
                        int channel, int mouseButton) {
        this.active = mouseButton == 0 ? this.active.next() : FilterType.getDefault();

        CompoundTag data = new CompoundTag();
        this.active.write(data);
        state.setButton(clientTab.getTabSettingsName().toString(), this.buttonName, data);

        clientTab.resetFilteredIngredientsViews(channel);
    }

    @Override
    public String getTranslationKey() {
        return "gui." + Reference.MOD_ID + ".terminal_storage.crafting.filter";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getTooltip(Player player, TooltipFlag tooltipFlag, List<Component> lines) {
        lines.add(Component.translatable("gui." + Reference.MOD_ID + ".terminal_storage.crafting.filter.info").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable(active.getLabel()));
    }

    public Predicate<TerminalStorageTabIngredientComponentClient.InstanceWithMetadata<T>> getEffectiveFilter() {
        return (Predicate) active.getFilter();
    }

    /**
     * @return The comparator that should be used for ordering stored and craftable ingredients,
     *         or null if this button should not influence the ordering.
     */
    @Nullable
    public Comparator<TerminalStorageTabIngredientComponentClient.InstanceWithMetadata<T>> getEffectiveOrder() {
        return (Comparator) active.getOrder();
    }

    public static enum FilterType {
        ALL(Images.BUTTON_MIDDLE_FILTER_CRAFTING_ALL,
                "gui.integratedterminals.terminal_storage.crafting.filter.type.all",
                i -> true,
                1),
        ALL_CRAFTABLE_FIRST(Images.BUTTON_MIDDLE_FILTER_CRAFTING_ALL_CRAFTABLE_FIRST,
                "gui.integratedterminals.terminal_storage.crafting.filter.type.all_craftable_first",
                i -> true,
                -1),
        STORAGE(Images.BUTTON_MIDDLE_FILTER_CRAFTING_STORAGE,
                "gui.integratedterminals.terminal_storage.crafting.filter.type.storage",
                i -> i.getCraftingOption() == null,
                0),
        CRAFTABLE(Images.BUTTON_MIDDLE_FILTER_CRAFTING_CRAFTABLE,
                "gui.integratedterminals.terminal_storage.crafting.filter.type.craftable",
                i -> i.getCraftingOption() != null,
                0);

        private static final String NBT_ACTIVE = "active";
        private static final String NBT_ACTIVE_NAME = "activeName";

        /**
         * The order in which the filter types were declared before {@link #ALL_CRAFTABLE_FIRST} was introduced.
         * This is only used for reading states that were persisted by older versions,
         * which stored the filter type by its ordinal.
         */
        private static final FilterType[] LEGACY_ORDINALS = { ALL, STORAGE, CRAFTABLE };

        @Nullable
        private final IImage image;
        private final String label;
        private final Predicate<TerminalStorageTabIngredientComponentClient.InstanceWithMetadata<?>> filter;
        @Nullable
        private final Comparator<TerminalStorageTabIngredientComponentClient.InstanceWithMetadata<?>> order;

        FilterType(@Nullable IImage image, String label,
                   Predicate<TerminalStorageTabIngredientComponentClient.InstanceWithMetadata<?>> filter,
                   int craftableRank) {
            this.image = image;
            this.label = label;
            this.filter = filter;
            if (craftableRank == 0) {
                this.order = null;
            } else {
                this.order = Comparator.comparingInt(
                        instance -> instance.getCraftingOption() == null ? 0 : craftableRank);
            }
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

        /**
         * @return The comparator that groups craftable ingredients before or after stored ingredients,
         *         or null if this filter type should not influence the ordering.
         */
        @Nullable
        public Comparator<TerminalStorageTabIngredientComponentClient.InstanceWithMetadata<?>> getOrder() {
            return order;
        }

        /**
         * @return The next filter type when cycling through them.
         */
        public FilterType next() {
            return values()[(this.ordinal() + 1) % values().length];
        }

        /**
         * @return The filter type that is active by default.
         */
        public static FilterType getDefault() {
            return ALL;
        }

        /**
         * Write this filter type to the given tag.
         *
         * The ordinal is written as well, so that older versions can still read the state,
         * albeit by falling back to the default for filter types they don't know about.
         *
         * @param tag A tag.
         */
        public void write(CompoundTag tag) {
            tag.putString(NBT_ACTIVE_NAME, name());
            int legacyOrdinal = 0;
            for (int i = 0; i < LEGACY_ORDINALS.length; i++) {
                if (LEGACY_ORDINALS[i] == this) {
                    legacyOrdinal = i;
                    break;
                }
            }
            tag.putInt(NBT_ACTIVE, legacyOrdinal);
        }

        /**
         * Read a filter type from the given tag.
         *
         * Filter types are persisted by name, as ordinals are not stable across versions.
         * States that were persisted before this was the case are read by their ordinal.
         *
         * @param tag A tag.
         * @return The persisted filter type, or the default if none could be read.
         */
        public static FilterType read(CompoundTag tag) {
            if (tag.contains(NBT_ACTIVE_NAME)) {
                String name = tag.getString(NBT_ACTIVE_NAME);
                for (FilterType filterType : values()) {
                    if (filterType.name().equals(name)) {
                        return filterType;
                    }
                }
                return getDefault();
            }

            int legacyOrdinal = tag.getInt(NBT_ACTIVE);
            if (legacyOrdinal < 0 || legacyOrdinal >= LEGACY_ORDINALS.length) {
                return getDefault();
            }
            return LEGACY_ORDINALS[legacyOrdinal];
        }
    }
}
