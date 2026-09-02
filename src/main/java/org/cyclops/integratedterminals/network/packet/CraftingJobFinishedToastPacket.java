package org.cyclops.integratedterminals.network.packet;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.client.gui.toast.CraftingJobToast;

/**
 * Packet for showing a toast when a crafting job that the player requested has been completed.
 * @author rubensworks
 */
public class CraftingJobFinishedToastPacket extends PacketCodec<CraftingJobFinishedToastPacket> {

    public static final Type<CraftingJobFinishedToastPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "crafting_job_finished_toast"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingJobFinishedToastPacket> CODEC = getCodec(CraftingJobFinishedToastPacket::new);

    @CodecField
    private ItemStack outputItem = ItemStack.EMPTY;
    @CodecField
    private String outputLabel;

    public CraftingJobFinishedToastPacket() {
        super(ID);
    }

    /**
     * @param outputItem The crafted item, or an empty stack if the job did not output items.
     * @param outputLabel A textual description of the output, used when no output item is available.
     */
    public CraftingJobFinishedToastPacket(ItemStack outputItem, String outputLabel) {
        super(ID);
        this.outputItem = outputItem;
        this.outputLabel = outputLabel;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(Level world, Player player) {
        if (!GeneralConfig.craftingJobFinishedToast) {
            return;
        }

        boolean hasItem = !this.outputItem.isEmpty();
        Component output = hasItem
                ? Component.literal(this.outputItem.getCount() + "x ").append(this.outputItem.getHoverName())
                : Component.literal(this.outputLabel);
        Component title = Component.translatable("gui.integratedterminals.crafting_job.finished.title")
                .withStyle(ChatFormatting.GREEN);
        Component subtitle = Component.translatable("gui.integratedterminals.crafting_job.finished", output);

        // Group toasts by output, so that repeated crafts of the same thing don't pile up.
        Object token = hasItem ? this.outputItem.getItem() : this.outputLabel;
        ItemStack icon = hasItem ? this.outputItem : new ItemStack(Items.CRAFTING_TABLE);

        var toasts = Minecraft.getInstance().getToasts();
        CraftingJobToast existing = toasts.getToast(CraftingJobToast.class, token);
        if (existing != null) {
            existing.reset(title, subtitle);
        } else {
            toasts.addToast(new CraftingJobToast(token, icon, title, subtitle));
        }
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
        // Server-to-client only packet
    }

}
