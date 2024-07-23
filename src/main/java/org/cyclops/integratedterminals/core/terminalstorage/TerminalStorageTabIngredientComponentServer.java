package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientCollapsedCollectionMutable;
import org.cyclops.cyclopscore.ingredient.collection.IngredientArrayList;
import org.cyclops.cyclopscore.ingredient.collection.IngredientCollectionHelpers;
import org.cyclops.cyclopscore.ingredient.collection.diff.IngredientCollectionDiff;
import org.cyclops.cyclopscore.ingredient.collection.diff.IngredientCollectionDiffHelpers;
import org.cyclops.cyclopscore.ingredient.collection.diff.IngredientCollectionDiffManager;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentStorageObservable;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientPositionsIndex;
import org.cyclops.integrateddynamics.api.ingredient.capability.IIngredientComponentValueHandler;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabServer;
import org.cyclops.integratedterminals.api.terminalstorage.TerminalClickType;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingOption;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.TerminalStorageTabIngredientCraftingHandlers;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientChangeEventPacket;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientCraftingOptionsPacket;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientMaxQuantityPacket;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientUpdateActiveStorageIngredientPacket;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A server-side storage terminal ingredient tab.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientComponentServer<T, M> implements ITerminalStorageTabServer,
        IIngredientComponentStorageObservable.IIndexChangeObserver<T, M> {

    private final ResourceLocation name;
    private final INetwork network;
    private final IngredientComponent<T, M> ingredientComponent;
    private final IPositionedAddonsNetworkIngredients<T, M> ingredientNetwork;
    private final ServerPlayer player;
    private final IIngredientComponentValueHandler<?, ?, T, M> valueHandler;
    private final Int2ObjectMap<Collection<HandlerWrappedTerminalCraftingOption<T>>> craftingOptions;

    @Nullable
    private Predicate<T> ingredientsFilter;

    // These collections are needed to perform server-side filtering
    // and sending change events based on them to the client.
    private final Int2ObjectMap<IIngredientCollapsedCollectionMutable<T, M>> unfilteredIngredientsViews;
    private final Int2ObjectMap<IngredientCollectionDiffManager<T, M>> filteredDiffManagers;
    private boolean initialized; // True if the first change event has been sent to the client.

    public TerminalStorageTabIngredientComponentServer(ResourceLocation name, INetwork network,
                                                       IngredientComponent<T, M> ingredientComponent,
                                                       IPositionedAddonsNetworkIngredients<T, M> ingredientNetwork,
                                                       ServerPlayer player) {
        this.name = name;
        this.network = network;
        this.ingredientComponent = ingredientComponent;
        this.ingredientNetwork = ingredientNetwork;
        this.player = player;
        this.valueHandler = ingredientComponent.getCapability(Capabilities.IngredientComponentValueHandler.INGREDIENT)
                .orElseThrow(() -> new IllegalStateException("No value handler was found for " + ingredientComponent.getName()));
        this.craftingOptions = new Int2ObjectOpenHashMap<>();

        this.ingredientsFilter = null;
        this.unfilteredIngredientsViews = new Int2ObjectOpenHashMap<>();
        this.filteredDiffManagers = new Int2ObjectOpenHashMap<>();

        // Schedule an observation on creation, as the channel may not have been indexed yet.
        ingredientNetwork.scheduleObservation();
    }

    @Override
    public ResourceLocation getName() {
        return name;
    }

    @Override
    public void init() {
        // Calculate set of available channels from the storage AND the crafting options.
        Set<Integer> channels = Sets.newHashSet();
        for (int channel : this.ingredientNetwork.getChannels()) {
            channels.add(channel);
        }
        for (ITerminalStorageTabIngredientCraftingHandler handler : TerminalStorageTabIngredientCraftingHandlers.REGISTRY.getHandlers()) {
            for (int channel : handler.getChannels(this)) {
                channels.add(channel);
            }
        }

        // Send _all_ current network contents
        for (int channel : channels) {
            initChannel(channel);
        }

        // Listen to future network changes
        this.ingredientNetwork.addObserver(this);
    }

    protected void initChannel(int channel) {
        // Grab ingredients
        IIngredientPositionsIndex<T, M> channelInstance = this.ingredientNetwork.getChannelIndex(channel);
        onChange(new IIngredientComponentStorageObservable.StorageChangeEvent<>(channel, null,
                IIngredientComponentStorageObservable.Change.ADDITION, false,
                channelInstance, false));

        // Grab crafting options
        // We assume that crafting options don't change that often,
        // so we don't have any observers that listen on recipe index changes.
        // Consequence is: players will have to re-open the terminal when they want to see recipe changes.
        List<HandlerWrappedTerminalCraftingOption<T>> channeledCraftingOptions = Lists.newArrayList();
        for (ITerminalStorageTabIngredientCraftingHandler handler : TerminalStorageTabIngredientCraftingHandlers.REGISTRY.getHandlers()) {
            Collection<ITerminalCraftingOption<T>> options = handler.getCraftingOptions(this, channel);
            for (ITerminalCraftingOption<T> option : options) {
                if (option.getOutputComponents().contains(this.ingredientComponent)) {
                    channeledCraftingOptions.add(new HandlerWrappedTerminalCraftingOption<>(handler, option));
                }
            }
        }
        this.craftingOptions.put(channel, channeledCraftingOptions);
        boolean firstChannel = true;
        if (channeledCraftingOptions.size() > 0) {
            this.sendCraftingOptionsToClient(channel, channeledCraftingOptions, false, firstChannel);
            firstChannel = false;
        }
    }

    @Override
    public void deInit() {
        this.ingredientNetwork.removeObserver(this);
    }

    @Override
    public void updateActive() {
        this.ingredientNetwork.scheduleObservation();
    }

    protected IIngredientCollapsedCollectionMutable<T, M> getUnfilteredIngredientsView(int channel) {
        IIngredientCollapsedCollectionMutable<T, M> ingredientsView = unfilteredIngredientsViews.get(channel);
        if (ingredientsView == null) {
            ingredientsView = IngredientCollectionHelpers.createCollapsedCollection(this.ingredientComponent);
            unfilteredIngredientsViews.put(channel, ingredientsView);
        }
        return ingredientsView;
    }

    protected IngredientCollectionDiffManager<T, M> getFilteredDiffManager(int channel) {
        IngredientCollectionDiffManager<T, M> diffManager = filteredDiffManagers.get(channel);
        if (diffManager == null) {
            diffManager = new IngredientCollectionDiffManager<>(this.ingredientComponent);
            filteredDiffManagers.put(channel, diffManager);
        }
        return diffManager;
    }

    public void updateFilter(List<IVariable<ValueTypeOperator.ValueOperator>> variables,
                             TerminalStorageTabIngredientComponentCommon<?, ?> errorListener) {
        if (variables.isEmpty()) {
            this.ingredientsFilter = null;
            return;
        }

        this.ingredientsFilter = (instance) -> false;
        Predicate<T> newFilter = (instance) -> true;
        try {
            for (IVariable<ValueTypeOperator.ValueOperator> variable : variables) {
                if (variable.getType() == ValueTypes.OPERATOR) {
                    ValueTypeOperator.ValueOperator operator = variable.getValue();
                    IValueType<?> inputValueType = valueHandler.getValueType();
                    newFilter = newFilter.and((instance) -> {
                        if (NetworkHelpers.shouldWork()) {
                            try {
                                IValue inputValue = valueHandler.toValue(instance);
                                IOperator predicate = operator.getRawValue();
                                if (predicate.getInputTypes().length == 1
                                        && ValueHelpers.correspondsTo(predicate.getInputTypes()[0], inputValueType)
                                        && ValueHelpers.correspondsTo(predicate.getOutputType(), ValueTypes.BOOLEAN)) {
                                    IValue result = ValueHelpers.evaluateOperator(predicate, inputValue);
                                    ValueHelpers.validatePredicateOutput(predicate, result);
                                    return ((ValueTypeBoolean.ValueBoolean) result).getRawValue();
                                } else {
                                    Component current = ValueTypeOperator.getSignature(predicate);
                                    Component expected = ValueTypeOperator.getSignature(new IValueType[]{inputValueType}, ValueTypes.BOOLEAN);
                                    throw new EvaluationException(Component.translatable(
                                            L10NValues.ASPECT_ERROR_INVALIDTYPE, expected, current));
                                }
                            } catch (EvaluationException e) {
                                if (!errorListener.hasErrors()) {
                                    errorListener.addError(e.getErrorMessage());
                                    this.ingredientsFilter = (t) -> false; // Reset our filter
                                }
                                return false;
                            }
                        }
                        return false;
                    });
                } else {
                    throw new EvaluationException(Component.translatable(
                            L10NValues.ASPECT_ERROR_INVALIDTYPE, ValueTypes.OPERATOR, variable.getType()));
                }
            }
        } catch (EvaluationException e) {
            errorListener.addError(e.getErrorMessage());
            return; // Don't update our filter, deny-all
        }
        this.ingredientsFilter = newFilter;
    }

    @Nullable
    protected Predicate<T> getIngredientsFilter() {
        return this.ingredientsFilter;
    }

    @Override
    public void onChange(IIngredientComponentStorageObservable.StorageChangeEvent<T, M> event) {
        // We don't receive events for wildcard channel.
        // We also don't have do handle them, as the server doesn't use it, only the client.
        int channel = event.getChannel();

        // First, apply the diff to our unfiltered overview
        IngredientCollectionDiff<T, M> diffIn = event.getDiff();
        IngredientCollectionDiffHelpers.applyDiff(ingredientComponent, diffIn, getUnfilteredIngredientsView(channel));

        // Re-filter our complete unfiltered view
        reApplyFilter(event);
    }

    protected void reApplyFilter(@Nullable IIngredientComponentStorageObservable.StorageChangeEvent<T, M> event) {
        boolean firstChannel = true;
        for (int channel : this.unfilteredIngredientsViews.keySet()) {
            Predicate<T> ingredientsFilter = getIngredientsFilter();
            if (ingredientsFilter != null || event == null) {
                Iterator<T> newFilteredIngredients = getUnfilteredIngredientsView(channel)
                        .stream().filter(ingredientsFilter == null ? (instance) -> true : ingredientsFilter).iterator();

                // Send out the diff between the last filtered view
                IngredientCollectionDiffManager<T, M> filteredDiffManager = getFilteredDiffManager(channel);
                IngredientCollectionDiff<T, M> diffOut = filteredDiffManager.onChange(newFilteredIngredients);
                if (!initialized || diffOut.hasAdditions()) {
                    this.sendToClient(new IIngredientComponentStorageObservable.StorageChangeEvent<>(channel, null,
                            IIngredientComponentStorageObservable.Change.ADDITION, false, diffOut.getAdditions(), false));
                }
                if (diffOut.hasDeletions()) {
                    this.sendToClient(new IIngredientComponentStorageObservable.StorageChangeEvent<>(channel, null,
                            IIngredientComponentStorageObservable.Change.DELETION, diffOut.isCompletelyEmpty(), diffOut.getDeletions(), false));
                }
            } else {
                // If the filter is null (=show all ingredients), forward the diff to the client as-is.
                // This allows us to skip the expensive filteredDiffManager.onChange call.
                if (!initialized || event.getDiff().hasAdditions()) {
                    this.sendToClient(new IIngredientComponentStorageObservable.StorageChangeEvent<>(channel, null,
                            IIngredientComponentStorageObservable.Change.ADDITION, false, event.getDiff().getAdditions(), false));
                }
                if (event.getDiff().hasDeletions()) {
                    this.sendToClient(new IIngredientComponentStorageObservable.StorageChangeEvent<>(channel, null,
                            IIngredientComponentStorageObservable.Change.DELETION, event.getDiff().isCompletelyEmpty(), event.getDiff().getDeletions(), false));
                }

                // Also apply the diff to our diff manager
                IngredientCollectionDiffManager<T, M> filteredDiffManager = getFilteredDiffManager(channel);
                if (event.getDiff().hasAdditions()) {
                    filteredDiffManager.getInstancesCache().addAll(event.getDiff().getAdditions());
                }
                if (event.getDiff().hasDeletions()) {
                    filteredDiffManager.getInstancesCache().removeAll(event.getDiff().getDeletions());
                }
            }

            // Filter crafting options and re-send to client
            Collection<HandlerWrappedTerminalCraftingOption<T>> channeledCraftingOptions = this.craftingOptions.get(channel);
            if (channeledCraftingOptions != null) {
                Collection<HandlerWrappedTerminalCraftingOption<T>> channeledCraftingOptionsFiltered;
                if (ingredientsFilter != null) {
                    channeledCraftingOptionsFiltered = channeledCraftingOptions
                            .stream()
                            .filter(o -> {
                                Iterator<T> it = o.getCraftingOption().getOutputs();
                                while (it.hasNext()) {
                                    if (ingredientsFilter.test(it.next())) {
                                        return true;
                                    }
                                }
                                return false;
                            })
                            .collect(Collectors.toList());
                } else {
                    channeledCraftingOptionsFiltered = channeledCraftingOptions;
                }
                this.sendCraftingOptionsToClient(channel, channeledCraftingOptionsFiltered, true, firstChannel);
            }

            firstChannel = false;
        }

        initialized = true;
    }

    protected void sendToClient(IIngredientComponentStorageObservable.StorageChangeEvent<T, M> event) {
        long maxQuantity = this.ingredientNetwork.getChannel(event.getChannel()).getMaxQuantity();

        // Only allow ingredient collection of a max given size to be sent in a packet
        if (event.getInstances().size() <= GeneralConfig.terminalStoragePacketMaxInstances) {
            IntegratedTerminals._instance.getPacketHandler().sendToPlayer(
                    new TerminalStorageIngredientChangeEventPacket(this.getName().toString(), event, this.ingredientNetwork.hasPositions()), player);
            IntegratedTerminals._instance.getPacketHandler().sendToPlayer(
                    new TerminalStorageIngredientMaxQuantityPacket(this.getName().toString(), event.getInstances().getComponent(), maxQuantity, event.getChannel()), player);
        } else {
            IngredientArrayList<T, M> buffer = new IngredientArrayList<>(event.getInstances().getComponent(),
                    GeneralConfig.terminalStoragePacketMaxInstances);
            for (T instance : event.getInstances()) {
                buffer.add(instance);

                // If our buffer reaches its capacity,
                // flush it, and create a new buffer
                if (buffer.size() == GeneralConfig.terminalStoragePacketMaxInstances) {
                    sendToClient(new IIngredientComponentStorageObservable.StorageChangeEvent<>(
                            event.getChannel(), event.getPos(), event.getChangeType(), event.isCompleteChange(), buffer, false
                    ));
                    buffer = new IngredientArrayList<>(event.getInstances().getComponent(),
                            GeneralConfig.terminalStoragePacketMaxInstances);
                }
            }

            // Our buffer can contain some remaining instances, make sure to flush them as well.
            if (!buffer.isEmpty()) {
                sendToClient(new IIngredientComponentStorageObservable.StorageChangeEvent<>(
                        event.getChannel(), event.getPos(), event.getChangeType(), event.isCompleteChange(), buffer, false
                ));
            }
        }
    }

    private void sendCraftingOptionsToClient(int channel, Collection<HandlerWrappedTerminalCraftingOption<T>> channeledCraftingOptions,
                                             boolean reset, boolean firstChannel) {
        // Only allow collection of a max given size to be sent in a packet
        if (channeledCraftingOptions.size() <= GeneralConfig.terminalStoragePacketMaxRecipes) {
            IntegratedTerminals._instance.getPacketHandler().sendToPlayer(
                    new TerminalStorageIngredientCraftingOptionsPacket(this.getName().toString(), channel, channeledCraftingOptions, reset, firstChannel), player);
        } else {
            List<HandlerWrappedTerminalCraftingOption<T>> buffer = Lists.newArrayListWithExpectedSize(GeneralConfig.terminalStoragePacketMaxRecipes);

            for (HandlerWrappedTerminalCraftingOption<T> instance : channeledCraftingOptions) {
                buffer.add(instance);

                // If our buffer reaches its capacity,
                // flush it, and create a new buffer
                if (buffer.size() == GeneralConfig.terminalStoragePacketMaxRecipes) {
                    sendCraftingOptionsToClient(channel, buffer, reset, firstChannel);
                    reset = false; // Only reset in first packet
                    buffer = Lists.newArrayListWithExpectedSize(GeneralConfig.terminalStoragePacketMaxRecipes);
                }
            }

            // Our buffer can contain some remaining instances, make sure to flush them as well.
            if (!buffer.isEmpty()) {
                sendCraftingOptionsToClient(channel, buffer, reset, firstChannel);
            }
        }
    }

    public INetwork getNetwork() {
        return network;
    }

    public IPositionedAddonsNetworkIngredients<T, M> getIngredientNetwork() {
        return ingredientNetwork;
    }

    @Nullable
    public void handleStorageSlotClick(AbstractContainerMenu container, ServerPlayer player, TerminalClickType clickType,
                                       int channel, T hoveringStorageInstance, int hoveredContainerSlot,
                                       long moveQuantityPlayerSlot, T activeStorageInstance, boolean transferFullSelection) {
        IIngredientComponentTerminalStorageHandler<T, M> viewHandler = ingredientComponent.getCapability(org.cyclops.integratedterminals.Capabilities.IngredientComponentTerminalStorageHandler.INGREDIENT)
                .orElseThrow(() -> new IllegalStateException("Could not find an ingredient terminal storage handler"));
        IIngredientComponentStorage<T, M> storage = ingredientNetwork.getChannel(channel);

        boolean updateActivePlayerStack = false;

        switch (clickType) {
            case STORAGE_QUICK_MOVE:
                viewHandler.insertMaxIntoContainer(storage, container, 0, 4 * 9, hoveringStorageInstance);
                break;
            case STORAGE_QUICK_MOVE_INCREMENTAL:
                viewHandler.insertMaxIntoContainer(storage, container, 0, 4 * 9,
                        ingredientComponent.getMatcher().withQuantity(hoveringStorageInstance, viewHandler.getIncrementalInstanceMovementQuantity()));
                break;
            case STORAGE_PLACE_WORLD:
                viewHandler.throwIntoWorld(storage, activeStorageInstance, player);
                break;
            case STORAGE_PLACE_PLAYER:
                T movedInstance = viewHandler.insertIntoContainer(storage, container, hoveredContainerSlot, activeStorageInstance, player, transferFullSelection);
                updateActivePlayerStack = true;
                IIngredientMatcher<T, M> matcher = this.ingredientComponent.getMatcher();
                T remainingInstance = matcher.withQuantity(movedInstance,
                        matcher.getQuantity(activeStorageInstance) - matcher.getQuantity(movedInstance));
                IntegratedTerminals._instance.getPacketHandler().sendToPlayer(
                        new TerminalStorageIngredientUpdateActiveStorageIngredientPacket(this.getName().toString(),
                                this.ingredientComponent, channel, remainingInstance), player);
                break;
            case PLAYER_PLACE_STORAGE:
                viewHandler.extractActiveStackFromPlayerInventory(storage, container, player.getInventory(), moveQuantityPlayerSlot);
                updateActivePlayerStack = true;
                break;
            case PLAYER_QUICK_MOVE:
                viewHandler.extractMaxFromContainerSlot(storage, container, hoveredContainerSlot, player.getInventory(), -1);
                break;
            case PLAYER_QUICK_MOVE_INCREMENTAL:
                viewHandler.extractMaxFromContainerSlot(storage, container, hoveredContainerSlot, player.getInventory(), viewHandler.getIncrementalInstanceMovementQuantity());
                break;
        }

        // Notify the client that the currently hovering player stack has changed.
        if (updateActivePlayerStack) {
            player.connection.send(new ClientboundContainerSetSlotPacket(-1, 0, 0, container.getCarried()));
        }
    }
}
