package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketBuffer;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.RegistryEntries;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.CraftingJobStartException;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientOpenPacket;
import org.cyclops.integratedterminals.part.PartTypeTerminalStorage;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A container for previewing a crafting plan.
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingPlan extends ContainerMultipart<PartTypeTerminalStorage, PartTypeTerminalStorage.State> {

    public static final String BUTTON_START = "start";
    private static final ExecutorService WORKER_POOL = Executors.newFixedThreadPool(GeneralConfig.craftingPlannerThreads);

    private final CraftingOptionGuiData craftingOptionGuiData;
    private final int craftingPlanNotifierId;

    private boolean calculatedCraftingPlan;
    private ITerminalCraftingPlan craftingPlan;

    public ContainerTerminalStorageCraftingPlan(int id, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
        this(id, playerInventory, Optional.empty(), Optional.empty(), PartHelpers.readPart(packetBuffer),
                CraftingOptionGuiData.readFromPacketBuffer(packetBuffer));
    }

    public ContainerTerminalStorageCraftingPlan(int id, PlayerInventory playerInventory, Optional<PartTarget> target, Optional<IPartContainer> partContainer,
                                                PartTypeTerminalStorage partType, CraftingOptionGuiData craftingOptionGuiData) {
        super(RegistryEntries.CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_PLAN, id, playerInventory, new Inventory(), target, partContainer, partType);

        this.craftingOptionGuiData = craftingOptionGuiData;

        this.craftingPlanNotifierId = getNextValueId();

        putButtonAction(BUTTON_START, (buttonId, container) -> startCraftingJob());
    }

    public CraftingOptionGuiData getCraftingOptionGuiData() {
        return craftingOptionGuiData;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        // Calculate crafting plan on server
        if (!player.world.isRemote && !calculatedCraftingPlan) {
            this.calculatedCraftingPlan = true;
            updateCraftingPlan();
        }
    }

    public int getCraftingPlanNotifierId() {
        return craftingPlanNotifierId;
    }

    protected void updateCraftingPlan() {
        HandlerWrappedTerminalCraftingOption craftingOptionWrapper = this.craftingOptionGuiData.getCraftingOption();
        INetwork network = NetworkHelpers.getNetworkChecked(getTarget().get().getCenter());
        if (GeneralConfig.craftingPlannerEnableMultithreading) {
            WORKER_POOL.execute(() -> this.updateCraftingPlanJob(craftingOptionWrapper, network));
        } else {
            this.updateCraftingPlanJob(craftingOptionWrapper, network);
        }
    }

    protected void updateCraftingPlanJob(HandlerWrappedTerminalCraftingOption craftingOptionWrapper, INetwork network) {
        this.setCraftingPlan(craftingOptionWrapper.getHandler().calculateCraftingPlan(network,
                this.craftingOptionGuiData.getChannel(), craftingOptionWrapper.getCraftingOption(), this.craftingOptionGuiData.getAmount()));
    }

    protected void setCraftingPlan(ITerminalCraftingPlan craftingPlan) {
        this.craftingPlan = craftingPlan;
        setValue(this.craftingPlanNotifierId, this.craftingOptionGuiData.getCraftingOption().getHandler().serializeCraftingPlan(this.craftingPlan));
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    private void startCraftingJob() {
        if (!getWorld().isRemote()) {
            // Start the crafting job
            if (craftingPlan != null) {
                NetworkHelpers.getNetwork(PartPos.of(getWorld(), craftingOptionGuiData.getPos(), craftingOptionGuiData.getSide()))
                        .ifPresent(network -> {
                            try {
                                craftingOptionGuiData.getCraftingOption().getHandler()
                                        .startCraftingJob(network, craftingOptionGuiData.getChannel(), craftingPlan, (ServerPlayerEntity) player);

                                // Re-open terminal gui
                                TerminalStorageIngredientOpenPacket.openServer(
                                        getWorld(),
                                        craftingOptionGuiData.getPos(),
                                        craftingOptionGuiData.getSide(),
                                        (ServerPlayerEntity) player,
                                        craftingOptionGuiData.getTabName(),
                                        craftingOptionGuiData.getChannel()
                                );
                            } catch (CraftingJobStartException e) {
                                // If the job could not be started, display the error in the plan
                                craftingPlan.setError(e.getUnlocalizedError());
                                this.setCraftingPlan(craftingPlan);
                            }
                        });
            }
        }
    }

}
