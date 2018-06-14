package org.cyclops.integratedterminals.inventory.container;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.cyclopscore.inventory.container.ExtendedInventoryContainer;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentHandler;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.ingredient.IngredientComponentHandlers;

import java.util.List;
import java.util.Objects;

/**
 * @author rubensworks
 */
public class ContainerTerminalStorage extends ExtendedInventoryContainer {

    private final PartTarget target;
    private final IPartContainer partContainer;
    private final IPartType partType;
    private final List<ITab> tabs;

    private int selectedTabIndexValueId;

    /**
     * Make a new instance.
     * @param target The target.
     * @param player The player.
     * @param partContainer The part container.
     * @param partType The part type.
     */
    public ContainerTerminalStorage(final EntityPlayer player, PartTarget target, IPartContainer partContainer, IPartType partType) {
        super(player.inventory, (IGuiContainerProvider) partType);

        this.target = target;
        this.partContainer = partContainer;
        this.partType = partType;
        this.tabs = Lists.newLinkedList();

        this.selectedTabIndexValueId = getNextValueId();

        addPlayerInventory(player.inventory, 9, 143);

        for (IngredientComponent<?, ?> ingredientComponent : IngredientComponent.REGISTRY.getValuesCollection()) {
            addTab(new TabIngredientComponent(ingredientComponent));
        }

        setSelectedTabIndex(0);
    }

    public void addTab(ITab tab) {
        this.tabs.add(tab);
    }

    public List<ITab> getTabs() {
        return tabs;
    }

    public PartTarget getTarget() {
        return target;
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return PartHelpers.canInteractWith(getTarget(), player, this.partContainer);
    }

    public void setSelectedTabIndex(int selectedTabIndex) {
        ValueNotifierHelpers.setValue(this, selectedTabIndexValueId, selectedTabIndex);
    }

    public int getSelectedTabIndex() {
        return ValueNotifierHelpers.getValueInt(this, selectedTabIndexValueId);
    }

    public static interface ITab {

        public ItemStack getIcon();

        List<String> getTooltip(int mouseX, int mouseY);
    }

    public static class TabIngredientComponent implements ITab {

        private final IngredientComponent<?, ?> ingredientComponent;
        private final IIngredientComponentHandler<?, ?, ?, ?, ?> ingredientComponentHandler;
        private final ItemStack icon;

        public TabIngredientComponent(IngredientComponent<?, ?> ingredientComponent) {
            this.ingredientComponent = ingredientComponent;
            this.ingredientComponentHandler = Objects.requireNonNull(IngredientComponentHandlers.REGISTRY.getComponentHandler(this.ingredientComponent));
            this.icon = ingredientComponentHandler.getIcon();
        }

        @Override
        public ItemStack getIcon() {
            return this.icon;
        }

        @Override
        public List<String> getTooltip(int mouseX, int mouseY) {
            return Lists.newArrayList(L10NHelpers.localize("gui.integratedterminals.terminal_storage.storage_name",
                    L10NHelpers.localize(this.ingredientComponent.getUnlocalizedName())));
        }
    }
}
