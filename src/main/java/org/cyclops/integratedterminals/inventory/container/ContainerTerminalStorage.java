package org.cyclops.integratedterminals.inventory.container;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.cyclopscore.inventory.container.ExtendedInventoryContainer;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabServer;
import org.cyclops.integratedterminals.part.PartTypeTerminalStorage;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author rubensworks
 */
public class ContainerTerminalStorage extends ExtendedInventoryContainer {

    private final World world;
    private final PartTarget target;
    private final IPartContainer partContainer;
    private final IPartType partType;
    private final PartTypeTerminalStorage.State partState;
    private final Map<String, ITerminalStorageTabClient<?>> tabsClient;
    private final Map<String, ITerminalStorageTabServer> tabsServer;
    private final Map<String, ITerminalStorageTabCommon> tabsCommon;
    private final Map<String, List<Triple<Slot, Integer, Integer>>> tabSlots;

    private int selectedTabIndexValueId;
    private int selectedChannelValueId;
    private boolean serverTabsInitialized;

    private final List<String> channelStrings;
    private String channelAllLabel;

    // Fields for storing the last tab client-side
    private static String lastSelectedTab = null;

    /**
     * Make a new instance.
     * @param target The target.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The part type.
     */
    public ContainerTerminalStorage(final EntityPlayer player, PartTarget target, IPartContainer partContainer, IPartType partType) {
        super(player.inventory, (IGuiContainerProvider) partType);

        this.partState = (PartTypeTerminalStorage.State) partContainer.getPartState(target.getCenter().getSide());

        this.target = target;
        this.partContainer = partContainer;
        this.partType = partType;
        this.tabsClient = Maps.newLinkedHashMap();
        this.tabsServer = Maps.newLinkedHashMap();
        this.tabsCommon = Maps.newLinkedHashMap();
        this.tabSlots = Maps.newHashMap();

        this.selectedTabIndexValueId = getNextValueId();
        this.selectedChannelValueId = getNextValueId();
        this.serverTabsInitialized = false;

        addPlayerInventory(player.inventory, 31, 143);

        this.world = player.world;
        this.channelAllLabel = L10NHelpers.localize("gui.integratedterminals.terminal_storage.channel_all");
        this.channelStrings = Lists.newArrayList(this.channelAllLabel);

        // Add tabs for all ingredients (and itemstack crafting)
        for (IngredientComponent<?, ?> ingredientComponent : IngredientComponent.REGISTRY.getValuesCollection()) {
            INetwork network = NetworkHelpers.getNetwork(target.getCenter());
            boolean addCraftingTab = ingredientComponent == IngredientComponents.ITEMSTACK; // TODO: abstract this as "auxiliary" tabs
            if (this.world.isRemote) {
                TerminalStorageTabIngredientComponentClient tab = new TerminalStorageTabIngredientComponentClient(ingredientComponent);
                this.tabsClient.put(tab.getId(), tab);
                // Hard-coded crafting tab
                if (addCraftingTab) {
                    TerminalStorageTabIngredientComponentClientItemStackCrafting tabCrafting =
                            new TerminalStorageTabIngredientComponentClientItemStackCrafting(ingredientComponent);
                    this.tabsClient.put(tabCrafting.getId(), tabCrafting);
                }
            } else {
                IPositionedAddonsNetworkIngredients<?, ?> ingredientNetwork = NetworkHelpers.getIngredientNetwork(network, ingredientComponent);
                addServerTab(new TerminalStorageTabIngredientComponentServer(ingredientComponent, ingredientNetwork,
                        target.getCenter(), (EntityPlayerMP) player));
                // Hard-coded crafting tab
                if (addCraftingTab) {
                    addServerTab(new TerminalStorageTabIngredientComponentServerItemStackCrafting(
                            (IngredientComponent<ItemStack, Integer>) ingredientComponent,
                            (IPositionedAddonsNetworkIngredients<ItemStack, Integer>) ingredientNetwork,
                            target.getCenter(), (EntityPlayerMP) player));
                }
            }

            // TODO: abstract this
            if (addCraftingTab) {
                int slotStartIndex = this.inventorySlots.size();
                TerminalStorageTabIngredientComponentCommontemStackCrafting tab = new TerminalStorageTabIngredientComponentCommontemStackCrafting(IngredientComponents.ITEMSTACK);
                this.tabsCommon.put(tab.getId(), tab);
                List<Slot> slots = tab.loadSlots(this, slotStartIndex, player, partState);
                this.tabSlots.put(tab.getId(), slots.stream()
                        .map(slot -> Triple.of(slot, slot.xPos, slot.yPos)).collect(Collectors.toList()));
                for (Slot slot : slots) {
                    this.addSlotToContainer(slot);
                }
                disableSlots(tab.getId());
            }
        }

        setSelectedTab(player.world.isRemote && lastSelectedTab != null ? lastSelectedTab
                : getTabsClient().size() > 0 ? Iterables.getFirst(getTabsClient().values(), null).getId() : null);
        setSelectedChannel(IPositionedAddonsNetwork.WILDCARD_CHANNEL);
    }

