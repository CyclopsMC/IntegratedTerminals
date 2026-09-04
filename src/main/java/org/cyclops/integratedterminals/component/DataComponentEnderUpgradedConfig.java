package org.cyclops.integratedterminals.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import org.cyclops.cyclopscore.config.extendedconfig.DataComponentConfig;
import org.cyclops.integratedterminals.IntegratedTerminals;

/**
 * @author rubensworks
 */
public class DataComponentEnderUpgradedConfig extends DataComponentConfig<Boolean> {

    public DataComponentEnderUpgradedConfig() {
        super(IntegratedTerminals._instance, "ender_upgraded", builder -> builder
                .persistent(Codec.BOOL)
                .networkSynchronized(ByteBufCodecs.BOOL));
    }
}
