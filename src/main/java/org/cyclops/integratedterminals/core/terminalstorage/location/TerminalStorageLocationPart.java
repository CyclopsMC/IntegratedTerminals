package org.cyclops.integratedterminals.core.terminalstorage.location;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.location.ITerminalStorageLocation;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingOptionAmountPart;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingPlanPart;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientPartOpenPacket;
import org.cyclops.integratedterminals.part.PartTypeTerminalStorage;
import org.cyclops.integratedterminals.part.PartTypes;

import java.util.Optional;

/**
 * @author rubensworks
 */
public class TerminalStorageLocationPart implements ITerminalStorageLocation<PartPos> {

    @Override
    public ResourceLocation getName() {
        return new ResourceLocation(Reference.MOD_ID, "part");
    }

    @Override
    public <T, M> void openContainerFromClient(CraftingOptionGuiData<T, M, PartPos> craftingOptionGuiData) {
        PartPos partPos = craftingOptionGuiData.getLocationInstance();
        TerminalStorageIngredientPartOpenPacket.send(partPos.getPos().getBlockPos(), partPos.getSide(),
                craftingOptionGuiData.getTabName(), craftingOptionGuiData.getChannel());
    }

    @Override
    public <T, M> void openContainerFromServer(CraftingOptionGuiData<T, M, PartPos> craftingOptionGuiData, Level world, ServerPlayer player) {
        PartPos partPos = craftingOptionGuiData.getLocationInstance();
        TerminalStorageIngredientPartOpenPacket.openServer(
                world,
                partPos.getPos().getBlockPos(),
                partPos.getSide(),
                player,
                craftingOptionGuiData.getTabName(),
                craftingOptionGuiData.getChannel()
        );
    }

    @Override
    public <T, M> void openContainerCraftingPlan(CraftingOptionGuiData<T, M, PartPos> craftingOptionGuiData, Level world, ServerPlayer player) {
        // Create temporary container provider
        MenuProvider containerProvider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return new TextComponent("");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                PartPos location = craftingOptionGuiData.getLocationInstance();
                Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(PartPos.of(world, location.getPos().getBlockPos(), location.getSide()));
                return new ContainerTerminalStorageCraftingPlanPart(id, playerInventory,
                        Optional.of(data.getRight()), Optional.of(data.getLeft()), (PartTypeTerminalStorage) data.getMiddle(),
                        craftingOptionGuiData);
            }
        };

        // Trigger gui opening
        NetworkHooks.openGui(player, containerProvider, packetBuffer -> {
            packetBuffer.writeUtf(PartTypes.TERMINAL_STORAGE.getUniqueName().toString());
            craftingOptionGuiData.writeToPacketBuffer(packetBuffer);
        });
    }

    @Override
    public <T, M> void openContainerCraftingOptionAmount(CraftingOptionGuiData<T, M, PartPos> craftingOptionGuiData, Level world, ServerPlayer player) {
        // Create temporary container provider
        MenuProvider containerProvider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return new TextComponent("");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                PartPos location = craftingOptionGuiData.getLocationInstance();
                Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(PartPos.of(world, location.getPos().getBlockPos(), location.getSide()));
                return new ContainerTerminalStorageCraftingOptionAmountPart(id, playerInventory,
                        Optional.of(data.getRight()), Optional.of(data.getLeft()), (PartTypeTerminalStorage) data.getMiddle(),
                        craftingOptionGuiData);
            }
        };

        // Trigger gui opening
        NetworkHooks.openGui(player, containerProvider, packetBuffer -> {
            packetBuffer.writeUtf(PartTypes.TERMINAL_STORAGE.getUniqueName().toString());
            craftingOptionGuiData.writeToPacketBuffer(packetBuffer);
        });
    }

    @Override
    public void writeToPacketBuffer(FriendlyByteBuf packetBuffer, PartPos location) {
        PacketCodec.getAction(PartPos.class).encode(location, packetBuffer);
    }

    @Override
    public PartPos readFromPacketBuffer(FriendlyByteBuf packetBuffer) {
        return (PartPos) PacketCodec.getAction(PartPos.class).decode(packetBuffer);
    }
}
