package org.cyclops.integratedterminals.part;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartStateEmpty;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.advancement.criterion.TerminalStorageEnderUpgradedTrigger;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.core.part.PartTypeTerminal;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCrafting;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStoragePart;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A part that exposes a gui using which players can access storage indexes in the network.
 * @author rubensworks
 */
public class PartTypeTerminalStorage extends PartTypeTerminal<PartTypeTerminalStorage, PartTypeTerminalStorage.State> {

    private static final int PARTICLE_INTERVAL = 5;

    public PartTypeTerminalStorage(String name) {
        super(name);
    }

    @Override
    public int getConsumptionRate(State state) {
        return GeneralConfig.terminalStorageBaseConsumption;
    }

    @Override
    protected PartTypeTerminalStorage.State constructDefaultState() {
        return new PartTypeTerminalStorage.State();
    }

    @Override
    public InteractionResult onPartActivated(State partState, BlockPos pos, Level world, Player player, InteractionHand hand, ItemStack heldItem, BlockHitResult hit) {
        // Ender-upgrade this terminal when it is right-clicked with an eye of ender
        if (GeneralConfig.terminalStorageTabEnderChestEnabled
                && !partState.isEnderUpgraded() && heldItem.is(Items.ENDER_EYE)) {
            if (!world.isClientSide()) {
                partState.setEnderUpgraded(true);
                if (!player.getAbilities().instabuild) {
                    heldItem.shrink(1);
                }
                world.playSound(null, pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                TerminalStorageEnderUpgradedTrigger.onEnderUpgraded((ServerPlayer) player);
            }
            return InteractionResult.sidedSuccess(world.isClientSide());
        }

        return super.onPartActivated(partState, pos, world, player, hand, heldItem, hit);
    }

    @Override
    public boolean isUpdate(State state) {
        // Ender-upgraded terminals need updates for spawning particles, even if energy consumption is disabled
        return super.isUpdate(state) || state.isEnderUpgraded();
    }

    @Override
    public void update(INetwork network, IPartNetwork partNetwork, PartTarget target, State state) {
        super.update(network, partNetwork, target, state);

        if (state.isEnderUpgraded()) {
            spawnEnderParticles(target);
        }
    }

    protected void spawnEnderParticles(PartTarget target) {
        Level level = target.getCenter().getPos().getLevel(false);
        if (level instanceof ServerLevel serverLevel && serverLevel.getGameTime() % PARTICLE_INTERVAL == 0) {
            BlockPos pos = target.getCenter().getPos().getBlockPos();
            Direction side = target.getCenter().getSide();
            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    pos.getX() + 0.5D + side.getStepX() * 0.4D,
                    pos.getY() + 0.5D + side.getStepY() * 0.4D,
                    pos.getZ() + 0.5D + side.getStepZ() * 0.4D,
                    2, 0.2D, 0.2D, 0.2D, 0.05D);
        }
    }

    @Override
    public Optional<MenuProvider> getContainerProvider(PartPos pos) {
        return Optional.of(new MenuProvider() {

            @Override
            public Component getDisplayName() {
                return Component.translatable(getTranslationKey());
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(pos);
                PartTypeTerminalStorage.State state = (PartTypeTerminalStorage.State) data.getLeft()
                        .getPartState(data.getRight().getCenter().getSide());
                TerminalStorageState terminalStorageState = state.getPlayerStorageState(playerEntity);
                return new ContainerTerminalStoragePart(id, playerInventory,
                        data.getRight(), (PartTypeTerminalStorage) data.getMiddle(),
                        Optional.empty(), terminalStorageState);
            }

            @Override
            public boolean shouldTriggerClientSideContainerClosingOnOpen() {
                return false;
            }
        });
    }

    @Override
    public void writeExtraGuiData(RegistryFriendlyByteBuf packetBuffer, PartPos pos, ServerPlayer player) {
        PacketCodec.write(packetBuffer, pos);

        super.writeExtraGuiData(packetBuffer, pos, player);

        // A false to indicate that there will follow no init data object
        packetBuffer.writeBoolean(false);

        PartTypeTerminalStorage.State state = (PartTypeTerminalStorage.State) PartHelpers
                .getPartContainerChecked(pos)
                .getPartState(pos.getSide());
        TerminalStorageState terminalStorageState = state.getPlayerStorageState(player);
        terminalStorageState.writeToPacketBuffer(packetBuffer);
    }

