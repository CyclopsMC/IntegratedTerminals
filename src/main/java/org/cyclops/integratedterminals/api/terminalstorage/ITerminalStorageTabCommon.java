package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.cyclops.integratedterminals.part.PartTypeTerminalStorage;

import java.util.Collections;
import java.util.List;

/**
 * A common-side terminal storage tab for loading slots.
 * @author rubensworks
 */
public interface ITerminalStorageTabCommon {

    /**
     * @return The unique tab id, must be equal to its client-side variant.
     */
    public String getId();

    public default List<Slot> loadSlots(Container container, int startIndex, EntityPlayer player,
                                        PartTypeTerminalStorage.State partState) {
        return Collections.emptyList();
    }

}
