package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.integratedterminals.client.gui.container.GuiTerminalStorage;

import javax.annotation.Nullable;

/**
 * A single slot in a storage terminal
 * @author rubensworks
 */
public interface ITerminalStorageSlot {

    @SideOnly(Side.CLIENT)
    public void drawGuiContainerLayer(GuiContainer gui, GuiTerminalStorage.DrawLayer layer,
                                      float partialTick, int x, int y, int mouseX, int mouseY,
                                      ITerminalStorageTabClient tab, int channel, @Nullable String label);

}
