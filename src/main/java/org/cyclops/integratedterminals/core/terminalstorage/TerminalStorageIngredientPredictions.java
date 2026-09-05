package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Lists;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient.InstanceWithMetadata;

import java.util.Iterator;
import java.util.List;

/**
 * Client-side predictions of storage changes that were caused by the player,
 * but that have not been confirmed by the server yet.
 *
 * These are applied on top of the server-provided ingredients view, and are never merged into it,
 * as the server sends diffs that would otherwise be applied twice.
 *
 * A prediction is dropped as soon as the server sends a change for its instance,
 * or when it expires, so a wrong prediction always corrects itself.
 *
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class TerminalStorageIngredientPredictions<T, M> {

    /**
     * The time after which unconfirmed predictions are dropped.
     * This is only reached when the server did not send any change for the predicted instance,
     * which means that the prediction was wrong.
     */
    private static final long EXPIRY_TIME_MS = 2000;

    private final IngredientComponent<T, M> ingredientComponent;
    private final List<Prediction<T>> predictions;

    public TerminalStorageIngredientPredictions(IngredientComponent<T, M> ingredientComponent) {
        this.ingredientComponent = ingredientComponent;
        this.predictions = Lists.newArrayList();
    }

    public boolean isEmpty() {
        return this.predictions.isEmpty();
    }

    /**
     * Predict that the given instance was added to or removed from the given channel.
     * @param channel The channel the change was caused in.
     * @param instance The changed instance, with the changed quantity.
     * @param addition If the instance was added, otherwise it was removed.
     */
    public void add(int channel, T instance, boolean addition) {
        if (!this.ingredientComponent.getMatcher().isEmpty(instance)) {
            this.predictions.add(new Prediction<>(channel, instance, addition,
                    System.currentTimeMillis() + EXPIRY_TIME_MS));
        }
    }

    /**
     * Drop all predictions that were not confirmed in time.
     * @return If at least one prediction was dropped.
     */
    public boolean removeExpired() {
        long time = System.currentTimeMillis();
        return this.predictions.removeIf(prediction -> prediction.getExpiryTime() <= time);
    }

    /**
     * Confirm the given change that the server has sent.
     *
     * The changed quantities are subtracted from the predictions that expected them,
     * so that predictions that the change does not cover yet remain shown.
     * Without this, a second click would briefly be shown as undone
     * when the server confirms the first one.
     *
     * Predictions are matched on their instance only, deliberately not on their channel:
     * a click in the wildcard channel is stored under that channel,
     * while the server confirms it in the channel it actually happened in.
     * The cost is that unrelated network activity for the same instance confirms a prediction early,
     * after which the shown quantity falls back to the server's until the real change arrives.
     *
     * @param instances The changed instances.
     * @param addition If the instances were added, otherwise they were removed.
     * @return If at least one prediction was confirmed.
     */
    public boolean consume(Iterable<T> instances, boolean addition) {
        if (this.predictions.isEmpty()) {
            return false;
        }
        IIngredientMatcher<T, M> matcher = this.ingredientComponent.getMatcher();
        M matchCondition = matcher.getExactMatchNoQuantityCondition();
        boolean consumed = false;
        for (T instance : instances) {
            long remaining = matcher.getQuantity(instance);
            Iterator<Prediction<T>> it = this.predictions.iterator();
            while (it.hasNext() && remaining > 0) {
                Prediction<T> prediction = it.next();
                if (prediction.isAddition() == addition
                        && matcher.matches(prediction.getInstance(), instance, matchCondition)) {
                    long quantity = matcher.getQuantity(prediction.getInstance());
                    if (quantity <= remaining) {
                        remaining -= quantity;
                        it.remove();
                    } else {
                        prediction.setInstance(matcher.withQuantity(prediction.getInstance(), quantity - remaining));
                        remaining = 0;
                    }
                    consumed = true;
                }
            }
        }
        return consumed;
    }

    /**
     * @param channel The channel that is being viewed.
     * @param instance An instance.
     * @return The predicted quantity change for the given instance, which can be negative.
     */
    public long getDelta(int channel, T instance) {
        IIngredientMatcher<T, M> matcher = this.ingredientComponent.getMatcher();
        M matchCondition = matcher.getExactMatchNoQuantityCondition();
        long delta = 0;
        for (Prediction<T> prediction : this.predictions) {
            if (appliesTo(prediction, channel)
                    && matcher.matches(prediction.getInstance(), instance, matchCondition)) {
                delta += matcher.getQuantity(prediction.getInstance()) * (prediction.isAddition() ? 1 : -1);
            }
        }
        return delta;
    }

    /**
     * Apply all predictions of the given channel to the given ingredients view.
     * @param channel The channel that is being viewed.
     * @param view A mutable ingredients view, without any predictions applied yet.
     */
    public void apply(int channel, List<InstanceWithMetadata<T>> view) {
        if (this.predictions.isEmpty()) {
            return;
        }
        IIngredientMatcher<T, M> matcher = this.ingredientComponent.getMatcher();
        M matchCondition = matcher.getExactMatchNoQuantityCondition();
        for (Prediction<T> prediction : this.predictions) {
            if (!appliesTo(prediction, channel)) {
                continue;
            }
            long delta = matcher.getQuantity(prediction.getInstance()) * (prediction.isAddition() ? 1 : -1);
            boolean applied = false;
            for (int i = 0; i < view.size(); i++) {
                InstanceWithMetadata<T> entry = view.get(i);
                // Crafting option entries show a recipe output, not a stored quantity, so they are never predicted
                if (entry.getCraftingOption() == null
                        && matcher.matches(entry.getInstance(), prediction.getInstance(), matchCondition)) {
                    long quantity = matcher.getQuantity(entry.getInstance()) + delta;
                    if (quantity <= 0) {
                        view.remove(i);
                    } else {
                        view.set(i, new InstanceWithMetadata<>(
                                matcher.withQuantity(entry.getInstance(), quantity), null));
                    }
                    applied = true;
                    break;
                }
            }
            if (!applied && delta > 0) {
                view.add(new InstanceWithMetadata<>(prediction.getInstance(), null));
            }
        }
    }

    /**
     * Predictions are stored for the channel they were caused in,
     * and are also shown in the wildcard channel, just like the server-sent changes.
     */
    protected boolean appliesTo(Prediction<T> prediction, int channel) {
        return prediction.getChannel() == channel
                || (channel == IPositionedAddonsNetwork.WILDCARD_CHANNEL
                    && prediction.getChannel() != IPositionedAddonsNetwork.WILDCARD_CHANNEL);
    }

    public static class Prediction<T> {

        private final int channel;
        private T instance;
        private final boolean addition;
        private final long expiryTime;

        public Prediction(int channel, T instance, boolean addition, long expiryTime) {
            this.channel = channel;
            this.instance = instance;
            this.addition = addition;
            this.expiryTime = expiryTime;
        }

        public int getChannel() {
            return channel;
        }

        public T getInstance() {
            return instance;
        }

        /**
         * Reduce this prediction to the part that the server has not confirmed yet.
         * @param instance The remaining instance.
         */
        public void setInstance(T instance) {
            this.instance = instance;
        }

        public boolean isAddition() {
            return addition;
        }

        public long getExpiryTime() {
            return expiryTime;
        }

    }

}
