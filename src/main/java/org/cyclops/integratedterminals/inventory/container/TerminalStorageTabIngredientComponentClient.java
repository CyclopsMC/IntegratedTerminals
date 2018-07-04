package org.cyclops.integratedterminals.inventory.container;

import com.google.common.collect.Lists;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientCollapsedCollectionMutable;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientListMutable;
import org.cyclops.cyclopscore.ingredient.collection.IngredientArrayList;
import org.cyclops.cyclopscore.ingredient.collection.IngredientCollectionPrototypeMap;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentStorageObservable;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.capability.ingredient.IngredientComponentTerminalStorageHandlerConfig;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientSlotClickPacket;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A client-side storage terminal ingredient tab.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientComponentClient<T, M>
        implements ITerminalStorageTabClient<TerminalStorageSlotIngredient<T, M>> {

    private final IngredientComponent<T, M> ingredientComponent;
    private final IIngredientComponentTerminalStorageHandler<T, M> ingredientComponentViewHandler;
    private final ItemStack icon;

    private final TIntObjectMap<IIngredientListMutable<T, M>> ingredientsViews;

    private final TIntLongMap maxQuantities;
    private final TIntLongMap totalQuantities;
    private int activeSlotId;
    private int activeSlotQuantity;

    public TerminalStorageTabIngredientComponentClient(IngredientComponent<?, ?> ingredientComponent) {
        this.ingredientComponent = (IngredientComponent<T, M>) ingredientComponent;
        this.ingredientComponentViewHandler = Objects.requireNonNull(this.ingredientComponent.getCapability(IngredientComponentTerminalStorageHandlerConfig.CAPABILITY));
        this.icon = ingredientComponentViewHandler.getIcon();

        this.ingredientsViews = new TIntObjectHashMap<>();

        this.maxQuantities = new TIntLongHashMap();
        this.totalQuantities = new TIntLongHashMap();
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

    protected IIngredientListMutable<T, M> getSafeIngredientsView(int channel) {
        IIngredientListMutable<T, M> ingredientsView = ingredientsViews.get(channel);
        if (ingredientsView == null) {
            ingredientsView = new IngredientArrayList<>(this.ingredientComponent);
            ingredientsViews.put(channel, ingredientsView);
        }
        return ingredientsView;
    }

    @Override
    public List<TerminalStorageSlotIngredient<T, M>> getSlots(int channel, int offset, int limit) {
        IIngredientListMutable<T, M> ingredients = getSafeIngredientsView(channel);
        int size = ingredients.size();
        if (offset >= size) {
            return Lists.newArrayList();
        }
        return ingredients.subList(offset, offset + Math.min(limit, size)).stream()
                .map(instance -> new TerminalStorageSlotIngredient<>(ingredientComponentViewHandler, instance))
                .collect(Collectors.toList());
    }

    @Override
    public int getSlotCount(int channel) {
        return getSafeIngredientsView(channel).size();
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
     */
    public void onChange(int channel, IIngredientComponentStorageObservable.Change changeType, IngredientArrayList<T, M> ingredients) {
        // Apply the change to the wildcard channel as well
        if (channel != IPositionedAddonsNetwork.WILDCARD_CHANNEL) {
            onChange(IPositionedAddonsNetwork.WILDCARD_CHANNEL, changeType, ingredients);
        }

        // Calculate quantity-diff
        long quantity = 0;
        IIngredientMatcher<T, M> matcher = ingredients.getComponent().getMatcher();
        for (T ingredient : ingredients) {
            quantity += matcher.getQuantity(ingredient);
        }

        // Use a prototype-based collection so that ingredients are collapsed
        IIngredientListMutable<T, M> persistedIngredients = getSafeIngredientsView(channel);
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

        long newQuantity = totalQuantities.get(channel) + quantity;
        if (newQuantity != 0) {
            totalQuantities.put(channel, newQuantity);
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
    }

    @Override
    public boolean handleClick(int channel, int hoveringStorageSlot, int mouseButton, boolean hasClickedOutside, int hoveredPlayerSlot) {
        IIngredientMatcher<T, M> matcher = ingredientComponent.getMatcher();
        List<TerminalStorageSlotIngredient<T, M>> slots = hoveringStorageSlot >= 0 ? getSlots(channel, hoveringStorageSlot, 1) : Lists.newArrayList();
        boolean validHoveringStorageSlot = !slots.isEmpty();
        T hoveringStorageInstance = slots.size() > 0 ? slots.get(0).getInstance() : matcher.getEmptyInstance();
        IIngredientComponentTerminalStorageHandler<T, M> viewHandler = ingredientComponent.getCapability(IngredientComponentTerminalStorageHandlerConfig.CAPABILITY);
        boolean shift = (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (mouseButton == 0 || mouseButton == 1) {
            TerminalClickType clickType = null;
            boolean reset = false; // So that a reset occurs after the packet is sent
            if (validHoveringStorageSlot && player.inventory.getItemStack().isEmpty() && activeSlotId < 0) {
                if (hoveringStorageInstance != null && shift) {
                    // Quick move max quantity from storage to player
                    clickType = TerminalClickType.STORAGE_QUICK_MOVE;
                } else {
                    // Pick up
                    this.activeSlotId = hoveringStorageSlot;
                    this.activeSlotQuantity = Math.min((int) ingredientComponent.getMatcher().getQuantity(hoveringStorageInstance),
                            viewHandler.getInitialInstanceMovementQuantity());
                    if (mouseButton == 1) {
                        this.activeSlotQuantity = (int) Math.ceil((double) this.activeSlotQuantity / 2);
                    }
                }
            } else if (hoveredPlayerSlot >= 0 && !player.inventory.getStackInSlot(hoveredPlayerSlot).isEmpty() && shift) {
                // Quick move max quantity from player to storage
                clickType = TerminalClickType.PLAYER_QUICK_MOVE;
            } else if (hoveringStorageSlot >= 0 && !player.inventory.getItemStack().isEmpty()) {
                // Move into storage
                clickType = TerminalClickType.PLAYER_PLACE_STORAGE;
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
                    reset = true;
                } else if (hoveringStorageSlot >= 0 && mouseButton == 1) {
                    // Adjust active quantity
                    this.activeSlotQuantity = Math.max(0, this.activeSlotQuantity - viewHandler.getIncrementalInstanceMovementQuantity());
                } else {
                    // Deselect slot
                    resetActiveSlot();
                }

                if (activeSlotQuantity == 0) {
                    activeSlotId = -1;
                }
            }
            if (clickType != null) {
                T activeInstance = matcher.getEmptyInstance();
                if (activeSlotId >= 0) {
                    activeInstance = matcher.withQuantity(getSlots(channel, activeSlotId, 1).get(0).getInstance(), activeSlotQuantity);
                }
                IntegratedTerminals._instance.getPacketHandler().sendToServer(new TerminalStorageIngredientSlotClickPacket<>(
                        ingredientComponent, clickType, channel, hoveringStorageInstance, hoveredPlayerSlot, activeInstance));
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
}