    protected void addServerTab(TerminalStorageTabIngredientComponentServer tab) {
        this.tabsServer.put(tab.getId(), tab);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (!serverTabsInitialized) {
            serverTabsInitialized = true;
            for (ITerminalStorageTabServer tab : this.tabsServer.values()) {
                tab.init();
            }
        }
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        // Do nothing, we handle this manually using dirty listeners
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (!world.isRemote) {
            for (ITerminalStorageTabServer tab : this.tabsServer.values()) {
                tab.deInit();
            }
        }
    }

    public PartTarget getTarget() {
        return target;
    }

    public PartTypeTerminalStorage.State getPartState() {
        return partState;
    }

    @Override
    protected int getSizeInventory() {
        return inventorySlots.size() - player.inventory.mainInventory.size();
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return PartHelpers.canInteractWith(getTarget(), player, this.partContainer);
    }

    protected void enableSlots(String tabName) {
        List<Triple<Slot, Integer, Integer>> slots = this.tabSlots.get(tabName);
        if (slots != null) {
            for (Triple<Slot, Integer, Integer> slot : slots) {
                slot.getLeft().xPos = slot.getMiddle();
                slot.getLeft().yPos = slot.getRight();
            }
        }
    }

    protected void disableSlots(String tabName) {
        List<Triple<Slot, Integer, Integer>> slots = this.tabSlots.get(tabName);
        if (slots != null) {
            for (Triple<Slot, Integer, Integer> slot : slots) {
                slot.getLeft().xPos = -100;
                slot.getLeft().yPos = -100;
            }
        }
    }

    public void setSelectedTab(@Nullable String selectedTab) {
        disableSlots(getSelectedTab());

        if (player.world.isRemote) {
            lastSelectedTab = selectedTab;
        }
        if (selectedTab != null) {
            ValueNotifierHelpers.setValue(this, selectedTabIndexValueId, selectedTab);
        }

        enableSlots(getSelectedTab());
    }

    @Nullable
    public String getSelectedTab() {
        return ValueNotifierHelpers.getValueString(this, selectedTabIndexValueId);
    }

    public void setSelectedChannel(int selectedChannel) {
        ValueNotifierHelpers.setValue(this, selectedChannelValueId, selectedChannel);
        refreshChannelStrings();
    }

    public int getSelectedChannel() {
        return ValueNotifierHelpers.getValueInt(this, selectedChannelValueId);
    }

    @Nullable
    public ITerminalStorageTabClient getTabClient(String id) {
        return tabsClient.get(id);
    }

    @Nullable
    public ITerminalStorageTabServer getTabServer(String id) {
        return tabsServer.get(id);
    }

    @Nullable
    public ITerminalStorageTabCommon getTabCommon(String id) {
        return tabsCommon.get(id);
    }

    public int getTabsClientCount() {
        return tabsClient.size();
    }

    public Map<String, ITerminalStorageTabClient<?>> getTabsClient() {
        Map<String, ITerminalStorageTabClient<?>> tabs = Maps.newLinkedHashMap();
        for (Map.Entry<String, ITerminalStorageTabClient<?>> entry : tabsClient.entrySet()) {
            if (entry.getValue().isEnabled()) {
                tabs.put(entry.getKey(), entry.getValue());
            }
        }
        return tabs;
    }

    public Map<String, ITerminalStorageTabServer> getTabsServer() {
        return tabsServer;
    }

    public List<String> getChannelStrings() {
        return channelStrings;
    }

    public void refreshChannelStrings() {
        this.channelStrings.clear();
        this.channelStrings.add(channelAllLabel);
        ITerminalStorageTabClient<?> tab = tabsClient.get(getSelectedTab());
        if (tab != null) {
            for (int channel : tab.getChannels()) {
                this.channelStrings.add(String.valueOf(channel));
            }
        }
    }

}
