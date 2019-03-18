package org.cyclops.integratedterminals.capability.ingredient.sorter;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
    @SideOnly(Side.CLIENT)
    public void getTooltip(EntityPlayer player, ITooltipFlag tooltipFlag, List<String> lines) {
        lines.add(L10NHelpers.localize(this.unlocalizedName + ".info"));
    }
}
