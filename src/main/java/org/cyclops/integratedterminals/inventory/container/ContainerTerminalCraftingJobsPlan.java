package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.part.PartStateEmpty;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.RegistryEntries;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlanFlat;
import org.cyclops.integratedterminals.core.client.gui.CraftingJobGuiData;
import org.cyclops.integratedterminals.part.PartTypeTerminalCraftingJob;

import java.util.List;
import java.util.Optional;

/**
 * A container for visualizing a live crafting plan.
 * @author rubensworks
 */
public class ContainerTerminalCraftingJobsPlan extends ContainerMultipart<PartTypeTerminalCraftingJob, PartStateEmpty<PartTypeTerminalCraftingJob>> {

    private final CraftingJobGuiData craftingJobGuiData;
    private final int craftingPlanNotifierId;
    private final int craftingPlanFlatNotifierId;

    private long lastUpdate;
    private Optional<ITerminalCraftingPlan> craftingPlan;
    private Optional<ITerminalCraftingPlanFlat> craftingPlanFlat;

    public ContainerTerminalCraftingJobsPlan(int id, Inventory playerInventory, RegistryFriendlyByteBuf packetBuffer) {
        this(id, playerInventory, PartHelpers.readPartTarget(packetBuffer), Optional.empty(), PartHelpers.readPart(packetBuffer),
                CraftingJobGuiData.readFromPacketBuffer(packetBuffer));
    }

    public ContainerTerminalCraftingJobsPlan(int id, Inventory playerInventory,
                                             PartTarget target, Optional<IPartContainer> partContainer,
                                             PartTypeTerminalCraftingJob partType,
                                             CraftingJobGuiData craftingJobGuiData) {
        super(RegistryEntries.CONTAINER_PART_TERMINAL_CRAFTING_JOBS_PLAN.get(), id, playerInventory, new SimpleContainer(), Optional.of(target), partContainer, partType);

        this.craftingJobGuiData = craftingJobGuiData;
        this.craftingPlan = Optional.empty();
        this.craftingPlanFlat = Optional.empty();

        this.craftingPlanNotifierId = getNextValueId();
        this.craftingPlanFlatNotifierId = getNextValueId();
    }

    public CraftingJobGuiData getCraftingJobGuiData() {
        return craftingJobGuiData;
    }

    public Optional<ITerminalCraftingPlan> getCraftingPlan() {
        return craftingPlan;
    }

    public Optional<ITerminalCraftingPlanFlat> getCraftingPlanFlat() {
        return craftingPlanFlat;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        // Calculate crafting plan on server
        if (!this.getLevel().isClientSide()
                && this.lastUpdate < System.currentTimeMillis()) {
            this.lastUpdate = System.currentTimeMillis() + GeneralConfig.guiTerminalCraftingJobsUpdateFrequency;
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
        getTarget().ifPresent(target -> {
            INetwork network = NetworkHelpers.getNetworkChecked(target.getCenter());
            this.craftingPlan = Optional.ofNullable(craftingJobGuiData.getHandler().getCraftingJob(network,
                    this.craftingJobGuiData.getChannel(), craftingJobGuiData.getCraftingJob()));
            if (this.craftingPlan.isPresent()) {
                ITerminalCraftingPlan plan = this.craftingPlan.get();
                if (!ContainerTerminalCraftingJobsPlan.isPlanTooLarge(plan)) {
                    setValue(this.craftingPlanNotifierId, this.craftingJobGuiData.getHandler().serializeCraftingPlan(plan));
                }
                setValue(this.craftingPlanFlatNotifierId, this.craftingJobGuiData.getHandler().serializeCraftingPlanFlat(plan.flatten()));
            } else {
                setValue(this.craftingPlanNotifierId, new CompoundTag());
            }
        });
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    @Override
    public void onUpdate(int valueId, CompoundTag value) {
        if (valueId == this.craftingPlanNotifierId) {
            try {
                this.craftingPlan = Optional.of(craftingJobGuiData.getHandler().deserializeCraftingPlan(value));
            } catch (IllegalArgumentException e) {
                this.craftingPlan = Optional.empty();
            }
        } else if (valueId == this.craftingPlanFlatNotifierId) {
            try {
                this.craftingPlanFlat = Optional.of(craftingJobGuiData.getHandler().deserializeCraftingPlanFlat(value));
            } catch (IllegalArgumentException e) {
                this.craftingPlanFlat = Optional.empty();
            }
        }

        super.onUpdate(valueId, value);
    }

    public static boolean isPlanTooLarge(ITerminalCraftingPlan craftingPlan) {
        return getPlanSize(craftingPlan) > GeneralConfig.terminalStorageMaxTreePlanSize;
    }

    public static int getPlanSize(ITerminalCraftingPlan craftingPlan) {
        List<ITerminalCraftingPlan<?>> deps = craftingPlan.getDependencies();
        if (deps.isEmpty()) {
            return 1;
        } else {
            return deps.stream()
                    .mapToInt(ContainerTerminalCraftingJobsPlan::getPlanSize)
                    .sum();
        }
    }

}
