package org.cyclops.integratedterminals.api.terminalstorage.event;

import net.minecraftforge.eventbus.api.Event;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

import java.util.List;

/**
 * An event that is emitted on the Forge event bus when
 * a {@link ITerminalStorageTabClient} is constructed and populates its buttons.
 * is called.
 * @author rubensworks
 */
public class TerminalStorageTabClientLoadButtonsEvent extends Event {

    private final ContainerTerminalStorageBase container;
    private final ITerminalStorageTabClient<?> clientTab;

    private List<ITerminalButton<?, ?, ?>> buttons;

    public TerminalStorageTabClientLoadButtonsEvent(ContainerTerminalStorageBase container,
                                                    ITerminalStorageTabClient<?> clientTab,
                                                    List<ITerminalButton<?, ?, ?>> buttons) {
        this.container = container;
        this.clientTab = clientTab;

        this.buttons = buttons;
    }

    public ContainerTerminalStorageBase getContainer() {
        return container;
    }

    public ITerminalStorageTabClient<?> getClientTab() {
        return clientTab;
    }

    public List<ITerminalButton<?, ?, ?>> getButtons() {
        return buttons;
    }

    public void setButtons(List<ITerminalButton<?, ?, ?>> buttons) {
        this.buttons = buttons;
    }
}
