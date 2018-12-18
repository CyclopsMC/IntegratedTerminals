package org.cyclops.integratedterminals.client.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerExtended;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalCraftingJobs;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorage;

import java.io.IOException;

/**
 * The crafting jobs overview gui.
 * @author rubensworks
 */
public class GuiTerminalCraftingJobs extends GuiContainerExtended {

    public GuiTerminalCraftingJobs(EntityPlayer player, PartTarget target, IPartContainer partContainer,
                                   IPartType partType) {
        super(new ContainerTerminalCraftingJobs(player, target, partContainer, partType));
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    @Override
    protected ResourceLocation constructResourceLocation() {
        return new ResourceLocation(Reference.MOD_ID, this.getGuiTexture());
    }

    @Override
    public String getGuiTexture() {
        return IntegratedTerminals._instance.getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI)
                + "crafting_plan.png";
    }

    @Override
    public int getBaseXSize() {
        return 256;
    }

    @Override
    public int getBaseYSize() {
        return 222;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawCurrentScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawCurrentScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public ContainerTerminalStorage getContainer() {
        return (ContainerTerminalStorage) super.getContainer();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

}
