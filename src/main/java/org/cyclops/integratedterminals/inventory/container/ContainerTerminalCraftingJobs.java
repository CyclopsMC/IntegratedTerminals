package org.cyclops.integratedterminals.inventory.container;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.cyclopscore.inventory.container.ExtendedInventoryContainer;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.part.PartStateEmpty;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingPlan;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.TerminalStorageTabIngredientCraftingHandlers;
import org.cyclops.integratedterminals.part.PartTypeTerminalCraftingJob;

import java.util.List;

/**
 * Container for the crafting jobs overview gui.
 * @author rubensworks
 */
public class ContainerTerminalCraftingJobs extends ExtendedInventoryContainer {

    private final World world;
    private final PartTarget target;
    private final IPartContainer partContainer;
    private final IPartType partType;
    private final PartStateEmpty<PartTypeTerminalCraftingJob> partState;
    private final INetwork network;
    private final int valueIdCraftingJobs;

    private long lastUpdate;
    private List<HandlerWrappedTerminalCraftingPlan> craftingJobs;

    /**
     * Make a new instance.
     * @param target The target.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The part type.
     */
    public ContainerTerminalCraftingJobs(final EntityPlayer player, PartTarget target, IPartContainer partContainer,
                                         IPartType partType) {
        super(player.inventory, (IGuiContainerProvider) partType);

        this.target = target;
        this.partContainer = partContainer;
        this.partType = partType;
        this.world = player.world;
        this.partState = (PartStateEmpty<PartTypeTerminalCraftingJob>) partContainer.getPartState(target.getCenter().getSide());
        this.network = NetworkHelpers.getNetwork(target.getCenter());

        this.lastUpdate = 0;
        this.craftingJobs = Lists.newArrayList();
        this.valueIdCraftingJobs = getNextValueId();
    }

    public PartTarget getTarget() {
        return target;
    }

    public int getChannel() {
        return this.partState.getChannel();
    }

    public int getValueIdCraftingJobs() {
        return valueIdCraftingJobs;
    }

    public List<HandlerWrappedTerminalCraftingPlan> getCraftingJobs() {
        return craftingJobs;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if (!this.world.isRemote
                && this.lastUpdate < Minecraft.getSystemTime()) {
            this.lastUpdate = Minecraft.getSystemTime() + GeneralConfig.guiTerminalCraftingJobsUpdateFrequency;

            // Load crafting jobs
            int channel = getChannel();
            this.craftingJobs = Lists.newArrayList();
            for (ITerminalStorageTabIngredientCraftingHandler<?, ?> handler : TerminalStorageTabIngredientCraftingHandlers.REGISTRY.getHandlers()) {
                for (ITerminalCraftingPlan craftingJob : handler.getCraftingJobs(network, channel)) {
                    this.craftingJobs.add(new HandlerWrappedTerminalCraftingPlan(handler, craftingJob));
                }
            }

            // Send crafting jobs to client
            NBTTagList tagList = new NBTTagList();
            for (HandlerWrappedTerminalCraftingPlan craftingJob : this.craftingJobs) {
                tagList.appendTag(HandlerWrappedTerminalCraftingPlan.serialize(craftingJob));
            }
            NBTTagCompound tag = new NBTTagCompound();
            tag.setTag("craftingJobs", tagList);
            setValue(this.valueIdCraftingJobs, tag);
        }
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    @Override
    public void onUpdate(int valueId, NBTTagCompound value) {
        super.onUpdate(valueId, value);

        if (valueId == this.valueIdCraftingJobs) {
            NBTTagList tagList = value.getTagList("craftingJobs", Constants.NBT.TAG_COMPOUND);
            this.craftingJobs = Lists.newArrayListWithExpectedSize(tagList.tagCount());
            for (int i = 0; i < tagList.tagCount(); i++) {
                this.craftingJobs.add(HandlerWrappedTerminalCraftingPlan.deserialize(tagList.getCompoundTagAt(i)));
            }
        }
    }
}
