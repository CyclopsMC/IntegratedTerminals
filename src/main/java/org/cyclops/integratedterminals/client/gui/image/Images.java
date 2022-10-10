package org.cyclops.integratedterminals.client.gui.image;

import net.minecraft.resources.ResourceLocation;
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
    public static final Image BUTTON_MIDDLE_AUTOREFILL_DISABLED = new Image(ICONS, 54, 18, 18, 18);
    public static final Image BUTTON_MIDDLE_AUTOREFILL_STORAGE = new Image(ICONS, 72, 18, 18, 18);
    public static final Image BUTTON_MIDDLE_AUTOREFILL_PLAYER = new Image(ICONS, 90, 18, 18, 18);
    public static final Image BUTTON_MIDDLE_AUTOREFILL_STORAGEPLAYER = new Image(ICONS, 108, 18, 18, 18);
    public static final Image BUTTON_MIDDLE_AUTOREFILL_PLAYERSTORAGE = new Image(ICONS, 126, 18, 18, 18);
    public static final Image BUTTON_MIDDLE_JEI_SYNC = new Image(ICONS, 144, 18, 18, 18);
    public static final Image BUTTON_MIDDLE_FILTER_CRAFTING_ALL = new Image(ICONS, 162, 18, 18, 18);
    public static final Image BUTTON_MIDDLE_FILTER_CRAFTING_STORAGE = new Image(ICONS, 180, 18, 18, 18);
    public static final Image BUTTON_MIDDLE_FILTER_CRAFTING_CRAFTABLE = new Image(ICONS, 198, 18, 18, 18);
    public static final Image BUTTON_MIDDLE_SCALE_XY = new Image(ICONS, 18, 36, 18, 18);
    public static final Image BUTTON_MIDDLE_SCALE_Y = new Image(ICONS, 36, 36, 18, 18);
    public static final Image BUTTON_MIDDLE_SCALE_X = new Image(ICONS, 54, 36, 18, 18);
    public static final Image BUTTON_MIDDLE_SCALE_SMALL = new Image(ICONS, 72, 36, 18, 18);
    public static final Image BUTTON_MIDDLE_SCALE_MEDIUM = new Image(ICONS, 90, 36, 18, 18);
    public static final Image BUTTON_MIDDLE_SCALE_LARGE = new Image(ICONS, 108, 36, 18, 18);

    public static final Image BUTTON_SMALL_BACKGROUND_INACTIVE = new Image(ICONS, 0, 36, 8, 8);

    public static final Image BUTTON_SMALL_OVERLAY_CROSS = new Image(ICONS, 0, 44, 8, 8);
    public static final Image BUTTON_SMALL_OVERLAY_SQUARE = new Image(ICONS, 8, 44, 8, 8);

}
