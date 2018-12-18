package org.cyclops.integratedterminals.client.gui.container;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonText;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerExtended;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlan;
import org.cyclops.integratedterminals.core.client.gui.CraftingJobGuiData;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalCraftingJobsPlan;
import org.cyclops.integratedterminals.network.packet.OpenCraftingJobsGuiPacket;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * A gui for visualizing a live crafting plan.
 * @author rubensworks
 */
public class GuiTerminalCraftingJobsPlan extends GuiContainerExtended {

    private final EntityPlayer player;

    @Nullable
    private GuiCraftingPlan guiCraftingPlan;

    public GuiTerminalCraftingJobsPlan(EntityPlayer player, PartTarget target, IPartContainer partContainer,
                                       IPartType partType, CraftingJobGuiData craftingPlanGuiData) {
        super(new ContainerTerminalCraftingJobsPlan(player, target, partContainer, partType, craftingPlanGuiData));

        this.player = player;
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
    public void initGui() {
        super.initGui();

        this.buttonList.clear();

        ITerminalCraftingPlan craftingPlan = getContainer().getCraftingPlan();
        if (craftingPlan != null) {
            this.guiCraftingPlan = new GuiCraftingPlan(this, craftingPlan, guiLeft, guiTop, 9, 18, 10);
            // TODO: remember state? or just update the inner plan instead of recreating?

            this.buttonList.add(new GuiButtonText(0, guiLeft + 70, guiTop + 198, 100, 20, TextFormatting.BOLD
                    + L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.cancel"), true)
            );
        } else {
            this.guiCraftingPlan = null;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!this.checkHotbarKeys(keyCode)) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                returnToOverview();
            } else {
                super.keyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    protected ContainerTerminalCraftingJobsPlan getContainer() {
        return (ContainerTerminalCraftingJobsPlan) super.getContainer();
    }

    private void returnToOverview() {
        PartPos center = getContainer().getTarget().getCenter();
        OpenCraftingJobsGuiPacket.send(center.getPos().getBlockPos(), center.getSide());
    }

    @Override
    public boolean requiresAction(int buttonId) {
        return true;
    }

    @Override
    public void onButtonClick(int buttonId) {
        super.onButtonClick(buttonId);
        GuiButton button = buttonList.get(buttonId);
        if (button instanceof GuiButtonText) {
            cancelCraftingJob();
        }
    }

    private void cancelCraftingJob() {
        // Send packet to cancel crafting job
        // TODO

        // Return to overview
        returnToOverview();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        } else {
            drawCenteredString(fontRenderer, L10NHelpers.localize("gui.integratedterminals.terminal_crafting_job.craftingplan.empty"),
                    guiLeft + getBaseXSize() / 2, guiTop + 23, 16777215);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.drawGuiContainerForegroundLayer(mouseX, mouseY);
        }
    }

    @Override
    protected void drawCurrentScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawCurrentScreen(mouseX, mouseY, partialTicks);
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.drawCurrentScreen(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.handleMouseInput();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void onUpdate(int valueId, NBTTagCompound value) {
        super.onUpdate(valueId, value);

        if (getContainer().getCraftingPlanNotifierId() == valueId) {
            this.initGui();
        }
    }
}
