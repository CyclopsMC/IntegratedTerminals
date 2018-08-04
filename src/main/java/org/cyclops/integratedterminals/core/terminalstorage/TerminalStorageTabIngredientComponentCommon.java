package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Lists;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.evaluate.InventoryVariableEvaluator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;
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

    private final ResourceLocation name;
    private final IngredientComponent<T, M> ingredientComponent;

    private List<L10NHelpers.UnlocalizedString> globalErrors = Lists.newArrayList();
    private final List<InventoryVariableEvaluator<ValueTypeOperator.ValueOperator>> variableEvaluators = Lists.newArrayList();
    private final List<IVariable<ValueTypeOperator.ValueOperator>> variables = Lists.newArrayList();

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

        ContainerTerminalStorage containerTerminalStorage = (ContainerTerminalStorage) container;

        SimpleInventory inventory = new SimpleInventory(3, "inv", 1);
        partState.loadNamedInventory(this.getName().toString(), inventory);
        variableEvaluators.clear();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            variableEvaluators.add(new InventoryVariableEvaluator<>(inventory, i, ValueTypes.OPERATOR));
        }

        inventory.addDirtyMarkListener(() -> {
            if (!player.world.isRemote) {
                partState.saveNamedInventory(this.getName().toString(), inventory);

                // Update variable facades
                INetwork network = NetworkHelpers.getNetwork(containerTerminalStorage.getTarget().getCenter());

                this.globalErrors.clear();
                this.variables.clear();
                if (network == null) {
                    this.globalErrors.add(new L10NHelpers.UnlocalizedString(L10NValues.GENERAL_ERROR_NONETWORK));
                } else {
                    for (int i = 0; i < inventory.getSizeInventory(); i++) {
                        InventoryVariableEvaluator<ValueTypeOperator.ValueOperator> evaluator = variableEvaluators.get(i);
                        evaluator.refreshVariable(network, false);
                        IVariable<ValueTypeOperator.ValueOperator> variable = evaluator.getVariable(network);
                        if (variable != null) {
                            this.variables.add(variable);
                        }
                    }
                }

                // Tell the container that our filter may have changed
                TerminalStorageTabIngredientComponentServer tabServer = (TerminalStorageTabIngredientComponentServer)
                        containerTerminalStorage.getTabServer(getName().toString());
                tabServer.updateFilter(this.variables, this);
                tabServer.reApplyFilter();
            }
        });

        slots.add(new SlotVariable(inventory, 0, 201, 136));
        slots.add(new SlotVariable(inventory, 1, 201, 154));
        slots.add(new SlotVariable(inventory, 2, 201, 172));

        return slots;
    }

    @Override
    public void addError(L10NHelpers.UnlocalizedString error) {
        this.globalErrors.add(error);
    }
}
