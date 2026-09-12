package org.cyclops.integratedterminals.client.gui.tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integratedterminals.Capabilities;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;

import javax.annotation.Nullable;

/**
 * Base class for tooltip components that render a grid of slots.
 *
 * The grid wraps after {@link #MAX_COLUMNS} slots,
 * and is spread evenly over as few rows as possible.
 *
 * @author rubensworks
 */
public abstract class ClientCraftingOptionSlotsTooltip implements ClientTooltipComponent {

    protected static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot");
    protected static final int SLOT_WIDTH = 18;
    protected static final int SLOT_HEIGHT = 18;
    protected static final int MAX_COLUMNS = 9;
    protected static final int MARGIN_Y = 2;

    /**
     * The number of ticks an alternative is shown before cycling to the next one.
     */
    protected static final int TICK_DELAY = 30;

    private final int slots;
    private final int columns;
    private final int rows;

    public ClientCraftingOptionSlotsTooltip(int slots) {
        this.slots = slots;
        this.rows = Math.max(1, (int) Math.ceil((double) slots / MAX_COLUMNS));
        this.columns = Math.max(1, (int) Math.ceil((double) slots / this.rows));
    }

    /**
     * Draw the contents of the slot at the given index.
     * @param guiGraphics The gui graphics.
     * @param slot The slot index.
     * @param x The X position to draw at.
     * @param y The Y position to draw at.
     */
    protected abstract void drawSlot(GuiGraphicsExtractor guiGraphics, int slot, int x, int y);

    @Override
    public int getHeight(Font font) {
        return this.rows * SLOT_HEIGHT + MARGIN_Y;
    }

    @Override
    public int getWidth(Font font) {
        return this.columns * SLOT_WIDTH;
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor guiGraphics) {
        for (int i = 0; i < this.slots; i++) {
            int slotX = x + (i % this.columns) * SLOT_WIDTH;
            int slotY = y + (i / this.columns) * SLOT_HEIGHT;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, slotX, slotY, SLOT_WIDTH, SLOT_HEIGHT);
            drawSlot(guiGraphics, i, slotX + 1, slotY + 1);
        }
    }

    protected static <T, M> void drawIngredient(GuiGraphicsExtractor guiGraphics, IPrototypedIngredient<T, M> ingredient, int x, int y) {
        IngredientComponent<T, M> ingredientComponent = ingredient.getComponent();
        long quantity = ingredientComponent.getMatcher().getQuantity(ingredient.getPrototype());
        ingredientComponent.getCapability(Capabilities.IngredientComponentTerminalStorageHandler.INGREDIENT)
                .ifPresent(handler -> handler.getClient().drawInstance(guiGraphics, ingredient.getPrototype(), quantity, null,
                        getContainerScreen(), ContainerScreenTerminalStorage.DrawLayer.BACKGROUND, 0,
                        x, y, 0, 0, null, null));
    }

    @Nullable
    protected static AbstractContainerScreen<?> getContainerScreen() {
        Screen screen = Minecraft.getInstance().gui.screen();
        return screen instanceof AbstractContainerScreen<?> containerScreen ? containerScreen : null;
    }

    protected static int getTick() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? 0 : (int) (minecraft.level.getGameTime() / TICK_DELAY);
    }

}
