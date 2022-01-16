package org.cyclops.integratedterminals.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
                eConfig -> new GlassBlock(Block.Properties.of(Material.GLASS)
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
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(getInstance(), RenderType.translucent());
    }

}
