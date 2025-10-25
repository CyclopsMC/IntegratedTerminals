package org.cyclops.integratedterminals.network.packet;

import com.google.common.collect.Lists;
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
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCrafting;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

import java.util.Collection;
import java.util.List;

/**
 * Packet for sending a storage change event from server to client.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientCraftingOptionsPacket extends PacketCodec<TerminalStorageIngredientCraftingOptionsPacket> {

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
    @CodecField
    private String ingredientComponentName;

    public TerminalStorageIngredientCraftingOptionsPacket() {
        super(ID);
    }

    public <T> TerminalStorageIngredientCraftingOptionsPacket(HolderLookup.Provider lookupProvider,
                                                              String tabId,
                                                              int channel,
                                                              Collection<HandlerWrappedTerminalCraftingOption<T>> craftingOptions,
                                                              boolean reset,
                                                              boolean firstChannel,
                                                              IngredientComponent<?, ?> ingredientComponent) {
        super(ID);
        this.tabId = tabId;
        this.channel = channel;
        this.data = new CompoundTag();
        ListTag list = new ListTag();
        for (HandlerWrappedTerminalCraftingOption<?> option : craftingOptions) {
            list.add(HandlerWrappedTerminalCraftingOption.serialize(lookupProvider, option));
        }
        this.data.put("craftingOptions", list);
        this.reset = reset;
        this.firstChannel = firstChannel;
        this.ingredientComponentName = IngredientComponent.REGISTRY.getKey(ingredientComponent).toString();
    }

    @Override
    public boolean isAsync() {
        return GeneralConfig.packetDeserializationEnableMultithreading;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(Level world, Player player) {
        IngredientComponent<?, ?> ingredientComponent = IngredientComponent.REGISTRY.get(ResourceLocation.parse(ingredientComponentName));
        if (ingredientComponentName == null) {
            throw new IllegalArgumentException("Could not find the ingredient component type " + ingredientComponentName);
        }
        ListTag list = this.data.getList("craftingOptions", Tag.TAG_COMPOUND);
        List<HandlerWrappedTerminalCraftingOption<?>> craftingOptions = Lists.newArrayListWithExpectedSize(list.size());
        for (int i = 0; i < list.size(); i++) {
            HandlerWrappedTerminalCraftingOption<?> option = HandlerWrappedTerminalCraftingOption
                    .deserialize(world.registryAccess(), ingredientComponent, list.getCompound(i));
            craftingOptions.add(option);
        }

        // Run the following code in the render thread, since this packet runs in a different thread. (isAsync is true)
        Minecraft.getInstance().execute(() -> {
            if(player.containerMenu instanceof ContainerTerminalStorageBase container) {
                TerminalStorageTabIngredientComponentClient<?, ?> tab = (TerminalStorageTabIngredientComponentClient<?, ?>) container.getTabClient(tabId);
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
        });
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {

    }

}
