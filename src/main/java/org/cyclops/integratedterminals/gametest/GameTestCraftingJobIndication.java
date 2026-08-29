package org.cyclops.integratedterminals.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.integratedcrafting.api.crafting.CraftingJob;
import org.cyclops.integratedcrafting.core.CraftingHelpers;
import org.cyclops.integratedcrafting.gametest.GameTestHelpersIntegratedCrafting;
import org.cyclops.integratedcrafting.part.PartTypeInterfaceCrafting;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.PendingCraftingJobOutputs;

import java.util.Iterator;

/**
 * Game tests for the indication of running crafting jobs in the storage terminal.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestCraftingJobIndication {

    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);
    public static final int CRAFT_AMOUNT = 4;

    /**
     * Craft a batch of chests, and check on every tick that the crafted item is indicated as being crafted
     * for as long as the crafting job is running.
     */
    @GameTest(template = "empty10", templateNamespace = Reference.MOD_ID, timeoutTicks = 2000)
    public void testCraftingIndicationWhileJobIsRunning(GameTestHelper helper) {
        GameTestHelpersIntegratedCrafting.INetworkPositions<PartTypeInterfaceCrafting.State> positions =
                GameTestHelpersIntegratedCrafting.createBasicNetwork(helper, POS);

        // Insert crafting inputs in the interface chest
        ChestBlockEntity chest = helper.getBlockEntity(POS.east());
        chest.setItem(0, new ItemStack(Items.OAK_PLANKS, 64));

        // Add the chest recipe to the crafting interface
        positions.interfaceRecipeAdders().get(0).accept(Triple.of(0, RecipeType.CRAFTING,
                ResourceLocation.fromNamespaceAndPath("minecraft", "chest")));

        MutableInt ticksRunning = new MutableInt();
        MutableInt ticksIndicated = new MutableInt();
        MutableInt ticksMissingIndication = new MutableInt();

        helper.startSequence()
                .thenIdle(20)
                .thenExecute(() -> {
                    INetwork network = getNetwork(helper);
                    helper.assertTrue(CraftingHelpers.calculateAndScheduleCraftingJob(network,
                            IPositionedAddonsNetworkIngredients.DEFAULT_CHANNEL, IngredientComponents.ITEMSTACK,
                            new ItemStack(Items.CHEST, CRAFT_AMOUNT), ItemMatch.ITEM, true, true,
                            CraftingHelpers.getGlobalCraftingJobIdentifier(), null) != null,
                            "Crafting job could not be scheduled");
                })
                .thenExecuteFor(400, () -> {
                    INetwork network = getNetwork(helper);
                    if (!hasPendingCraftingJob(network)) {
                        // Nothing is left to be crafted, so nothing has to be indicated
                        return;
                    }

                    ticksRunning.increment();
                    // Both when a single channel is shown, and when all channels are shown at once
                    if (PendingCraftingJobOutputs.collectFromNetwork(IngredientComponents.ITEMSTACK, network,
                            IPositionedAddonsNetworkIngredients.DEFAULT_CHANNEL).get(new ItemStack(Items.CHEST)) != null
                            && PendingCraftingJobOutputs.collectFromNetwork(IngredientComponents.ITEMSTACK, network,
                            IPositionedAddonsNetworkIngredients.WILDCARD_CHANNEL).get(new ItemStack(Items.CHEST)) != null) {
                        ticksIndicated.increment();
                    } else {
                        ticksMissingIndication.increment();
                    }
                })
                .thenExecute(() -> {
                    helper.assertTrue(ticksRunning.intValue() > 0, "The crafting job never started running");
                    helper.assertTrue(ticksIndicated.intValue() > 0,
                            "The crafted item was never indicated as being crafted during the "
                                    + ticksRunning.intValue() + " ticks in which the crafting job was running");
                    helper.assertTrue(ticksMissingIndication.intValue() == 0,
                            "The crafted item was not indicated as being crafted during "
                                    + ticksMissingIndication.intValue() + " of the " + ticksRunning.intValue()
                                    + " ticks in which the crafting job was running");
                    GameTestHelpersIntegratedCrafting.chestContains(helper, chest,
                            new ItemStack(Items.CHEST, CRAFT_AMOUNT));
                })
                .thenSucceed();
    }

    /**
     * @param network A network.
     * @return If a crafting job with remaining outputs is present in the given network.
     */
    private static boolean hasPendingCraftingJob(INetwork network) {
        Iterator<CraftingJob> craftingJobs = CraftingHelpers.getCraftingNetworkChecked(network)
                .getCraftingJobs(IPositionedAddonsNetworkIngredients.WILDCARD_CHANNEL);
        while (craftingJobs.hasNext()) {
            if (craftingJobs.next().getAmount() > 0) {
                return true;
            }
        }
        return false;
    }

    private static INetwork getNetwork(GameTestHelper helper) {
        return NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null)
                .orElseThrow(() -> new IllegalStateException("Could not find a network"));
    }

}
