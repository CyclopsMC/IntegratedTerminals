package org.cyclops.integratedterminals.inventory.container;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTab;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabServer;
import org.cyclops.integratedterminals.api.terminalstorage.event.TerminalStorageTabCommonLoadSlotsEvent;
import org.cyclops.integratedterminals.api.terminalstorage.location.ITerminalStorageLocation;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabs;
import org.cyclops.integratedterminals.core.terminalstorage.location.TerminalStorageLocationPart;
import org.cyclops.integratedterminals.network.packet.TerminalStorageChangeGuiState;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientOpenCraftingJobAmountGuiPacket;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientOpenCraftingPlanGuiPacket;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author rubensworks
 */
public abstract class ContainerTerminalStorageBase<L> extends InventoryContainer implements IDirtyMarkListener {

    public static final String BUTTON_SET_DEFAULTS = "button_set_defaults";

    private final World world;
    private final Map<String, ITerminalStorageTabClient<?>> tabsClient;
    private final Map<String, ITerminalStorageTabServer> tabsServer;
    private final Map<String, ITerminalStorageTabCommon> tabsCommon;
    private final Map<String, List<Triple<Slot, Integer, Integer>>> tabSlots;
    private final TerminalStorageState terminalStorageState;
    private final Optional<INetwork> network;
    private final Optional<ITerminalStorageTabCommon.IVariableInventory> variableInventory;

    private int selectedTabIndexValueId;
    private int selectedChannelValueId;
    private boolean serverTabsInitialized;

    private final List<String> channelStrings;
    private String channelAllLabel;

    public ContainerTerminalStorageBase(@Nullable ContainerType<?> type, int id, PlayerInventory playerInventory,
                                        Optional<ContainerTerminalStorageBase.InitTabData> initTabData,
                                        TerminalStorageState terminalStorageState, Optional<INetwork> network,
                                        Optional<ITerminalStorageTabCommon.IVariableInventory> variableInventory) {
        super(type, id, playerInventory, new Inventory());

        this.world = player.getCommandSenderWorld();
        this.tabsClient = Maps.newLinkedHashMap();
        this.tabsServer = Maps.newLinkedHashMap();
        this.tabsCommon = Maps.newLinkedHashMap();
        this.tabSlots = Maps.newHashMap();
        this.terminalStorageState = terminalStorageState;
        this.network = network;
        this.variableInventory = variableInventory;

        this.selectedTabIndexValueId = getNextValueId();
        this.selectedChannelValueId = getNextValueId();
        this.serverTabsInitialized = false;

        addPlayerInventory(player.inventory, 31, 143);

        this.channelAllLabel = "All";
        this.channelStrings = Lists.newArrayList(this.channelAllLabel);

        // Add all tabs from the registry
        for (ITerminalStorageTab tab : TerminalStorageTabs.REGISTRY.getTabs()) {
            String tabId = tab.getName().toString();
            if (this.getWorld().isClientSide()) {
                this.tabsClient.put(tabId, tab.createClientTab(this, player));
            } else {
                this.tabsServer.put(tabId, tab.createServerTab(this, player, network.get()));
            }
            ITerminalStorageTabCommon commonTab = tab.createCommonTab(this, player);
            if (commonTab != null) {
                this.tabsCommon.put(tabId, commonTab);

                int slotStartIndex = this.slots.size();
                List<Slot> slots = commonTab.loadSlots(this, slotStartIndex, player, getVariableInventory());
                TerminalStorageTabCommonLoadSlotsEvent loadSlotsEvent = new TerminalStorageTabCommonLoadSlotsEvent(
                        commonTab, this, slots);
                MinecraftForge.EVENT_BUS.post(loadSlotsEvent);
                slots = loadSlotsEvent.getSlots();
                this.tabSlots.put(tabId, slots.stream()
                        .map(slot -> Triple.of(slot, slot.x, slot.y)).collect(Collectors.toList()));
                for (Slot slot : slots) {
                    if (slot.index == 0) {
                        this.addSlot(slot);
                    }
                }
            }
        }

        // Disable all tab slots
        for (ITerminalStorageTabCommon tabCommon : this.tabsCommon.values()) {
            disableSlots(tabCommon.getName().toString());
        }

        // Load gui state
        if (player.level.isClientSide()) {
            TerminalStorageState state = getGuiState();
            setSelectedTab(state.hasTab() ? state.getTab() : getTabsClient().size() > 0
                    ? Iterables.getFirst(getTabsClient().values(), null).getName().toString() : null);
            setSelectedChannel(IPositionedAddonsNetwork.WILDCARD_CHANNEL);
        } else {
            setSelectedTab(null);
            setSelectedChannel(IPositionedAddonsNetwork.WILDCARD_CHANNEL);
        }

        initTabData.ifPresent(d -> {
            setSelectedTab(d.getTabName());
            setSelectedChannel(d.getChannel());
        });

        // Update player's default state
        putButtonAction(ContainerTerminalStorageBase.BUTTON_SET_DEFAULTS, (s, containerExtended) -> {
            if (!playerInventory.player.level.isClientSide()) {
                TerminalStorageState.setPlayerDefault(playerInventory.player, getGuiState());
            }
        });
    }

