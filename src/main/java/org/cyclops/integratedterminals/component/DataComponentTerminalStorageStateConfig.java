package org.cyclops.integratedterminals.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import org.cyclops.cyclopscore.config.extendedconfig.DataComponentConfig;
import org.cyclops.integrateddynamics.core.helper.Codecs;
import org.cyclops.integratedterminals.IntegratedTerminals;

/**
 * @author rubensworks
 */
public class DataComponentTerminalStorageStateConfig extends DataComponentConfig<CompoundTag> {

    public DataComponentTerminalStorageStateConfig() {
        super(IntegratedTerminals._instance, "terminal_storage_state", builder -> builder
                .persistent(Codecs.COMPOUND_TAG)
                .networkSynchronized(ByteBufCodecs.COMPOUND_TAG));
    }
}
