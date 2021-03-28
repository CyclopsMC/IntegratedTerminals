package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integratedterminals.RegistryEntries;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.part.PartTypeTerminalStorage;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @author rubensworks
 */
public class ContainerTerminalStorageCraftingOptionAmountPart extends ContainerTerminalStorageCraftingOptionAmountBase<PartPos> {

    // Based on ContainerMultipart

    private final Optional<PartTarget> target;
    private final Optional<IPartContainer> partContainer;
    private final PartTypeTerminalStorage partType;

    public ContainerTerminalStorageCraftingOptionAmountPart(int id, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
        this(id, playerInventory, Optional.empty(), Optional.empty(), PartHelpers.readPart(packetBuffer),
                CraftingOptionGuiData.readFromPacketBuffer(packetBuffer));
    }

    public ContainerTerminalStorageCraftingOptionAmountPart(int id, PlayerInventory playerInventory,
                                                            Optional<PartTarget> target, Optional<IPartContainer> partContainer,
                                                            PartTypeTerminalStorage partType, CraftingOptionGuiData craftingOptionGuiData) {
        this(RegistryEntries.CONTAINER_PART_TERMINAL_STORAGE_CRAFTING_OPTION_AMOUNT_PART, id, playerInventory,
                target, partContainer, partType, craftingOptionGuiData);
    }

    public ContainerTerminalStorageCraftingOptionAmountPart(@Nullable ContainerType<?> type, int id, PlayerInventory playerInventory,
                                                            Optional<PartTarget> target, Optional<IPartContainer> partContainer, PartTypeTerminalStorage partType,
                                                            CraftingOptionGuiData craftingOptionGuiData) {
        super(type, id, playerInventory, craftingOptionGuiData);
        this.target = target;
        this.partType = partType;
        this.partContainer = partContainer;
    }

}