    public Optional<ITerminalStorageTabCommon.IVariableInventory> getVariableInventory() {
        return this.variableInventory;
    }

    public Optional<INetwork> getNetwork() {
        return this.network;
    }

    public abstract ITerminalStorageLocation<L> getLocation();
    public abstract L getLocationInstance();

    @Override
    public void onDirty() {

    }

    public World getWorld() {
        return world;
    }

    public TerminalStorageState getGuiState() {
        return this.terminalStorageState;
    }

    public void sendGuiStateToServer() {
        if (player.level.isClientSide()) {
            IntegratedTerminals._instance.getPacketHandler().sendToServer(new TerminalStorageChangeGuiState(getGuiState()));
        }
    }

    public int getNextValueId() {
        return super.getNextValueId();
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        // Init tabs
        if (!serverTabsInitialized) {
            for (ITerminalStorageTabServer tab : this.tabsServer.values()) {
                tab.init();
            }
            serverTabsInitialized = true;
        }

        // Update common tabs
        for (ITerminalStorageTabCommon tab : this.tabsCommon.values()) {
            tab.onUpdate(this, player, getVariableInventory());
        }

        // Update active server tab
        ITerminalStorageTabServer activeServerTab = getTabServer(getSelectedTab());
        if (activeServerTab != null) {
            activeServerTab.updateActive();
        }
    }

    public <T, M, L> void sendOpenCraftingPlanGuiPacketToServer(CraftingOptionGuiData<T, M, L> craftingOptionData) {
        IntegratedTerminals._instance.getPacketHandler().sendToServer(
                new TerminalStorageIngredientOpenCraftingPlanGuiPacket<>(craftingOptionData));
    }

    public <T, M, L> void sendOpenCraftingJobAmountGuiPacketToServer(CraftingOptionGuiData<T, M, L> craftingOptionData) {
        IntegratedTerminals._instance.getPacketHandler().sendToServer(
                new TerminalStorageIngredientOpenCraftingJobAmountGuiPacket<>(craftingOptionData));
    }

    @Override
    public void slotsChanged(IInventory inventoryIn) {
        // Do nothing, we handle this manually using dirty listeners
    }

    @Override
    public void removed(PlayerEntity playerIn) {
        super.removed(playerIn);
        if (!getWorld().isClientSide() && serverTabsInitialized) {
            for (ITerminalStorageTabServer tab : this.tabsServer.values()) {
                tab.deInit();
            }
        }
    }

    @Override
    protected int getSizeInventory() {
        return slots.size() - player.inventory.items.size();
    }

    public List<Triple<Slot, Integer, Integer>> getTabSlots(String tabName) {
        List<Triple<Slot, Integer, Integer>> slots = this.tabSlots.get(tabName);
        if (slots == null) {
            return Collections.emptyList();
        }
        return slots;
    }

    protected void enableSlots(String tabName) {
        List<Triple<Slot, Integer, Integer>> slots = getTabSlots(tabName);
        if (slots != null) {
            for (Triple<Slot, Integer, Integer> slot : slots) {
                setSlotPosX(slot.getLeft(), slot.getMiddle());
                setSlotPosY(slot.getLeft(), slot.getRight());
            }
        }
    }

    protected void disableSlots(String tabName) {
        List<Triple<Slot, Integer, Integer>> slots = getTabSlots(tabName);
        if (slots != null) {
            for (Triple<Slot, Integer, Integer> slot : slots) {
                setSlotPosX(slot.getLeft(), Integer.MIN_VALUE);
                setSlotPosY(slot.getLeft(), Integer.MIN_VALUE);
            }
        }
    }

    public void setSelectedTab(@Nullable String selectedTab) {
        disableSlots(getSelectedTab());

        if (player.level.isClientSide) {
            ITerminalStorageTabClient previousTab = getTabClient(getSelectedTab());
            if (previousTab != null) {
                previousTab.onDeselect(getSelectedChannel());
            }
            getGuiState().setTab(selectedTab);
            ITerminalStorageTabClient newTab = getTabClient(selectedTab);
            if (newTab != null) {
                newTab.onSelect(getSelectedChannel());
            }
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
        return getTabsClient().size();
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

    public Map<String, ITerminalStorageTabCommon> getTabsCommon() {
        Map<String, ITerminalStorageTabCommon> tabs = Maps.newLinkedHashMap();
        for (Map.Entry<String, ITerminalStorageTabCommon> entry : tabsCommon.entrySet()) {
            tabs.put(entry.getKey(), entry.getValue());
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

    public abstract void onVariableContentsUpdated(INetwork network, IVariable<?> variable);

    public static class InitTabData {

        private final String tabName;
        private final int channel;

        public InitTabData(String tabName, int channel) {
            this.tabName = tabName;
            this.channel = channel;
        }

        public String getTabName() {
            return tabName;
        }

        public int getChannel() {
            return channel;
        }

        public void writeToPacketBuffer(PacketBuffer packetBuffer) {
            packetBuffer.writeUtf(tabName);
            packetBuffer.writeInt(channel);
        }

        public static InitTabData readFromPacketBuffer(PacketBuffer packetBuffer) {
            return new InitTabData(packetBuffer.readUtf(32767), packetBuffer.readInt());
        }

    }

}
