package org.cyclops.integratedterminals.core.terminalstorage;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameRules;
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

/**
 * A common-side storage terminal ingredient tab for crafting with {@link ItemStack} instances.
 * @author rubensworks
 */
public class TerminalStorageTabIngredientComponentItemStackCraftingCommon
        extends TerminalStorageTabIngredientComponentCommon<ItemStack, Integer> {

    private CraftingInventory inventoryCrafting;
    private CraftResultInventory inventoryCraftResult;
    private CraftingResultSlot slotCrafting;
    private List<Slot> slots;
    private TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType autoRefill = TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType.STORAGE;

    public TerminalStorageTabIngredientComponentItemStackCraftingCommon(ContainerTerminalStorageBase containerTerminalStorage,
                                                                        ResourceLocation name,
                                                                        IngredientComponent<ItemStack, Integer> ingredientComponent) {
        super(containerTerminalStorage, name, ingredientComponent);
    }

    public static int getCraftingResultSlotIndex(Container container, ResourceLocation name) {
        ITerminalStorageTabCommon tabCommon = ((ContainerTerminalStorageBase) container).getTabCommon(name.toString());
        TerminalStorageTabIngredientComponentItemStackCraftingCommon tabCommonCrafting =
                (TerminalStorageTabIngredientComponentItemStackCraftingCommon) tabCommon;
        return tabCommonCrafting.getSlotCrafting().slotNumber;
    }

    @Override
    public List<Slot> loadSlots(Container container, int startIndex, PlayerEntity player,
                                Optional<IVariableInventory> variableInventoryOptional) {
        IVariableInventory variableInventory = variableInventoryOptional.get();
        slots = Lists.newArrayListWithCapacity(10);

        // Reload the recipe when the input slots are updated
        IDirtyMarkListener dirtyListener = () -> updateCraftingResult(player, container, variableInventory);

        this.inventoryCraftResult = new CraftResultInventory() {
            @Override
            public void markDirty() {
                dirtyListener.onDirty();
                super.markDirty();
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
                    this.inventoryCraftResult.setInventorySlotContents(i++, tabItem);
                } else {
                    this.inventoryCrafting.setInventorySlotContents(i++ - 1, tabItem);
                }
            }
        }

        List<Slot> returnSlots = Lists.newArrayList(slots);
        for (Triple<Slot, Integer, Integer> slot : ((ContainerTerminalStorageBase<?>) container).getTabSlots(ingredientComponent.getName().toString())) {
            returnSlots.add(slot.getLeft());
        }
        return returnSlots;
    }

    public CraftingInventory getInventoryCrafting() {
        return inventoryCrafting;
    }

    public CraftResultInventory getInventoryCraftResult() {
        return inventoryCraftResult;
    }

    public CraftingResultSlot getSlotCrafting() {
        return slotCrafting;
    }

    public TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType getAutoRefill() {
        return autoRefill;
    }

    public void setAutoRefill(TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType autoRefill) {
        this.autoRefill = autoRefill;
    }

    public void updateCraftingResult(PlayerEntity player, Container container,
                                     ITerminalStorageTabCommon.IVariableInventory variableInventory) {
        if (!player.world.isRemote) {
            ServerPlayerEntity entityplayermp = (ServerPlayerEntity)player;
            ItemStack itemstack = ItemStack.EMPTY;
            ICraftingRecipe recipe = CraftingHelpers.findRecipeCached(IRecipeType.CRAFTING, inventoryCrafting, player.world, false).orElse(null);

            if (recipe != null && (recipe.isDynamic()
                    || !player.world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING)
                    || entityplayermp.getRecipeBook().isUnlocked(recipe))) {
                inventoryCraftResult.setRecipeUsed(recipe);
                itemstack = recipe.getCraftingResult(inventoryCrafting);
            }

            inventoryCraftResult.setInventorySlotContents(0, itemstack);
            IntegratedTerminals._instance.getPacketHandler().sendToPlayer(
                    new TerminalStorageIngredientItemStackCraftingGridSetResult(getName().toString(), itemstack),
                    (ServerPlayerEntity) player);
        }

        // Save changes into the part state
        NonNullList<ItemStack> latestItems = NonNullList.create();
        for (Slot slot : slots) {
            latestItems.add(slot.getStack());
        }
        variableInventory.setNamedInventory(this.getName().toString(), latestItems);
    }
}
