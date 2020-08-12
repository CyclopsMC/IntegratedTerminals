package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingPlan;
import org.cyclops.integratedterminals.part.PartTypeTerminalStorage;
import org.cyclops.integratedterminals.part.PartTypes;

import java.util.Optional;

/**
 * Packet for opening the crafting plan gui.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientOpenCraftingPlanGuiPacket<T, M> extends TerminalStorageIngredientCraftingOptionDataPacketAbstract<T, M> {

    public TerminalStorageIngredientOpenCraftingPlanGuiPacket() {

    }

    public TerminalStorageIngredientOpenCraftingPlanGuiPacket(CraftingOptionGuiData<T, M> craftingOptionData) {
        super(craftingOptionData);
    }

    @Override
    public void actionServer(World world, ServerPlayerEntity player) {
        // Create common data
        CraftingOptionGuiData<T, M> craftingJobGuiData = getCraftingOptionData();

        // Create temporary container provider
        INamedContainerProvider containerProvider = new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new StringTextComponent("");
            }

            @Override
            public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(PartPos.of(world, craftingJobGuiData.getPos(), craftingJobGuiData.getSide()));
                return new ContainerTerminalStorageCraftingPlan(id, playerInventory,
                        Optional.of(data.getRight()), Optional.of(data.getLeft()), (PartTypeTerminalStorage) data.getMiddle(),
                        craftingJobGuiData);
            }
        };

        // Trigger gui opening
        NetworkHooks.openGui(player, containerProvider, packetBuffer -> {
            packetBuffer.writeString(PartTypes.TERMINAL_STORAGE.getUniqueName().toString());
            craftingJobGuiData.writeToPacketBuffer(packetBuffer);
        });
    }

}