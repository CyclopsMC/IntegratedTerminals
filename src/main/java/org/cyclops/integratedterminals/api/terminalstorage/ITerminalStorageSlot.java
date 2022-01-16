package org.cyclops.integratedterminals.api.terminalstorage;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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
    public void drawGuiContainerLayer(AbstractContainerScreen gui, PoseStack matrixStack, ContainerScreenTerminalStorage.DrawLayer layer,
                                      float partialTick, int x, int y, int mouseX, int mouseY,
                                      ITerminalStorageTabClient tab, int channel, @Nullable String label);

}
