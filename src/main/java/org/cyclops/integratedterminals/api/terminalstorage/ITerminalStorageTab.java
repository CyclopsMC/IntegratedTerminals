package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;

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
     * @param target The terminal storage part target.
     * @return A new client-side tab.
     */
    public ITerminalStorageTabClient<?> createClientTab(ContainerTerminalStorage container, EntityPlayer player, PartTarget target);

    /**
     * @param container The container in which the tab is about to be created.
     * @param player The player opening the container.
     * @param target The terminal storage part target.
     * @return A new server-side tab.
     */
    public ITerminalStorageTabServer createServerTab(ContainerTerminalStorage container, EntityPlayer player, PartTarget target);

    /**
     * @param container The container in which the tab is about to be created.
     * @param player The player opening the container.
     * @param target The terminal storage part target.
     * @return A new common tab.
     */
    @Nullable
    public ITerminalStorageTabCommon createCommonTab(ContainerTerminalStorage container, EntityPlayer player, PartTarget target);

}
