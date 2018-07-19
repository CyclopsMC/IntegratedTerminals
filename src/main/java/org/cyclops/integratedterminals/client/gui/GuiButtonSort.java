package org.cyclops.integratedterminals.client.gui;

import net.minecraft.client.Minecraft;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonImage;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.integratedterminals.client.gui.image.Images;

/**
 * A gui button for toggling sorting modes.
 * @author rubensworks
 */
public class GuiButtonSort extends GuiButtonImage {

    private final boolean active;
    private final boolean descending;

    public GuiButtonSort(int id, int x, int y, IImage image, boolean active, boolean descending) {
        super(id, x, y, image);
        this.active = active;
        this.descending = descending;
    }

    @Override
    protected void drawButtonInner(Minecraft minecraft, int i, int j, boolean mouseOver) {
        (active ? Images.BUTTON_BACKGROUND_ACTIVE : Images.BUTTON_BACKGROUND_INACTIVE).draw(this, x, y);
        super.drawButtonInner(minecraft, i, j, mouseOver);
        if (active) {
            (descending ? Images.BUTTON_OVERLAY_DESCENDING : Images.BUTTON_OVERLAY_ASCENDING).draw(this, x, y);
        }
    }
}
