package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketBuffer;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integratedterminals.RegistryEntries;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.part.PartTypeTerminalStorage;

import java.util.Optional;

/**
 * A container for setting the amount for a given crafting option.
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingOptionAmount extends ContainerMultipart<PartTypeTerminalStorage, PartTypeTerminalStorage.State> {

    private final CraftingOptionGuiData craftingOptionGuiData;

    public ContainerTerminalStorageCraftingOptionAmount(int id, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
        this(id, playerInventory, Optional.empty(), Optional.empty(), PartHelpers.readPart(packetBuffer),
                CraftingOptionGuiData.readFromPacketBuffer(packetBuffer));
    }

    public ContainerTerminalStorageCraftingOptionAmount(int id, PlayerInventory playerInventory,
                                                        Optional<PartTarget> target, Optional<IPartContainer> partContainer,
                                                        PartTypeTerminalStorage partType, CraftingOptionGuiData craftingOptionGuiData) {
        super(RegistryEntries.CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_OPTION_AMOUNT, id, playerInventory, new Inventory(), target, partContainer, partType);

        addPlayerInventory(player.inventory, 9, 80);

        this.craftingOptionGuiData = craftingOptionGuiData;
    }

    public CraftingOptionGuiData getCraftingOptionGuiData() {
        return craftingOptionGuiData;
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

}
