package org.cyclops.integratedterminals.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integratedterminals.IntegratedTerminals;

/**
 * Config for the Crystalized Menril block.
 * @author rubensworks
 *
 */
public class BlockChorusGlassConfig extends BlockConfig {

    public BlockChorusGlassConfig() {
        super(
                IntegratedTerminals._instance,
                "chorus_glass",
                eConfig -> new GlassBlock(Block.Properties.create(Material.GLASS)
                        .hardnessAndResistance(0.3F)
                        .sound(SoundType.GLASS)
                        .notSolid()) {
                    @Override
                    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
                        return VoxelShapes.empty();
                    }
                },
                getDefaultItemConstructor(IntegratedTerminals._instance)
        );
    }

}
