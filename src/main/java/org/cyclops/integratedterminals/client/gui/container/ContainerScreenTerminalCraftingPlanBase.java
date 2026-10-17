package org.cyclops.integratedterminals.client.gui.container;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.inventory.container.ContainerExtended;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlanFlat;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanButtons;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanGraph;
import org.cyclops.integratedterminals.client.gui.container.component.GuiCraftingPlanToggler;
import org.cyclops.integratedterminals.client.gui.container.component.IGuiCraftingPlanView;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

/**
 * Base gui for showing a crafting plan in one of the crafting plan views.
 * @author rubensworks
 */
public abstract class ContainerScreenTerminalCraftingPlanBase<C extends ContainerExtended> extends ContainerScreenExtended<C> {

    private static final Identifier TEXTURE_PANEL = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/part_terminal_storage.png");

    protected final GuiCraftingPlanToggler guiCraftingPlanToggler;

    @Nullable
    protected IGuiCraftingPlanView activeView;
    // The X position after the view buttons in the bottom row
    protected int viewButtonsRight;
    // Kept so that its visualization state can be inherited after an update
    @Nullable
    private GuiCraftingPlanGraph guiCraftingPlanGraph;

    public ContainerScreenTerminalCraftingPlanBase(C container, Inventory inventory, Component title) {
        super(container, inventory, title);

        this.guiCraftingPlanToggler = new GuiCraftingPlanToggler(
                this::getCraftingPlan,
                this::getCraftingPlanFlat,
                () -> setActiveView(createTreeView()),
                this::initGraphView,
                () -> setActiveView(createFlatView()),
                () -> this.activeView = null
        );
    }

    /**
     * @return The plan to show in the tree and graph views.
     */
    @Nullable
    protected abstract ITerminalCraftingPlan getCraftingPlan();

    /**
     * @return The plan to show in the flat view.
     */
    @Nullable
    protected abstract ITerminalCraftingPlanFlat getCraftingPlanFlat();

    protected abstract IGuiCraftingPlanView createTreeView();

    protected abstract IGuiCraftingPlanView createFlatView();

    /**
     * @return If the plan is of a running job.
     */
    protected abstract boolean isLive();

    /**
     * @return The unlocalized message shown when there is no plan to show.
     */
    protected abstract String getEmptyMessage();

    /**
     * Called when escape is pressed.
     */
    protected abstract void onEscape();

    /**
     * Handle a key press that was not handled by the active view.
     * @param evt The key event.
     * @return If the key press was handled.
     */
    protected boolean onKeyPressed(KeyEvent evt) {
        return false;
    }

    protected void initGraphView() {
        GuiCraftingPlanGraph previous = this.guiCraftingPlanGraph;
        ITerminalCraftingPlan craftingPlan = getCraftingPlan();
        // Live plans change on each update, previews only keep their state for the same plan
        if (previous != null && !isLive() && previous.getCraftingPlan() != craftingPlan) {
            previous = null;
        }
        this.guiCraftingPlanGraph = new GuiCraftingPlanGraph(this, craftingPlan, leftPos, topPos, 9, 18,
                imageWidth - 18, imageHeight - 44, isLive(), previous);
        // Overlay buttons must be added before the graph, so that they receive clicks first
        for (Button button : GuiCraftingPlanButtons.createGraphButtons(this.guiCraftingPlanGraph, this::init)) {
            addRenderableWidget(button);
        }
        setActiveView(this.guiCraftingPlanGraph);
    }

    protected void setActiveView(IGuiCraftingPlanView view) {
        this.activeView = view;
        addRenderableWidget(view);
    }

    @Override
    protected Identifier constructGuiTexture() {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/crafting_plan.png");
    }

    @Override
    public Identifier getGuiTexture() {
        return this.guiCraftingPlanToggler.getCraftingPlanDisplayMode() == GuiCraftingPlanToggler.CraftingPlanDisplayMode.FLAT ? Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/crafting_plan_flat.png") : super.getGuiTexture();
    }

    @Override
    public int getBaseXSize() {
        return this.guiCraftingPlanToggler.getWidth(this.width);
    }

    @Override
    public int getBaseYSize() {
        return this.guiCraftingPlanToggler.getHeight(this.height);
    }

    @Override
    public void init() {
        // The display mode determines the gui size
        this.guiCraftingPlanToggler.resolveDisplayMode();
        super.init();

        // Reset states
        this.renderables.clear();
        this.children().clear();
        this.activeView = null;

        this.viewButtonsRight = leftPos + 8;
        for (Button button : GuiCraftingPlanButtons.createViewButtons(this.guiCraftingPlanToggler, leftPos + 8, topPos + imageHeight - 23, this::init)) {
            addRenderableWidget(button);
            this.viewButtonsRight = Math.max(this.viewButtonsRight, button.getX() + button.getWidth());
        }

        this.guiCraftingPlanToggler.init();
    }

    @Override
    public boolean keyPressed(KeyEvent evt) {
        if (evt.key() == GLFW.GLFW_KEY_ESCAPE) {
            onEscape();
            return true;
        }
        if (this.activeView != null && this.activeView.keyPressed(evt)) {
            return true;
        }
        if (onKeyPressed(evt)) {
            return true;
        }
        return super.keyPressed(evt);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (this.guiCraftingPlanToggler.getCraftingPlanDisplayMode() == GuiCraftingPlanToggler.CraftingPlanDisplayMode.GRAPH) {
            ContainerScreenTerminalStorage.drawPanel(guiGraphics, TEXTURE_PANEL, leftPos, topPos, imageWidth, imageHeight);
        } else {
            super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        }
        if (this.activeView != null) {
            activeView.drawGuiContainerBackgroundLayer(guiGraphics, partialTicks, mouseX, mouseY);
        } else {
            guiGraphics.centeredText(font, IModHelpers.get().getL10NHelpers().localize(getEmptyMessage()),
                    leftPos + imageWidth / 2, topPos + 23, ARGB.opaque(16777215));
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (this.activeView != null) {
            activeView.drawGuiContainerForegroundLayer(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    protected void drawCurrentScreen(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawCurrentScreen(guiGraphics, mouseX, mouseY, partialTicks);
        if (this.activeView != null) {
            activeView.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean charTyped(CharacterEvent evt) {
        if (this.activeView != null && this.activeView.charTyped(evt)) {
            return true;
        }
        return super.charTyped(evt);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double mouseZ, double delta) {
        if (this.activeView != null) {
            return activeView.mouseScrolled(mouseX, mouseY, mouseZ, delta);
        }
        return super.mouseScrolled(mouseX, mouseY, mouseZ, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouse, boolean isDoubleClick) {
        // The views are not hit-tested by the default event routing, so they need to be notified explicitly
        if (this.activeView != null && activeView.mouseClicked(mouse, isDoubleClick)) {
            return true;
        }
        return super.mouseClicked(mouse, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouse, double mouseXPrev, double mouseYPrev) {
        if (this.activeView != null) {
            return activeView.mouseDragged(mouse, mouseXPrev, mouseYPrev);
        }
        return super.mouseDragged(mouse, mouseXPrev, mouseYPrev);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mouse) {
        // The views are not hit-tested by the default event routing, so they need to be notified explicitly
        boolean handled = this.activeView != null && activeView.mouseReleased(mouse);
        return super.mouseReleased(mouse) || handled;
    }
}
