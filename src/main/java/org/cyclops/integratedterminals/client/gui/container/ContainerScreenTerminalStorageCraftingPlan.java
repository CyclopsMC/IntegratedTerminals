package org.cyclops.integratedterminals.client.gui.container;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlanFlat;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlan;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanFlat;
import org.cyclops.integratedterminals.client.gui.container.component.IGuiCraftingPlanView;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingPlanBase;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientOpenCraftingJobAmountGuiPacket;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

/**
 * A gui for previewing a crafting plan.
 * @author rubensworks
 */
public class ContainerScreenTerminalStorageCraftingPlan<L, C extends ContainerTerminalStorageCraftingPlanBase<L>> extends ContainerScreenTerminalCraftingPlanBase<C> {

    private ITerminalCraftingPlan craftingPlan;
    private ITerminalCraftingPlanFlat craftingPlanFlat;
    private ButtonText buttonConfirm;

    public ContainerScreenTerminalStorageCraftingPlan(C container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Nullable
    @Override
    protected ITerminalCraftingPlan getCraftingPlan() {
        return this.craftingPlan;
    }

    @Nullable
    @Override
    protected ITerminalCraftingPlanFlat getCraftingPlanFlat() {
        return this.craftingPlanFlat;
    }

    @Override
    protected IGuiCraftingPlanView createTreeView() {
        return new GuiCraftingPlan(this, this.craftingPlan, leftPos, topPos, 9, 18, 10);
    }

    @Override
    protected IGuiCraftingPlanView createFlatView() {
        return new GuiCraftingPlanFlat(this, this.craftingPlanFlat, leftPos, topPos, 9, 18, 10);
    }

    @Override
    protected boolean isLive() {
        return false;
    }

    @Override
    protected String getEmptyMessage() {
        return "gui.integratedterminals.terminal_storage.step.crafting_plan_calculating";
    }

    @Override
    protected void onEscape() {
        returnToCraftingOptionAmount();
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(new ButtonText(leftPos + imageWidth - 25 - 50 - 55, topPos + imageHeight - 24, 50, 20,
                Component.translatable("gui.integratedterminals.terminal_storage.step.back"),
                Component.translatable("gui.integratedterminals.terminal_storage.step.back"),
                (b) -> returnToCraftingOptionAmount(),
                true));

        addRenderableWidget(Checkbox.builder(Component.translatable("gui.integratedterminals.terminal_storage.step.craft.notify"), font)
                .pos(leftPos + 72, topPos + imageHeight - 22)
                .selected(getMenu().isNotifyOnCompletion())
                .tooltip(Tooltip.create(Component.translatable("gui.integratedterminals.terminal_storage.step.craft.notify.info")))
                .onValueChange((widget, selected) -> getMenu().setNotifyOnCompletion(selected))
                .build());

        addRenderableWidget(buttonConfirm = new ButtonText(leftPos + imageWidth - 25 - 50, topPos + imageHeight - 24, 50, 20,
                Component.translatable("gui.integratedterminals.terminal_storage.step.craft"),
                Component.translatable("gui.integratedterminals.terminal_storage.step.craft").withStyle(ChatFormatting.YELLOW),
                createServerPressable(ContainerTerminalStorageCraftingPlanBase.BUTTON_START, (b) -> {}),
                true));
        buttonConfirm.active = this.activeView != null && this.activeView.isValid();
    }

    @Override
    protected boolean onKeyPressed(KeyEvent evt) {
        if (this.activeView != null && this.activeView.isValid()
                && (evt.key() == GLFW.GLFW_KEY_ENTER || evt.key() == GLFW.GLFW_KEY_KP_ENTER)) {
            buttonConfirm.onPress(evt);
            return true;
        }
        return false;
    }

    /**
     * Go back to the gui in which the crafting amount can be set.
     * If no crafting option is known, the terminal itself is opened again.
     */
    private void returnToCraftingOptionAmount() {
        CraftingOptionGuiData data = getMenu().getCraftingOptionGuiData();
        if (data.getCraftingOption() == null) {
            returnToTerminalStorage();
        } else {
            IntegratedTerminals._instance.getPacketHandler().sendToServer(
                    new TerminalStorageIngredientOpenCraftingJobAmountGuiPacket(getMenu().getPlayerIInventory().player.registryAccess(), data));
        }
    }

    private void returnToTerminalStorage() {
        CraftingOptionGuiData data = getMenu().getCraftingOptionGuiData();
        data.getLocation().openContainerFromClient(data);
    }

    @Override
    public void onUpdate(int valueId, CompoundTag value) {
        super.onUpdate(valueId, value);

        if (getMenu().getCraftingPlanNotifierId() == valueId) {
            this.craftingPlan = IModHelpers.get().getMinecraftHelpers().valueInputFromNbt(value, container.getPlayerIInventory().player.registryAccess(), getMenu().getCraftingOptionGuiData().getCraftingOption().getHandler()::deserializeCraftingPlan);
            this.guiCraftingPlanToggler.setCraftingPlanDisplayMode(null);
            this.init();
        }
        if (getMenu().getCraftingPlanFlatNotifierId() == valueId) {
            this.craftingPlanFlat = IModHelpers.get().getMinecraftHelpers().valueInputFromNbt(value, container.getPlayerIInventory().player.registryAccess(), getMenu().getCraftingOptionGuiData().getCraftingOption().getHandler()::deserializeCraftingPlanFlat);
            this.guiCraftingPlanToggler.setCraftingPlanDisplayMode(null);
            this.init();
        }
    }
}
