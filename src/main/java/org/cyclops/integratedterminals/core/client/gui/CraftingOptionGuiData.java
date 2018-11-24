package org.cyclops.integratedterminals.core.client.gui;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;

/**
 * @author rubensworks
 */
public class CraftingOptionGuiData<T, M> {

    private final BlockPos pos;
    private final EnumFacing side;
    private final IngredientComponent<T, M> component;
    private final String tabName;
    private final int channel;
    private final HandlerWrappedTerminalCraftingOption<T> craftingOption;
    private final int amount;

    public CraftingOptionGuiData(BlockPos pos, EnumFacing side, IngredientComponent<T, M> component, String tabName,
                                 int channel, HandlerWrappedTerminalCraftingOption<T> craftingOption, int amount) {
        this.pos = pos;
        this.side = side;
        this.component = component;
        this.tabName = tabName;
        this.channel = channel;
        this.craftingOption = craftingOption;
        this.amount = amount;
    }

    public BlockPos getPos() {
        return pos;
    }

    public EnumFacing getSide() {
        return side;
    }

    public IngredientComponent<T, M> getComponent() {
        return component;
    }

    public String getTabName() {
        return tabName;
    }

    public int getChannel() {
        return channel;
    }

    public HandlerWrappedTerminalCraftingOption<T> getCraftingOption() {
        return craftingOption;
    }

    public int getAmount() {
        return amount;
    }

    public static <T, M> CraftingOptionGuiData<T, M> copyWithAmount(CraftingOptionGuiData<T, M> craftingOptionGuiData, int amount) {
        return new CraftingOptionGuiData<>(
                craftingOptionGuiData.getPos(),
                craftingOptionGuiData.getSide(),
                craftingOptionGuiData.getComponent(),
                craftingOptionGuiData.getTabName(),
                craftingOptionGuiData.getChannel(),
                craftingOptionGuiData.getCraftingOption(),
                amount
        );
    }
}
