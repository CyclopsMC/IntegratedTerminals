package org.cyclops.integratedterminals.inventory.container;

import com.google.common.collect.Lists;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.SimpleContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import java.util.Optional;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.part.PartStateEmpty;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.RegistryEntries;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingPlan;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.TerminalStorageTabIngredientCraftingHandlers;
import org.cyclops.integratedterminals.part.PartTypeTerminalCraftingJob;

import java.util.List;
import java.util.Optional;

/**
 * Container for the crafting jobs overview gui.
 * @author rubensworks
 */
public class ContainerTerminalCraftingJobs extends ContainerMultipart<PartTypeTerminalCraftingJob, PartStateEmpty<PartTypeTerminalCraftingJob>> {

    private final Optional<INetwork> network;
    private final int valueIdCraftingJobs;

    private long lastUpdate;
    private List<HandlerWrappedTerminalCraftingPlan> craftingJobs;

    public ContainerTerminalCraftingJobs(int id, Inventory playerInventory, RegistryFriendlyByteBuf packetBuffer) {
        this(id, playerInventory, PartHelpers.readPartTarget(packetBuffer), Optional.empty(), PartHelpers.readPart(packetBuffer));
    }

    public ContainerTerminalCraftingJobs(int id, Inventory playerInventory,
                                         PartTarget target, Optional<IPartContainer> partContainer,
                                         PartTypeTerminalCraftingJob partType) {
        super(RegistryEntries.CONTAINER_PART_TERMINAL_CRAFTING_JOBS.get(), id, playerInventory, new SimpleContainer(), Optional.of(target), partContainer, partType);

        this.network = getTarget()
                .map(t -> NetworkHelpers.getNetwork(t.getCenter()))
                .orElse(Optional.empty());

        this.lastUpdate = 0;
        this.craftingJobs = Lists.newArrayList();
        this.valueIdCraftingJobs = getNextValueId();
    }

    public Optional<INetwork> getNetwork() {
        return network;
    }

    public int getChannel() {
        return IPositionedAddonsNetwork.WILDCARD_CHANNEL;
    }

    public int getValueIdCraftingJobs() {
        return valueIdCraftingJobs;
    }

    public List<HandlerWrappedTerminalCraftingPlan> getCraftingJobs() {
        return craftingJobs;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (!this.getLevel().isClientSide()
                && this.lastUpdate < System.currentTimeMillis()) {
            getNetwork().ifPresent(network -> {
                this.lastUpdate = System.currentTimeMillis() + GeneralConfig.guiTerminalCraftingJobsUpdateFrequency;

                // Load crafting jobs
                int channel = getChannel();
                this.craftingJobs = Lists.newArrayList();
                for (ITerminalStorageTabIngredientCraftingHandler<?, ?> handler : TerminalStorageTabIngredientCraftingHandlers.REGISTRY.getHandlers()) {
                    for (ITerminalCraftingPlan craftingJob : handler.getCraftingJobs(network, channel)) {
                        this.craftingJobs.add(new HandlerWrappedTerminalCraftingPlan(handler, craftingJob));
                    }
                }

                // Send crafting jobs to client
                ListTag tagList = new ListTag();
                for (HandlerWrappedTerminalCraftingPlan craftingJob : this.craftingJobs) {
                    tagList.add(HandlerWrappedTerminalCraftingPlan.serialize(craftingJob));
                }
                CompoundTag tag = new CompoundTag();
                tag.put("craftingJobs", tagList);
                setValue(this.valueIdCraftingJobs, tag);
            });
        }
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
        super.onUpdate(valueId, value);

        if (valueId == this.valueIdCraftingJobs) {
            ListTag tagList = value.getList("craftingJobs", Tag.TAG_COMPOUND);
            this.craftingJobs = Lists.newArrayListWithExpectedSize(tagList.size());
            for (int i = 0; i < tagList.size(); i++) {
                this.craftingJobs.add(HandlerWrappedTerminalCraftingPlan.deserialize(tagList.getCompound(i)));
            }
        }
    }

}
