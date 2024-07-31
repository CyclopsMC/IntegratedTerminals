package org.cyclops.integratedterminals.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
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
public class OpenCraftingJobsPlanGuiPacket extends PacketCodec<OpenCraftingJobsPlanGuiPacket> {

    public static final Type<OpenCraftingJobsPlanGuiPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "open_crafting_jobs_plan_gui"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenCraftingJobsPlanGuiPacket> CODEC = getCodec(OpenCraftingJobsPlanGuiPacket::new);

    @CodecField
    private BlockPos pos;
    @CodecField
    private Direction side;
    @CodecField
    private int channel;
    @CodecField
    private String craftingPlanHandler;
    @CodecField
    private CompoundTag craftingJobId;

    public OpenCraftingJobsPlanGuiPacket() {
        super(ID);
    }

    public OpenCraftingJobsPlanGuiPacket(CraftingJobGuiData craftingPlanGuiData) {
        super(ID);
        this.pos = craftingPlanGuiData.getPos();
        this.side = craftingPlanGuiData.getSide();
        this.channel = craftingPlanGuiData.getChannel();
        this.craftingPlanHandler = craftingPlanGuiData.getHandler().getId().toString();
        this.craftingJobId = new CompoundTag();
        this.craftingJobId.put("id", craftingPlanGuiData.getHandler().serializeCraftingJobId(craftingPlanGuiData.getCraftingJob()));
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(Level world, Player player) {

    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
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
        MenuProvider containerProvider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(partPos);
                return new ContainerTerminalCraftingJobsPlan(id, playerInventory,
                        data.getRight(), Optional.of(data.getLeft()), (PartTypeTerminalCraftingJob) data.getMiddle(),
                        craftingJobGuiData);
            }
        };

        // Trigger gui opening
        player.openMenu(containerProvider, packetBuffer -> {
            PacketCodec.write(packetBuffer, partPos);
            packetBuffer.writeUtf(PartTypes.TERMINAL_CRAFTING_JOB.getUniqueName().toString());
            craftingJobGuiData.writeToPacketBuffer(packetBuffer);
        });
    }

    protected ITerminalStorageTabIngredientCraftingHandler getHandler() {
        return TerminalStorageTabIngredientCraftingHandlers.REGISTRY.getHandler(
                ResourceLocation.parse(this.craftingPlanHandler));
    }

    public static void send(BlockPos pos, Direction side,
                            int channel, HandlerWrappedTerminalCraftingPlan craftingPlan) {
        CraftingJobGuiData data = new CraftingJobGuiData(pos, side, channel, craftingPlan.getHandler(),
                craftingPlan.getCraftingPlanFlat().getId());
        IntegratedTerminals._instance.getPacketHandler().sendToServer(new OpenCraftingJobsPlanGuiPacket(data));
    }

}
