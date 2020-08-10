package org.cyclops.integratedterminals.capability.ingredient.sorter;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.cyclopscore.helper.L10NHelpers;
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
    public void getTooltip(PlayerEntity player, ITooltipFlag tooltipFlag, List<ITextComponent> lines) {
        lines.add(new TranslationTextComponent(this.unlocalizedName + ".info"));
    }
}
