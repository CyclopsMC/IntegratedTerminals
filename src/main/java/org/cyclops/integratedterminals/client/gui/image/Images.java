package org.cyclops.integratedterminals.client.gui.image;

import net.minecraft.util.ResourceLocation;
import org.cyclops.cyclopscore.client.gui.image.Image;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integratedterminals.IntegratedTerminals;

/**
 * Default images provided by this mod.
 * @author rubensworks
 */
public class Images {

    public static final ResourceLocation ICONS = new ResourceLocation(IntegratedTerminals._instance.getModId(),
            IntegratedTerminals._instance.getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI) + "icons.png");

    public static final Image BUTTON_BACKGROUND_INACTIVE = new Image(ICONS, 0, 0, 18, 18);
    public static final Image BUTTON_BACKGROUND_ACTIVE = new Image(ICONS, 18, 0, 18, 18);

    public static final Image BUTTON_OVERLAY_DESCENDING = new Image(ICONS, 36, 0, 18, 18);
    public static final Image BUTTON_OVERLAY_ASCENDING = new Image(ICONS, 54, 0, 18, 18);

    public static final Image BUTTON_MIDDLE_ID = new Image(ICONS, 0, 18, 18, 18);
    public static final Image BUTTON_MIDDLE_NAME = new Image(ICONS, 18, 18, 18, 18);
    public static final Image BUTTON_MIDDLE_QUANTITY = new Image(ICONS, 36, 18, 18, 18);

}
