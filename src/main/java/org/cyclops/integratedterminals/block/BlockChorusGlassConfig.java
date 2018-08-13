package org.cyclops.integratedterminals.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockGlass;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integratedterminals.IntegratedTerminals;

import javax.annotation.Nullable;

/**
 * Config for the Crystalized Menril block.
 * @author rubensworks
 *
 */
public class BlockChorusGlassConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static BlockChorusGlassConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockChorusGlassConfig() {
        super(
                IntegratedTerminals._instance,
                true,
                "chorus_glass",
                null,
                null
        );
    }

    @Override
    protected ConfigurableBlockGlass initSubInstance() {
        ConfigurableBlockGlass block = new ConfigurableBlockGlass(this, Material.GLASS, true) {
            @Override
            public BlockRenderLayer getBlockLayer() {
                return BlockRenderLayer.TRANSLUCENT;
            }

            @Nullable
            @Override
            public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
                // Allow any entity to walk through this block
                return null;
            }
        };
        return block;
    }
    
}
