package org.cyclops.integratedterminals.api.terminalstorage;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentViewHandler;

/**
 * A single slot in a storage terminal
 * @author rubensworks
 */
@SideOnly(Side.CLIENT)
public interface ITerminalStorageSlot<T, M> {

    public void drawGuiContainerLayer(GuiContainer gui, IIngredientComponentViewHandler.DrawLayer layer,
                                      float partialTick, int x, int y, int mouseX, int mouseY,
                                      ITerminalStorageTabClient tab, int channel);

}
