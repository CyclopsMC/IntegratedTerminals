package org.cyclops.integratedterminals.core.part;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.part.panel.PartTypePanel;
import org.cyclops.integratedterminals.IntegratedTerminals;

/**
 * Base part for a terminal.
 * @author rubensworks
 */
public abstract class PartTypeTerminal<P extends PartTypeTerminal<P, S>, S extends IPartState<P>> extends PartTypePanel<P, S> {

    public PartTypeTerminal(String name) {
        super(name);
    }

    @Override
    public ActionResultType onPartActivated(S partState, BlockPos pos, World world, PlayerEntity player, Hand hand, ItemStack heldItem, BlockRayTraceResult hit) {
        if (!partState.isEnabled()) {
            player.sendStatusMessage(new TranslationTextComponent(L10NValues.PART_ERROR_LOWENERGY), true);
            return ActionResultType.FAIL;
        }
        return super.onPartActivated(partState, pos, world, player, hand, heldItem, hit);
    }

    @Override
    protected Block createBlock(BlockConfig blockConfig) {
        return new IgnoredBlock();
    }

    @Override
    public ModBase getMod() {
        return IntegratedTerminals._instance;
    }

    @Override
    public boolean isUpdate(S state) {
        return getConsumptionRate(state) > 0 && org.cyclops.integrateddynamics.GeneralConfig.energyConsumptionMultiplier > 0;
    }

}
