package org.cyclops.integratedterminals.capability.ingredient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.storage.IIngredientComponentStorage;
import org.cyclops.cyclopscore.client.gui.RenderItemExtendedSlotCount;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.ingredient.storage.IngredientStorageHelpers;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.client.gui.container.GuiTerminalStorage;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

/**
 * Terminal storage handler for items.
 * @author rubensworks
 */
public class IngredientComponentTerminalStorageHandlerItemStack implements IIngredientComponentTerminalStorageHandler<ItemStack, Integer> {

    private final IngredientComponent<ItemStack, Integer> ingredientComponent;

    public IngredientComponentTerminalStorageHandlerItemStack(IngredientComponent<ItemStack, Integer> ingredientComponent) {
        this.ingredientComponent = ingredientComponent;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Blocks.CHEST);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInstance(ItemStack instance, long maxQuantity, @Nullable String label, GuiContainer gui,
                             GuiTerminalStorage.DrawLayer layer, float partialTick, int x, int y, int mouseX, int mouseY, int channel) {
        RenderItemExtendedSlotCount renderItem = RenderItemExtendedSlotCount.getInstance();
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        if (layer == GuiTerminalStorage.DrawLayer.BACKGROUND) {
            renderItem.renderItemAndEffectIntoGUI(instance, x, y);
            renderItem.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, instance, x, y, label);
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

    @Override
    public int getInitialInstanceMovementQuantity() {
        return GeneralConfig.guiStorageItemInitialQuantity;
    }

    @Override
    public int getIncrementalInstanceMovementQuantity() {
        return GeneralConfig.guiStorageItemIncrementalQuantity;
    }

    @Override
    public int throwIntoWorld(IIngredientComponentStorage<ItemStack, Integer> storage, ItemStack maxInstance,
                              EntityPlayer player) {
        ItemStack extracted = storage.extract(maxInstance, ItemMatch.EXACT, false);
        if (!extracted.isEmpty()) {
            player.dropItem(extracted, true);
        }
        return extracted.getCount();
    }

    @Override
    public void insertMaxIntoPlayerInventory(IIngredientComponentStorage<ItemStack, Integer> storage,
                                             InventoryPlayer playerInventory, ItemStack instance) {
        IIngredientComponentStorage<ItemStack, Integer> playerStorage = storage.getComponent()
                .getStorageWrapperHandler(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .wrapComponentStorage(new PlayerMainInvWrapper(playerInventory));
        IngredientStorageHelpers.moveIngredients(storage, playerStorage, instance,
                ItemMatch.EXACT, false);
    }

    @Override
    public int insertIntoPlayerInventory(IIngredientComponentStorage<ItemStack, Integer> storage,
                                         InventoryPlayer playerInventory, int playerSlot, ItemStack maxInstance) {
        PlayerMainInvWrapper inv = new PlayerMainInvWrapper(playerInventory);
        ItemStack extracted = storage.extract(maxInstance, ItemMatch.EXACT, true);
        ItemStack playerStack = inv.getStackInSlot(playerSlot);
        if (playerStack.isEmpty() || ItemHandlerHelper.canItemStacksStack(extracted, playerStack)) {
            int newCount = Math.min(playerStack.getCount() + extracted.getCount(), extracted.getMaxStackSize());
            int inserted = newCount - playerStack.getCount();
            IIngredientMatcher<ItemStack, Integer> matcher = IngredientComponent.ITEMSTACK.getMatcher();
            storage.extract(matcher.withQuantity(maxInstance, inserted), ItemMatch.EXACT, false);

            inv.setStackInSlot(playerSlot, matcher.withQuantity(maxInstance, newCount));
            playerInventory.player.openContainer.detectAndSendChanges();
            return inserted;
        }
        return 0;
    }

    @Override
    public void extractActiveStackFromPlayerInventory(IIngredientComponentStorage<ItemStack, Integer> storage,
                                                      InventoryPlayer playerInventory) {
        ItemStack playerStack = playerInventory.getItemStack();
        playerInventory.setItemStack(storage.insert(playerStack, false));
    }

    @Override
    public void extractMaxFromPlayerInventorySlot(IIngredientComponentStorage<ItemStack, Integer> storage,
                                                  InventoryPlayer playerInventory, int playerSlot) {
        ItemStack toMove = playerInventory.getStackInSlot(playerSlot);
        if (!toMove.isEmpty()) {
            playerInventory.setInventorySlotContents(playerSlot, storage.insert(toMove, false));
            playerInventory.player.openContainer.detectAndSendChanges();
        }
    }
}
