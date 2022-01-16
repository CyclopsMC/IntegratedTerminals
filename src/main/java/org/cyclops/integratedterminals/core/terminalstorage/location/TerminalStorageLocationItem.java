package org.cyclops.integratedterminals.core.terminalstorage.location;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.location.ITerminalStorageLocation;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingOptionAmountItem;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingPlanItem;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientItemOpenPacket;

/**
 * @author rubensworks
 */
public class TerminalStorageLocationItem implements ITerminalStorageLocation<Pair<InteractionHand, Integer>> {

    @Override
    public ResourceLocation getName() {
        return new ResourceLocation(Reference.MOD_ID, "item");
    }

    @Override
    public <T, M> void openContainerFromClient(CraftingOptionGuiData<T, M, Pair<InteractionHand, Integer>> craftingOptionGuiData) {
        Pair<InteractionHand, Integer> slot = craftingOptionGuiData.getLocationInstance();
        TerminalStorageIngredientItemOpenPacket.send(slot,
                craftingOptionGuiData.getTabName(), craftingOptionGuiData.getChannel());
    }

    @Override
    public <T, M> void openContainerFromServer(CraftingOptionGuiData<T, M, Pair<InteractionHand, Integer>> craftingOptionGuiData, Level world, ServerPlayer player) {
        Pair<InteractionHand, Integer> slot = craftingOptionGuiData.getLocationInstance();
        TerminalStorageIngredientItemOpenPacket.openServer(
                world,
                slot,
                player,
                craftingOptionGuiData.getTabName(),
                craftingOptionGuiData.getChannel()
        );
    }

    @Override
    public <T, M> void openContainerCraftingPlan(CraftingOptionGuiData<T, M, Pair<InteractionHand, Integer>> craftingOptionGuiData, Level world, ServerPlayer player) {
        Pair<InteractionHand, Integer> location = craftingOptionGuiData.getLocationInstance();

        // Create temporary container provider
        MenuProvider containerProvider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return new TextComponent("");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                return new ContainerTerminalStorageCraftingPlanItem(id, playerInventory,
                        location.getRight(), location.getLeft(), craftingOptionGuiData);
            }
        };

        // Trigger gui opening
        NetworkHooks.openGui(player, containerProvider, packetBuffer -> {
            packetBuffer.writeInt(location.getRight());
            packetBuffer.writeBoolean(location.getLeft() == InteractionHand.MAIN_HAND);

            craftingOptionGuiData.writeToPacketBuffer(packetBuffer);
        });
    }

    @Override
    public <T, M> void openContainerCraftingOptionAmount(CraftingOptionGuiData<T, M, Pair<InteractionHand, Integer>> craftingOptionGuiData, Level world, ServerPlayer player) {
        Pair<InteractionHand, Integer> location = craftingOptionGuiData.getLocationInstance();

        // Create temporary container provider
        MenuProvider containerProvider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return new TextComponent("");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                return new ContainerTerminalStorageCraftingOptionAmountItem(id, playerInventory,
                        location.getRight(), location.getLeft(), craftingOptionGuiData);
            }
        };

        // Trigger gui opening
        NetworkHooks.openGui(player, containerProvider, packetBuffer -> {
            packetBuffer.writeInt(location.getRight());
            packetBuffer.writeBoolean(location.getLeft() == InteractionHand.MAIN_HAND);

            craftingOptionGuiData.writeToPacketBuffer(packetBuffer);
        });
    }

    @Override
    public void writeToPacketBuffer(FriendlyByteBuf packetBuffer, Pair<InteractionHand, Integer> location) {
        packetBuffer.writeUtf(location.getLeft().name());
        packetBuffer.writeInt(location.getRight());
    }

    @Override
    public Pair<InteractionHand, Integer> readFromPacketBuffer(FriendlyByteBuf packetBuffer) {
        return Pair.of(InteractionHand.valueOf(packetBuffer.readUtf(32767)), packetBuffer.readInt());
    }
}
