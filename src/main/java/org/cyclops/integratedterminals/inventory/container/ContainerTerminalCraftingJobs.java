package org.cyclops.integratedterminals.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.cyclopscore.inventory.container.ExtendedInventoryContainer;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;

/**
 * Container for the crafting jobs overview gui.
 * @author rubensworks
 */
public class ContainerTerminalCraftingJobs extends ExtendedInventoryContainer {

    private final World world;
    private final PartTarget target;
    private final IPartContainer partContainer;
    private final IPartType partType;

    /**
     * Make a new instance.
     * @param target The target.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The part type.
     */
    public ContainerTerminalCraftingJobs(final EntityPlayer player, PartTarget target, IPartContainer partContainer,
                                         IPartType partType) {
        super(player.inventory, (IGuiContainerProvider) partType);

        this.target = target;
        this.partContainer = partContainer;
        this.partType = partType;
        this.world = player.world;
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
}
