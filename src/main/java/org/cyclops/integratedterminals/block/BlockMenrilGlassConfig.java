package org.cyclops.integratedterminals.block;

import net.minecraft.block.material.Material;
import net.minecraft.util.BlockRenderLayer;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockGlass;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;

/**
 * Config for the Crystalized Menril block.
 * @author rubensworks
 *
 */
public class BlockMenrilGlassConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static BlockMenrilGlassConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockMenrilGlassConfig() {
        super(
                IntegratedTerminals._instance,
                true,
                "menril_glass",
                null,
                null
        );
    }

    @Override
    protected ConfigurableBlockGlass initSubInstance() {
        ConfigurableBlockGlass block = new ConfigurableBlockGlass(this, Material.GLASS, true) {
            @Override
            public BlockRenderLayer getRenderLayer() {
                return BlockRenderLayer.TRANSLUCENT;
            }
        };
        block.setLightLevel(1F);
        return block;
    }

    @Override
    public String getOreDictionaryId() {
        return Reference.DICT_BLOCKGLASS;
    }
}
