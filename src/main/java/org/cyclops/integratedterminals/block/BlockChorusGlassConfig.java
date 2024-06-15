package org.cyclops.integratedterminals.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TintedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
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
                eConfig -> new TintedGlassBlock(Block.Properties.of()
                        .strength(0.3F)
                        .sound(SoundType.GLASS)
                        .noOcclusion()) {
                    @Override
                    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
                        return Shapes.empty();
                    }
                },
                getDefaultItemConstructor(IntegratedTerminals._instance)
        );
    }

}
