package org.cyclops.integratedterminals.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integratedcrafting.api.crafting.UnavailableCraftingInterfacesException;
import org.cyclops.integratedcrafting.core.CraftingHelpers;
import org.cyclops.integratedcrafting.gametest.GameTestHelpersIntegratedCrafting;
import org.cyclops.integratedcrafting.part.PartTypeInterfaceCrafting;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlanFlat;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.modcompat.integratedcrafting.TerminalCraftingOptionRecipeDefinition;
import org.cyclops.integratedterminals.modcompat.integratedcrafting.TerminalCraftingPlanCraftingJobDependencyGraph;
import org.cyclops.integratedterminals.modcompat.integratedcrafting.TerminalStorageTabIngredientCraftingHandlerCraftingNetwork;

/**
 * Game tests for running crafting jobs that are missing ingredients.
 * @author rubensworks
 */
public class GameTestTerminalCraftingPlanMissing {

    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);
    public static final int TIMEOUT = 2000;

    /**
     * The missing ingredients of a running job are its total missing quantities,
     * so they must not be multiplied by its remaining crafts.
     */
    @GameTest(template = Reference.MOD_ID + ":empty10", timeoutTicks = TIMEOUT)
    public void testCraftingPlanMissingQuantities(GameTestHelper helper) {
        GameTestHelpersIntegratedCrafting.INetworkPositions<PartTypeInterfaceCrafting.State> positions =
                GameTestHelpersIntegratedCrafting.createBasicNetwork(helper, POS);
        ChestBlockEntity chest = helper.getBlockEntity(POS.east(), ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.OAK_PLANKS, 16));
        positions.interfaceRecipeAdders().get(0).accept(Triple.of(0, RecipeType.CRAFTING, Identifier.fromNamespaceAndPath("minecraft", "chest")));
        TerminalStorageTabIngredientCraftingHandlerCraftingNetwork handler = new TerminalStorageTabIngredientCraftingHandlerCraftingNetwork();
        Integer[] jobId = new Integer[1];

        helper.onEachTick(() -> {
            INetwork network = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null).orElse(null);
            if (network == null) {
                return;
            }
            if (jobId[0] == null) {
                IRecipeDefinition recipe = CraftingHelpers.getCraftingNetworkChecked(network)
                        .getRecipeCraftingInterfaces(IPositionedAddonsNetworkIngredients.DEFAULT_CHANNEL).keySet().stream()
                        .filter(r -> r.getOutput().getInstances(IngredientComponent.ITEMSTACK).stream().anyMatch(s -> s.getItem() == Items.CHEST))
                        .findFirst()
                        .orElse(null);
                if (recipe == null) {
                    return;
                }

                // Plan 2 chests, but take 10 of their 16 planks away before the job is started
                ITerminalCraftingPlan<Integer> plan = handler.calculateCraftingPlan(network, IPositionedAddonsNetworkIngredients.DEFAULT_CHANNEL,
                        new TerminalCraftingOptionRecipeDefinition<>(IngredientComponent.ITEMSTACK, recipe), 2);
                if (plan.getStatus() != TerminalCraftingJobStatus.UNSTARTED) {
                    // The planks are not indexed in storage yet
                    return;
                }
                chest.setItem(0, new ItemStack(Items.OAK_PLANKS, 6));
                try {
                    CraftingHelpers.scheduleCraftingJobs(CraftingHelpers.getCraftingNetworkChecked(network),
                            CraftingHelpers.getNetworkStorageGetter(network, IPositionedAddonsNetworkIngredients.DEFAULT_CHANNEL, false),
                            ((TerminalCraftingPlanCraftingJobDependencyGraph) plan).getCraftingJobDependencyGraph(), true, null);
                } catch (UnavailableCraftingInterfacesException e) {
                    helper.fail("The crafting job could not be started");
                }
                jobId[0] = plan.getId();
                return;
            }

            ITerminalCraftingPlan<Integer> plan = handler.getCraftingJob(network, IPositionedAddonsNetworkIngredients.DEFAULT_CHANNEL, jobId[0]);
            helper.assertTrue(plan != null, "The crafting job is not running");
            if (plan.getStatus() != TerminalCraftingJobStatus.PENDING_INPUTS) {
                return;
            }
            helper.assertValueEqual(plan.getCraftingQuantity(), 2L, "Remaining crafts of the job");

            ITerminalCraftingPlanFlat.IEntry planks = plan.flatten().getEntries().stream()
                    .filter(e -> ((ItemStack) e.getInstances().get(0).getPrototype()).getItem() == Items.OAK_PLANKS)
                    .findFirst()
                    .orElse(null);
            helper.assertTrue(planks != null, "The planks are not in the flattened plan");
            helper.assertValueEqual(planks.getQuantityMissing(), 10L, "Missing planks");
            helper.succeed();
        });
    }

}
