package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Lists;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.nbt.NBTClassType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.core.evaluate.InventoryVariableEvaluator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

import java.util.List;
import java.util.Optional;

/**
 * A common storage terminal ingredient tab.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientComponentCommon<T, M> implements ITerminalStorageTabCommon, IVariableFacade.IValidator {

    private final ContainerTerminalStorageBase containerTerminalStorage;
    private final ResourceLocation name;
    protected final IngredientComponent<T, M> ingredientComponent;

    private final int errorsValueId;

    private SimpleInventory inventory = null;
    private boolean dirtyInv;
    private final List<InventoryVariableEvaluator<ValueTypeOperator.ValueOperator>> variableEvaluators = Lists.newArrayList();
    private final List<IVariable<ValueTypeOperator.ValueOperator>> variables = Lists.newArrayList();

    private int variableSlotNumberStart;
    private int variableSlotNumberEnd;

    public TerminalStorageTabIngredientComponentCommon(ContainerTerminalStorageBase containerTerminalStorage,
                                                       ResourceLocation name,
                                                       IngredientComponent<T, M> ingredientComponent) {
        this.containerTerminalStorage = containerTerminalStorage;
        this.name = name;
        this.ingredientComponent = ingredientComponent;

        this.errorsValueId = containerTerminalStorage.getNextValueId();
    }

    @Override
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public List<Pair<Slot, ISlotPositionCallback>> loadSlots(AbstractContainerMenu container, int startIndex, Player player,
                                                             Optional<IVariableInventory> variableInventoryOptional,
                                                             ValueDeseralizationContext valueDeseralizationContext) {
        IVariableInventory variableInventory = variableInventoryOptional.get();
        List<Pair<Slot, ITerminalStorageTabCommon.ISlotPositionCallback>> slots = Lists.newArrayList();

        variableSlotNumberStart = startIndex;
        inventory = new SimpleInventory(3, 1);
        variableInventory.loadNamedInventory(this.getName().toString(), inventory, valueDeseralizationContext.holderLookupProvider());
        variableEvaluators.clear();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            int slot = i;
            variableEvaluators.add(new InventoryVariableEvaluator<ValueTypeOperator.ValueOperator>(inventory,
                    slot, valueDeseralizationContext, ValueTypes.OPERATOR) {
                @Override
                public void onErrorsChanged() {
                    super.onErrorsChanged();
                    setLocalErrors(slot, getErrors());
                }
            });
        }
        variableSlotNumberEnd = startIndex + inventory.getContainerSize();

        inventory.addDirtyMarkListener(() -> dirtyInv = true);

        slots.add(Pair.of(
                new SlotVariable(inventory, 0, 0, 0),
                factors -> Pair.of(
                        factors.offsetX() + (factors.gridXSize() / 2) + factors.playerInventoryOffsetX() + 139,
                        factors.offsetY() + factors.gridYSize() + factors.playerInventoryOffsetY() + 63
                )
        ));
        slots.add(Pair.of(
                new SlotVariable(inventory, 1, 0, 0),
                factors -> Pair.of(
                        factors.offsetX() + (factors.gridXSize() / 2) + factors.playerInventoryOffsetX() + 139,
                        factors.offsetY() + factors.gridYSize() + factors.playerInventoryOffsetY() + 81
                )
        ));
        slots.add(Pair.of(
                new SlotVariable(inventory, 2, 0, 0),
                factors -> Pair.of(
                        factors.offsetX() + (factors.gridXSize() / 2) + factors.playerInventoryOffsetX() + 139,
                        factors.offsetY() + factors.gridYSize() + factors.playerInventoryOffsetY() + 99
                )
        ));

        dirtyInv = true;

        return slots;
    }

    public int getVariableSlotNumberStart() {
        return variableSlotNumberStart;
    }

    public int getVariableSlotNumberEnd() {
        return variableSlotNumberEnd;
    }

    @Override
    public void onUpdate(AbstractContainerMenu container, Player player,
                         Optional<IVariableInventory> variableInventory) {
        if (this.dirtyInv && !player.level().isClientSide) {
            this.dirtyInv = false;

            ContainerTerminalStorageBase<?> containerTerminalStorage = (ContainerTerminalStorageBase) container;

            variableInventory.get().saveNamedInventory(this.getName().toString(), inventory, player.registryAccess());

            // Update variable facades
            INetwork network = containerTerminalStorage.getNetwork().get();

            clearGlobalErrors();
            this.variables.clear();
            if (network == null) {
                addError(Component.translatable(L10NValues.GENERAL_ERROR_NONETWORK));
            } else {
                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    InventoryVariableEvaluator<ValueTypeOperator.ValueOperator> evaluator = variableEvaluators.get(i);
                    evaluator.refreshVariable(network, false);
                    IVariable<ValueTypeOperator.ValueOperator> variable = evaluator.getVariable(network);
                    if (variable != null) {
                        // Refresh filter when variable is invalidated
                        variable.addInvalidationListener(() -> inventory.setChanged());
                        this.variables.add(variable);
                    }

                    containerTerminalStorage.onVariableContentsUpdated(network, variable);
                }
            }

            // Tell the container that our filter may have changed
            TerminalStorageTabIngredientComponentServer tabServer = (TerminalStorageTabIngredientComponentServer)
                    containerTerminalStorage.getTabServer(getName().toString());
            tabServer.updateFilter(this.variables, this);
            tabServer.reApplyFilter(null);
        }
    }

        protected HolderLookup.Provider getHolderLookupProvider() {
        return this.containerTerminalStorage.getPlayerIInventory().player.registryAccess();
    }

    @Override
    public void addError(MutableComponent error) {
        List<Component> errors = getGlobalErrors();
        errors.add(error);
        CompoundTag tag = this.containerTerminalStorage.getValue(this.errorsValueId);
        if (tag == null) {
            tag = new CompoundTag();
        } else {
            tag = tag.copy();
        }
        NBTClassType.writeNbt(List.class, getName().toString() + ":globalErrors", errors, tag, getHolderLookupProvider());
        this.containerTerminalStorage.setValue(this.errorsValueId, tag);
    }

    public List<Component> getGlobalErrors() {
        CompoundTag tag = this.containerTerminalStorage.getValue(this.errorsValueId);
        if (tag == null) {
            return Lists.newArrayList();
        } else {
            return NBTClassType.readNbt(List.class, getName().toString() + ":globalErrors", tag, getHolderLookupProvider());
        }
    }

    public void clearGlobalErrors() {
        CompoundTag tag = this.containerTerminalStorage.getValue(this.errorsValueId);
        if (tag == null) {
            tag = new CompoundTag();
        } else {
            tag = tag.copy();
        }
        NBTClassType.writeNbt(List.class, getName().toString() + ":globalErrors", Lists.newArrayList(), tag, getHolderLookupProvider());
        this.containerTerminalStorage.setValue(this.errorsValueId, tag);
    }

    public void setLocalErrors(int slot, List<MutableComponent> errors) {
        CompoundTag tag = this.containerTerminalStorage.getValue(this.errorsValueId);
        if (tag == null) {
            tag = new CompoundTag();
        } else {
            tag = tag.copy();
        }
        NBTClassType.writeNbt(List.class, getName().toString() + ":localErrors" + slot, errors, tag, getHolderLookupProvider());
        this.containerTerminalStorage.setValue(this.errorsValueId, tag);
    }

    public List<Component> getLocalErrors(int slot) {
        CompoundTag tag = this.containerTerminalStorage.getValue(this.errorsValueId);
        if (tag == null) {
            return Lists.newArrayList();
        } else {
            return NBTClassType.readNbt(List.class, getName().toString() + ":localErrors" + slot, tag, getHolderLookupProvider());
        }
    }

    public boolean hasErrors() {
        return !getGlobalErrors().isEmpty();
    }
}
