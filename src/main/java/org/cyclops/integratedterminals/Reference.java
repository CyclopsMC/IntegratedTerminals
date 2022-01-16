package org.cyclops.integratedterminals;

import org.cyclops.cyclopscore.helper.MinecraftHelpers;

/**
 * Class that can hold basic static things that are better not hard-coded
 * like mod details, texture paths, ID's...
 * @author rubensworks (aka kroeserr)
 *
 */
@SuppressWarnings("javadoc")
public class Reference {

    // Mod info
    public static final String MOD_ID = "integratedterminals";
    public static final String GA_TRACKING_ID = "UA-65307010-18";
    public static final String VERSION_URL = "https://raw.githubusercontent.com/CyclopsMC/Versions/master/" + MinecraftHelpers.getMinecraftVersionMajorMinor() + "/IntegratedTerminals.txt";

    // MOD ID's
    public static final String MOD_FORGE = "forge";
    public static final String MOD_CYCLOPSCORE = "cyclopscore";
    public static final String MOD_INTEGRATEDDYNAMICS = "integrateddynamics";
    public static final String MOD_INTEGRATECRAFTING = "integratedcrafting";

}
