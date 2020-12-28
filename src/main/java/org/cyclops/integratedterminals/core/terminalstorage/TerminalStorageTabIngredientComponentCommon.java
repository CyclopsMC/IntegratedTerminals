package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.nbt.NBTClassType;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.evaluate.InventoryVariableEvaluator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;
import org.cyclops.integrateddynamics.core.part.event.PartVariableDrivenVariableContentsUpdatedEvent;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;
import org.cyclops.integratedterminals.part.PartTypeTerminalStorage;

import java.util.List;

/**
 * A common storage terminal ingredient tab.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientComponentCommon<T, M> implements ITerminalStorageTabCommon, IVariableFacade.IValidator {

    private final ContainerTerminalStorage containerTerminalStorage;
    private final ResourceLocation name;
    protected final IngredientComponent<T, M> ingredientComponent;

    private final int errorsValueId;

    private SimpleInventory inventory = null;
    private boolean dirtyInv;
    private final List<InventoryVariableEvaluator<ValueTypeOperator.ValueOperator>> variableEvaluators = Lists.newArrayList();
    private final List<IVariable<ValueTypeOperator.ValueOperator>> variables = Lists.newArrayList();

    private int variableSlotNumberStart;
    private int variableSlotNumberEnd;

    public TerminalStorageTabIngredientComponentCommon(ContainerTerminalStorage containerTerminalStorage,
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
    public List<Slot> loadSlots(Container container, int startIndex, PlayerEntity player,
                                PartTypeTerminalStorage.State partState) {
        List<Slot> slots = Lists.newArrayList();

        variableSlotNumberStart = startIndex;
        inventory = new SimpleInventory(3, 1);
        partState.loadNamedInventory(this.getName().toString(), inventory);
        variableEvaluators.clear();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            int slot = i;
            variableEvaluators.add(new InventoryVariableEvaluator<ValueTypeOperator.ValueOperator>(inventory,
                    slot, ValueTypes.OPERATOR) {
                @Override
                public void onErrorsChanged() {
                    super.onErrorsChanged();
                    setLocalErrors(slot, getErrors());
                }
            });
        }
        variableSlotNumberEnd = startIndex + inventory.getSizeInventory();

        inventory.addDirtyMarkListener(() -> dirtyInv = true);

        slots.add(new SlotVariable(inventory, 0, 201, 136));
        slots.add(new SlotVariable(inventory, 1, 201, 154));
        slots.add(new SlotVariable(inventory, 2, 201, 172));

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
    public void onUpdate(Container container, PlayerEntity player, PartTypeTerminalStorage.State partState) {
        if (this.dirtyInv && !player.world.isRemote) {
            this.dirtyInv = false;

            ContainerTerminalStorage containerTerminalStorage = (ContainerTerminalStorage) container;

            partState.saveNamedInventory(this.getName().toString(), inventory);

            // Update variable facades
            INetwork network = NetworkHelpers.getNetwork(containerTerminalStorage.getPartTarget().getCenter()).orElse(null);

            clearGlobalErrors();
            this.variables.clear();
            if (network == null) {
                addError(new TranslationTextComponent(L10NValues.GENERAL_ERROR_NONETWORK));
            } else {
                for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    InventoryVariableEvaluator<ValueTypeOperator.ValueOperator> evaluator = variableEvaluators.get(i);
                    evaluator.refreshVariable(network, false);
                    IVariable<ValueTypeOperator.ValueOperator> variable = evaluator.getVariable(network);
                    if (variable != null) {
                        // Refresh filter when variable is invalidated
                        variable.addInvalidationListener(() -> inventory.markDirty());
                        this.variables.add(variable);
                    }

                    try {
                        IPartNetwork partNetwork = NetworkHelpers.getPartNetworkChecked(network);
                        MinecraftForge.EVENT_BUS.post(new PartVariableDrivenVariableContentsUpdatedEvent<>(network,
                                partNetwork, containerTerminalStorage.getPartTarget(),
                                containerTerminalStorage.getPartType(), partState, player, variable,
                                variable != null ? variable.getValue() : null));
                    } catch (EvaluationException e) {
                        // Ignore error
                    }
                }
            }

            // Tell the container that our filter may have changed
            TerminalStorageTabIngredientComponentServer tabServer = (TerminalStorageTabIngredientComponentServer)
                    containerTerminalStorage.getTabServer(getName().toString());
            tabServer.updateFilter(this.variables, this);
            tabServer.reApplyFilter();
        }
    }

    @Override
    public void addError(IFormattableTextComponent error) {
        List<ITextComponent> errors = getGlobalErrors();
        errors.add(error);
        CompoundNBT tag = this.containerTerminalStorage.getValue(this.errorsValueId);
        if (tag == null) {
            tag = new CompoundNBT();
        } else {
            tag = tag.copy();
        }
        NBTClassType.writeNbt(List.class, getName().toString() + ":globalErrors", errors, tag);
        this.containerTerminalStorage.setValue(this.errorsValueId, tag);
    }

    public List<ITextComponent> getGlobalErrors() {
        CompoundNBT tag = this.containerTerminalStorage.getValue(this.errorsValueId);
        if (tag == null) {
            return Lists.newArrayList();
        } else {
            return NBTClassType.readNbt(List.class, getName().toString() + ":globalErrors", tag);
        }
    }

    public void clearGlobalErrors() {
        CompoundNBT tag = this.containerTerminalStorage.getValue(this.errorsValueId);
        if (tag == null) {
            tag = new CompoundNBT();
        } else {
            tag = tag.copy();
        }
        NBTClassType.writeNbt(List.class, getName().toString() + ":globalErrors", Lists.newArrayList(), tag);
        this.containerTerminalStorage.setValue(this.errorsValueId, tag);
    }

    public void setLocalErrors(int slot, List<IFormattableTextComponent> errors) {
        CompoundNBT tag = this.containerTerminalStorage.getValue(this.errorsValueId);
        if (tag == null) {
            tag = new CompoundNBT();
        } else {
            tag = tag.copy();
        }
        NBTClassType.writeNbt(List.class, getName().toString() + ":localErrors" + slot, errors, tag);
        this.containerTerminalStorage.setValue(this.errorsValueId, tag);
    }

    public List<ITextComponent> getLocalErrors(int slot) {
        CompoundNBT tag = this.containerTerminalStorage.getValue(this.errorsValueId);
        if (tag == null) {
            return Lists.newArrayList();
        } else {
            return NBTClassType.readNbt(List.class, getName().toString() + ":localErrors" + slot, tag);
        }
    }

    public boolean hasErrors() {
        return !getGlobalErrors().isEmpty();
    }
}
