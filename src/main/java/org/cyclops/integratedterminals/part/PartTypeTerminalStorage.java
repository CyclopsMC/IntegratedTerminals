package org.cyclops.integratedterminals.part;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.network.PacketCodecs;
import org.cyclops.integrateddynamics.api.PartStateException;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetworkElement;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
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
    private static final double FACE_OFFSET = 0.4D;

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
                refreshNetworkElementUpdateable(world, pos, partState);
            }
            return InteractionResult.SUCCESS;
        }

        return super.onPartActivated(partState, pos, world, player, hand, heldItem, hit);
    }

    /**
     * Make the network re-check if the part at the given position needs update ticks.
     *
     * A network only calls {@link INetwork#addNetworkElementUpdateable} when an element is added to it,
     * so a part that only starts needing update ticks later on
     * would otherwise not receive any until its network is reloaded.
     */
    protected void refreshNetworkElementUpdateable(Level world, BlockPos pos, State partState) {
        NetworkHelpers.getNetwork(world, pos, null).ifPresent(network -> {
            for (INetworkElement element : network.getElements()) {
                if (element instanceof IPartNetworkElement<?, ?> partNetworkElement) {
                    try {
                        if (partNetworkElement.getPartState() == partState) {
                            network.addNetworkElementUpdateable(element);
                            return;
                        }
                    } catch (PartStateException e) {
                        // This element's part was removed in the meantime
                    }
                }
            }
        });
    }

    @Override
    public boolean isUpdate(State state) {
        // Ender-upgraded terminals need updates for spawning particles, even if energy consumption is disabled
        return super.isUpdate(state) || (GeneralConfig.terminalStorageEnderParticles && state.isEnderUpgraded());
    }

    @Override
    public void update(INetwork network, IPartNetwork partNetwork, PartTarget target, State state) {
        super.update(network, partNetwork, target, state);

        if (GeneralConfig.terminalStorageEnderParticles && state.isEnderUpgraded()) {
            spawnEnderParticles(target);
        }
    }

    /**
     * Spawn ender particles in front of the terminal.
     *
     * This uses the same particle motion as {@link net.minecraft.world.level.block.EnderChestBlock#animateTick},
     * with the spawn area shifted from the center of the block onto the face that the terminal is placed on.
     *
     * Unlike an ender chest, a terminal is not a block of its own, so its particles can not be spawned
     * from a client-side {@code animateTick}. They are sent from here instead,
     * which is why they are spawned once every {@link #PARTICLE_INTERVAL} ticks rather than on every tick.
     */
    protected void spawnEnderParticles(PartTarget target) {
        Level level = target.getCenter().getPos().getLevel(false);
        if (!(level instanceof ServerLevel serverLevel) || serverLevel.getGameTime() % PARTICLE_INTERVAL != 0) {
            return;
        }

        BlockPos pos = target.getCenter().getPos().getBlockPos();
        Direction side = target.getCenter().getSide();
        RandomSource random = serverLevel.getRandom();

        for (int i = 0; i < 3; i++) {
            int j = random.nextInt(2) * 2 - 1;
            int k = random.nextInt(2) * 2 - 1;
            double x = pos.getX() + 0.5D + 0.25D * j + side.getStepX() * FACE_OFFSET;
            double y = pos.getY() + random.nextFloat() + side.getStepY() * FACE_OFFSET;
            double z = pos.getZ() + 0.5D + 0.25D * k + side.getStepZ() * FACE_OFFSET;
            double motionX = random.nextFloat() * j;
            double motionY = (random.nextFloat() - 0.5D) * 0.125D;
            double motionZ = random.nextFloat() * k;

            // A count of 0 makes the offsets be interpreted as the exact motion of a single particle,
            // just like the client-side particles of an ender chest
            serverLevel.sendParticles(ParticleTypes.PORTAL, x, y, z, 0, motionX, motionY, motionZ, 1.0D);
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
        PacketCodecs.write(packetBuffer, pos);

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
        public void serialize(ValueOutput valueOutput) {
            super.serialize(valueOutput);

            valueOutput.putBoolean("enderUpgraded", this.enderUpgraded);

            // Write namedInventories
            ValueOutput.ValueOutputList namedInventoriesList = valueOutput.childrenList("namedInventories");
            for (Map.Entry<String, NonNullList<ItemStack>> entry : this.namedInventories.entrySet()) {
                ValueOutput listEntry = namedInventoriesList.addChild();
                listEntry.putString("tabName", entry.getKey());
                listEntry.putInt("itemCount", entry.getValue().size());
                ContainerHelper.saveAllItems(listEntry, entry.getValue());
            }

            // Write playerStorageStates
            ValueOutput.ValueOutputList playerStorageStatesList = valueOutput.childrenList("playerStorageStates");
            for (Map.Entry<String, TerminalStorageState> entry : this.playerStorageStates.entrySet()) {
                ValueOutput stateEntry = playerStorageStatesList.addChild();
                stateEntry.putString("player", entry.getKey());
                stateEntry.store("value", ExtraCodecs.NBT, entry.getValue().getTag());
            }
        }

        @Override
        public void deserialize(ValueInput valueInput) {
            super.deserialize(valueInput);

            this.enderUpgraded = valueInput.getBooleanOr("enderUpgraded", false);

            // Read namedInventories
            for (ValueInput listEntry : valueInput.childrenList("namedInventories").orElseThrow()) {
                NonNullList<ItemStack> list = NonNullList.withSize(listEntry.getInt("itemCount").orElseThrow(), ItemStack.EMPTY);
                String tabName = listEntry.getString("tabName").orElseThrow();
                ContainerHelper.loadAllItems(listEntry, list);
                this.namedInventories.put(tabName, list);
            }

            // Read playerStorageStates
            for (ValueInput listEntry : valueInput.childrenList("playerStorageStates").orElseThrow()) {
                String playerName = listEntry.getString("player").orElseThrow();
                TerminalStorageState state = new TerminalStorageState((CompoundTag) listEntry.read("value", ExtraCodecs.NBT).orElseThrow(), this);
                this.playerStorageStates.put(playerName, state);
            }
        }
    }

}
