package org.cyclops.integratedterminals.api.ingredient;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;

/**
 * Capability for displaying ingredient components of a certain type.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public interface IIngredientComponentViewHandler<T, M> {

    /**
     * @return The item that can be used to visually represent this ingredient component type.
     */
    public ItemStack getIcon();

    /**
     * Draw the given instance in the given gui.
     * @param instance An instance.
     * @param gui A gui to render in.
     * @param layer The layer to render in.
     * @param partialTick The partial tick.
     * @param x The slot X position.
     * @param y The slot Y position.
     * @param mouseX The mouse X position.
     * @param mouseY The mouse Y position.
     * @param tab The client tab.
     * @param channel The current channel.
     */
    @SideOnly(Side.CLIENT)
    public void drawInstanceSlot(T instance, GuiContainer gui, DrawLayer layer, float partialTick, int x, int y,
                                 int mouseX, int mouseY, ITerminalStorageTabClient tab, int channel);

    public static enum DrawLayer {
        BACKGROUND,
        FOREGROUND
    }

}
