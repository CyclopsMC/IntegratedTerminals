package org.cyclops.integratedterminals.core.terminalstorage;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabServer;

/**
 * A server-side storage terminal tab for Ender Chest.
 * @author rubensworks
 */
public class TerminalStorageTabEnderChestServer implements ITerminalStorageTabServer {

    private final ResourceLocation name;
    private final ServerPlayer player;

    public TerminalStorageTabEnderChestServer(ResourceLocation name, ServerPlayer player) {
        this.name = name;
        this.player = player;
    }

    @Override
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public void init() {
        // No initialization needed
    }

    @Override
    public void deInit() {
        // No cleanup needed
    }

    @Override
    public void updateActive() {
        // No active updates needed - Minecraft handles Ender Chest inventory updates automatically
    }
}
