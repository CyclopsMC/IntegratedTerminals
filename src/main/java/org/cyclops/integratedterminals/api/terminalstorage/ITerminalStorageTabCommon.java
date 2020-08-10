package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
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

    public default List<Slot> loadSlots(Container container, int startIndex, PlayerEntity player,
                                        PartTypeTerminalStorage.State partState) {
        return Collections.emptyList();
    }

    public default void onUpdate(Container container, PlayerEntity player, PartTypeTerminalStorage.State partState) {

    }

}
