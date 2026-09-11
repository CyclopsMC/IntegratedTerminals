package org.cyclops.integratedterminals.gametest;

import com.google.common.collect.Lists;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.cyclopscore.ingredient.collection.IngredientArrayList;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageIngredientPredictions;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient.InstanceWithMetadata;

import java.util.List;

/**
 * Game tests for the client-side predictions of storage terminal interactions.
 * @author rubensworks
 */
public class GameTestTerminalStorageIngredientPredictions {

    private static final int CHANNEL = 3;
    private static final int CLICK = 1;

    private static TerminalStorageIngredientPredictions<ItemStack, Integer> createPredictions() {
        return new TerminalStorageIngredientPredictions<>(IngredientComponents.ITEMSTACK);
    }

    private static List<InstanceWithMetadata<ItemStack>> createView(ItemStack... instances) {
        List<InstanceWithMetadata<ItemStack>> view = Lists.newArrayList();
        for (ItemStack instance : instances) {
            view.add(new InstanceWithMetadata<>(instance, null));
        }
        return view;
    }

    private static ItemStack getInstance(List<InstanceWithMetadata<ItemStack>> view, int index) {
        return view.get(index).getInstance();
    }

    private static IngredientArrayList<ItemStack, Integer> createChange(ItemStack... instances) {
        return new IngredientArrayList<>(IngredientComponents.ITEMSTACK, instances);
    }

    @GameTest(template = "cyclopscore:empty")
    public void testEmptyPredictionsDontChangeTheView(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        List<InstanceWithMetadata<ItemStack>> view = createView(new ItemStack(Items.STONE, 10));

        helper.assertTrue(predictions.isEmpty(), "No predictions should be pending");
        predictions.apply(CHANNEL, view);

        helper.assertTrue(view.size() == 1, "The view should be unchanged");
        helper.assertTrue(getInstance(view, 0).getCount() == 10, "The quantity should be unchanged");

        helper.succeed();
    }

    @GameTest(template = "cyclopscore:empty")
    public void testRemovalIsSubtracted(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CLICK, CHANNEL, new ItemStack(Items.STONE, 4), false);
        List<InstanceWithMetadata<ItemStack>> view = createView(new ItemStack(Items.STONE, 10));
        predictions.apply(CHANNEL, view);

        helper.assertTrue(view.size() == 1, "The instance should still be shown");
        helper.assertTrue(getInstance(view, 0).getCount() == 6, "4 stone should have been subtracted");
        helper.assertTrue(predictions.getDelta(CHANNEL, new ItemStack(Items.STONE)) == -4,
                "The predicted delta should be negative");

