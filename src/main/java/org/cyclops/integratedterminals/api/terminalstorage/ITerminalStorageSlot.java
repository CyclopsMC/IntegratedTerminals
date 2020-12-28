package org.cyclops.integratedterminals.api.terminalstorage;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;

import javax.annotation.Nullable;

/**
 * A single slot in a storage terminal
 * @author rubensworks
 */
public interface ITerminalStorageSlot {

    @OnlyIn(Dist.CLIENT)
    public void drawGuiContainerLayer(ContainerScreen gui, MatrixStack matrixStack, ContainerScreenTerminalStorage.DrawLayer layer,
                                      float partialTick, int x, int y, int mouseX, int mouseY,
                                      ITerminalStorageTabClient tab, int channel, @Nullable String label);

}
