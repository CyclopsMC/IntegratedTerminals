package org.cyclops.integratedterminals.api.terminalstorage.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;
import org.cyclops.integratedterminals.part.PartTypeTerminalStorage;

import java.util.List;

/**
 * An event that is emitted on the Forge event bus after
 * {@link ITerminalStorageTabCommon#loadSlots(Container, int, EntityPlayer, PartTypeTerminalStorage.State)}
 * is called.
 * @author rubensworks
 */
public class TerminalStorageTabCommonLoadSlotsEvent extends Event {

    private final ITerminalStorageTabCommon commonTab;
    private final ContainerTerminalStorage container;

    private List<Slot> slots;

    public TerminalStorageTabCommonLoadSlotsEvent(ITerminalStorageTabCommon commonTab,
                                                  ContainerTerminalStorage container,
                                                  List<Slot> slots) {
        this.commonTab = commonTab;
        this.container = container;

        this.slots = slots;
    }


    public ITerminalStorageTabCommon getCommonTab() {
        return commonTab;
    }

    public ContainerTerminalStorage getContainer() {
        return container;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public void setSlots(List<Slot> slots) {
        this.slots = slots;
    }
}
