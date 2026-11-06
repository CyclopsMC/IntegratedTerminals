package org.cyclops.integratedterminals.core.part;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.panel.PartTypePanel;
import org.cyclops.integratedterminals.IntegratedTerminals;

/**
 * Base part for a terminal.
 * @author rubensworks
 */
public abstract class PartTypeTerminal<P extends PartTypeTerminal<P, S>, S extends IPartState<P>> extends PartTypePanel<P, S> {

    /**
     * The maximum squared distance from which players can interact with terminal parts, as for vanilla containers.
     */
    public static final double MAX_INTERACTION_DISTANCE_SQ = 64.0D;

    public PartTypeTerminal(String name) {
        super(name);
    }

    /**
     * Check if a player is close enough to a part position to interact with it.
     * @param player A player.
     * @param partPos A part position.
     * @return If the position is in the player's level, loaded, and within interaction distance.
     */
    public static boolean isWithinInteractionDistance(Player player, PartPos partPos) {
        BlockPos blockPos = partPos.getPos().getBlockPos();
        return partPos.getPos().getLevelKey().equals(player.level().dimension())
                && player.level().isLoaded(blockPos)
                && player.distanceToSqr(Vec3.atCenterOf(blockPos)) <= MAX_INTERACTION_DISTANCE_SQ;
    }

    /**
     * Check if a player can interact with a part at a position that was sent by the client.
     * @param player A player.
     * @param partPos A part position.
     * @param partType The expected part type.
     * @return If the part is within interaction distance and has the expected type.
     */
    public static boolean canInteractWith(Player player, PartPos partPos, IPartType<?, ?> partType) {
        if (!isWithinInteractionDistance(player, partPos)) {
            return false;
        }
        PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(partPos);
        return partStateHolder != null && partStateHolder.getPart() == partType;
    }

    @Override
    public InteractionResult onPartActivated(S partState, BlockPos pos, Level world, Player player, InteractionHand hand, ItemStack heldItem, BlockHitResult hit) {
        if (isUpdate(partState) && !partState.isEnabled()) {
            player.displayClientMessage(Component.translatable(L10NValues.PART_ERROR_LOWENERGY), true);
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
