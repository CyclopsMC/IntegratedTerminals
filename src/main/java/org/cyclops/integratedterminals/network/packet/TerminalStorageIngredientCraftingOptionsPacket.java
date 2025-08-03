package org.cyclops.integratedterminals.network.packet;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.item.TagPathElement;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCrafting;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;

/**
 * Packet for sending a storage change event from server to client.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientCraftingOptionsPacket extends PacketCodec<TerminalStorageIngredientCraftingOptionsPacket> {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Type<TerminalStorageIngredientCraftingOptionsPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "terminal_storage_ingredient_crafting_options"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalStorageIngredientCraftingOptionsPacket> CODEC = getCodec(TerminalStorageIngredientCraftingOptionsPacket::new);

    @CodecField
    private String tabId;
    @CodecField
    private int channel;
    @CodecField
    private CompoundTag data;
    @CodecField
    private boolean reset;
    @CodecField
    private boolean firstChannel;

    public TerminalStorageIngredientCraftingOptionsPacket() {
        super(ID);
    }

    public <T> TerminalStorageIngredientCraftingOptionsPacket(HolderLookup.Provider lookupProvider,
                                                              String tabId,
                                                              int channel,
                                                              Collection<HandlerWrappedTerminalCraftingOption<T>> craftingOptions,
                                                              boolean reset,
                                                              boolean firstChannel) {
        super(ID);
        this.tabId = tabId;
        this.channel = channel;
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new TagPathElement(new CompoundTag()), LOGGER)) {
            TagValueOutput valueOutput = TagValueOutput.createWithContext(scopedCollector, lookupProvider);
            ValueOutput.ValueOutputList list = valueOutput.childrenList("craftingOptions");
            for (HandlerWrappedTerminalCraftingOption<?> option : craftingOptions) {
                HandlerWrappedTerminalCraftingOption.serialize(list.addChild(), option);
            }
            this.data = valueOutput.buildResult();
        }
        this.reset = reset;
        this.firstChannel = firstChannel;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(Level world, Player player) {
        if(player.containerMenu instanceof ContainerTerminalStorageBase) {
            ContainerTerminalStorageBase container = ((ContainerTerminalStorageBase) player.containerMenu);


            TerminalStorageTabIngredientComponentClient<?, ?> tab = (TerminalStorageTabIngredientComponentClient<?, ?>) container.getTabClient(tabId);
            IngredientComponent<?, ?> ingredientComponent = tab.getIngredientComponent();

            List<HandlerWrappedTerminalCraftingOption<?>> craftingOptions = Lists.newArrayList();
            try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new TagPathElement(this.data), LOGGER)) {
                ValueInput input = TagValueInput.create(scopedCollector, world.registryAccess(), this.data);
                for (ValueInput craftingOption : input.childrenList("craftingOptions").orElseThrow()) {
                    HandlerWrappedTerminalCraftingOption<?> option = HandlerWrappedTerminalCraftingOption
                            .deserialize(craftingOption, ingredientComponent);
                    craftingOptions.add(option);
                }
            }
            tab.addCraftingOptions(channel, (List) craftingOptions, this.reset, this.firstChannel);

            // Hard-coded crafting tab
            // TODO: abstract this as "auxiliary" tabs
            if (tabId.equals(IngredientComponents.ITEMSTACK.getName().toString())) {
                TerminalStorageTabIngredientComponentClient<?, ?> tabCrafting = (TerminalStorageTabIngredientComponentClient<?, ?>) container
                        .getTabClient(TerminalStorageTabIngredientComponentItemStackCrafting.NAME.toString());
                tabCrafting.addCraftingOptions(channel, (List) craftingOptions, this.reset, this.firstChannel);
            }

            container.refreshChannelStrings();
        }
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {

    }

}
