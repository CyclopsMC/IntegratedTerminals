package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.cyclops.integratedterminals.part.PartTypeTerminalStorage;

import java.util.Collections;
import java.util.List;

/**
 * A common-side terminal storage tab for loading slots.
 * @author rubensworks
 */
public interface ITerminalStorageTabCommon {

    /**
     * @return The unique tab name, as inherited from {@link ITerminalStorageTab#getName()}.
     */
    public ResourceLocation getName();

    public default List<Slot> loadSlots(Container container, int startIndex, EntityPlayer player,
                                        PartTypeTerminalStorage.State partState) {
        return Collections.emptyList();
    }

}
