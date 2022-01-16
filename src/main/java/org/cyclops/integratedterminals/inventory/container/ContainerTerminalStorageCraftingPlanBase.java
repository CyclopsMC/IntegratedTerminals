package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.CraftingJobStartException;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A container for previewing a crafting plan.
 * @author rubensworks
 */
public abstract class ContainerTerminalStorageCraftingPlanBase<L> extends InventoryContainer {

    public static final String BUTTON_START = "start";
    private static final ExecutorService WORKER_POOL = Executors.newFixedThreadPool(GeneralConfig.craftingPlannerThreads);

    private final CraftingOptionGuiData craftingOptionGuiData;
    private final int craftingPlanNotifierId;
    private final World world;

    private boolean calculatedCraftingPlan;
    private ITerminalCraftingPlan craftingPlan;

    public ContainerTerminalStorageCraftingPlanBase(@Nullable ContainerType<?> type, int id, PlayerInventory playerInventory,
                                                    CraftingOptionGuiData craftingOptionGuiData) {
        super(type, id, playerInventory, new Inventory());

        this.craftingOptionGuiData = craftingOptionGuiData;
        this.craftingPlanNotifierId = getNextValueId();
        this.world = playerInventory.player.level;

        putButtonAction(BUTTON_START, (buttonId, container) -> startCraftingJob());
    }

    public abstract Optional<INetwork> getNetwork();

    public World getWorld() {
        return world;
    }

    public CraftingOptionGuiData getCraftingOptionGuiData() {
        return craftingOptionGuiData;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        // Calculate crafting plan on server
        if (!player.level.isClientSide && !calculatedCraftingPlan) {
            this.calculatedCraftingPlan = true;
            updateCraftingPlan();
        }
    }

    public int getCraftingPlanNotifierId() {
        return craftingPlanNotifierId;
    }

    protected void updateCraftingPlan() {
        HandlerWrappedTerminalCraftingOption craftingOptionWrapper = this.craftingOptionGuiData.getCraftingOption();
        getNetwork().ifPresent(network -> {
            if (GeneralConfig.craftingPlannerEnableMultithreading) {
                WORKER_POOL.execute(() -> {
                    TileHelpers.UNSAFE_TILE_ENTITY_GETTER = true;
                    this.updateCraftingPlanJob(craftingOptionWrapper, network);
                    TileHelpers.UNSAFE_TILE_ENTITY_GETTER = false;
                });
            } else {
                this.updateCraftingPlanJob(craftingOptionWrapper, network);
            }
        });
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
    public boolean stillValid(PlayerEntity playerIn) {
        return true;
    }

    private void startCraftingJob() {
        if (!getWorld().isClientSide()) {
            // Start the crafting job
            if (craftingPlan != null) {
                getNetwork().ifPresent(network -> {
                    try {
                        craftingOptionGuiData.getCraftingOption().getHandler()
                                .startCraftingJob(network, craftingOptionGuiData.getChannel(), craftingPlan, (ServerPlayerEntity) player);

                        // Re-open terminal gui
                        craftingOptionGuiData.getLocation()
                                .openContainerFromServer(craftingOptionGuiData, getWorld(), (ServerPlayerEntity) player);
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
