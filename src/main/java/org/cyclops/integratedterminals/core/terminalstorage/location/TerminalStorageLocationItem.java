package org.cyclops.integratedterminals.core.terminalstorage.location;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
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
public class TerminalStorageLocationItem implements ITerminalStorageLocation<Pair<Hand, Integer>> {

    @Override
    public ResourceLocation getName() {
        return new ResourceLocation(Reference.MOD_ID, "item");
    }

    @Override
    public <T, M> void openContainerFromClient(CraftingOptionGuiData<T, M, Pair<Hand, Integer>> craftingOptionGuiData) {
        Pair<Hand, Integer> slot = craftingOptionGuiData.getLocationInstance();
        TerminalStorageIngredientItemOpenPacket.send(slot,
                craftingOptionGuiData.getTabName(), craftingOptionGuiData.getChannel());
    }

    @Override
    public <T, M> void openContainerFromServer(CraftingOptionGuiData<T, M, Pair<Hand, Integer>> craftingOptionGuiData, World world, ServerPlayerEntity player) {
        Pair<Hand, Integer> slot = craftingOptionGuiData.getLocationInstance();
        TerminalStorageIngredientItemOpenPacket.openServer(
                world,
                slot,
                player,
                craftingOptionGuiData.getTabName(),
                craftingOptionGuiData.getChannel()
        );
    }

    @Override
    public <T, M> void openContainerCraftingPlan(CraftingOptionGuiData<T, M, Pair<Hand, Integer>> craftingOptionGuiData, World world, ServerPlayerEntity player) {
        Pair<Hand, Integer> location = craftingOptionGuiData.getLocationInstance();

        // Create temporary container provider
        INamedContainerProvider containerProvider = new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new StringTextComponent("");
            }

            @Override
            public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                return new ContainerTerminalStorageCraftingPlanItem(id, playerInventory,
                        location.getRight(), location.getLeft(), craftingOptionGuiData);
            }
        };

        // Trigger gui opening
        NetworkHooks.openGui(player, containerProvider, packetBuffer -> {
            packetBuffer.writeInt(location.getRight());
            packetBuffer.writeBoolean(location.getLeft() == Hand.MAIN_HAND);

            craftingOptionGuiData.writeToPacketBuffer(packetBuffer);
        });
    }

    @Override
    public <T, M> void openContainerCraftingOptionAmount(CraftingOptionGuiData<T, M, Pair<Hand, Integer>> craftingOptionGuiData, World world, ServerPlayerEntity player) {
        Pair<Hand, Integer> location = craftingOptionGuiData.getLocationInstance();

        // Create temporary container provider
        INamedContainerProvider containerProvider = new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new StringTextComponent("");
            }

            @Override
            public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                return new ContainerTerminalStorageCraftingOptionAmountItem(id, playerInventory,
                        location.getRight(), location.getLeft(), craftingOptionGuiData);
            }
        };

        // Trigger gui opening
        NetworkHooks.openGui(player, containerProvider, packetBuffer -> {
            packetBuffer.writeInt(location.getRight());
            packetBuffer.writeBoolean(location.getLeft() == Hand.MAIN_HAND);

            craftingOptionGuiData.writeToPacketBuffer(packetBuffer);
        });
    }

    @Override
    public void writeToPacketBuffer(PacketBuffer packetBuffer, Pair<Hand, Integer> location) {
        packetBuffer.writeUtf(location.getLeft().name());
        packetBuffer.writeInt(location.getRight());
    }

    @Override
    public Pair<Hand, Integer> readFromPacketBuffer(PacketBuffer packetBuffer) {
        return Pair.of(Hand.valueOf(packetBuffer.readUtf(32767)), packetBuffer.readInt());
    }
}
