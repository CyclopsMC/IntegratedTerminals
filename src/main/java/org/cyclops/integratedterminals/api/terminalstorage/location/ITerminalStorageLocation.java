package org.cyclops.integratedterminals.api.terminalstorage.location;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;

/**
 * Indicates the location of a storage terminal.
 * @param <L> The location type
 * @author rubensworks
 */
public interface ITerminalStorageLocation<L> {

    public ResourceLocation getName();
    public <T, M> void openContainerFromClient(CraftingOptionGuiData<T, M, L> craftingOptionGuiData);
    public <T, M> void openContainerFromServer(CraftingOptionGuiData<T, M, L> craftingOptionGuiData, Level world, ServerPlayer player);
    public <T, M> void openContainerCraftingPlan(CraftingOptionGuiData<T, M, L> craftingOptionGuiData, Level world, ServerPlayer player);
    public <T, M> void openContainerCraftingOptionAmount(CraftingOptionGuiData<T, M, L> craftingOptionGuiData, Level world, ServerPlayer player);
    public void writeToPacketBuffer(RegistryFriendlyByteBuf packetBuffer, L location);
    public L readFromPacketBuffer(RegistryFriendlyByteBuf packetBuffer);

}