    @Override
    public void addDrops(PartTarget target, State state, List<ItemStack> itemStacks, boolean dropMainElement, boolean saveState) {
        // Give back the eye of ender that was used to ender-upgrade this terminal
        if (!saveState && state.isEnderUpgraded()) {
            itemStacks.add(new ItemStack(Items.ENDER_EYE));
            state.setEnderUpgraded(false);
        }

        for (Map.Entry<String, NonNullList<ItemStack>> entry : state.getNamedInventories().entrySet()) {
            // TODO: for now hardcoded on crafting tab
            if (entry.getKey().equals(TerminalStorageTabIngredientComponentItemStackCrafting.NAME.toString())) {
                entry.getValue().set(0, ItemStack.EMPTY);
            }
            for (ItemStack itemStack : entry.getValue()) {
                if (!itemStack.isEmpty()) {
                    itemStacks.add(itemStack);
                }
            }
        }
        state.clearNamedInventories();

        super.addDrops(target, state, itemStacks, dropMainElement, saveState);
    }

    public static class State extends PartStateEmpty<PartTypeTerminalStorage>
            implements ITerminalStorageTabCommon.IVariableInventory {

        private final Map<String, NonNullList<ItemStack>> namedInventories;
        private final Map<String, TerminalStorageState> playerStorageStates;
        private boolean enderUpgraded;

        public State() {
            this.namedInventories = Maps.newHashMap();
            this.playerStorageStates = Maps.newHashMap();
            this.enderUpgraded = false;
        }

        public boolean isEnderUpgraded() {
            return this.enderUpgraded;
        }

        public void setEnderUpgraded(boolean enderUpgraded) {
            this.enderUpgraded = enderUpgraded;
            this.onDirty();
            this.sendUpdate();
        }

        @Override
        public int getUpdateInterval() {
            return 1; // For enabling energy consumption
        }

        public void clearNamedInventories() {
            this.namedInventories.clear();
        }

        @Override
        public void setNamedInventory(String name, NonNullList<ItemStack> inventory, HolderLookup.Provider holderLookupProvider) {
            this.namedInventories.put(name, inventory);
            this.onDirty();
        }

        public Map<String, NonNullList<ItemStack>> getNamedInventories() {
            return namedInventories;
        }

        @Override
        @Nullable
        public NonNullList<ItemStack> getNamedInventory(String name, HolderLookup.Provider holderLookupProvider) {
            return this.namedInventories.get(name);
        }

        public TerminalStorageState getPlayerStorageState(Player player) {
            TerminalStorageState state = playerStorageStates.get(player.getUUID().toString());
            if (state == null) {
                state = TerminalStorageState.getPlayerDefault(player, this);
                playerStorageStates.put(player.getUUID().toString(), state);
                this.onDirty();
            }
            return state;
        }

        @Override
        public void writeToNBT(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag) {
            super.writeToNBT(valueDeseralizationContext, tag);

            tag.putBoolean("enderUpgraded", this.enderUpgraded);

            // Write namedInventories
            ListTag namedInventoriesList = new ListTag();
            for (Map.Entry<String, NonNullList<ItemStack>> entry : this.namedInventories.entrySet()) {
                CompoundTag listEntry = new CompoundTag();
                listEntry.putString("tabName", entry.getKey());
                listEntry.putInt("itemCount", entry.getValue().size());
                ContainerHelper.saveAllItems(listEntry, entry.getValue(), valueDeseralizationContext.holderLookupProvider());
                namedInventoriesList.add(listEntry);
            }
            tag.put("namedInventories", namedInventoriesList);

            // Write playerStorageStates
            ListTag playerStorageStatesList = new ListTag();
            for (Map.Entry<String, TerminalStorageState> entry : this.playerStorageStates.entrySet()) {
                CompoundTag stateEntry = new CompoundTag();
                stateEntry.putString("player", entry.getKey());
                stateEntry.put("value", entry.getValue().getTag());
                playerStorageStatesList.add(stateEntry);
            }
            tag.put("playerStorageStates", playerStorageStatesList);
        }

        @Override
        public void readFromNBT(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag) {
            super.readFromNBT(valueDeseralizationContext, tag);

            this.enderUpgraded = tag.getBoolean("enderUpgraded");

            // Read namedInventories
            for (Tag listEntry : tag.getList("namedInventories", Tag.TAG_COMPOUND)) {
                NonNullList<ItemStack> list = NonNullList.withSize(((CompoundTag) listEntry).getInt("itemCount"), ItemStack.EMPTY);
                String tabName = ((CompoundTag) listEntry).getString("tabName");
                ContainerHelper.loadAllItems((CompoundTag) listEntry, list, valueDeseralizationContext.holderLookupProvider());
                this.namedInventories.put(tabName, list);
            }

            // Read playerStorageStates
            for (Tag listEntry : tag.getList("playerStorageStates", Tag.TAG_COMPOUND)) {
                String playerName = ((CompoundTag) listEntry).getString("player");
                TerminalStorageState state = new TerminalStorageState(((CompoundTag) listEntry).getCompound("value"), this);
                this.playerStorageStates.put(playerName, state);
            }
        }
    }

}
