package org.cyclops.integratedterminals.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.integratedterminals.Capabilities;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;

/**
 * Game tests for the client-side simulation of storage terminal clicks.
 *
 * These run the same movement logic that the server runs when it handles a click,
 * so that what is predicted is what the server will do.
 *
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestTerminalStorageClickPredictions {

    /**
     * The player inventory slots in the player's own container.
     */
    private static final int SLOT_START = 9;
    private static final int SLOT_END = 45;

    private static IIngredientComponentTerminalStorageHandler<ItemStack, Integer> getHandler() {
        return IngredientComponents.ITEMSTACK
                .getCapability(Capabilities.IngredientComponentTerminalStorageHandler.INGREDIENT)
                .orElseThrow(() -> new IllegalStateException("Could not find an ingredient terminal storage handler"));
    }

    private static AbstractContainerMenu createMenu(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().clearContent();
        return player.inventoryMenu;
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testQuickMoveMovesOneStack(GameTestHelper helper) {
        AbstractContainerMenu menu = createMenu(helper);

        ItemStack moved = getHandler().predictInsertMaxIntoContainer(menu, SLOT_START, SLOT_END,
                new ItemStack(Items.STONE, 500), 500);

        helper.assertTrue(moved.getCount() == 64, "One stack should be moved, but was " + moved.getCount());
        helper.assertTrue(menu.getSlot(SLOT_START).getItem().getCount() == 64,
                "The first slot should hold one stack");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testQuickMoveIsLimitedByTheStorage(GameTestHelper helper) {
        AbstractContainerMenu menu = createMenu(helper);

        ItemStack moved = getHandler().predictInsertMaxIntoContainer(menu, SLOT_START, SLOT_END,
                new ItemStack(Items.STONE, 10), 10);

        helper.assertTrue(moved.getCount() == 10, "Only the available quantity should be moved");
        helper.assertTrue(menu.getSlot(SLOT_START).getItem().getCount() == 10,
                "The first slot should hold the available quantity");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testQuickMoveWithoutStorageContentsMovesNothing(GameTestHelper helper) {
        AbstractContainerMenu menu = createMenu(helper);

        ItemStack moved = getHandler().predictInsertMaxIntoContainer(menu, SLOT_START, SLOT_END,
                new ItemStack(Items.STONE, 64), 0);

        helper.assertTrue(moved.isEmpty(), "Nothing should be moved");
        helper.assertTrue(menu.getSlot(SLOT_START).getItem().isEmpty(), "The first slot should stay empty");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testQuickMoveFillsPartialStacks(GameTestHelper helper) {
        AbstractContainerMenu menu = createMenu(helper);
        menu.getSlot(SLOT_START).set(new ItemStack(Items.STONE, 60));

        ItemStack moved = getHandler().predictInsertMaxIntoContainer(menu, SLOT_START, SLOT_END,
                new ItemStack(Items.STONE, 500), 500);

        helper.assertTrue(moved.getCount() == 64, "One stack should be moved, but was " + moved.getCount());
        helper.assertTrue(menu.getSlot(SLOT_START).getItem().getCount() == 64,
                "The partial stack should be filled up");
        helper.assertTrue(menu.getSlot(SLOT_START + 1).getItem().getCount() == 60,
                "The remainder should end up in the next slot");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testPlaceInSlotMovesTheSelection(GameTestHelper helper) {
        AbstractContainerMenu menu = createMenu(helper);

        ItemStack moved = getHandler().predictInsertIntoContainer(menu, SLOT_START,
                new ItemStack(Items.STONE, 16), true, 100);

        helper.assertTrue(moved.getCount() == 16, "The selection should be moved");
        helper.assertTrue(menu.getSlot(SLOT_START).getItem().getCount() == 16, "The slot should hold the selection");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testPlaceInOccupiedSlotMovesNothing(GameTestHelper helper) {
        AbstractContainerMenu menu = createMenu(helper);
        menu.getSlot(SLOT_START).set(new ItemStack(Items.DIRT, 1));

        ItemStack moved = getHandler().predictInsertIntoContainer(menu, SLOT_START,
                new ItemStack(Items.STONE, 16), true, 100);

        // The server picks the other item up into the player's cursor, which is deliberately not predicted
        helper.assertTrue(moved.isEmpty(), "Nothing should be moved into a slot holding another item");
        helper.assertTrue(menu.getSlot(SLOT_START).getItem().getItem() == Items.DIRT, "The slot should be untouched");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testExtractFromSlotEmptiesIt(GameTestHelper helper) {
        AbstractContainerMenu menu = createMenu(helper);
        menu.getSlot(SLOT_START).set(new ItemStack(Items.STONE, 32));

        ItemStack moved = getHandler().predictExtractMaxFromContainerSlot(menu, SLOT_START,
                helper.makeMockPlayer(GameType.SURVIVAL).getInventory(), -1);

        helper.assertTrue(moved.getCount() == 32, "The whole slot should be moved");
        helper.assertTrue(menu.getSlot(SLOT_START).getItem().isEmpty(), "The slot should be emptied");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testExtractFromSlotIsLimited(GameTestHelper helper) {
        AbstractContainerMenu menu = createMenu(helper);
        menu.getSlot(SLOT_START).set(new ItemStack(Items.STONE, 32));

        ItemStack moved = getHandler().predictExtractMaxFromContainerSlot(menu, SLOT_START,
                helper.makeMockPlayer(GameType.SURVIVAL).getInventory(), 2);

        helper.assertTrue(moved.getCount() == 2, "Only the limit should be moved");
        helper.assertTrue(menu.getSlot(SLOT_START).getItem().getCount() == 30, "The rest should stay in the slot");

        helper.succeed();
    }

}
