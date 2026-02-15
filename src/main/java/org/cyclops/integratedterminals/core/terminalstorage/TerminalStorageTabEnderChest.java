package org.cyclops.integratedterminals.core.terminalstorage;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTab;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabServer;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

import javax.annotation.Nullable;

/**
 * Terminal storage tab for Ender Chest.
 * @author rubensworks
 */
public class TerminalStorageTabEnderChest implements ITerminalStorageTab {

    public static ResourceLocation NAME = new ResourceLocation(Reference.MOD_ID, "ender_chest");

    @Override
    public ResourceLocation getName() {
        return NAME;
    }

    @Override
    public ITerminalStorageTabClient<?> createClientTab(ContainerTerminalStorageBase container, Player player) {
        return new TerminalStorageTabEnderChestClient(container, getName());
    }

    @Override
    public ITerminalStorageTabServer createServerTab(ContainerTerminalStorageBase container, Player player, INetwork network) {
        return new TerminalStorageTabEnderChestServer(getName(), (ServerPlayer) player);
    }

    @Nullable
    @Override
    public ITerminalStorageTabCommon createCommonTab(ContainerTerminalStorageBase container, Player player) {
        return new TerminalStorageTabEnderChestCommon(container, getName());
    }
}
