package org.cyclops.integratedterminals.core.part;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
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
    public InteractionResult onPartActivated(S partState, BlockPos pos, Level world, Player player, InteractionHand hand, ItemStack heldItem, BlockHitResult hit) {
        if (isUpdate(partState) && !partState.isEnabled()) {
            player.displayClientMessage(new TranslatableComponent(L10NValues.PART_ERROR_LOWENERGY), true);
            return InteractionResult.FAIL;
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
