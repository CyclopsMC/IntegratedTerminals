package org.cyclops.integratedterminals.core.terminalstorage;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTab;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabServer;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

/**
 * A storage terminal tab that exposes the ender chest inventory of the player.
 *
 * This tab is only available in terminals that have been ender-upgraded.
 *
 * @author rubensworks
 */
public class TerminalStorageTabEnderChest implements ITerminalStorageTab {

    public static final Identifier NAME = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "ender_chest");

    @Override
    public Identifier getName() {
        return NAME;
    }

    @Override
    public ITerminalStorageTabClient<?> createClientTab(ContainerTerminalStorageBase container, Player player) {
        return new TerminalStorageTabEnderChestClient(container, getName());
    }

    @Override
    public ITerminalStorageTabServer createServerTab(ContainerTerminalStorageBase container, Player player, INetwork network) {
        return new TerminalStorageTabEnderChestServer(getName());
    }

    @Override
    public ITerminalStorageTabCommon createCommonTab(ContainerTerminalStorageBase container, Player player) {
        return new TerminalStorageTabEnderChestCommon(container, getName());
    }
}
