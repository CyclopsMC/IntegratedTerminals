package org.cyclops.integratedterminals.core.terminalstorage;

import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.ingredient.storage.IngredientComponentStorageWrapped;

import javax.annotation.Nonnull;

/**
 * A storage that counts how much actually moved through it.
 *
 * The terminal uses this to tell the client what a click really did,
 * which is not something the movement methods report by themselves.
 *
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class IngredientComponentStorageCounting<T, M> extends IngredientComponentStorageWrapped<T, M> {

    private long movedQuantity;

    public IngredientComponentStorageCounting(IIngredientComponentStorage<T, M> storage) {
        super(storage, true, true, true);
        this.movedQuantity = 0;
    }

    /**
     * @return The quantity that was inserted into or extracted from this storage, simulations excluded.
     */
    public long getMovedQuantity() {
        return this.movedQuantity;
    }

    @Override
    public T insert(@Nonnull T ingredient, boolean simulate) {
        T remaining = super.insert(ingredient, simulate);
        if (!simulate) {
            IIngredientMatcher<T, M> matcher = getComponent().getMatcher();
            this.movedQuantity += matcher.getQuantity(ingredient) - matcher.getQuantity(remaining);
        }
        return remaining;
    }

    @Override
    public T extract(@Nonnull T prototype, M matchCondition, boolean simulate) {
        return count(super.extract(prototype, matchCondition, simulate), simulate);
    }

    @Override
    public T extract(long maxQuantity, boolean simulate) {
        return count(super.extract(maxQuantity, simulate), simulate);
    }

    protected T count(T extracted, boolean simulate) {
        if (!simulate) {
            this.movedQuantity += getComponent().getMatcher().getQuantity(extracted);
        }
        return extracted;
    }

}
