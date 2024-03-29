package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

import javax.annotation.Nullable;

/**
 * A terminal storage tab.
 * @author rubensworks
 */
public interface ITerminalStorageTab {

    /**
     * @return The unique name of this tab.
     */
    public ResourceLocation getName();

    /**
     * @param container The container in which the tab is about to be created.
     * @param player The player opening the container.
     * @return A new client-side tab.
     */
    public ITerminalStorageTabClient<?> createClientTab(ContainerTerminalStorageBase container, Player player);

    /**
     * @param container The container in which the tab is about to be created.
     * @param player The player opening the container.
     * @param network The network of the storage terminal.
     * @return A new server-side tab.
     */
    public ITerminalStorageTabServer createServerTab(ContainerTerminalStorageBase container, Player player, INetwork network);

    /**
     * @param container The container in which the tab is about to be created.
     * @param player The player opening the container.
     * @return A new common tab.
     */
    @Nullable
    public ITerminalStorageTabCommon createCommonTab(ContainerTerminalStorageBase container, Player player);

}
