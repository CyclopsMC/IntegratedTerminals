package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingPlan;

/**
 * Packet for starting a crafting job.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientStartCraftingJobPacket<T, M> extends TerminalStorageIngredientCraftingOptionDataPacketAbstract<T, M> {

    public TerminalStorageIngredientStartCraftingJobPacket() {

    }

    public TerminalStorageIngredientStartCraftingJobPacket(CraftingOptionGuiData<T, M> craftingOptionData) {
        super(craftingOptionData);
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        HandlerWrappedTerminalCraftingPlan craftingPlan = getCraftingPlan();
        if (craftingPlan != null) {
            CraftingOptionGuiData<T, M> data = getCraftingOptionData();
            INetwork network = NetworkHelpers.getNetwork(PartPos.of(world, data.getPos(), data.getSide()));
            if (network != null) {
                craftingPlan.getHandler().startCraftingJob(network, getChannel(), craftingPlan.getCraftingPlan());
            }
        }
    }

}