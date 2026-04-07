package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;

import javax.annotation.Nullable;

/**
 * A single slot in a storage terminal
 * @author rubensworks
 */
public interface ITerminalStorageSlot {

    public void drawGuiContainerLayer(AbstractContainerScreen gui, GuiGraphicsExtractor guiGraphics, ContainerScreenTerminalStorage.DrawLayer layer,
                                      float partialTick, int x, int y, int mouseX, int mouseY,
                                      ITerminalStorageTabClient tab, int channel, @Nullable String label);

}
