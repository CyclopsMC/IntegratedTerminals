package org.cyclops.integratedterminals.core.terminalstorage.location;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
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
    public <T, M> void openContainerFromServer(CraftingOptionGuiData<T, M, PartPos> craftingOptionGuiData, World world, ServerPlayerEntity player) {
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
    public <T, M> void openContainerCraftingPlan(CraftingOptionGuiData<T, M, PartPos> craftingOptionGuiData, World world, ServerPlayerEntity player) {
        // Create temporary container provider
        INamedContainerProvider containerProvider = new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new StringTextComponent("");
            }

            @Override
            public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                PartPos location = craftingOptionGuiData.getLocationInstance();
                Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(PartPos.of(world, location.getPos().getBlockPos(), location.getSide()));
                return new ContainerTerminalStorageCraftingPlanPart(id, playerInventory,
                        Optional.of(data.getRight()), Optional.of(data.getLeft()), (PartTypeTerminalStorage) data.getMiddle(),
                        craftingOptionGuiData);
            }
        };

        // Trigger gui opening
        NetworkHooks.openGui(player, containerProvider, packetBuffer -> {
            packetBuffer.writeString(PartTypes.TERMINAL_STORAGE.getUniqueName().toString());
            craftingOptionGuiData.writeToPacketBuffer(packetBuffer);
        });
    }

    @Override
    public <T, M> void openContainerCraftingOptionAmount(CraftingOptionGuiData<T, M, PartPos> craftingOptionGuiData, World world, ServerPlayerEntity player) {
        // Create temporary container provider
        INamedContainerProvider containerProvider = new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new StringTextComponent("");
            }

            @Override
            public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                PartPos location = craftingOptionGuiData.getLocationInstance();
                Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(PartPos.of(world, location.getPos().getBlockPos(), location.getSide()));
                return new ContainerTerminalStorageCraftingOptionAmountPart(id, playerInventory,
                        Optional.of(data.getRight()), Optional.of(data.getLeft()), (PartTypeTerminalStorage) data.getMiddle(),
                        craftingOptionGuiData);
            }
        };

        // Trigger gui opening
        NetworkHooks.openGui(player, containerProvider, packetBuffer -> {
            packetBuffer.writeString(PartTypes.TERMINAL_STORAGE.getUniqueName().toString());
            craftingOptionGuiData.writeToPacketBuffer(packetBuffer);
        });
    }

    @Override
    public void writeToPacketBuffer(PacketBuffer packetBuffer, PartPos location) {
        PacketCodec.getAction(PartPos.class).encode(location, packetBuffer);
    }

    @Override
    public PartPos readFromPacketBuffer(PacketBuffer packetBuffer) {
        return (PartPos) PacketCodec.getAction(PartPos.class).decode(packetBuffer);
    }
}
