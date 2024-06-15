package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.SimpleContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
import org.cyclops.integratedterminals.core.client.gui.CraftingJobGuiData;
import org.cyclops.integratedterminals.part.PartTypeTerminalCraftingJob;

import java.util.Optional;

/**
 * A container for visualizing a live crafting plan.
 * @author rubensworks
 */
public class ContainerTerminalCraftingJobsPlan extends ContainerMultipart<PartTypeTerminalCraftingJob, PartStateEmpty<PartTypeTerminalCraftingJob>> {

    private final CraftingJobGuiData craftingJobGuiData;
    private final int craftingPlanNotifierId;

    private long lastUpdate;
    private Optional<ITerminalCraftingPlan> craftingPlan;

    public ContainerTerminalCraftingJobsPlan(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
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

        this.craftingPlanNotifierId = getNextValueId();
    }

    public CraftingJobGuiData getCraftingJobGuiData() {
        return craftingJobGuiData;
    }

    public Optional<ITerminalCraftingPlan> getCraftingPlan() {
        return craftingPlan;
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

    protected void updateCraftingPlan() {
        getTarget().ifPresent(target -> {
            INetwork network = NetworkHelpers.getNetworkChecked(target.getCenter());
            this.craftingPlan = Optional.ofNullable(craftingJobGuiData.getHandler().getCraftingJob(network,
                    this.craftingJobGuiData.getChannel(), craftingJobGuiData.getCraftingJob()));
            setValue(this.craftingPlanNotifierId, this.craftingPlan
                    .map(p -> this.craftingJobGuiData.getHandler().serializeCraftingPlan(p))
                    .orElse(new CompoundTag()));
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
        }

        super.onUpdate(valueId, value);
    }

}
