package org.cyclops.integratedterminals.item;

import net.minecraft.world.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integratedterminals.IntegratedTerminals;

/**
 * Config for the portable storage terminal.
 * @author rubensworks
 */
public class ItemTerminalStoragePortableConfig extends ItemConfig {

    public ItemTerminalStoragePortableConfig() {
        super(
                IntegratedTerminals._instance,
                "terminal_storage_portable",
                eConfig -> new ItemTerminalStoragePortable(new Item.Properties())
        );
    }

}
