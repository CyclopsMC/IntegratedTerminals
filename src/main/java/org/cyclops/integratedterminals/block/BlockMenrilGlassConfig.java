package org.cyclops.integratedterminals.block;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(getInstance(), RenderType.translucent());
    }

}
