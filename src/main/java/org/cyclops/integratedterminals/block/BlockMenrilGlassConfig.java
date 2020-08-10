package org.cyclops.integratedterminals.block;

import net.minecraft.block.Block;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
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
                eConfig -> new GlassBlock(Block.Properties.create(Material.GLASS)
                        .hardnessAndResistance(0.3F)
                        .sound(SoundType.GLASS)
                        .notSolid()),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

}
