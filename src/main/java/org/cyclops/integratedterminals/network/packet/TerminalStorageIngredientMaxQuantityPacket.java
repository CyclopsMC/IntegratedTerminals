package org.cyclops.integratedterminals.network.packet;

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
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCrafting;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

/**
 * Packet for sending a storage's quantity from server to client.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientMaxQuantityPacket extends PacketCodec<TerminalStorageIngredientMaxQuantityPacket> {

    public static final Type<TerminalStorageIngredientMaxQuantityPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "terminal_storage_ingredient_max_quantity"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalStorageIngredientMaxQuantityPacket> CODEC = getCodec(TerminalStorageIngredientMaxQuantityPacket::new);

    @CodecField
    private String tabId;
    @CodecField
    private String ingredientName;
    @CodecField
    private long maxQuantity;
    @CodecField
    private int channel;

    public TerminalStorageIngredientMaxQuantityPacket() {
        super(ID);
    }

    public TerminalStorageIngredientMaxQuantityPacket(String tabId, IngredientComponent<?, ?> ingredientComponent,
                                                      long maxQuantity, int channel) {
        super(ID);
        this.tabId = tabId;
        this.ingredientName = ingredientComponent.getName().toString();
        this.maxQuantity = maxQuantity;
        this.channel = channel;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(Level world, Player player) {
        if(player.containerMenu instanceof ContainerTerminalStorageBase) {
            ContainerTerminalStorageBase container = ((ContainerTerminalStorageBase) player.containerMenu);
            IngredientComponent<?, ?> ingredientComponent = IngredientComponent.REGISTRY.get(ResourceLocation.parse(this.ingredientName));
            if (ingredientComponent == null) {
                throw new IllegalArgumentException("No ingredient component with the given name was found: " + ingredientName);
            }
            TerminalStorageTabIngredientComponentClient<?, ?> tab = (TerminalStorageTabIngredientComponentClient<?, ?>) container.getTabClient(tabId);
            tab.setMaxQuantity(channel, maxQuantity);

            // Hard-coded crafting tab
            // TODO: abstract this as "auxiliary" tabs
            if (tabId.equals(IngredientComponents.ITEMSTACK.getName().toString())) {
                TerminalStorageTabIngredientComponentClient<?, ?> tabCrafting = (TerminalStorageTabIngredientComponentClient<?, ?>) container
                        .getTabClient(TerminalStorageTabIngredientComponentItemStackCrafting.NAME.toString());
                tabCrafting.setMaxQuantity(channel, maxQuantity);
            }
        }
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {

    }

}
