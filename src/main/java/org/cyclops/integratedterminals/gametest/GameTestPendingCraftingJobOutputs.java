package org.cyclops.integratedterminals.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.PendingCraftingJobOutput;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.PendingCraftingJobOutputs;

/**
 * Game tests for the aggregation of pending crafting job outputs in the storage terminal.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestPendingCraftingJobOutputs {

    private static PendingCraftingJobOutputs<ItemStack, Integer> createOutputs() {
        return new PendingCraftingJobOutputs<>(IngredientComponents.ITEMSTACK);
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testEmpty(GameTestHelper helper) {
        PendingCraftingJobOutputs<ItemStack, Integer> outputs = createOutputs();

        helper.assertTrue(outputs.isEmpty(), "No outputs should be pending");
        helper.assertTrue(outputs.get(0, new ItemStack(Items.STONE)) == null,
                "No output should be pending for stone");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testLookupIgnoresQuantity(GameTestHelper helper) {
        PendingCraftingJobOutputs<ItemStack, Integer> outputs = createOutputs();
        outputs.add(0, new ItemStack(Items.STONE, 5), TerminalCraftingJobStatus.CRAFTING);

        helper.assertTrue(!outputs.isEmpty(), "Outputs should be pending");
        PendingCraftingJobOutput<ItemStack> output = outputs.get(0, new ItemStack(Items.STONE, 64));
        helper.assertTrue(output != null, "An output should be pending for stone of any quantity");
        helper.assertTrue(output.getInstance().getCount() == 5, "5 stone should be pending");
        helper.assertTrue(output.getStatus() == TerminalCraftingJobStatus.CRAFTING, "Stone should be crafting");

        helper.assertTrue(outputs.get(0, new ItemStack(Items.DIRT)) == null,
                "No output should be pending for another item");
        helper.assertTrue(outputs.get(1, new ItemStack(Items.STONE)) == null,
                "No output should be pending in another channel");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testQuantitiesAreSummed(GameTestHelper helper) {
        PendingCraftingJobOutputs<ItemStack, Integer> outputs = createOutputs();
        outputs.add(0, new ItemStack(Items.STONE, 5), TerminalCraftingJobStatus.CRAFTING);
        outputs.add(0, new ItemStack(Items.STONE, 7), TerminalCraftingJobStatus.CRAFTING);

        helper.assertTrue(outputs.get(0, new ItemStack(Items.STONE)).getInstance().getCount() == 12,
                "12 stone should be pending");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testMostRelevantStatusIsKept(GameTestHelper helper) {
        PendingCraftingJobOutputs<ItemStack, Integer> outputs = createOutputs();
        outputs.add(0, new ItemStack(Items.STONE), TerminalCraftingJobStatus.CRAFTING);
        outputs.add(0, new ItemStack(Items.STONE), TerminalCraftingJobStatus.PENDING_INPUTS);
        helper.assertTrue(outputs.get(0, new ItemStack(Items.STONE)).getStatus() == TerminalCraftingJobStatus.PENDING_INPUTS,
                "Missing inputs should take precedence over crafting");

        PendingCraftingJobOutputs<ItemStack, Integer> outputsReversed = createOutputs();
        outputsReversed.add(0, new ItemStack(Items.STONE), TerminalCraftingJobStatus.PENDING_INPUTS);
        outputsReversed.add(0, new ItemStack(Items.STONE), TerminalCraftingJobStatus.CRAFTING);
        helper.assertTrue(outputsReversed.get(0, new ItemStack(Items.STONE)).getStatus() == TerminalCraftingJobStatus.PENDING_INPUTS,
                "Missing inputs should take precedence over crafting, independent of insertion order");

        PendingCraftingJobOutputs<ItemStack, Integer> outputsQueueing = createOutputs();
        outputsQueueing.add(0, new ItemStack(Items.STONE), TerminalCraftingJobStatus.QUEUEING);
        outputsQueueing.add(0, new ItemStack(Items.STONE), TerminalCraftingJobStatus.CRAFTING);
        helper.assertTrue(outputsQueueing.get(0, new ItemStack(Items.STONE)).getStatus() == TerminalCraftingJobStatus.CRAFTING,
                "Crafting should take precedence over queueing");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testEmptyInstancesAreIgnored(GameTestHelper helper) {
        PendingCraftingJobOutputs<ItemStack, Integer> outputs = createOutputs();
        outputs.add(0, ItemStack.EMPTY, TerminalCraftingJobStatus.CRAFTING);

        helper.assertTrue(outputs.isEmpty(), "Empty instances should not be pending");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testMultipleChannels(GameTestHelper helper) {
        PendingCraftingJobOutputs<ItemStack, Integer> outputs = createOutputs();
        outputs.add(0, new ItemStack(Items.STONE, 5), TerminalCraftingJobStatus.CRAFTING);
        outputs.add(1, new ItemStack(Items.STONE, 3), TerminalCraftingJobStatus.QUEUEING);

        helper.assertTrue(outputs.getChanneledOutputs().size() == 2, "Two channels should have pending outputs");
        helper.assertTrue(outputs.get(0, new ItemStack(Items.STONE)).getInstance().getCount() == 5,
                "5 stone should be pending in channel 0");
        helper.assertTrue(outputs.get(1, new ItemStack(Items.STONE)).getInstance().getCount() == 3,
                "3 stone should be pending in channel 1");

        helper.succeed();
    }

}