        helper.succeed();
    }

    @GameTest(template = "cyclopscore:empty")
    public void testFullRemovalHidesTheInstance(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CLICK, CHANNEL, new ItemStack(Items.STONE, 10), false);
        List<InstanceWithMetadata<ItemStack>> view = createView(new ItemStack(Items.STONE, 10),
                new ItemStack(Items.DIRT, 2));
        predictions.apply(CHANNEL, view);

        helper.assertTrue(view.size() == 1, "The emptied instance should not be shown anymore");
        helper.assertTrue(getInstance(view, 0).getItem() == Items.DIRT, "Other instances should be kept");

        helper.succeed();
    }

    @GameTest(template = "cyclopscore:empty")
    public void testOtherInstancesAreUntouched(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CLICK, CHANNEL, new ItemStack(Items.STONE, 4), false);
        List<InstanceWithMetadata<ItemStack>> view = createView(new ItemStack(Items.DIRT, 10));
        predictions.apply(CHANNEL, view);

        helper.assertTrue(getInstance(view, 0).getCount() == 10, "Other instances should be unchanged");

        helper.succeed();
    }

    @GameTest(template = "cyclopscore:empty")
    public void testPredictionsAreShownInTheWildcardChannel(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CLICK, CHANNEL, new ItemStack(Items.STONE, 4), false);

        List<InstanceWithMetadata<ItemStack>> wildcardView = createView(new ItemStack(Items.STONE, 10));
        predictions.apply(IPositionedAddonsNetwork.WILDCARD_CHANNEL, wildcardView);
        helper.assertTrue(getInstance(wildcardView, 0).getCount() == 6,
                "The prediction should also be shown in the wildcard channel");

        List<InstanceWithMetadata<ItemStack>> otherView = createView(new ItemStack(Items.STONE, 10));
        predictions.apply(CHANNEL + 1, otherView);
        helper.assertTrue(getInstance(otherView, 0).getCount() == 10,
                "The prediction should not be shown in other channels");

        helper.succeed();
    }

    @GameTest(template = "cyclopscore:empty")
    public void testServerChangeConfirmsPrediction(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CLICK, CHANNEL, new ItemStack(Items.STONE, 4), false);

        helper.assertTrue(!predictions.consume(createChange(new ItemStack(Items.DIRT, 4)), false),
                "A change for another instance should not confirm the prediction");
        helper.assertTrue(!predictions.isEmpty(), "The prediction should still be pending");

        helper.assertTrue(predictions.consume(createChange(new ItemStack(Items.STONE, 4)), false),
                "A change for the predicted instance should confirm the prediction");
        helper.assertTrue(predictions.isEmpty(), "No predictions should be pending anymore");

        List<InstanceWithMetadata<ItemStack>> view = createView(new ItemStack(Items.STONE, 10));
        predictions.apply(CHANNEL, view);
        helper.assertTrue(getInstance(view, 0).getCount() == 10, "The server state should be shown as-is");

        helper.succeed();
    }

    @GameTest(template = "cyclopscore:empty")
    public void testPartialServerChangeKeepsRemainingPrediction(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CLICK, CHANNEL, new ItemStack(Items.STONE, 4), false);
        predictions.add(CLICK + 1, CHANNEL, new ItemStack(Items.STONE, 6), false);

        // The server confirms the first click only
        helper.assertTrue(predictions.consume(createChange(new ItemStack(Items.STONE, 4)), false),
                "The change should confirm the first prediction");
        helper.assertTrue(predictions.getDelta(CHANNEL, new ItemStack(Items.STONE)) == -6,
                "The unconfirmed prediction should be kept");

        List<InstanceWithMetadata<ItemStack>> view = createView(new ItemStack(Items.STONE, 16));
        predictions.apply(CHANNEL, view);
        helper.assertTrue(getInstance(view, 0).getCount() == 10,
                "The second click should still be shown as applied");

        helper.succeed();
    }

    @GameTest(template = "cyclopscore:empty")
    public void testLargerServerChangeConfirmsAllPredictions(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CLICK, CHANNEL, new ItemStack(Items.STONE, 4), false);

        // Another player took some stone as well, so the change is larger than what we predicted
        helper.assertTrue(predictions.consume(createChange(new ItemStack(Items.STONE, 100)), false),
                "The change should confirm the prediction");
        helper.assertTrue(predictions.isEmpty(), "No predictions should be pending anymore");

        helper.succeed();
    }

    @GameTest(template = "cyclopscore:empty")
    public void testPredictionsDontExpireImmediately(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CLICK, CHANNEL, new ItemStack(Items.STONE, 4), false);

        helper.assertTrue(!predictions.removeExpired(), "The prediction should not have expired yet");
        helper.assertTrue(!predictions.isEmpty(), "The prediction should still be pending");

        helper.succeed();
    }

    @GameTest(template = "cyclopscore:empty")
    public void testAdditionIsShown(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CLICK, CHANNEL, new ItemStack(Items.STONE, 4), true);

        List<InstanceWithMetadata<ItemStack>> view = createView(new ItemStack(Items.STONE, 10));
        predictions.apply(CHANNEL, view);
        helper.assertTrue(getInstance(view, 0).getCount() == 14, "4 stone should have been added");

        List<InstanceWithMetadata<ItemStack>> emptyView = createView();
        predictions.apply(CHANNEL, emptyView);
        helper.assertTrue(emptyView.size() == 1, "An instance that is not shown yet should be added");
        helper.assertTrue(getInstance(emptyView, 0).getCount() == 4, "4 stone should be shown");

        helper.succeed();
    }

    @GameTest(template = "cyclopscore:empty")
    public void testAdditionIsConfirmedByAnAddition(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CLICK, CHANNEL, new ItemStack(Items.STONE, 4), true);

        helper.assertTrue(!predictions.consume(createChange(new ItemStack(Items.STONE, 4)), false),
                "A removal should not confirm an addition");
        helper.assertTrue(predictions.consume(createChange(new ItemStack(Items.STONE, 4)), true),
                "An addition should confirm an addition");
        helper.assertTrue(predictions.isEmpty(), "No predictions should be pending anymore");

        helper.succeed();
    }

    /**
     * The server reports what a click really moved, which is less than predicted when the storage
     * held less than the client knew of, or when it refused what was offered to it.
     */
    @GameTest(template = "cyclopscore:empty")
    public void testServerResultCutsThePredictionDown(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CLICK, CHANNEL, new ItemStack(Items.STONE, 10), false);

        helper.assertTrue(!predictions.reconcile(CLICK + 99, 0),
                "Another click's result should not touch this prediction");
        helper.assertTrue(!predictions.reconcile(CLICK, 10),
                "A result that moved everything should leave the prediction alone");
        helper.assertTrue(!predictions.reconcile(CLICK, 20),
                "A result that moved more than predicted should leave the prediction alone");

        helper.assertTrue(predictions.reconcile(CLICK, 4), "A partial result should cut the prediction down");
        helper.assertTrue(predictions.getDelta(CHANNEL, new ItemStack(Items.STONE)) == -4,
                "Only what really moved should still be shown");

        helper.succeed();
    }

    @GameTest(template = "cyclopscore:empty")
    public void testRefusedClickDropsThePredictionAtOnce(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CLICK, CHANNEL, new ItemStack(Items.STONE, 10), true);

        helper.assertTrue(predictions.reconcile(CLICK, 0), "A result of nothing should drop the prediction");
        helper.assertTrue(predictions.isEmpty(), "Nothing should be shown for a click that moved nothing");

        List<InstanceWithMetadata<ItemStack>> view = createView(new ItemStack(Items.DIRT, 1));
        predictions.apply(CHANNEL, view);
        helper.assertTrue(view.size() == 1, "The refused instance should not be shown");

        helper.succeed();
    }


    /**
     * The tests below cover clicks that are still unconfirmed when the next one is made,
     * which is what happens when the player clicks faster than the round trip.
     */

    @GameTest(template = "cyclopscore:empty")
    public void testPipelinedClicksAreShownTogether(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CLICK, CHANNEL, new ItemStack(Items.STONE, 10), false);
        predictions.add(CLICK + 1, CHANNEL, new ItemStack(Items.STONE, 20), false);
        predictions.add(CLICK + 2, CHANNEL, new ItemStack(Items.STONE, 5), false);

        List<InstanceWithMetadata<ItemStack>> view = createView(new ItemStack(Items.STONE, 100));
        predictions.apply(CHANNEL, view);

        helper.assertTrue(getInstance(view, 0).getCount() == 65, "All pending clicks should be shown at once");

        helper.succeed();
    }

    /**
     * The server handles clicks in the order it received them, and predictions are kept in that same order,
     * so the change for the first click confirms the first prediction and leaves the later ones pending.
     */
    @GameTest(template = "cyclopscore:empty")
    public void testConfirmingOneClickKeepsTheLaterOnes(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CLICK, CHANNEL, new ItemStack(Items.STONE, 10), false);
        predictions.add(CLICK + 1, CHANNEL, new ItemStack(Items.STONE, 20), false);

        helper.assertTrue(predictions.consume(createChange(new ItemStack(Items.STONE, 10)), false),
                "The change of the first click should be confirmed");
        helper.assertTrue(predictions.getDelta(CHANNEL, new ItemStack(Items.STONE)) == -20,
                "The second click should still be shown in full");

        helper.assertTrue(predictions.consume(createChange(new ItemStack(Items.STONE, 20)), false),
                "The change of the second click should be confirmed");
        helper.assertTrue(predictions.isEmpty(), "Nothing should be left pending");

        helper.succeed();
    }

    /**
     * A single change can cover more than one click, as the server batches what it observed.
     */
    @GameTest(template = "cyclopscore:empty")
    public void testOneChangeCanConfirmSeveralClicks(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CLICK, CHANNEL, new ItemStack(Items.STONE, 10), false);
        predictions.add(CLICK + 1, CHANNEL, new ItemStack(Items.STONE, 20), false);
        predictions.add(CLICK + 2, CHANNEL, new ItemStack(Items.STONE, 5), false);

        helper.assertTrue(predictions.consume(createChange(new ItemStack(Items.STONE, 25)), false),
                "A batched change should be confirmed");
        helper.assertTrue(predictions.getDelta(CHANNEL, new ItemStack(Items.STONE)) == -10,
                "Only the part that the change did not cover should still be shown");

        helper.succeed();
    }

    /**
     * Results carry the id of the click they belong to, so they stay apart even when they arrive out of order.
     */
    @GameTest(template = "cyclopscore:empty")
    public void testResultsOfPipelinedClicksStayApart(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CLICK, CHANNEL, new ItemStack(Items.STONE, 10), false);
        predictions.add(CLICK + 1, CHANNEL, new ItemStack(Items.STONE, 20), false);

        helper.assertTrue(predictions.reconcile(CLICK + 1, 8), "The second click's result should be applied");
        helper.assertTrue(predictions.getDelta(CHANNEL, new ItemStack(Items.STONE)) == -18,
                "Only the second click should have been cut down");

        helper.assertTrue(predictions.reconcile(CLICK, 3), "The first click's result should be applied");
        helper.assertTrue(predictions.getDelta(CHANNEL, new ItemStack(Items.STONE)) == -11,
                "Both clicks should be cut down to what they really moved");

        helper.succeed();
    }

    /**
     * A result for a click that a change already confirmed is a no-op,
     * and must not fall through to another click's prediction.
     */
    @GameTest(template = "cyclopscore:empty")
    public void testLateResultDoesNotTouchAnotherClick(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CLICK, CHANNEL, new ItemStack(Items.STONE, 10), false);
        predictions.add(CLICK + 1, CHANNEL, new ItemStack(Items.STONE, 20), false);

        predictions.consume(createChange(new ItemStack(Items.STONE, 10)), false);
        helper.assertTrue(!predictions.reconcile(CLICK, 0),
                "A result for an already confirmed click should change nothing");
        helper.assertTrue(predictions.getDelta(CHANNEL, new ItemStack(Items.STONE)) == -20,
                "The other click should be untouched");

        helper.succeed();
    }

    /**
     * Clicks in opposite directions cancel out in the view, and are confirmed by their own direction only.
     */
    @GameTest(template = "cyclopscore:empty")
    public void testPipelinedClicksInBothDirections(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CLICK, CHANNEL, new ItemStack(Items.STONE, 10), false);
        predictions.add(CLICK + 1, CHANNEL, new ItemStack(Items.STONE, 4), true);

        List<InstanceWithMetadata<ItemStack>> view = createView(new ItemStack(Items.STONE, 100));
        predictions.apply(CHANNEL, view);
        helper.assertTrue(getInstance(view, 0).getCount() == 94, "Both directions should be shown");

        helper.assertTrue(predictions.consume(createChange(new ItemStack(Items.STONE, 4)), true),
                "The addition should be confirmed by an addition");
        helper.assertTrue(predictions.getDelta(CHANNEL, new ItemStack(Items.STONE)) == -10,
                "The removal should still be pending");

        helper.succeed();
    }

    /**
     * A click that empties an instance and a later click on it cannot both be shown,
     * as the second one is capped by what the first one left behind.
     */
    @GameTest(template = "cyclopscore:empty")
    public void testPipelinedRemovalsCannotGoNegative(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CLICK, CHANNEL, new ItemStack(Items.STONE, 10), false);
        predictions.add(CLICK + 1, CHANNEL, new ItemStack(Items.STONE, 10), false);

        List<InstanceWithMetadata<ItemStack>> view = createView(
                new ItemStack(Items.STONE, 15), new ItemStack(Items.DIRT, 3));
        predictions.apply(CHANNEL, view);

        helper.assertTrue(view.size() == 1, "An over-predicted instance should be hidden, not shown negative");
        helper.assertTrue(getInstance(view, 0).getItem() == Items.DIRT, "Other instances should be untouched");

        helper.succeed();
    }

}
