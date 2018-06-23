package org.cyclops.integratedterminals.capability.ingredient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentViewHandler;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.lwjgl.opengl.GL11;

/**
 * View handler for items.
 * @author rubensworks
 */
public class IngredientComponentViewHandlerItemStack implements IIngredientComponentViewHandler<ItemStack, Integer> {

    private final IngredientComponent<ItemStack, Integer> ingredientComponent;

    public IngredientComponentViewHandlerItemStack(IngredientComponent<ItemStack, Integer> ingredientComponent) {
        this.ingredientComponent = ingredientComponent;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Blocks.CHEST);
    }

    @Override
    public void drawInstanceSlot(ItemStack instance, GuiContainer gui, DrawLayer layer, float partialTick, int x, int y,
                                 int mouseX, int mouseY, ITerminalStorageTabClient tab, int channel) {
        RenderItemExtendedSlotCount renderItem = RenderItemExtendedSlotCount.getInstance();
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        if (layer == DrawLayer.BACKGROUND) {
            renderItem.renderItemAndEffectIntoGUI(instance, x, y);
            renderItem.renderItemOverlays(Minecraft.getMinecraft().fontRenderer, instance, x, y);
        } else {
            net.minecraftforge.fml.client.config.GuiUtils.preItemToolTip(instance);
            GuiHelpers.renderTooltip(gui, x, y, GuiHelpers.SLOT_SIZE_INNER, GuiHelpers.SLOT_SIZE_INNER, mouseX, mouseY, () -> instance.getTooltip(
                    Minecraft.getMinecraft().player, Minecraft.getMinecraft().gameSettings.advancedItemTooltips
                            ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL));
            net.minecraftforge.fml.client.config.GuiUtils.postItemToolTip();
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }
}
