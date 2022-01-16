package org.cyclops.integratedterminals.network.packet;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCrafting;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

/**
 * Packet for sending a storage's quantity from server to client.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientMaxQuantityPacket extends PacketCodec {

    @CodecField
    private String tabId;
    @CodecField
    private String ingredientName;
    @CodecField
    private long maxQuantity;
    @CodecField
    private int channel;

    public TerminalStorageIngredientMaxQuantityPacket() {

    }

    public TerminalStorageIngredientMaxQuantityPacket(String tabId, IngredientComponent<?, ?> ingredientComponent,
                                                      long maxQuantity, int channel) {
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
            IngredientComponent<?, ?> ingredientComponent = IngredientComponent.REGISTRY.getValue(new ResourceLocation(this.ingredientName));
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
