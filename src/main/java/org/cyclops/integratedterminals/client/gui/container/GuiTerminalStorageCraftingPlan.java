package org.cyclops.integratedterminals.client.gui.container;

import com.google.common.collect.Lists;
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
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlan;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingPlan;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingPlan;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientOpenPacket;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientStartCraftingJobPacket;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * A gui for visualizing a crafting plan.
 * @author rubensworks
 */
public class GuiTerminalStorageCraftingPlan extends GuiContainerExtended {

    private final CraftingOptionGuiData<?, ?> craftingOptionGuiData;

    @Nullable
    private GuiCraftingPlan guiCraftingPlan;

    private ITerminalCraftingPlan craftingPlan;

    public GuiTerminalStorageCraftingPlan(EntityPlayer player, PartTarget target, IPartContainer partContainer,
                                          IPartType partType, CraftingOptionGuiData craftingOptionGuiData) {
        super(new ContainerTerminalStorageCraftingPlan(player, target, partContainer, partType, craftingOptionGuiData));

        this.craftingOptionGuiData = craftingOptionGuiData;
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

        if (this.craftingPlan != null) {
            this.guiCraftingPlan = new GuiCraftingPlan(this, this.craftingPlan, guiLeft, guiTop, 9, 18, 10);
        } else {
            this.guiCraftingPlan = null;
        }

        GuiButtonText button;
        this.buttonList.clear();
        this.buttonList.addAll(Lists.newArrayList(
                button = new GuiButtonText(0, guiLeft + 95, guiTop + 198, 50, 20, TextFormatting.BOLD
                        + L10NHelpers.localize("gui.integratedterminals.terminal_storage.step.craft"), true)
        ));
        button.enabled = this.guiCraftingPlan != null && this.guiCraftingPlan.isValid();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!this.checkHotbarKeys(keyCode)) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                returnToTerminalStorage();
            } else if (keyCode == Keyboard.KEY_NUMPADENTER || keyCode == Keyboard.KEY_RETURN) {
                startCraftingJob();
            } else {
                super.keyTyped(typedChar, keyCode);
            }
        }
    }

    private void returnToTerminalStorage() {
        TerminalStorageIngredientOpenPacket.send(craftingOptionGuiData.getPos(), craftingOptionGuiData.getSide(),
                craftingOptionGuiData.getTabName(), craftingOptionGuiData.getChannel());
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
            startCraftingJob();
        }
    }

    private void startCraftingJob() {
        CraftingOptionGuiData<?, ?> craftingOptionData = new CraftingOptionGuiData<>(
                craftingOptionGuiData.getPos(),
                craftingOptionGuiData.getSide(),
                craftingOptionGuiData.getComponent(),
                craftingOptionGuiData.getTabName(),
                craftingOptionGuiData.getChannel(),
                null,
                craftingOptionGuiData.getAmount(),
                new HandlerWrappedTerminalCraftingPlan(craftingOptionGuiData.getCraftingOption().getHandler(), craftingPlan)
        );

        // Send packet to start crafting jon
        IntegratedTerminals._instance.getPacketHandler().sendToServer(
                new TerminalStorageIngredientStartCraftingJobPacket<>(craftingOptionData));

        // Send packet to re-open terminal gui
        TerminalStorageIngredientOpenPacket.send(craftingOptionGuiData.getPos(), craftingOptionGuiData.getSide(),
                craftingOptionGuiData.getTabName(), craftingOptionGuiData.getChannel());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        if (this.guiCraftingPlan != null) {
            guiCraftingPlan.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        } else {
            drawCenteredString(fontRenderer, L10NHelpers.localize("gui.integratedterminals.terminal_storage.step.crafting_plan_calculating"),
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

        if (((ContainerTerminalStorageCraftingPlan) getContainer()).getCraftingPlanNotifierId() == valueId) {
            this.craftingPlan = craftingOptionGuiData.getCraftingOption().getHandler().deserializeCraftingPlan(value);
            this.initGui();
        }
    }
}
