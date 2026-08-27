package org.cyclops.integratedterminals.network.packet;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCrafting;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.PendingCraftingJobOutput;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.PendingCraftingJobOutputEntry;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.PendingCraftingJobOutputs;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

import java.util.List;
import java.util.Map;

/**
 * Packet for sending the pending outputs of all running crafting jobs from server to client.
 *
 * This is used to indicate the ingredients that are being crafted in the storage terminal.
 *
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientCraftingJobsPacket extends PacketCodec<TerminalStorageIngredientCraftingJobsPacket> {

    public static final Type<TerminalStorageIngredientCraftingJobsPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "terminal_storage_ingredient_crafting_jobs"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalStorageIngredientCraftingJobsPacket> CODEC = getCodec(TerminalStorageIngredientCraftingJobsPacket::new);

    @CodecField
    private String tabId;
    @CodecField
    private CompoundTag data;

    public TerminalStorageIngredientCraftingJobsPacket() {
        super(ID);
    }

    public <T, M> TerminalStorageIngredientCraftingJobsPacket(HolderLookup.Provider lookupProvider, String tabId,
                                                              PendingCraftingJobOutputs<T, M> pendingCraftingJobOutputs) {
        super(ID);
        this.tabId = tabId;
        this.data = new CompoundTag();

        IIngredientMatcher<T, M> matcher = pendingCraftingJobOutputs.getIngredientComponent().getMatcher();
        ListTag list = new ListTag();
        for (Int2ObjectMap.Entry<Map<T, PendingCraftingJobOutput<T>>> channelEntry
                : pendingCraftingJobOutputs.getChanneledOutputs().int2ObjectEntrySet()) {
            for (PendingCraftingJobOutput<T> output : channelEntry.getValue().values()) {
                CompoundTag tag = new CompoundTag();
                tag.putInt("channel", channelEntry.getIntKey());
                tag.put("ingredient", IPrototypedIngredient.serialize(lookupProvider,
                        new PrototypedIngredient<>(pendingCraftingJobOutputs.getIngredientComponent(),
                                output.getInstance(), matcher.getExactMatchNoQuantityCondition())));
                tag.putInt("status", output.getStatus().ordinal());
                list.add(tag);
            }
        }
        this.data.put("craftingJobOutputs", list);
    }

    @Override
    public boolean isAsync() {
        return GeneralConfig.packetDeserializationEnableMultithreading;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(Level world, Player player) {
        ListTag list = this.data.getList("craftingJobOutputs", Tag.TAG_COMPOUND);
        List<PendingCraftingJobOutputEntry> outputs = Lists.newArrayListWithExpectedSize(list.size());
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            outputs.add(new PendingCraftingJobOutputEntry(
                    tag.getInt("channel"),
                    IPrototypedIngredient.deserialize(world.registryAccess(), tag.getCompound("ingredient")),
                    TerminalCraftingJobStatus.values()[tag.getInt("status")]));
        }

        // Run the following code in the render thread, since this packet runs in a different thread. (isAsync is true)
        Minecraft.getInstance().execute(() -> {
            if (player.containerMenu instanceof ContainerTerminalStorageBase container) {
                TerminalStorageTabIngredientComponentClient<?, ?> tab = (TerminalStorageTabIngredientComponentClient<?, ?>) container.getTabClient(tabId);
                if (tab != null) {
                    tab.setPendingCraftingJobOutputs(outputs);
                }

                // Hard-coded crafting tab
                // TODO: abstract this as "auxiliary" tabs
                if (tabId.equals(IngredientComponents.ITEMSTACK.getName().toString())) {
                    TerminalStorageTabIngredientComponentClient<?, ?> tabCrafting = (TerminalStorageTabIngredientComponentClient<?, ?>) container
                            .getTabClient(TerminalStorageTabIngredientComponentItemStackCrafting.NAME.toString());
                    if (tabCrafting != null) {
                        tabCrafting.setPendingCraftingJobOutputs(outputs);
                    }
                }
            }
        });
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {

    }

}
