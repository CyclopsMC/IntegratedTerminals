package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Lists;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.CraftingHelpers;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.core.terminalstorage.button.TerminalButtonItemStackCraftingGridAutoRefill;
import org.cyclops.integratedterminals.inventory.InventoryCraftingDirtyable;
import org.cyclops.integratedterminals.inventory.SlotCraftingAutoRefill;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;
import org.cyclops.integratedterminals.network.packet.TerminalStorageIngredientItemStackCraftingGridSetResult;

import java.util.List;
import java.util.Optional;

import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon.IVariableInventory;

/**
 * A common-side storage terminal ingredient tab for crafting with {@link ItemStack} instances.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientComponentItemStackCraftingCommon
        extends TerminalStorageTabIngredientComponentCommon<ItemStack, Integer> {

    private CraftingContainer inventoryCrafting;
    private ResultContainer inventoryCraftResult;
    private ResultSlot slotCrafting;
    private List<Slot> slots;
    private TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType autoRefill = TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType.STORAGE;

    public TerminalStorageTabIngredientComponentItemStackCraftingCommon(ContainerTerminalStorageBase containerTerminalStorage,
                                                                        ResourceLocation name,
                                                                        IngredientComponent<ItemStack, Integer> ingredientComponent) {
        super(containerTerminalStorage, name, ingredientComponent);
    }

    public static int getCraftingResultSlotIndex(AbstractContainerMenu container, ResourceLocation name) {
        ITerminalStorageTabCommon tabCommon = ((ContainerTerminalStorageBase) container).getTabCommon(name.toString());
        TerminalStorageTabIngredientComponentItemStackCraftingCommon tabCommonCrafting =
                (TerminalStorageTabIngredientComponentItemStackCraftingCommon) tabCommon;
        return tabCommonCrafting.getSlotCrafting().index;
    }

    @Override
    public List<Slot> loadSlots(AbstractContainerMenu container, int startIndex, Player player,
                                Optional<IVariableInventory> variableInventoryOptional) {
        IVariableInventory variableInventory = variableInventoryOptional.get();
        slots = Lists.newArrayListWithCapacity(10);

        // Reload the recipe when the input slots are updated
        IDirtyMarkListener dirtyListener = () -> updateCraftingResult(player, container, variableInventory);

        this.inventoryCraftResult = new ResultContainer() {
            @Override
            public void setChanged() {
                dirtyListener.onDirty();
                super.setChanged();
            }
        };
        this.inventoryCrafting = new InventoryCraftingDirtyable(container, 3, 3, dirtyListener);

        slots.add(slotCrafting = new SlotCraftingAutoRefill(player, this.inventoryCrafting, this.inventoryCraftResult,
                0, 115, 76, this, (TerminalStorageTabIngredientComponentServer<ItemStack, Integer>)
                ((ContainerTerminalStorageBase) container).getTabServer(getName().toString()),
                (ContainerTerminalStorageBase) container));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                slots.add(new Slot(this.inventoryCrafting, j + i * 3, 31 + j * 18, 58 + i * 18));
            }
        }

        // Load the items that were stored in the part state into the crafting grid slots
        NonNullList<ItemStack> tabItems = variableInventory.getNamedInventory(this.getName().toString());
        if (tabItems != null) {
            int i = 0;
            for (ItemStack tabItem : tabItems) {
                if (i == 0) {
                    this.inventoryCraftResult.setItem(i++, tabItem);
                } else {
                    this.inventoryCrafting.setItem(i++ - 1, tabItem);
                }
            }
        }

        List<Slot> returnSlots = Lists.newArrayList(slots);
        for (Triple<Slot, Integer, Integer> slot : ((ContainerTerminalStorageBase<?>) container).getTabSlots(ingredientComponent.getName().toString())) {
            returnSlots.add(slot.getLeft());
        }
        return returnSlots;
    }

    public CraftingContainer getInventoryCrafting() {
        return inventoryCrafting;
    }

    public ResultContainer getInventoryCraftResult() {
        return inventoryCraftResult;
    }

    public ResultSlot getSlotCrafting() {
        return slotCrafting;
    }

    public TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType getAutoRefill() {
        return autoRefill;
    }

    public void setAutoRefill(TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType autoRefill) {
        this.autoRefill = autoRefill;
    }

    public void updateCraftingResult(Player player, AbstractContainerMenu container,
                                     ITerminalStorageTabCommon.IVariableInventory variableInventory) {
        if (!player.level.isClientSide) {
            ServerPlayer entityplayermp = (ServerPlayer)player;
            ItemStack itemstack = ItemStack.EMPTY;
            CraftingRecipe recipe = CraftingHelpers.findServerRecipe(RecipeType.CRAFTING, inventoryCrafting, player.level).orElse(null);

            if (recipe != null && (recipe.isSpecial()
                    || !player.level.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING)
                    || entityplayermp.getRecipeBook().contains(recipe))) {
                inventoryCraftResult.setRecipeUsed(recipe);
                itemstack = recipe.assemble(inventoryCrafting);
            }

            inventoryCraftResult.setItem(0, itemstack);
            IntegratedTerminals._instance.getPacketHandler().sendToPlayer(
                    new TerminalStorageIngredientItemStackCraftingGridSetResult(getName().toString(), itemstack),
                    (ServerPlayer) player);
        }

        // Save changes into the part state
        NonNullList<ItemStack> latestItems = NonNullList.create();
        for (Slot slot : slots) {
            latestItems.add(slot.getItem());
        }
        variableInventory.setNamedInventory(this.getName().toString(), latestItems);
    }
}
