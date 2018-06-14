package org.cyclops.integratedterminals.part;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.integrateddynamics.core.part.PartStateEmpty;
import org.cyclops.integratedterminals.client.gui.container.GuiTerminalStorage;
import org.cyclops.integratedterminals.core.part.PartTypeTerminal;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;

/**
 * A part that exposes a gui using which players can access storage indexes in the network.
 * @author rubensworks
 */
public class PartTypeTerminalStorage extends PartTypeTerminal<PartTypeTerminalStorage, PartStateEmpty<PartTypeTerminalStorage>> {

    public PartTypeTerminalStorage(String name) {
        super(name);
    }

    @Override
    protected PartStateEmpty<PartTypeTerminalStorage> constructDefaultState() {
        return new PartStateEmpty<>();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Class<? extends GuiScreen> getGui() {
        return GuiTerminalStorage.class;
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerTerminalStorage.class;
    }

}
