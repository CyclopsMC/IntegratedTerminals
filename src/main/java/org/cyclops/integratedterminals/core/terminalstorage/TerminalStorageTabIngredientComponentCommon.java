package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.part.PartTypeTerminalStorage;

import java.util.List;

/**
 * A common storage terminal ingredient tab.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientComponentCommon<T, M> implements ITerminalStorageTabCommon {

    private final ResourceLocation name;
    private final IngredientComponent<T, M> ingredientComponent;

    public TerminalStorageTabIngredientComponentCommon(ResourceLocation name, IngredientComponent<T, M> ingredientComponent) {
        this.name = name;
        this.ingredientComponent = ingredientComponent;
    }

    @Override
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public List<Slot> loadSlots(Container container, int startIndex, EntityPlayer player,
                                PartTypeTerminalStorage.State partState) {
        List<Slot> slots = Lists.newArrayList();

        SimpleInventory inventory = new SimpleInventory(3, "inv", 1);
        partState.loadNamedInventory(this.getName().toString(), inventory);
        inventory.addDirtyMarkListener(() -> {
            partState.saveNamedInventory(this.getName().toString(), inventory);
        });

        slots.add(new SlotVariable(inventory, 0, 201, 136));
        slots.add(new SlotVariable(inventory, 1, 201, 154));
        slots.add(new SlotVariable(inventory, 2, 201, 172));

        return slots;
    }
}
