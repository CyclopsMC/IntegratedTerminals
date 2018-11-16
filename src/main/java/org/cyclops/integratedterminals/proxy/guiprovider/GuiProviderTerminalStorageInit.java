package org.cyclops.integratedterminals.proxy.guiprovider;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.integratedterminals.client.gui.container.GuiTerminalStorage;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;

/**
 * @author rubensworks
 */
public class GuiProviderTerminalStorageInit implements IGuiContainerProvider {

    private final int guiID;
    private final ModBase modGui;

    public GuiProviderTerminalStorageInit(int guiID, ModBase modGui) {
        this.guiID = guiID;
        this.modGui = modGui;
    }

    @Override
    public int getGuiID() {
        return guiID;
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerTerminalStorage.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GuiScreen> getGui() {
        return GuiTerminalStorage.class;
    }

    @Override
    public ModBase getModGui() {
        return modGui;
    }

}
