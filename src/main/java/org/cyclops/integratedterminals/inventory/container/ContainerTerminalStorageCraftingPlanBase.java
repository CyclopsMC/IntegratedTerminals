package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.CraftingJobStartException;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingPlan;

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
    private final int craftingPlanFlatNotifierId;
    private final Level world;

    private boolean calculatedCraftingPlan;
    private ITerminalCraftingPlan craftingPlan;

    public ContainerTerminalStorageCraftingPlanBase(@Nullable MenuType<?> type, int id, Inventory playerInventory,
                                                    CraftingOptionGuiData craftingOptionGuiData) {
        super(type, id, playerInventory, new SimpleContainer());

        this.craftingOptionGuiData = craftingOptionGuiData;
        this.craftingPlanNotifierId = getNextValueId();
        this.craftingPlanFlatNotifierId = getNextValueId();
        this.world = playerInventory.player.level;

        putButtonAction(BUTTON_START, (buttonId, container) -> startCraftingJob());
    }

    public abstract Optional<INetwork> getNetwork();

    public Level getWorld() {
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

    public int getCraftingPlanFlatNotifierId() {
        return craftingPlanFlatNotifierId;
    }

    protected void updateCraftingPlan() {
        HandlerWrappedTerminalCraftingOption craftingOptionWrapper = this.craftingOptionGuiData.getCraftingOption();
        getNetwork().ifPresent(network -> {
            if (GeneralConfig.craftingPlannerEnableMultithreading) {
                WORKER_POOL.execute(() -> {
                    BlockEntityHelpers.UNSAFE_BLOCK_ENTITY_GETTER = true;
                    this.updateCraftingPlanJob(craftingOptionWrapper, network);
                    BlockEntityHelpers.UNSAFE_BLOCK_ENTITY_GETTER = false;
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
        if (!HandlerWrappedTerminalCraftingPlan.isPlanTooLarge(this.craftingPlan)) {
            setValue(this.craftingPlanNotifierId, this.craftingOptionGuiData.getCraftingOption().getHandler().serializeCraftingPlan(this.craftingPlan));
        }
        setValue(this.craftingPlanFlatNotifierId, this.craftingOptionGuiData.getCraftingOption().getHandler().serializeCraftingPlanFlat(this.craftingPlan.flatten()));
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    private void startCraftingJob() {
        if (!getWorld().isClientSide()) {
            // Start the crafting job
            if (craftingPlan != null) {
                getNetwork().ifPresent(network -> {
                    try {
                        craftingOptionGuiData.getCraftingOption().getHandler()
                                .startCraftingJob(network, craftingOptionGuiData.getChannel(), craftingPlan, (ServerPlayer) player);

                        // Re-open terminal gui
                        craftingOptionGuiData.getLocation()
                                .openContainerFromServer(craftingOptionGuiData, getWorld(), (ServerPlayer) player);
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
