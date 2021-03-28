package org.cyclops.integratedterminals.api.terminalstorage.location;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;

/**
 * Indicates the location of a storage terminal.
 * @param <L> The location type
 * @author rubensworks
 */
public interface ITerminalStorageLocation<L> {

    public ResourceLocation getName();
    public <T, M> void openContainerFromClient(CraftingOptionGuiData<T, M, L> craftingOptionGuiData);
    public <T, M> void openContainerFromServer(CraftingOptionGuiData<T, M, L> craftingOptionGuiData, World world, ServerPlayerEntity player);
    public <T, M> void openContainerCraftingPlan(CraftingOptionGuiData<T, M, L> craftingOptionGuiData, World world, ServerPlayerEntity player);
    public <T, M> void openContainerCraftingOptionAmount(CraftingOptionGuiData<T, M, L> craftingOptionGuiData, World world, ServerPlayerEntity player);
    public void writeToPacketBuffer(PacketBuffer packetBuffer, L location);
    public L readFromPacketBuffer(PacketBuffer packetBuffer);

}
