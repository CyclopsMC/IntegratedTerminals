package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.part.event.PartVariableDrivenVariableContentsUpdatedEvent;
import org.cyclops.integratedterminals.RegistryEntries;
import org.cyclops.integratedterminals.api.terminalstorage.location.ITerminalStorageLocation;
import org.cyclops.integratedterminals.core.terminalstorage.location.TerminalStorageLocations;
import org.cyclops.integratedterminals.part.PartTypeTerminalStorage;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @author rubensworks
 */
public class ContainerTerminalStoragePart extends ContainerTerminalStorageBase<PartPos> {

    // Based on ContainerMultipart

    private final PartTarget target;
    private final Optional<IPartContainer> partContainer;
    private final PartTypeTerminalStorage partType;

    public ContainerTerminalStoragePart(int id, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
        this(id, playerInventory, PartHelpers.readPartTarget(packetBuffer), PartHelpers.readPart(packetBuffer),
                packetBuffer.readBoolean() ? Optional.of(InitTabData.readFromPacketBuffer(packetBuffer)) : Optional.empty(),
                TerminalStorageState.readFromPacketBuffer(packetBuffer));
        getGuiState().setDirtyMarkListener(this::sendGuiStateToServer);
    }

    public ContainerTerminalStoragePart(int id, PlayerInventory playerInventory, PartTarget target,
                                        PartTypeTerminalStorage partType, Optional<ContainerTerminalStorageBase.InitTabData> initTabData,
                                        TerminalStorageState terminalStorageState) {
        this(RegistryEntries.CONTAINER_PART_TERMINAL_STORAGE_PART, id, playerInventory,
                target, Optional.of(PartHelpers.getPartContainer(target.getCenter().getPos(), target.getCenter().getSide())
                        .orElseThrow(() -> new IllegalStateException("Could not find part container"))), partType,
                initTabData, terminalStorageState);
    }

    public ContainerTerminalStoragePart(@Nullable ContainerType<?> type, int id, PlayerInventory playerInventory,
                                        PartTarget target, Optional<IPartContainer> partContainer, PartTypeTerminalStorage partType,
                                        Optional<ContainerTerminalStorageBase.InitTabData> initTabData,
                                        TerminalStorageState terminalStorageState) {
        super(type, id, playerInventory, initTabData, terminalStorageState,
                NetworkHelpers.getNetwork(target.getCenter()).map(a -> a),
                partContainer.map(p -> (PartTypeTerminalStorage.State) p.getPartState(target.getCenter().getSide())));
        this.target = target;
        this.partType = partType;
        this.partContainer = partContainer;

        putButtonAction(ContainerMultipart.BUTTON_SETTINGS, (s, containerExtended) -> {
            if(!getWorld().isClientSide()) {
                PartHelpers.openContainerPart((ServerPlayerEntity) player, target.getCenter(), partType);
            }
        });
    }

    public PartTypeTerminalStorage getPartType() {
        return partType;
    }

    public PartTarget getPartTarget() {
        return target;
    }

    public Optional<PartTypeTerminalStorage.State> getPartState() {
        return partContainer.map(p -> (PartTypeTerminalStorage.State) p.getPartState(getPartTarget().getCenter().getSide()));
    }

    public Optional<IPartContainer> getPartContainer() {
        return partContainer;
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return PartHelpers.canInteractWith(getPartTarget(), player, this.partContainer.get());
    }

    @Override
    public ITerminalStorageLocation<PartPos> getLocation() {
        return TerminalStorageLocations.PART;
    }

    @Override
    public PartPos getLocationInstance() {
        return getPartTarget().getCenter();
    }

    @Override
    public void onVariableContentsUpdated(INetwork network, IVariable<?> variable) {
        try {
            IPartNetwork partNetwork = NetworkHelpers.getPartNetworkChecked(network);
            MinecraftForge.EVENT_BUS.post(new PartVariableDrivenVariableContentsUpdatedEvent<>(network,
                    partNetwork, getPartTarget(),
                    getPartType(), getPartState().get(), player, variable,
                    variable != null ? variable.getValue() : null));
        } catch (EvaluationException e) {
            // Ignore error
        }
    }
}
