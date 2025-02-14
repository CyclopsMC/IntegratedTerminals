package org.cyclops.integratedterminals.item;

import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integratedterminals.IntegratedTerminals;

/**
 * Config for the portable storage terminal.
 * @author rubensworks
 */
public class ItemTerminalStoragePortableConfig extends ItemConfigCommon<IModBase> {

    public ItemTerminalStoragePortableConfig() {
        super(
                IntegratedTerminals._instance,
                "terminal_storage_portable",
                (eConfig, properties) -> new ItemTerminalStoragePortable(properties)
        );
    }

}
