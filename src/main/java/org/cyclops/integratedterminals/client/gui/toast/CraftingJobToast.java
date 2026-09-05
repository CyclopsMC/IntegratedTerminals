package org.cyclops.integratedterminals.client.gui.toast;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integratedterminals.Capabilities;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;

import java.util.List;

/**
 * A toast that shows an ingredient icon alongside a title and a wrapping subtitle.
 *
 * Toasts with an equal token replace each other instead of being queued,
 * so the token determines how toasts are grouped.
 *
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class CraftingJobToast<T, M> implements Toast {

    private static final Identifier BACKGROUND_SPRITE = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "toast/crafting_job");
    private static final int DISPLAY_MILLIS = 5000;
    private static final int MARGIN = 7;
    private static final int ICON_LEFT = 7;
    private static final int ICON_SIZE = 16;
    private static final int TEXT_LEFT = ICON_LEFT + ICON_SIZE + 5;
    private static final int LINE_SPACING = 12;

    private final Object token;
    private final IngredientComponent<T, M> ingredientComponent;
    private T instance;
    private Component title;
    private List<FormattedCharSequence> subtitleLines;
    private long lastChangedAt = Long.MIN_VALUE;
    private boolean changed = true;
    private Visibility wantedVisibility = Visibility.SHOW;

    public CraftingJobToast(Object token, IngredientComponent<T, M> ingredientComponent, T instance,
                            Component title, Component subtitle) {
        this.token = token;
        this.ingredientComponent = ingredientComponent;
        this.instance = instance;
        this.title = title;
        this.subtitleLines = splitSubtitle(subtitle);
    }

    public IngredientComponent<T, M> getIngredientComponent() {
        return ingredientComponent;
    }

    /**
     * @return The shown output, where the quantity is the total that was crafted.
     */
    public T getInstance() {
        return instance;
    }

    /**
     * Update the contents of this toast in-place, without queueing a new one.
     * @param newInstance The new output.
     * @param newTitle The new title.
     * @param newSubtitle The new subtitle.
     */
    public void reset(T newInstance, Component newTitle, Component newSubtitle) {
        this.instance = newInstance;
        this.title = newTitle;
        this.subtitleLines = splitSubtitle(newSubtitle);
        this.changed = true;
    }

    private List<FormattedCharSequence> splitSubtitle(Component text) {
        return Minecraft.getInstance().font.split(text, width() - TEXT_LEFT - MARGIN);
    }

    @Override
    public int height() {
        return 20 + Math.max(1, subtitleLines.size()) * LINE_SPACING;
    }

    @Override
    public Visibility getWantedVisibility() {
        return this.wantedVisibility;
    }

    @Override
    public void update(ToastManager manager, long timeSinceLastVisible) {
        if (changed) {
            lastChangedAt = timeSinceLastVisible;
            changed = false;
        }

        this.wantedVisibility = timeSinceLastVisible - lastChangedAt < (long) (DISPLAY_MILLIS * manager.getNotificationDisplayTimeMultiplier())
                ? Visibility.SHOW
                : Visibility.HIDE;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long timeSinceLastVisible) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, width(), height());

        // Drawing through the storage handler keeps this working for items, fluids, energy, and any
        // ingredient component that other mods add. No screen is needed, as the background layer
        // draws the instance itself and only the foreground layer renders tooltips.
        this.ingredientComponent.getCapability(Capabilities.IngredientComponentTerminalStorageHandler.INGREDIENT)
                .ifPresent(handler -> handler.getClient().drawInstance(graphics, this.instance,
                        this.ingredientComponent.getMatcher().getQuantity(this.instance), null, null,
                        ContainerScreenTerminalStorage.DrawLayer.BACKGROUND, 0, ICON_LEFT, 8, 0, 0, null, null));

        graphics.text(font, title, TEXT_LEFT, 7, ARGB.opaque(0xFFFFFF), false);
        for (int i = 0; i < subtitleLines.size(); i++) {
            graphics.text(font, subtitleLines.get(i), TEXT_LEFT, 18 + i * LINE_SPACING, ARGB.opaque(0xAAAAAA), false);
        }
    }

    @Override
    public Object getToken() {
        return token;
    }

}
