package org.cyclops.integratedterminals.network.packet;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientSerializer;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.Capabilities;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.client.gui.toast.CraftingJobToast;

/**
 * Packet for showing a toast when a crafting job that the player requested has been completed.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class CraftingJobFinishedToastPacket<T, M> extends PacketCodec<CraftingJobFinishedToastPacket<T, M>> {

    public static final Type<CraftingJobFinishedToastPacket<?, ?>> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "crafting_job_finished_toast"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingJobFinishedToastPacket<?, ?>> CODEC = (StreamCodec) getCodec(CraftingJobFinishedToastPacket::new);

    @CodecField
    private String ingredientName;
    @CodecField
    private CompoundTag instanceData;

    public CraftingJobFinishedToastPacket() {
        super((Type) ID);
    }

    /**
     * @param lookupProvider A lookup provider.
     * @param ingredientComponent The component of the crafted output.
     * @param instance The crafted output, where the quantity is the total that was crafted.
     */
    public CraftingJobFinishedToastPacket(HolderLookup.Provider lookupProvider,
                                          IngredientComponent<T, M> ingredientComponent, T instance) {
        super((Type) ID);
        this.ingredientName = ingredientComponent.getName().toString();
        this.instanceData = new CompoundTag();
        this.instanceData.put("i", ingredientComponent.getSerializer().serializeInstance(lookupProvider, instance));
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

        IngredientComponent<T, M> ingredientComponent = getComponent();
        if (ingredientComponent == null) {
            return;
        }
        IIngredientSerializer<T, M> serializer = ingredientComponent.getSerializer();
        T instance = serializer.deserializeInstance(world.registryAccess(), this.instanceData.get("i"));

        // The quantity is formatted by the component's own handler, so that fluids, energy,
        // and ingredient components from other mods all read naturally.
        String quantity = ingredientComponent
                .getCapability(Capabilities.IngredientComponentTerminalStorageHandler.INGREDIENT)
                .map(handler -> handler.formatQuantity(instance))
                .orElseGet(() -> String.valueOf(ingredientComponent.getMatcher().getQuantity(instance)));
        Component title = Component.translatable("gui.integratedterminals.crafting_job.finished.title")
                .withStyle(ChatFormatting.GREEN);
        Component subtitle = Component.translatable("gui.integratedterminals.crafting_job.finished",
                quantity, ingredientComponent.getMatcher().getDisplayName(instance));

        // Group by output, so that repeated crafts of the same thing don't pile up
        Object token = this.ingredientName + "|"
                + ingredientComponent.getMatcher().getDisplayName(instance).getString();
        var toasts = Minecraft.getInstance().getToasts();
        CraftingJobToast<?, ?> existing = toasts.getToast(CraftingJobToast.class, token);
        if (existing != null) {
            existing.reset(title, subtitle);
        } else {
            toasts.addToast(new CraftingJobToast<>(token, ingredientComponent, instance, title, subtitle));
        }
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
        // Server-to-client only packet
    }

    protected IngredientComponent<T, M> getComponent() {
        return (IngredientComponent<T, M>) IngredientComponent.REGISTRY.get(ResourceLocation.parse(this.ingredientName));
    }

}
