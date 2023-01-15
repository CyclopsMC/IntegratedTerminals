package org.cyclops.integratedterminals.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.integratedterminals.client.gui.image.Images;

import net.minecraft.client.gui.components.Button.OnPress;

/**
 * A gui button for toggling sorting modes.
 * @author rubensworks
 */
public class ButtonSort extends ButtonImage {

    private final boolean active;
    private final boolean descending;

    public ButtonSort(int x, int y, Component narrationMessage, OnPress pressCallback, IImage image, boolean active, boolean descending) {
        super(x, y, narrationMessage, pressCallback, image);
        this.active = active;
        this.descending = descending;
    }

    @Override
    protected void drawButtonInner(PoseStack matrixStack, int mouseX, int mouseY) {
        (active ? Images.BUTTON_BACKGROUND_ACTIVE : Images.BUTTON_BACKGROUND_INACTIVE).draw(this, matrixStack, getX(), getY());
        super.drawButtonInner(matrixStack, mouseX, mouseY);
        if (active) {
            (descending ? Images.BUTTON_OVERLAY_DESCENDING : Images.BUTTON_OVERLAY_ASCENDING).draw(this, matrixStack, getX(), getY());
        }
    }
}
