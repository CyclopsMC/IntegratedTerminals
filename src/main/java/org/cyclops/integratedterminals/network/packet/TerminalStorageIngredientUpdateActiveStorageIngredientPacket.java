package org.cyclops.integratedterminals.network.packet;

import com.mojang.logging.LogUtils;
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
import org.cyclops.commoncapabilities.api.ingredient.IIngredientSerializer;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.item.TagPathElement;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;
import org.slf4j.Logger;

/**
 * Packet for sending the currently active storage stack from server to client.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientUpdateActiveStorageIngredientPacket<T> extends PacketCodec<TerminalStorageIngredientUpdateActiveStorageIngredientPacket<T>> {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Type<TerminalStorageIngredientUpdateActiveStorageIngredientPacket<?>> ID = new Type<>(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "terminal_storage_ingredient_update_active_storage_ingredient"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalStorageIngredientUpdateActiveStorageIngredientPacket<?>> CODEC = (StreamCodec) getCodec(TerminalStorageIngredientUpdateActiveStorageIngredientPacket::new);

    @CodecField
    private String tabId;
    @CodecField
    private String ingredientName;
    @CodecField
    private int channel;
    @CodecField
    private CompoundTag activeStorageInstanceData;

    public TerminalStorageIngredientUpdateActiveStorageIngredientPacket() {
        super((Type) ID);
    }

    public TerminalStorageIngredientUpdateActiveStorageIngredientPacket(HolderLookup.Provider lookupProvider, String tabId,
                                                                        IngredientComponent<T, ?> component,
                                                                        int channel, T activeStorageInstance) {
        super((Type) ID);
        this.tabId = tabId;
        this.ingredientName = component.getName().toString();
        this.channel = channel;
        IIngredientSerializer<T, ?> serializer = getComponent().getSerializer();
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new TagPathElement(new CompoundTag()), LOGGER)) {
            TagValueOutput valueOutput = TagValueOutput.createWithContext(scopedCollector, lookupProvider);
            serializer.serializeInstance(valueOutput, activeStorageInstance);
            this.activeStorageInstanceData = valueOutput.buildResult();
        }
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(Level world, Player player) {
        if(player.containerMenu instanceof ContainerTerminalStorageBase) {
            ContainerTerminalStorageBase container = ((ContainerTerminalStorageBase) player.containerMenu);
            TerminalStorageTabIngredientComponentClient<T, ?> tab = (TerminalStorageTabIngredientComponentClient<T, ?>)
                    container.getTabClient(tabId);
            IIngredientSerializer<T, ?> serializer = getComponent().getSerializer();
            try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new TagPathElement(this.activeStorageInstanceData), LOGGER)) {
                ValueInput input = TagValueInput.create(scopedCollector, world.registryAccess(), this.activeStorageInstanceData);
                T activeInstance = serializer.deserializeInstance(input);
                tab.handleActiveIngredientUpdate(getChannel(), activeInstance);
            }
        }
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {

    }

    public IngredientComponent<T, ?> getComponent() {
        IngredientComponent<T, ?> ingredientComponent = (IngredientComponent<T, ?>) IngredientComponent.REGISTRY.getValue(Identifier.parse(this.ingredientName));
        if (ingredientComponent == null) {
            throw new IllegalArgumentException("No ingredient component with the given name was found: " + ingredientName);
        }
        return ingredientComponent;
    }

    public int getChannel() {
        return channel;
    }

}
