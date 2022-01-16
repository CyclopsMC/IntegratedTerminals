package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalStorageTabIngredientCraftingHandler;
import org.cyclops.integratedterminals.core.client.gui.CraftingJobGuiData;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingPlan;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.TerminalStorageTabIngredientCraftingHandlers;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalCraftingJobsPlan;
import org.cyclops.integratedterminals.part.PartTypeTerminalCraftingJob;
import org.cyclops.integratedterminals.part.PartTypes;

import java.util.Optional;

/**
 * Packet for opening a live crafting plan gui.
 * @author rubensworks
 *
 */
public class OpenCraftingJobsPlanGuiPacket extends PacketCodec {

    @CodecField
    private BlockPos pos;
    @CodecField
    private Direction side;
    @CodecField
    private int channel;
    @CodecField
    private String craftingPlanHandler;
    @CodecField
    private CompoundNBT craftingJobId;

    public OpenCraftingJobsPlanGuiPacket() {

    }

    public OpenCraftingJobsPlanGuiPacket(CraftingJobGuiData craftingPlanGuiData) {
        this.pos = craftingPlanGuiData.getPos();
        this.side = craftingPlanGuiData.getSide();
        this.channel = craftingPlanGuiData.getChannel();
        this.craftingPlanHandler = craftingPlanGuiData.getHandler().getId().toString();
        this.craftingJobId = new CompoundNBT();
        this.craftingJobId.put("id", craftingPlanGuiData.getHandler().serializeCraftingJobId(craftingPlanGuiData.getCraftingJob()));
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, PlayerEntity player) {

    }

    @Override
    public void actionServer(World world, ServerPlayerEntity player) {
        // Create common data holder
        ITerminalStorageTabIngredientCraftingHandler handler = getHandler();
        CraftingJobGuiData craftingJobGuiData = new CraftingJobGuiData(
                pos,
                side,
                channel,
                handler,
                handler.deserializeCraftingJobId(craftingJobId.get("id"))
        );
        PartPos partPos = PartPos.of(world, pos, side);

        // Create temporary container provider
        INamedContainerProvider containerProvider = new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new StringTextComponent("");
            }

            @Override
            public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(partPos);
                return new ContainerTerminalCraftingJobsPlan(id, playerInventory,
                        data.getRight(), Optional.of(data.getLeft()), (PartTypeTerminalCraftingJob) data.getMiddle(),
                        craftingJobGuiData);
            }
        };

        // Trigger gui opening
        NetworkHooks.openGui(player, containerProvider, packetBuffer -> {
            PacketCodec.write(packetBuffer, partPos);
            packetBuffer.writeUtf(PartTypes.TERMINAL_CRAFTING_JOB.getUniqueName().toString());
            craftingJobGuiData.writeToPacketBuffer(packetBuffer);
        });
    }

    protected ITerminalStorageTabIngredientCraftingHandler getHandler() {
        return TerminalStorageTabIngredientCraftingHandlers.REGISTRY.getHandler(
                new ResourceLocation(this.craftingPlanHandler));
    }

    public static void send(BlockPos pos, Direction side,
                            int channel, HandlerWrappedTerminalCraftingPlan craftingPlan) {
        CraftingJobGuiData data = new CraftingJobGuiData(pos, side, channel, craftingPlan.getHandler(),
                craftingPlan.getCraftingPlan().getId());
        IntegratedTerminals._instance.getPacketHandler().sendToServer(new OpenCraftingJobsPlanGuiPacket(data));
    }

}