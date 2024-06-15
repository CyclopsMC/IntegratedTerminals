package org.cyclops.integratedterminals.api.ingredient;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.image.IImage;

import java.util.Comparator;
import java.util.List;

/**
 * A sorting comparator for ingredient instances.
 *
 * These don't have to be 0-equals-safe,
 * meaning that non-equal instances don't necessarily have to return a non-0 value.
 * This is because these sorters are typically chained.
 *
 * @author rubensworks
 */
public interface IIngredientInstanceSorter<T> extends Comparator<T> {

    /**
     * @return The icon that can be used to represent this sorter.
     */
    public IImage getIcon();

    /**
     * @return The unlocalized name
     */
    public String getTranslationKey();

    /**
     * Get the tooltip of this sorter.
     * @param player The player that is requesting the tooltip.
     * @param tooltipFlag The tooltip flag.
     * @param lines The tooltip lines.
     */
    @OnlyIn(Dist.CLIENT)
    public void getTooltip(Player player, TooltipFlag tooltipFlag, List<Component> lines);

}
