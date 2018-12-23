package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.inventory.container.ExtendedInventoryContainer;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;
import org.cyclops.integratedterminals.proxy.guiprovider.GuiProviders;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A container for previewing a crafting plan.
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingPlan extends ExtendedInventoryContainer {

    private static final ExecutorService WORKER_POOL = Executors.newFixedThreadPool(GeneralConfig.craftingPlannerThreads);

    private final World world;
    private final PartTarget target;
    private final IPartContainer partContainer;
    private final IPartType partType;
    private final CraftingOptionGuiData craftingOptionGuiData;
    private final int craftingPlanNotifierId;

    private boolean calculatedCraftingPlan;
    private ITerminalCraftingPlan craftingPlan;

    /**
     * Make a new instance.
     * @param target The target.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The part type.
     */
    public ContainerTerminalStorageCraftingPlan(final EntityPlayer player, PartTarget target,
                                                IPartContainer partContainer, IPartType partType,
                                                CraftingOptionGuiData craftingOptionGuiData) {
        super(player.inventory, GuiProviders.GUI_TERMINAL_STORAGE_CRAFTNG_PLAN);

        this.world = target.getCenter().getPos().getWorld();
        this.target = target;
        this.partContainer = partContainer;
        this.partType = partType;
        this.craftingOptionGuiData = craftingOptionGuiData;

        this.craftingPlanNotifierId = getNextValueId();
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
        if (GeneralConfig.craftingPlannerEnableMultithreading) {
            WORKER_POOL.execute(this::updateCraftingPlanJob);
        } else {
            this.updateCraftingPlanJob();
        }
    }

    protected void updateCraftingPlanJob() {
        HandlerWrappedTerminalCraftingOption craftingOptionWrapper = this.craftingOptionGuiData.getCraftingOption();
        INetwork network = NetworkHelpers.getNetwork(target.getCenter());
        this.craftingPlan = craftingOptionWrapper.getHandler().calculateCraftingPlan(network,
                this.craftingOptionGuiData.getChannel(), craftingOptionWrapper.getCraftingOption(), this.craftingOptionGuiData.getAmount());
        setValue(this.craftingPlanNotifierId, this.craftingOptionGuiData.getCraftingOption().getHandler().serializeCraftingPlan(this.craftingPlan));
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

}
