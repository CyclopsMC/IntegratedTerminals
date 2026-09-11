package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Helpers for telling the server which container slots a client-side prediction changed.
 *
 * This is the same reconciliation that vanilla container clicks use:
 * the client sends the slots it changed, and the server only sends back the ones it disagrees with.
 *
 * @author rubensworks
 */
public final class ContainerHelpers {

    private ContainerHelpers() {
    }

    /**
     * @param container A container.
     * @return A copy of the contents of all slots in the given container.
     */
    public static List<ItemStack> copyContents(AbstractContainerMenu container) {
        List<ItemStack> contents = Lists.newArrayListWithExpectedSize(container.slots.size());
        for (Slot slot : container.slots) {
            contents.add(slot.getItem().copy());
        }
        return contents;
    }

    /**
     * Determine which slots of the given container changed since the given contents were copied.
     * @param contentsBefore The contents from {@link #copyContents(AbstractContainerMenu)}.
     * @param container The container, after it was changed.
     * @return The new contents of the changed slots, by slot id.
     */
    public static Map<Integer, ItemStack> getChangedContents(List<ItemStack> contentsBefore,
                                                             AbstractContainerMenu container) {
        Map<Integer, ItemStack> changed = Maps.newHashMap();
        for (int i = 0; i < contentsBefore.size(); i++) {
            ItemStack after = container.getSlot(i).getItem();
            if (!ItemStack.matches(contentsBefore.get(i), after)) {
                changed.put(i, after.copy());
            }
        }
        return changed;
    }

}
