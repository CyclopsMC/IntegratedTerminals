package org.cyclops.integratedterminals.inventory.container;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.cyclopscore.inventory.container.ExtendedInventoryContainer;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTab;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabServer;
import org.cyclops.integratedterminals.api.terminalstorage.event.TerminalStorageTabCommonLoadSlotsEvent;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabs;
import org.cyclops.integratedterminals.part.PartTypeTerminalStorage;

import javax.annotation.Nullable;
import java.util.Collections;
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

    private static final TerminalStorageState GLOBAL_PLAYER_STATE = new TerminalStorageState();

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

        // Add all tabs from the registry
        for (ITerminalStorageTab tab : TerminalStorageTabs.REGISTRY.getTabs()) {
            String id = tab.getName().toString();
            if (this.world.isRemote) {
                this.tabsClient.put(id, tab.createClientTab(this, player, target));
            } else {
                this.tabsServer.put(id, tab.createServerTab(this, player, target));
            }
            ITerminalStorageTabCommon commonTab = tab.createCommonTab(this, player, target);
            if (commonTab != null) {
                this.tabsCommon.put(id, commonTab);

                int slotStartIndex = this.inventorySlots.size();
                List<Slot> slots = commonTab.loadSlots(this, slotStartIndex, player, partState);
                TerminalStorageTabCommonLoadSlotsEvent loadSlotsEvent = new TerminalStorageTabCommonLoadSlotsEvent(
                        commonTab, this, slots);
                MinecraftForge.EVENT_BUS.post(loadSlotsEvent);
                slots = loadSlotsEvent.getSlots();
                this.tabSlots.put(id, slots.stream()
                        .map(slot -> Triple.of(slot, slot.xPos, slot.yPos)).collect(Collectors.toList()));
                for (Slot slot : slots) {
                    if (slot.slotNumber == 0) {
                        this.addSlotToContainer(slot);
                    }
                }
            }
        }

        // Disable all tab slots
        for (ITerminalStorageTabCommon tabCommon : this.tabsCommon.values()) {
            disableSlots(tabCommon.getName().toString());
        }

        // Load gui state
        if (player.world.isRemote) {
            TerminalStorageState state = getGuiState();
            setSelectedTab(state.hasTab() ? state.getTab() : getTabsClient().size() > 0
                    ? Iterables.getFirst(getTabsClient().values(), null).getName().toString() : null);
            setSelectedChannel(IPositionedAddonsNetwork.WILDCARD_CHANNEL);
        } else {
            setSelectedTab(null);
            setSelectedChannel(IPositionedAddonsNetwork.WILDCARD_CHANNEL);
        }
    }

    /**
     * Make a new instance.
     * @param target The target.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The part type.
     * @param initTabData The tab and channel to select.
     */
    public ContainerTerminalStorage(EntityPlayer player, PartTarget target, IPartContainer partContainer,
                                    IPartType partType, ContainerTerminalStorage.InitTabData initTabData) {
        this(player, target, partContainer, partType);
        setSelectedTab(initTabData.getTabName());
        setSelectedChannel(initTabData.getChannel());
    }

    public TerminalStorageState getGuiState() {
        return GLOBAL_PLAYER_STATE;
    }

    public int getNextValueId() {
        return super.getNextValueId();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        // Init tabs
        if (!serverTabsInitialized) {
            serverTabsInitialized = true;
            for (ITerminalStorageTabServer tab : this.tabsServer.values()) {
                tab.init();
            }
        }

        // Update common tabs
        for (ITerminalStorageTabCommon tab : this.tabsCommon.values()) {
            tab.onUpdate(this, player, partState);
        }

        // Update active server tab
        ITerminalStorageTabServer activeServerTab = getTabServer(getSelectedTab());
        if (activeServerTab != null) {
            activeServerTab.updateActive();
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

    public PartTypeTerminalStorage getPartType() {
        return (PartTypeTerminalStorage) partType;
    }

    @Override
    protected int getSizeInventory() {
        return inventorySlots.size() - player.inventory.mainInventory.size();
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return PartHelpers.canInteractWith(getTarget(), player, this.partContainer);
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
                slot.getLeft().xPos = slot.getMiddle();
                slot.getLeft().yPos = slot.getRight();
            }
        }
    }

    protected void disableSlots(String tabName) {
        List<Triple<Slot, Integer, Integer>> slots = getTabSlots(tabName);
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
            getGuiState().setTab(selectedTab);
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

    }

}
