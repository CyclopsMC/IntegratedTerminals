package org.cyclops.integratedterminals.gametest;

import com.google.common.collect.Lists;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.cyclopscore.ingredient.collection.IngredientArrayList;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageIngredientPredictions;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient.InstanceWithMetadata;

import java.util.List;

/**
 * Game tests for the client-side predictions of storage terminal interactions.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestTerminalStorageIngredientPredictions {

    private static final int CHANNEL = 3;

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

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testEmptyPredictionsDontChangeTheView(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        List<InstanceWithMetadata<ItemStack>> view = createView(new ItemStack(Items.STONE, 10));

        helper.assertTrue(predictions.isEmpty(), "No predictions should be pending");
        predictions.apply(CHANNEL, view);

        helper.assertTrue(view.size() == 1, "The view should be unchanged");
        helper.assertTrue(getInstance(view, 0).getCount() == 10, "The quantity should be unchanged");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testRemovalIsSubtracted(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CHANNEL, new ItemStack(Items.STONE, 4), false);
        List<InstanceWithMetadata<ItemStack>> view = createView(new ItemStack(Items.STONE, 10));
        predictions.apply(CHANNEL, view);

        helper.assertTrue(view.size() == 1, "The instance should still be shown");
        helper.assertTrue(getInstance(view, 0).getCount() == 6, "4 stone should have been subtracted");
        helper.assertTrue(predictions.getDelta(CHANNEL, new ItemStack(Items.STONE)) == -4,
                "The predicted delta should be negative");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testFullRemovalHidesTheInstance(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CHANNEL, new ItemStack(Items.STONE, 10), false);
        List<InstanceWithMetadata<ItemStack>> view = createView(new ItemStack(Items.STONE, 10),
                new ItemStack(Items.DIRT, 2));
        predictions.apply(CHANNEL, view);

        helper.assertTrue(view.size() == 1, "The emptied instance should not be shown anymore");
        helper.assertTrue(getInstance(view, 0).getItem() == Items.DIRT, "Other instances should be kept");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testAdditionIsAdded(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CHANNEL, new ItemStack(Items.STONE, 4), true);
        List<InstanceWithMetadata<ItemStack>> view = createView(new ItemStack(Items.STONE, 10));
        predictions.apply(CHANNEL, view);

        helper.assertTrue(getInstance(view, 0).getCount() == 14, "4 stone should have been added");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testAdditionOfUnshownInstanceIsAdded(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CHANNEL, new ItemStack(Items.DIRT, 4), true);
        List<InstanceWithMetadata<ItemStack>> view = createView(new ItemStack(Items.STONE, 10));
        predictions.apply(CHANNEL, view);

        helper.assertTrue(view.size() == 2, "The new instance should be shown");
        helper.assertTrue(getInstance(view, 1).getItem() == Items.DIRT, "The new instance should be dirt");
        helper.assertTrue(getInstance(view, 1).getCount() == 4, "4 dirt should be shown");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testOtherInstancesAreUntouched(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CHANNEL, new ItemStack(Items.STONE, 4), false);
        List<InstanceWithMetadata<ItemStack>> view = createView(new ItemStack(Items.DIRT, 10));
        predictions.apply(CHANNEL, view);

        helper.assertTrue(getInstance(view, 0).getCount() == 10, "Other instances should be unchanged");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testPredictionsAreShownInTheWildcardChannel(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CHANNEL, new ItemStack(Items.STONE, 4), false);

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

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testServerChangeConfirmsPrediction(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CHANNEL, new ItemStack(Items.STONE, 4), false);

        helper.assertTrue(!predictions.consume(createChange(new ItemStack(Items.DIRT, 4)), false),
                "A change for another instance should not confirm the prediction");
        helper.assertTrue(!predictions.consume(createChange(new ItemStack(Items.STONE, 4)), true),
                "A change in the other direction should not confirm the prediction");
        helper.assertTrue(!predictions.isEmpty(), "The prediction should still be pending");

        helper.assertTrue(predictions.consume(createChange(new ItemStack(Items.STONE, 4)), false),
                "A change for the predicted instance should confirm the prediction");
        helper.assertTrue(predictions.isEmpty(), "No predictions should be pending anymore");

        List<InstanceWithMetadata<ItemStack>> view = createView(new ItemStack(Items.STONE, 10));
        predictions.apply(CHANNEL, view);
        helper.assertTrue(getInstance(view, 0).getCount() == 10, "The server state should be shown as-is");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testPartialServerChangeKeepsRemainingPrediction(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CHANNEL, new ItemStack(Items.STONE, 4), false);
        predictions.add(CHANNEL, new ItemStack(Items.STONE, 6), false);

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

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testLargerServerChangeConfirmsAllPredictions(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CHANNEL, new ItemStack(Items.STONE, 4), false);

        // Another player took some stone as well, so the change is larger than what we predicted
        helper.assertTrue(predictions.consume(createChange(new ItemStack(Items.STONE, 100)), false),
                "The change should confirm the prediction");
        helper.assertTrue(predictions.isEmpty(), "No predictions should be pending anymore");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testPredictionsDontExpireImmediately(GameTestHelper helper) {
        TerminalStorageIngredientPredictions<ItemStack, Integer> predictions = createPredictions();
        predictions.add(CHANNEL, new ItemStack(Items.STONE, 4), false);

        helper.assertTrue(!predictions.removeExpired(), "The prediction should not have expired yet");
        helper.assertTrue(!predictions.isEmpty(), "The prediction should still be pending");

        helper.succeed();
    }

}
