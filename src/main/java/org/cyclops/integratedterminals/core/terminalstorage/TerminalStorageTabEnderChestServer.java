package org.cyclops.integratedterminals.core.terminalstorage;

import net.minecraft.resources.Identifier;
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

    private final Identifier name;

    public TerminalStorageTabEnderChestServer(Identifier name) {
        this.name = name;
    }

    @Override
    public Identifier getName() {
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
