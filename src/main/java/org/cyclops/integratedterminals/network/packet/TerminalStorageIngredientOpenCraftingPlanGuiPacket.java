package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.core.client.gui.ExtendedGuiHandler;
import org.cyclops.integratedterminals.proxy.guiprovider.GuiProviders;

/**
 * Packet for opening the crafting plan gui.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientOpenCraftingPlanGuiPacket<T, M> extends TerminalStorageIngredientCraftingOptionDataPacketAbstract<T, M> {

    public TerminalStorageIngredientOpenCraftingPlanGuiPacket() {

    }

    public TerminalStorageIngredientOpenCraftingPlanGuiPacket(CraftingOptionGuiData<T, M> craftingOptionData) {
        super(craftingOptionData);
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        CraftingOptionGuiData<T, M> data = getCraftingOptionData();
        IntegratedTerminals._instance.getGuiHandler().setTemporaryData(ExtendedGuiHandler.CRAFTING_OPTION,
                Pair.of(data.getSide(), data));
        BlockPos cPos = data.getPos();
        player.openGui(IntegratedTerminals._instance, GuiProviders.GUI_TERMINAL_STORAGE_CRAFTNG_PLAN,
                world, cPos.getX(), cPos.getY(), cPos.getZ());
    }

}