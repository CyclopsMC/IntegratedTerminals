package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientCollection;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.IntFunction;

/**
 * Helpers for determining and displaying the channels in which ingredients are stored.
 * @author rubensworks
 */
public final class TerminalStorageChannels {

    private TerminalStorageChannels() {}

    /**
     * Determine the quantity of the given instance within each of the given channels.
     * @param ingredientComponent The ingredient component.
     * @param channels The channels to look in.
     * @param channelIngredients A function to get the stored ingredients of a channel.
     * @param instance The instance to look for, its quantity is ignored.
     * @param <T> The instance type.
     * @param <M> The matching condition parameter.
     * @return The quantity per channel, in the order of the given channels, without channels that don't store it.
     */
    public static <T, M> Int2LongMap getInstanceQuantitiesPerChannel(IngredientComponent<T, M> ingredientComponent,
                                                                     int[] channels,
                                                                     IntFunction<IIngredientCollection<T, M>> channelIngredients,
                                                                     T instance) {
        IIngredientMatcher<T, M> matcher = ingredientComponent.getMatcher();
        M matchCondition = matcher.getExactMatchNoQuantityCondition();
        Int2LongMap quantities = new Int2LongLinkedOpenHashMap();
        for (int channel : channels) {
            long quantity = 0;
            Iterator<T> it = channelIngredients.apply(channel).iterator(instance, matchCondition);
            while (it.hasNext()) {
                quantity += matcher.getQuantity(it.next());
            }
            if (quantity > 0) {
                quantities.put(channel, quantity);
            }
        }
        return quantities;
    }

    /**
     * Create the tooltip lines that indicate in which channels an instance is stored.
     * @param viewHandler The terminal storage handler of the ingredient component.
     * @param instance The instance that is stored, its quantity is ignored.
     * @param quantitiesPerChannel The stored quantity per channel,
     *                             as determined by {@link #getInstanceQuantitiesPerChannel}.
     * @param <T> The instance type.
     * @param <M> The matching condition parameter.
     * @return The tooltip lines, which are empty if the instance is not stored in any channel.
     */
    public static <T, M> List<Component> createChannelTooltipLines(IIngredientComponentTerminalStorageHandler<T, M> viewHandler,
                                                                   T instance,
                                                                   Int2LongMap quantitiesPerChannel) {
        List<Component> lines = Lists.newArrayList();
        if (quantitiesPerChannel.isEmpty()) {
            return lines;
        }

        if (quantitiesPerChannel.size() == 1) {
            lines.add(createChannelLine(quantitiesPerChannel.keySet().iterator().nextInt()));
        } else {
            IIngredientMatcher<T, M> matcher = viewHandler.getComponent().getMatcher();
            lines.add(Component.translatable("gui.integratedterminals.terminal_storage.tooltip.channels")
                    .withStyle(ChatFormatting.GRAY));
            for (Int2LongMap.Entry entry : quantitiesPerChannel.int2LongEntrySet()) {
                lines.add(Component.translatable("gui.integratedterminals.terminal_storage.tooltip.channel_quantity",
                                formatChannel(entry.getIntKey()),
                                viewHandler.formatQuantity(matcher.withQuantity(instance, entry.getLongValue())))
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        return lines;
    }

    /**
     * Create the tooltip line that indicates a single channel.
     * @param channel A channel id.
     * @return The tooltip line.
     */
    public static Component createChannelLine(int channel) {
        return Component.translatable("gui.integratedterminals.terminal_storage.tooltip.channel",
                formatChannel(channel)).withStyle(ChatFormatting.GRAY);
    }

    private static String formatChannel(int channel) {
        return String.format(Locale.ROOT, "%,d", channel);
    }

}
