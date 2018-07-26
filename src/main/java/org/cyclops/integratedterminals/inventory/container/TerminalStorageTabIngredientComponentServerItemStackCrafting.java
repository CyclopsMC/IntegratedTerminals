package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.api.part.PartPos;

/**
 * A server-side storage terminal ingredient tab for crafting with {@link ItemStack} instances.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientComponentServerItemStackCrafting
        extends TerminalStorageTabIngredientComponentServer<ItemStack, Integer> {

    public TerminalStorageTabIngredientComponentServerItemStackCrafting(IngredientComponent<ItemStack, Integer> ingredientComponent,
                                                                        IPositionedAddonsNetworkIngredients<ItemStack, Integer> ingredientNetwork,
                                                                        PartPos pos, EntityPlayerMP player) {
        super(ingredientComponent, ingredientNetwork, pos, player);
    }

    @Override
    public void init() {
        // No inv syncing needed, we handle this from the canonical tab
    }

    @Override
    protected void initChannel(int channel) {
        // No inv syncing needed, we handle this from the canonical tab
    }

    @Override
    public void deInit() {
        // No inv syncing needed, we handle this from the canonical tab
    }

    @Override
    public String getId() {
        return super.getId() + "_crafting";
    }
}
