package org.cyclops.integratedterminals.client.gui.container;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerExtended;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

/**
 * @author rubensworks
 */
public class GuiTerminalStorage extends GuiContainerExtended {

    private static int TAB_OFFSET_X = 2;
    private static int TAB_WIDTH = 24;
    private static int TAB_UNSELECTED_HEIGHT = 21;
    private static int TAB_SELECTED_HEIGHT = 24;
    private static int TAB_ICON_OFFSET = 4;
    private static int TAB_UNSELECTED_TEXTURE_X = 0;
    private static int TAB_SELECTED_TEXTURE_X = 24;
    private static int TAB_UNSELECTED_TEXTURE_Y = 225;
    private static int TAB_SELECTED_TEXTURE_Y = 225;

    public GuiTerminalStorage(EntityPlayer player, PartTarget target, IPartContainer partContainer, IPartType partType) {
        super(new ContainerTerminalStorage(player, target, partContainer, partType));
    }

    @Override
    protected ResourceLocation constructResourceLocation() {
        return new ResourceLocation(Reference.MOD_ID, this.getGuiTexture());
    }

    @Override
    public String getGuiTexture() {
        return IntegratedTerminals._instance.getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI)
                + "part_terminal_storage.png";
    }

    @Override
    public int getBaseXSize() {
        return 196;
    }

    @Override
    public int getBaseYSize() {
        return 225;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        drawTabs();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        drawTabsOverlay(mouseX, mouseY);
    }

    @Override
    protected ContainerTerminalStorage getContainer() {
        return (ContainerTerminalStorage) super.getContainer();
    }

    protected void drawTabs() {
        int offsetX = TAB_OFFSET_X;
        int i = 0;
        // Draw all tabs next to each other horizontally
        for (ContainerTerminalStorage.ITab tab : getContainer().getTabs()) {
            boolean selected = i == getContainer().getSelectedTabIndex();
            int x = getGuiLeft() + offsetX;
            int y = getGuiTop();
            int width = TAB_WIDTH;
            int height = selected ? TAB_SELECTED_HEIGHT : TAB_UNSELECTED_HEIGHT;
            int textureX = selected ? TAB_SELECTED_TEXTURE_X : TAB_UNSELECTED_TEXTURE_X;
            int textureY = selected ? TAB_SELECTED_TEXTURE_Y : TAB_UNSELECTED_TEXTURE_Y;

            // Draw background
            this.mc.renderEngine.bindTexture(this.texture);
            this.drawTexturedModalRect(x, y, textureX, textureY, width, height);

            // Draw icon
            ItemStack icon = tab.getIcon();
            RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableRescaleNormal();
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            renderItem.renderItemAndEffectIntoGUI(icon, x + TAB_ICON_OFFSET, y + TAB_ICON_OFFSET);
            renderItem.renderItemOverlays(Minecraft.getMinecraft().fontRenderer, icon, x + TAB_ICON_OFFSET, y + TAB_ICON_OFFSET);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            offsetX += width;
            i++;
        }
    }

    protected void drawTabsOverlay(int mouseX, int mouseY) {
        if (mouseY < getGuiTop() + TAB_UNSELECTED_HEIGHT
                && mouseX > getGuiLeft() + TAB_OFFSET_X && mouseX <= getGuiLeft() + (TAB_WIDTH * getContainer().getTabs().size())) {
            int tabIndex = (mouseX - TAB_OFFSET_X - getGuiLeft()) / TAB_WIDTH;
            ContainerTerminalStorage.ITab tab = getContainer().getTabs().get(tabIndex);
            this.drawTooltip(tab.getTooltip(mouseX, mouseY), mouseX - getGuiLeft(), mouseY - getGuiTop());
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0
                && mouseY < getGuiTop() + TAB_UNSELECTED_HEIGHT
                && mouseX > getGuiLeft() + TAB_OFFSET_X && mouseX <= getGuiLeft() + (TAB_WIDTH * getContainer().getTabs().size())) {
            getContainer().setSelectedTabIndex((mouseX - TAB_OFFSET_X - getGuiLeft()) / TAB_WIDTH);
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
