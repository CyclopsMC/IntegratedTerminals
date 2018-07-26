package org.cyclops.integratedterminals.inventory.container;

import com.google.common.collect.Lists;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A client-side storage terminal ingredient tab for crafting with {@link ItemStack} instances.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientComponentClientItemStackCrafting
        extends TerminalStorageTabIngredientComponentClient<ItemStack, Integer> {

    private final ItemStack icon;

    public TerminalStorageTabIngredientComponentClientItemStackCrafting(IngredientComponent<?, ?> ingredientComponent) {
        super(ingredientComponent);
        this.icon = new ItemStack(Blocks.CRAFTING_TABLE);

        this.buttons.add(new TerminalButtonItemStackCraftingGridClear<>());
    }

    @Override
    public String getId() {
        return super.getId() + "_crafting";
    }

    @Override
    public ItemStack getIcon() {
        return this.icon;
    }

    @Override
    public List<String> getTooltip() {
        return Lists.newArrayList(L10NHelpers.localize("gui.integratedterminals.terminal_storage.crafting_name",
                L10NHelpers.localize(this.ingredientComponent.getUnlocalizedName())));
    }

    @Override
    public int getSlotOffsetX() {
        return ITerminalStorageTabClient.DEFAULT_SLOT_OFFSET_X + 108;
    }

    @Override
    public int getSlotRowLength() {
        return 3;
    }

    @Nullable
    @Override
    public ResourceLocation getBackgroundTexture() {
        return new ResourceLocation(Reference.MOD_ID, IntegratedTerminals._instance
                .getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI) + "part_terminal_storage_crafting.png");
    }
}
