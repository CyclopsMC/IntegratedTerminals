package org.cyclops.integratedterminals.network.packet;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.client.gui.toast.CraftingJobToastHelpers;

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
    public void actionClient(Level world, Player player) {
        if (!GeneralConfig.craftingJobFinishedToast) {
            return;
        }

        IngredientComponent<T, M> ingredientComponent = getComponent();
        if (ingredientComponent == null) {
            return;
        }
        T instance = ingredientComponent.getSerializer()
                .deserializeInstance(world.registryAccess(), this.instanceData.get("i"));

        // Showing the toast is delegated, as referring to a client-only class from here breaks dedicated servers
        CraftingJobToastHelpers.showCraftingJobFinished(ingredientComponent, instance);
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
        // Server-to-client only packet
    }

    protected IngredientComponent<T, M> getComponent() {
        return (IngredientComponent<T, M>) IngredientComponent.REGISTRY.get(ResourceLocation.parse(this.ingredientName));
    }

}
