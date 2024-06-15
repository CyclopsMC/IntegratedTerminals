package org.cyclops.integratedterminals.capability.ingredient.sorter;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.ingredient.IIngredientInstanceSorter;

import java.util.List;

/**
 * An adapter implementation of {@link IIngredientInstanceSorter}.
 * @author rubensworks
 */
public abstract class IngredientInstanceSorterAdapter<T> implements IIngredientInstanceSorter<T> {

    private final IImage icon;
    private final String unlocalizedName;

    public IngredientInstanceSorterAdapter(IImage icon, String ingredientType, String kind) {
        this.icon = icon;
        this.unlocalizedName = "gui." + Reference.MOD_ID + ".terminal_storage.sort." + ingredientType + "." + kind;
    }

    @Override
    public IImage getIcon() {
        return icon;
    }

    @Override
    public String getTranslationKey() {
        return this.unlocalizedName;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getTooltip(Player player, TooltipFlag tooltipFlag, List<Component> lines) {
        lines.add(Component.translatable(this.unlocalizedName + ".info").withStyle(ChatFormatting.GRAY));
    }
}
