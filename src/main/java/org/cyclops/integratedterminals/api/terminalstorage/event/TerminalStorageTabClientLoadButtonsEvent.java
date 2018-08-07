package org.cyclops.integratedterminals.api.terminalstorage.event;

import net.minecraftforge.fml.common.eventhandler.Event;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;

import java.util.List;

/**
 * An event that is emitted on the Forge event bus when
 * a {@link ITerminalStorageTabClient} is constructed and populates its buttons.
 * is called.
 * @author rubensworks
 */
public class TerminalStorageTabClientLoadButtonsEvent extends Event {

    private final ITerminalStorageTabClient<?> clientTab;

    private List<ITerminalButton<?, ?, ?>> buttons;

    public TerminalStorageTabClientLoadButtonsEvent(ITerminalStorageTabClient<?> clientTab,
                                                    List<ITerminalButton<?, ?, ?>> buttons) {
        this.clientTab = clientTab;

        this.buttons = buttons;
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
