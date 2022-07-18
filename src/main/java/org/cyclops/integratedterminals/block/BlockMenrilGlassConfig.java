package org.cyclops.integratedterminals.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
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
                eConfig -> new GlassBlock(Block.Properties.of(Material.GLASS)
                        .strength(0.3F)
                        .sound(SoundType.GLASS)
                        .lightLevel((state) -> 3)
                        .noOcclusion()),
                getDefaultItemConstructor(IntegratedTerminals._instance)
        );
    }

}
