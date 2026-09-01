package org.cyclops.integratedterminals.network.packet;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.item.TagPathElement;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCrafting;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.PendingCraftingJobOutput;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.PendingCraftingJobOutputEntry;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.PendingCraftingJobOutputs;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;
import org.slf4j.Logger;

import java.util.List;

/**
 * Packet for sending the pending outputs of all running crafting jobs from server to client.
 *
 * This is used to indicate the ingredients that are being crafted in the storage terminal.
 *
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientCraftingJobsPacket extends PacketCodec<TerminalStorageIngredientCraftingJobsPacket> {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Type<TerminalStorageIngredientCraftingJobsPacket> ID = new Type<>(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "terminal_storage_ingredient_crafting_jobs"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalStorageIngredientCraftingJobsPacket> CODEC = getCodec(TerminalStorageIngredientCraftingJobsPacket::new);

    @CodecField
    private String tabId;
    @CodecField
    private int channel;
    @CodecField
    private CompoundTag data;

    public TerminalStorageIngredientCraftingJobsPacket() {
        super(ID);
    }

    public <T, M> TerminalStorageIngredientCraftingJobsPacket(HolderLookup.Provider lookupProvider, String tabId,
                                                              PendingCraftingJobOutputs<T, M> pendingCraftingJobOutputs) {
        super(ID);
        this.tabId = tabId;
        this.channel = pendingCraftingJobOutputs.getChannel();

        IIngredientMatcher<T, M> matcher = pendingCraftingJobOutputs.getIngredientComponent().getMatcher();
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new TagPathElement(new CompoundTag()), LOGGER)) {
            TagValueOutput valueOutput = TagValueOutput.createWithContext(scopedCollector, lookupProvider);
            ValueOutput.ValueOutputList list = valueOutput.childrenList("craftingJobOutputs");
            for (PendingCraftingJobOutput<T> output : pendingCraftingJobOutputs.getOutputs()) {
                ValueOutput child = list.addChild();
                IPrototypedIngredient.serialize(child.child("ingredient"),
                        new PrototypedIngredient<>(pendingCraftingJobOutputs.getIngredientComponent(),
                                output.getInstance(), matcher.getExactMatchNoQuantityCondition()));
                child.putInt("status", output.getStatus().ordinal());
            }
            this.data = valueOutput.buildResult();
        }
    }

    @Override
    public boolean isAsync() {
        return GeneralConfig.packetDeserializationEnableMultithreading;
    }

    @Override
    public void actionClient(Level world, Player player) {
        List<PendingCraftingJobOutputEntry> outputs = Lists.newArrayList();
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new TagPathElement(this.data), LOGGER)) {
            ValueInput input = TagValueInput.create(scopedCollector, world.registryAccess(), this.data);
            for (ValueInput craftingJobOutput : input.childrenList("craftingJobOutputs").orElseThrow()) {
                outputs.add(new PendingCraftingJobOutputEntry(
                        IPrototypedIngredient.deserialize(craftingJobOutput.child("ingredient").orElseThrow()),
                        TerminalCraftingJobStatus.values()[craftingJobOutput.getIntOr("status", 0)]));
            }
        }

        // Run the following code in the render thread, since this packet runs in a different thread. (isAsync is true)
        Minecraft.getInstance().execute(() -> {
            if (player.containerMenu instanceof ContainerTerminalStorageBase container) {
                TerminalStorageTabIngredientComponentClient<?, ?> tab = (TerminalStorageTabIngredientComponentClient<?, ?>) container.getTabClient(tabId);
                if (tab != null) {
                    tab.setPendingCraftingJobOutputs(channel, outputs);
                }

                // Hard-coded crafting tab
                // TODO: abstract this as "auxiliary" tabs
                if (tabId.equals(IngredientComponents.ITEMSTACK.getName().toString())) {
                    TerminalStorageTabIngredientComponentClient<?, ?> tabCrafting = (TerminalStorageTabIngredientComponentClient<?, ?>) container
                            .getTabClient(TerminalStorageTabIngredientComponentItemStackCrafting.NAME.toString());
                    if (tabCrafting != null) {
                        tabCrafting.setPendingCraftingJobOutputs(channel, outputs);
                    }
                }
            }
        });
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {

    }

}
