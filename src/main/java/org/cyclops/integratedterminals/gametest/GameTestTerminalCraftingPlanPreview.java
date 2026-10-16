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
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integratedcrafting.core.CraftingHelpers;
import org.cyclops.integratedcrafting.gametest.GameTestHelpersIntegratedCrafting;
import org.cyclops.integratedcrafting.part.PartTypeInterfaceCrafting;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.cyclops.integratedterminals.modcompat.integratedcrafting.TerminalCraftingOptionRecipeDefinition;
import org.cyclops.integratedterminals.modcompat.integratedcrafting.TerminalStorageTabIngredientCraftingHandlerCraftingNetwork;

import java.util.List;

/**
 * Game tests for the crafting plans that are shown before a crafting job is started.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestTerminalCraftingPlanPreview {

    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);
    public static final int TIMEOUT = 2000;

    /**
     * Each job of a preview must show the ingredients it will take from storage.
     */
    @GameTest(template = "empty10", templateNamespace = Reference.MOD_ID, timeoutTicks = TIMEOUT)
    public void testCraftingPlanPreviewShowsStorageIngredients(GameTestHelper helper) {
        GameTestHelpersIntegratedCrafting.INetworkPositions<PartTypeInterfaceCrafting.State> positions =
                GameTestHelpersIntegratedCrafting.createBasicNetwork(helper, POS);
        ChestBlockEntity chest = helper.getBlockEntity(POS.east());
        chest.setItem(0, new ItemStack(Items.OAK_LOG, 2));
        positions.interfaceRecipeAdders().get(0).accept(Triple.of(0, RecipeType.CRAFTING, ResourceLocation.fromNamespaceAndPath("minecraft", "chest")));
        positions.interfaceRecipeAdders().get(0).accept(Triple.of(1, RecipeType.CRAFTING, ResourceLocation.fromNamespaceAndPath("minecraft", "oak_planks")));

        helper.succeedWhen(() -> {
            INetwork network = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null)
                    .orElseThrow(() -> new IllegalStateException("Could not find a network"));
            IRecipeDefinition recipe = CraftingHelpers.getCraftingNetworkChecked(network)
                    .getRecipeCraftingInterfaces(IPositionedAddonsNetworkIngredients.DEFAULT_CHANNEL).keySet().stream()
                    .filter(r -> r.getOutput().getInstances(IngredientComponent.ITEMSTACK).stream().anyMatch(s -> s.getItem() == Items.CHEST))
                    .findFirst()
                    .orElse(null);
            helper.assertTrue(recipe != null, "The network does not expose the chest recipe yet");

            ITerminalCraftingPlan<Integer> plan = new TerminalStorageTabIngredientCraftingHandlerCraftingNetwork()
                    .calculateCraftingPlan(network, IPositionedAddonsNetworkIngredients.DEFAULT_CHANNEL,
                            new TerminalCraftingOptionRecipeDefinition<>(IngredientComponent.ITEMSTACK, recipe), 1);
            helper.assertValueEqual(plan.getStatus(), TerminalCraftingJobStatus.UNSTARTED, "Status of the plan");

            // The chest takes nothing from storage, as its planks are crafted first
            helper.assertTrue(plan.getBufferedIngredients().isEmpty(), "The chest job takes ingredients from storage");

            // The planks are crafted from the logs in storage
            helper.assertValueEqual(plan.getDependencies().size(), 1, "Dependencies of the chest job");
            List<IPrototypedIngredient<?, ?>> storage = plan.getDependencies().get(0).getBufferedIngredients();
            helper.assertValueEqual(storage.size(), 1, "Storage ingredients of the planks job");
            ItemStack logs = (ItemStack) storage.get(0).getPrototype();
            helper.assertValueEqual(logs.getItem(), Items.OAK_LOG, "Storage ingredient of the planks job");
            helper.assertValueEqual(logs.getCount(), 2, "Storage quantity of the planks job");
        });
    }

}
