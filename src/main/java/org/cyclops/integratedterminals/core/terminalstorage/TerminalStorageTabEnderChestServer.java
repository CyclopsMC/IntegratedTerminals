package org.cyclops.integratedterminals.core.terminalstorage;

import net.minecraft.resources.ResourceLocation;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabServer;

/**
 * A server-side storage terminal tab for the ender chest.
 *
 * The ender chest contents are exposed through regular container slots,
 * so no server-side synchronization logic is needed here.
 *
 * @author rubensworks
 */
public class TerminalStorageTabEnderChestServer implements ITerminalStorageTabServer {

    private final ResourceLocation name;

    public TerminalStorageTabEnderChestServer(ResourceLocation name) {
        this.name = name;
    }

    @Override
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public void init() {

    }

    @Override
    public void deInit() {

    }

    @Override
    public void updateActive(int channel) {

    }
}
