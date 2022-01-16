package org.cyclops.integratedterminals.api.terminalstorage.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.eventbus.api.Event;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

import java.util.List;
import java.util.Optional;

/**
 * An event that is emitted on the Forge event bus after
 * {@link ITerminalStorageTabCommon#loadSlots(AbstractContainerMenu, int, Player, Optional)}
 * is called.
 * @author rubensworks
 */
public class TerminalStorageTabCommonLoadSlotsEvent extends Event {

    private final ITerminalStorageTabCommon commonTab;
    private final ContainerTerminalStorageBase container;

    private List<Slot> slots;

    public TerminalStorageTabCommonLoadSlotsEvent(ITerminalStorageTabCommon commonTab,
                                                  ContainerTerminalStorageBase container,
                                                  List<Slot> slots) {
        this.commonTab = commonTab;
        this.container = container;

        this.slots = slots;
    }


    public ITerminalStorageTabCommon getCommonTab() {
        return commonTab;
    }

    public ContainerTerminalStorageBase getContainer() {
        return container;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public void setSlots(List<Slot> slots) {
        this.slots = slots;
    }
}
