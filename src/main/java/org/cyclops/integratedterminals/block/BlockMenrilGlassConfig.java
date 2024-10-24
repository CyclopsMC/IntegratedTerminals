package org.cyclops.integratedterminals.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TintedGlassBlock;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integratedterminals.IntegratedTerminals;

/**
 * Config for the Crystalized Menril block.
 * @author rubensworks
 *
 */
public class BlockMenrilGlassConfig extends BlockConfig {

    public BlockMenrilGlassConfig() {
        super(
                IntegratedTerminals._instance,
                "menril_glass",
                eConfig -> new TintedGlassBlock(Block.Properties.of()
                        .strength(0.3F)
                        .sound(SoundType.GLASS)
                        .lightLevel((state) -> 3)
                        .noOcclusion()),
                getDefaultItemConstructor(IntegratedTerminals._instance)
        );
    }

}
