package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

import java.util.List;
import java.util.Optional;

/**
 * A common storage terminal tab for Ender Chest.
 * @author rubensworks
 */
public class TerminalStorageTabEnderChestCommon implements ITerminalStorageTabCommon {

    private final ContainerTerminalStorageBase containerTerminalStorage;
    private final ResourceLocation name;

    public TerminalStorageTabEnderChestCommon(ContainerTerminalStorageBase containerTerminalStorage,
                                               ResourceLocation name) {
        this.containerTerminalStorage = containerTerminalStorage;
        this.name = name;
    }

    @Override
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public List<Pair<Slot, ISlotPositionCallback>> loadSlots(AbstractContainerMenu container, int startIndex, Player player,
                                                              Optional<IVariableInventory> variableInventoryOptional) {
        List<Pair<Slot, ISlotPositionCallback>> slots = Lists.newArrayList();

        // Add Ender Chest slots (27 slots in 3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9;
                int finalRow = row;
                int finalCol = col;

                Slot slot = new Slot(player.getEnderChestInventory(), slotIndex, 0, 0);
                ISlotPositionCallback positionCallback = (factors) -> {
                    int x = factors.offsetX() + finalCol * 18;
                    int y = factors.offsetY() + finalRow * 18;
                    return Pair.of(x, y);
                };

                slots.add(Pair.of(slot, positionCallback));
            }
        }

        return slots;
    }

    @Override
    public void onUpdate(AbstractContainerMenu container, Player player,
                         Optional<IVariableInventory> variableInventory) {
        // No special update logic needed for Ender Chest
    }
}
