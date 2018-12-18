package org.cyclops.integratedterminals.core.client.gui;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;

/**
 * @author rubensworks
 */
public class CraftingJobGuiData {

    private final BlockPos pos;
    private final EnumFacing side;
    private final int channel;
    private final ITerminalStorageTabIngredientCraftingHandler handler;
    private final Object craftingJob;

    public CraftingJobGuiData(BlockPos pos, EnumFacing side, int channel,
                              ITerminalStorageTabIngredientCraftingHandler handler, Object craftingJob) {
        this.pos = pos;
        this.side = side;
        this.channel = channel;
        this.handler = handler;
        this.craftingJob = craftingJob;
    }

    public BlockPos getPos() {
        return pos;
    }

    public EnumFacing getSide() {
        return side;
    }

    public int getChannel() {
        return channel;
    }

    public ITerminalStorageTabIngredientCraftingHandler getHandler() {
        return handler;
    }

    public Object getCraftingJob() {
        return craftingJob;
    }
}
