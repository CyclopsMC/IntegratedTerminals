package org.cyclops.integratedterminals.client.gui.toast;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integratedterminals.Capabilities;

/**
 * Client-only helpers for showing {@link CraftingJobToast}s.
 *
 * The JVM verifier loads client-only classes when linking any class that refers to them,
 * so this must stay separated from the common code that triggers these toasts,
 * as that would crash dedicated servers.
 *
 * @author rubensworks
 */
public final class CraftingJobToastHelpers {

    private CraftingJobToastHelpers() {
    }

    /**
     * Show a toast for a finished crafting job.
     * @param ingredientComponent The component of the crafted output.
     * @param instance The crafted output, where the quantity is the total that was crafted.
     * @param <T> The instance type.
     * @param <M> The matching condition parameter.
     */
    public static <T, M> void showCraftingJobFinished(IngredientComponent<T, M> ingredientComponent, T instance) {
        IIngredientMatcher<T, M> matcher = ingredientComponent.getMatcher();

        // Group by output, so that repeated crafts of the same thing don't pile up.
        // A job that was distributed over multiple crafting interfaces completes as several jobs,
        // so their quantities are summed into a single toast.
        Object token = ingredientComponent.getName() + "|" + matcher.getDisplayName(instance).getString();
        var toasts = Minecraft.getInstance().gui.toastManager();
        CraftingJobToast<T, M> existing = (CraftingJobToast<T, M>) toasts.getToast(CraftingJobToast.class, token);
        if (existing != null) {
            instance = matcher.withQuantity(instance, addQuantities(matcher,
                    matcher.getQuantity(existing.getInstance()), matcher.getQuantity(instance)));
        }

        // The quantity is formatted by the component's own handler, so that fluids, energy,
        // and ingredient components from other mods all read naturally.
        T shownInstance = instance;
        String quantity = ingredientComponent
                .getCapability(Capabilities.IngredientComponentTerminalStorageHandler.INGREDIENT)
                .map(handler -> handler.formatQuantity(shownInstance))
                .orElseGet(() -> String.valueOf(matcher.getQuantity(shownInstance)));
        Component title = Component.translatable("gui.integratedterminals.crafting_job.finished.title")
                .withStyle(ChatFormatting.GREEN);
        Component subtitle = Component.translatable("gui.integratedterminals.crafting_job.finished",
                quantity, matcher.getDisplayName(shownInstance));

        if (existing != null) {
            existing.reset(shownInstance, title, subtitle);
        } else {
            toasts.addToast(new CraftingJobToast<>(token, ingredientComponent, shownInstance, title, subtitle));
        }
    }

    protected static <T, M> long addQuantities(IIngredientMatcher<T, M> matcher, long quantity, long quantityToAdd) {
        try {
            return Math.min(matcher.getMaximumQuantity(), Math.addExact(quantity, quantityToAdd));
        } catch (ArithmeticException e) {
            return matcher.getMaximumQuantity();
        }
    }

}
