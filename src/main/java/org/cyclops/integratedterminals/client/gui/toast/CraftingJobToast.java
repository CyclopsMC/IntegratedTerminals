package org.cyclops.integratedterminals.client.gui.toast;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integratedterminals.Reference;

import java.util.List;

/**
 * A toast that shows an item icon alongside a title and a wrapping subtitle.
 *
 * Toasts with an equal token replace each other instead of being queued,
 * so the token determines how toasts are grouped.
 *
 * @author rubensworks
 */
public class CraftingJobToast implements Toast {

    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "toast/crafting_job");
    private static final int DISPLAY_MILLIS = 5000;
    private static final int MARGIN = 7;
    private static final int ICON_LEFT = 7;
    private static final int ICON_SIZE = 16;
    private static final int TEXT_LEFT = ICON_LEFT + ICON_SIZE + 5;
    private static final int LINE_SPACING = 12;

    private final Object token;
    private final ItemStack icon;
    private Component title;
    private List<FormattedCharSequence> subtitleLines;
    private long lastChangedAt = Long.MIN_VALUE;
    private boolean changed = true;

    public CraftingJobToast(Object token, ItemStack icon, Component title, Component subtitle) {
        this.token = token;
        this.icon = icon;
        this.title = title;
        this.subtitleLines = splitSubtitle(subtitle);
    }

    /**
     * Update the contents of this toast in-place, without queueing a new one.
     * @param newTitle The new title.
     * @param newSubtitle The new subtitle.
     */
    public void reset(Component newTitle, Component newSubtitle) {
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
    public Visibility render(GuiGraphics graphics, ToastComponent toastComponent, long timeSinceLastVisible) {
        if (changed) {
            lastChangedAt = timeSinceLastVisible;
            changed = false;
        }

        graphics.blitSprite(BACKGROUND_SPRITE, 0, 0, width(), height());
        graphics.renderItem(icon, ICON_LEFT, 8);

        var font = toastComponent.getMinecraft().font;
        graphics.drawString(font, title, TEXT_LEFT, 7, 0xFFFFFF, false);
        for (int i = 0; i < subtitleLines.size(); i++) {
            graphics.drawString(font, subtitleLines.get(i), TEXT_LEFT, 18 + i * LINE_SPACING, 0xAAAAAA, false);
        }

        return timeSinceLastVisible - lastChangedAt < (long) (DISPLAY_MILLIS * toastComponent.getNotificationDisplayTimeMultiplier())
                ? Visibility.SHOW
                : Visibility.HIDE;
    }

    @Override
    public Object getToken() {
        return token;
    }

}
