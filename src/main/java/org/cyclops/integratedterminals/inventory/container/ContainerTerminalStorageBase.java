package org.cyclops.integratedterminals.inventory.container;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTab;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabServer;
import org.cyclops.integratedterminals.api.terminalstorage.event.TerminalStorageTabCommonLoadSlotsEvent;
import org.cyclops.integratedterminals.api.terminalstorage.location.ITerminalStorageLocation;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabs;
import org.cyclops.integratedterminals.network.packet.TerminalStorageChangeGuiState;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientOpenCraftingJobAmountGuiPacket;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientOpenCraftingPlanGuiPacket;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author rubensworks
 */
public abstract class ContainerTerminalStorageBase<L> extends InventoryContainer implements IDirtyMarkListener {

    public static final String BUTTON_SET_DEFAULTS = "button_set_defaults";

    private final Level world;
    private final Map<String, ITerminalStorageTabClient<?>> tabsClient;
    private final Map<String, ITerminalStorageTabServer> tabsServer;
    private final Map<String, ITerminalStorageTabCommon> tabsCommon;
    private final Map<String, List<Pair<Slot, ITerminalStorageTabCommon.ISlotPositionCallback>>> tabSlots;
    private final TerminalStorageState terminalStorageState;
    private final Optional<INetwork> network;
    private final Optional<ITerminalStorageTabCommon.IVariableInventory> variableInventory;

    private int selectedTabIndexValueId;
    private int selectedChannelValueId;
    private boolean serverTabsInitialized;

    private final List<String> channelStrings;
    private String channelAllLabel;

    @OnlyIn(Dist.CLIENT)
    public ContainerScreenTerminalStorage screen;

    public ContainerTerminalStorageBase(@Nullable MenuType<?> type, int id, Inventory playerInventory,
                                        Optional<ContainerTerminalStorageBase.InitTabData> initTabData,
                                        TerminalStorageState terminalStorageState, Optional<INetwork> network,
                                        Optional<ITerminalStorageTabCommon.IVariableInventory> variableInventory) {
        super(type, id, playerInventory, new SimpleContainer());

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

        addPlayerInventory(player.getInventory(), 31, 143);
        addInventoryAndOffHand(player.getInventory());

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
                List<Pair<Slot, ITerminalStorageTabCommon.ISlotPositionCallback>> slots = commonTab.loadSlots(this, slotStartIndex, player, getVariableInventory(), ValueDeseralizationContext.of(this.getWorld()));
                TerminalStorageTabCommonLoadSlotsEvent loadSlotsEvent = new TerminalStorageTabCommonLoadSlotsEvent(
                        commonTab, this, slots);
                NeoForge.EVENT_BUS.post(loadSlotsEvent);
                slots = loadSlotsEvent.getSlots();
                this.tabSlots.put(tabId, slots);
                for (Pair<Slot, ITerminalStorageTabCommon.ISlotPositionCallback> slot : slots) {
                    if (slot.getLeft().index == 0) {
                        this.addSlot(slot.getLeft());
                    }
                }
            }
        }

        // Disable all tab slots
        for (ITerminalStorageTabCommon tabCommon : this.tabsCommon.values()) {
            disableSlots(tabCommon.getName().toString());
        }

        // Load gui state
        if (player.level().isClientSide()) {
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
            if (!playerInventory.player.level().isClientSide()) {
                TerminalStorageState.setPlayerDefault(playerInventory.player, getGuiState());
            }
        });
    }

    protected void addInventoryAndOffHand(Inventory inventory) {
        EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        ResourceLocation[] TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET};

        for(int k = 0; k < 4; ++k) {
            final EquipmentSlot equipmentslot = SLOT_IDS[k];
            this.addSlot(new Slot(inventory, 39 - k, -7 + (k % 2) * 18, 152 + ((int) Math.floor(k / 2)) * 18) {
                public int getMaxStackSize() {
                    return 1;
                }

                public boolean mayPlace(ItemStack p_39746_) {
                    return p_39746_.canEquip(equipmentslot, player);
                }

                public boolean mayPickup(Player p_39744_) {
                    ItemStack itemstack = this.getItem();
                    return !itemstack.isEmpty() && !p_39744_.isCreative() && EnchantmentHelper.has(itemstack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) ? false : super.mayPickup(p_39744_);
                }

                public com.mojang.datafixers.util.Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return com.mojang.datafixers.util.Pair.of(InventoryMenu.BLOCK_ATLAS, TEXTURE_EMPTY_SLOTS[equipmentslot.getIndex()]);
                }
            });
        }
        this.addSlot(new Slot(inventory, 40, 2, 201) {
            public com.mojang.datafixers.util.Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return com.mojang.datafixers.util.Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
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

    public Level getWorld() {
        return world;
    }

    public TerminalStorageState getGuiState() {
        return this.terminalStorageState;
    }

    public void sendGuiStateToServer() {
        if (player.level().isClientSide()) {
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
    public void slotsChanged(Container inventoryIn) {
        // Do nothing, we handle this manually using dirty listeners
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        if (!getWorld().isClientSide() && serverTabsInitialized) {
            for (ITerminalStorageTabServer tab : this.tabsServer.values()) {
                tab.deInit();
            }
        }
    }

    @Override
    protected int getSizeInventory() {
        return slots.size() - player.getInventory().items.size();
    }

    public List<Pair<Slot, ITerminalStorageTabCommon.ISlotPositionCallback>> getTabSlots(String tabName) {
        List<Pair<Slot, ITerminalStorageTabCommon.ISlotPositionCallback>> slots = this.tabSlots.get(tabName);
        if (slots == null) {
            return Collections.emptyList();
        }
        return slots;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotID) {
        // Handle any (modded) client-side quick move controls
        if(player.level().isClientSide) {
            Optional<ITerminalStorageTabClient<?>> tabOptional = this.screen.getSelectedClientTab();
            if(tabOptional.isPresent() && !tabOptional.get().isQuickMovePrevented(slotID)) {
                tabOptional.get().handleClick(this, this.getSelectedChannel(), -1, 0,
                        false, false, slotID, true);
            }
        }
        // Always return empty stack because the tab's #handleClick already does the quick move
        return ItemStack.EMPTY;
    }

    protected void enableSlots(String tabName) {
        // Do nothing, they will be placed on the correct location client-side upon init
    }

    protected void disableSlots(String tabName) {
        List<Pair<Slot, ITerminalStorageTabCommon.ISlotPositionCallback>> slots = getTabSlots(tabName);
        if (slots != null) {
            for (Pair<Slot, ITerminalStorageTabCommon.ISlotPositionCallback> slot : slots) {
                setSlotPosX(slot.getLeft(), Integer.MIN_VALUE);
                setSlotPosY(slot.getLeft(), Integer.MIN_VALUE);
            }
        }
    }

    public void setSelectedTab(@Nullable String selectedTab) {
        disableSlots(getSelectedTab());

        if (player.level().isClientSide) {
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

        public void writeToPacketBuffer(FriendlyByteBuf packetBuffer) {
            packetBuffer.writeUtf(tabName);
            packetBuffer.writeInt(channel);
        }

        public static InitTabData readFromPacketBuffer(FriendlyByteBuf packetBuffer) {
            return new InitTabData(packetBuffer.readUtf(32767), packetBuffer.readInt());
        }

    }

}
