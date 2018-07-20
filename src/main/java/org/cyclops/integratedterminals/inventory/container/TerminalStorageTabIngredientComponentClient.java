package org.cyclops.integratedterminals.inventory.container;

import com.google.common.collect.Lists;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientCollapsedCollectionMutable;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientListMutable;
import org.cyclops.cyclopscore.ingredient.collection.IngredientArrayList;
import org.cyclops.cyclopscore.ingredient.collection.IngredientCollectionPrototypeMap;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentStorageObservable;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.api.ingredient.IIngredientInstanceSorter;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalButton;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.capability.ingredient.IngredientComponentTerminalStorageHandlerConfig;
import org.cyclops.integratedterminals.inventory.container.query.IIngredientQuery;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientSlotClickPacket;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A client-side storage terminal ingredient tab.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientComponentClient<T, M>
        implements ITerminalStorageTabClient<TerminalStorageSlotIngredient<T, M>> {

    static {
        MinecraftForge.EVENT_BUS.register(TerminalStorageTabIngredientComponentClient.class);
    }

    private final IngredientComponent<T, M> ingredientComponent;
    private final IIngredientComponentTerminalStorageHandler<T, M> ingredientComponentViewHandler;
    private final ItemStack icon;
    private final List<ITerminalButton<?, ?>> buttons;

    private final TIntObjectMap<IIngredientListMutable<T, M>> ingredientsViews;
    private final TIntObjectMap<IIngredientListMutable<T, M>> filteredIngredientsViews;
    private final TIntObjectMap<String> filters;

    private final TIntLongMap maxQuantities;
    private final TIntLongMap totalQuantities;
    private boolean enabled;
    private int activeSlotId;
    private int activeSlotQuantity;
    private int activeChannel;

    @SubscribeEvent
    public static void onToolTip(ItemTooltipEvent event) {
        // If this tab is active, render the quantity in all player inventory item tooltips.
        if (event.getEntityPlayer().openContainer instanceof ContainerTerminalStorage) {
            ContainerTerminalStorage container = (ContainerTerminalStorage) event.getEntityPlayer().openContainer;
            ITerminalStorageTabClient<?> tab = container.getTabsClient().get(container.getSelectedTab());
            if (tab instanceof TerminalStorageTabIngredientComponentClient) {
                IIngredientComponentTerminalStorageHandler handler = ((TerminalStorageTabIngredientComponentClient) tab).ingredientComponentViewHandler;
                Object instance = handler.getInstance(event.getItemStack());
                if (!(instance instanceof ItemStack)) {
                    handler.addQuantityTooltip(event.getToolTip(), instance);
                }
            }
        }
    }

    public TerminalStorageTabIngredientComponentClient(IngredientComponent<?, ?> ingredientComponent) {
        this.ingredientComponent = (IngredientComponent<T, M>) ingredientComponent;
        this.ingredientComponentViewHandler = Objects.requireNonNull(this.ingredientComponent.getCapability(IngredientComponentTerminalStorageHandlerConfig.CAPABILITY));
        this.icon = ingredientComponentViewHandler.getIcon();
        this.buttons = Lists.newArrayList();

        // Add all sorting buttons
        for (IIngredientInstanceSorter<T> instanceSorter : ingredientComponentViewHandler.getInstanceSorters()) {
            this.buttons.add(new TerminalButtonSort<>(instanceSorter));
        }

        this.ingredientsViews = new TIntObjectHashMap<>();
        this.filteredIngredientsViews = new TIntObjectHashMap<>();
        this.filters = new TIntObjectHashMap<>();

        this.maxQuantities = new TIntLongHashMap();
        this.totalQuantities = new TIntLongHashMap();
        this.enabled = false;
        resetActiveSlot();
    }

    @Override
    public String getId() {
        return ingredientComponent.getName().toString();
    }

    @Override
    public ItemStack getIcon() {
        return this.icon;
    }

    @Override
    public List<String> getTooltip() {
        return Lists.newArrayList(L10NHelpers.localize("gui.integratedterminals.terminal_storage.storage_name",
                L10NHelpers.localize(this.ingredientComponent.getUnlocalizedName())));
    }

    @Override
    public String getInstanceFilter(int channel) {
        String filter = this.filters.get(channel);
        return filter == null ? "" : filter;
    }

    public void resetFilteredIngredientsViews(int channel) {
        filteredIngredientsViews.remove(channel);
    }

    @Override
    public void setInstanceFilter(int channel, String filter) {
        resetFilteredIngredientsViews(channel);
        this.filters.put(channel, filter.toLowerCase(Locale.ENGLISH));
    }

    protected IIngredientListMutable<T, M> getUnfilteredIngredientsView(int channel) {
        IIngredientListMutable<T, M> ingredientsView = ingredientsViews.get(channel);
        if (ingredientsView == null) {
            ingredientsView = new IngredientArrayList<>(this.ingredientComponent);
            ingredientsViews.put(channel, ingredientsView);
        }
        return ingredientsView;
    }

    protected IIngredientListMutable<T, M> getFilteredIngredientsView(int channel) {
        IIngredientListMutable<T, M> ingredientsView = filteredIngredientsViews.get(channel);
        if (ingredientsView == null) {
            ingredientsView = getUnfilteredIngredientsView(channel);

            // Filter
            ingredientsView = new IngredientArrayList<>(ingredientsView.getComponent(), ingredientsView.stream()
                    .filter(IIngredientQuery.parse(ingredientComponent, getInstanceFilter(channel)))
                    .collect(Collectors.toList()));

            // Sort
            Comparator<T> sorter = getInstanceSorter();
            if (sorter != null) {
                ingredientsView.sort(sorter);
            }

            filteredIngredientsViews.put(channel, ingredientsView);
        }
        return ingredientsView;
    }

    @Override
    public List<TerminalStorageSlotIngredient<T, M>> getSlots(int channel, int offset, int limit) {
        IIngredientListMutable<T, M> ingredients = getFilteredIngredientsView(channel);
        int size = ingredients.size();
        if (offset >= size) {
            return Lists.newArrayList();
        }
        return ingredients.subList(offset, offset + Math.min(limit, size)).stream()
                .map(instance -> new TerminalStorageSlotIngredient<>(ingredientComponentViewHandler, instance))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    protected Optional<T> getSlotInstance(int channel, int index) {
        if (index >= 0) {
            List<TerminalStorageSlotIngredient<T, M>> lastSlots = getSlots(channel, index, 1);
            if (!lastSlots.isEmpty()) {
                return Optional.of(lastSlots.get(0).getInstance());
            }
        }
        return Optional.empty();
    }

    @Override
    public int getSlotCount(int channel) {
        return getFilteredIngredientsView(channel).size();
    }

    @Override
    public String getStatus(int channel) {
        return String.format("%,d / %,d", getTotalQuantity(channel), getMaxQuantity(channel));
    }

    /**
     * Receiver an ingredients change event.
     * @param channel A channel id.
     * @param changeType A change type.
     * @param ingredients A list of changed ingredients.
     * @param enabled If this tab is enabled.
     */
    public synchronized void onChange(int channel, IIngredientComponentStorageObservable.Change changeType,
                                      IngredientArrayList<T, M> ingredients, boolean enabled) {
        this.enabled = enabled;

        // Remember the selected instance, as this change event might change its position or quantity.
        // This is handled at the end of this method.
        Optional<T> lastInstance = getSlotInstance(channel, this.activeSlotId);

        // Apply the change to the wildcard channel as well
        if (channel != IPositionedAddonsNetwork.WILDCARD_CHANNEL) {
            onChange(IPositionedAddonsNetwork.WILDCARD_CHANNEL, changeType, ingredients, enabled);
        }

        // Calculate quantity-diff
        long quantity = 0;
        IIngredientMatcher<T, M> matcher = ingredients.getComponent().getMatcher();
        for (T ingredient : ingredients) {
            quantity += matcher.getQuantity(ingredient);
        }

        // Use a prototype-based collection so that ingredients are collapsed
        IIngredientListMutable<T, M> persistedIngredients = getUnfilteredIngredientsView(channel);
        IIngredientCollapsedCollectionMutable<T, M> prototypedIngredients = new IngredientCollectionPrototypeMap<>(this.ingredientComponent);
        prototypedIngredients.addAll(persistedIngredients);

        // Apply changes
        if (changeType == IIngredientComponentStorageObservable.Change.ADDITION) {
            prototypedIngredients.addAll(ingredients);
        } else {
            prototypedIngredients.removeAll(ingredients);
            quantity = -quantity;
        }

        // Persist changes
        persistedIngredients.clear();
        persistedIngredients.addAll(prototypedIngredients);
        resetFilteredIngredientsViews(channel);

        long newQuantity = totalQuantities.get(channel) + quantity;
        if (newQuantity != 0) {
            totalQuantities.put(channel, newQuantity);
        }

        // Update the active instance by searching for its new position in the slots
        // If this becomes a performance bottleneck, we could search _around_ the previous position.
        if (lastInstance.isPresent() && this.activeChannel == channel) {
            int newActiveSlot = 0;
            M matchCondition = matcher.withoutCondition(matcher.getExactMatchCondition(),
                    ingredients.getComponent().getPrimaryQuantifier().getMatchCondition());
            List<TerminalStorageSlotIngredient<T, M>> slots = getSlots(channel, 0, Integer.MAX_VALUE);
            for (TerminalStorageSlotIngredient<T, M> slot : slots) {
                T ingredient = slot.getInstance();
                if (matcher.matches(ingredient, lastInstance.get(), matchCondition)) {
                    this.activeSlotId = newActiveSlot;
                    this.activeSlotQuantity = Math.min(this.activeSlotQuantity, Helpers.castSafe(matcher.getQuantity(ingredient)));
                    break;
                }
                newActiveSlot++;
            }

            // None was found, deselect slot
            if (newActiveSlot == slots.size()) {
                this.activeSlotId = -1;
                this.activeSlotQuantity = 0;
            }
        }
    }

    /**
     * Get the total maximum allowed quantity in the given channel.
     * @param channel A channel id.
     * @return The max quantity.
     */
    public long getMaxQuantity(int channel) {
        // Take the sum of all channels when requesting wildcard channel
        if (channel == IPositionedAddonsNetwork.WILDCARD_CHANNEL) {
            return Arrays.stream(getChannels()).mapToLong(this::getMaxQuantity).sum();
        }
        return maxQuantities.get(channel);
    }

    /**
     * Set the max quantity in the given channel.
     * @param channel A channel id.
     * @param maxQuantity The new max quantity.
     */
    public void setMaxQuantity(int channel, long maxQuantity) {
        this.maxQuantities.put(channel, maxQuantity);
    }

    /**
     * Get the total instance quantities in the given channel.
     * @param channel A channel id.
     * @return The total quantity.
     */
    public long getTotalQuantity(int channel) {
        return totalQuantities.get(channel);
    }

    @Override
    public int[] getChannels() {
        int[] channels = maxQuantities.keys();
        Arrays.sort(channels);
        return channels;
    }

    @Override
    public void resetActiveSlot() {
        this.activeSlotId = -1;
        this.activeSlotQuantity = 0;
        this.activeChannel = -2;
    }

    @Override
    public boolean handleClick(int channel, int hoveringStorageSlot, int mouseButton, boolean hasClickedOutside, int hoveredPlayerSlot) {
        this.activeChannel = channel;

        IIngredientMatcher<T, M> matcher = ingredientComponent.getMatcher();
        Optional<T> hoveringStorageInstance = getSlotInstance(channel, hoveringStorageSlot);
        boolean validHoveringStorageSlot = hoveringStorageInstance.isPresent();
        IIngredientComponentTerminalStorageHandler<T, M> viewHandler = ingredientComponent.getCapability(IngredientComponentTerminalStorageHandlerConfig.CAPABILITY);
        boolean shift = (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (mouseButton == 0 || mouseButton == 1 || mouseButton == 2) {
            TerminalClickType clickType = null;
            long moveQuantity = this.activeSlotQuantity;
            long movePlayerQuantity = 0;
            boolean reset = false; // So that a reset occurs after the packet is sent
            if (validHoveringStorageSlot && player.inventory.getItemStack().isEmpty() && activeSlotId < 0) {
                if (shift) {
                    // Quick move max quantity from storage to player
                    clickType = TerminalClickType.STORAGE_QUICK_MOVE;
                } else {
                    // Pick up
                    this.activeSlotId = hoveringStorageSlot;
                    this.activeSlotQuantity = Math.min((int) ingredientComponent.getMatcher()
                                    .getQuantity(hoveringStorageInstance.orElse(matcher.getEmptyInstance())),
                            viewHandler.getInitialInstanceMovementQuantity());
                    if (mouseButton == 1) {
                        this.activeSlotQuantity = (int) Math.ceil((double) this.activeSlotQuantity / 2);
                    } else if (mouseButton == 2) {
                        this.activeSlotQuantity = 1;
                    }
                }
            } else if (hoveredPlayerSlot >= 0 && !player.inventory.getStackInSlot(hoveredPlayerSlot).isEmpty() && shift) {
                // Quick move max quantity from player to storage
                clickType = TerminalClickType.PLAYER_QUICK_MOVE;
            } else if (hoveringStorageSlot >= 0 && !player.inventory.getItemStack().isEmpty()) {
                // Move into storage
                clickType = TerminalClickType.PLAYER_PLACE_STORAGE;
                if (mouseButton == 0) {
                    movePlayerQuantity = viewHandler.getActivePlayerStackQuantity(player.inventory);
                } else if (mouseButton == 1) {
                    movePlayerQuantity = viewHandler.getIncrementalInstanceMovementQuantity();
                } else {
                    movePlayerQuantity = (int) Math.ceil((double) viewHandler.getActivePlayerStackQuantity(player.inventory) / 2);
                }
                viewHandler.drainActivePlayerStackQuantity(player.inventory, movePlayerQuantity);
                resetActiveSlot();
            } else if (activeSlotId >= 0) {
                // We have a storage slot selected
                if (hasClickedOutside) {
                    // Throw
                    clickType = TerminalClickType.STORAGE_PLACE_WORLD;
                    reset = true;
                } else if (hoveredPlayerSlot >= 0) {
                    // Insert into player inventory
                    clickType = TerminalClickType.STORAGE_PLACE_PLAYER;
                    if (mouseButton == 0) {
                        reset = true;
                        moveQuantity = this.activeSlotQuantity;
                    } else if (mouseButton == 1) {
                        moveQuantity = viewHandler.getIncrementalInstanceMovementQuantity();
                    } else {
                        moveQuantity = (int) Math.ceil((double) this.activeSlotQuantity / 2);
                    }
                    this.activeSlotQuantity -= moveQuantity;
                } else if (hoveringStorageSlot >= 0 && mouseButton == 1) {
                    // Adjust active quantity
                    this.activeSlotQuantity = Math.max(0, this.activeSlotQuantity - viewHandler.getIncrementalInstanceMovementQuantity());
                    if (this.activeSlotQuantity == 0) {
                        activeSlotId = -1;
                    }
                } else {
                    // Deselect slot
                    resetActiveSlot();
                }

                if (moveQuantity == 0) {
                    activeSlotId = -1;
                }
            }
            if (clickType != null) {
                T activeInstance = matcher.getEmptyInstance();
                if (activeSlotId >= 0) {
                    activeInstance = matcher.withQuantity(getSlots(channel, activeSlotId, 1).get(0).getInstance(), moveQuantity);
                }
                IntegratedTerminals._instance.getPacketHandler().sendToServer(new TerminalStorageIngredientSlotClickPacket<>(
                        ingredientComponent, clickType, channel,
                        hoveringStorageInstance.orElse(matcher.getEmptyInstance()), hoveredPlayerSlot, movePlayerQuantity, activeInstance));
                if (reset) {
                    resetActiveSlot();
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public int getActiveSlotId() {
        return this.activeSlotId;
    }

    @Override
    public int getActiveSlotQuantity() {
        return this.activeSlotQuantity;
    }

    @Override
    public List<ITerminalButton<?, ?>> getButtons() {
        return this.buttons;
    }

    @Nullable
    public Comparator<T> getInstanceSorter() {
        Comparator<T> sorter = null;

        // Chain all effective sorters from buttons of type TerminalButtonSort
        for (ITerminalButton<?, ?> button : this.buttons) {
            if (button instanceof TerminalButtonSort) {
                Comparator<T> partSorter = ((TerminalButtonSort<T>) button).getEffectiveSorter();
                if (partSorter != null) {
                    if (sorter == null) {
                        sorter = partSorter;
                    } else {
                        sorter = sorter.thenComparing(partSorter);
                    }
                }
            }
        }

        if (sorter != null) {
            // Make comparators 0-equals-safe
            sorter = sorter.thenComparing(ingredientComponent.getMatcher());
        }

        return sorter;
    }
}
