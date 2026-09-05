package org.cyclops.integratedterminals.gametest;

import com.google.common.collect.Lists;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.MixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingOption;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.TerminalCraftingOptionInputs;
import org.cyclops.integratedterminals.modcompat.integratedcrafting.TerminalCraftingOptionRecipeDefinition;

import java.util.List;

/**
 * Game tests for the inputs that are exposed by crafting options.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestTerminalCraftingOptionIntegratedTerminals {

    private static List<IPrototypedIngredient<ItemStack, Integer>> alternatives(ItemStack... itemStacks) {
        List<IPrototypedIngredient<ItemStack, Integer>> alternatives = Lists.newArrayList();
        for (ItemStack itemStack : itemStacks) {
            alternatives.add(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK, itemStack,
                    IngredientComponent.ITEMSTACK.getMatcher().getExactMatchNoQuantityCondition()));
        }
        return alternatives;
    }

    private static ITerminalCraftingOption<ItemStack> createCraftingOption(List<List<IPrototypedIngredient<ItemStack, Integer>>> inputs) {
        IRecipeDefinition recipe = RecipeDefinition.ofIngredients(IngredientComponent.ITEMSTACK, inputs,
                MixedIngredients.ofInstance(IngredientComponent.ITEMSTACK, new ItemStack(Items.STICK, 4)));
        return new TerminalCraftingOptionRecipeDefinition<>(IngredientComponent.ITEMSTACK, recipe, -1);
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testInputAlternativesSingle(GameTestHelper helper) {
        List<List<IPrototypedIngredient<ItemStack, Integer>>> inputs = Lists.newArrayList();
        inputs.add(alternatives(new ItemStack(Items.OAK_PLANKS, 2)));

        List<List<IPrototypedIngredient<ItemStack, Integer>>> alternatives = createCraftingOption(inputs)
                .getInputAlternatives(IngredientComponent.ITEMSTACK);
        helper.assertTrue(alternatives.size() == 1, "Expected a single input");
        helper.assertTrue(alternatives.get(0).size() == 1, "Expected a single alternative");
        helper.assertTrue(ItemStack.isSameItemSameComponents(alternatives.get(0).get(0).getPrototype(), new ItemStack(Items.OAK_PLANKS)),
                "Expected oak planks as input");
        helper.assertTrue(alternatives.get(0).get(0).getPrototype().getCount() == 2, "Expected an input quantity of 2");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testInputAlternativesMultiple(GameTestHelper helper) {
        List<List<IPrototypedIngredient<ItemStack, Integer>>> inputs = Lists.newArrayList();
        inputs.add(alternatives(new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.BIRCH_PLANKS)));
        inputs.add(alternatives(new ItemStack(Items.STONE)));
        ITerminalCraftingOption<ItemStack> craftingOption = createCraftingOption(inputs);

        List<List<IPrototypedIngredient<ItemStack, Integer>>> alternatives = craftingOption
                .getInputAlternatives(IngredientComponent.ITEMSTACK);
        helper.assertTrue(alternatives.size() == 2, "Expected two inputs");
        helper.assertTrue(alternatives.get(0).size() == 2, "Expected two alternatives for the first input");
        helper.assertTrue(ItemStack.isSameItemSameComponents(alternatives.get(0).get(0).getPrototype(), new ItemStack(Items.OAK_PLANKS)),
                "Expected oak planks as first alternative");
        helper.assertTrue(ItemStack.isSameItemSameComponents(alternatives.get(0).get(1).getPrototype(), new ItemStack(Items.BIRCH_PLANKS)),
                "Expected birch planks as second alternative");
        helper.assertTrue(alternatives.get(1).size() == 1, "Expected one alternative for the second input");

        // The plain inputs only expose the first alternative
        List<ItemStack> plainInputs = Lists.newArrayList(craftingOption.getInputs(IngredientComponent.ITEMSTACK));
        helper.assertTrue(plainInputs.size() == 2, "Expected two plain inputs");
        helper.assertTrue(ItemStack.isSameItemSameComponents(plainInputs.get(0), new ItemStack(Items.OAK_PLANKS)),
                "Expected oak planks as first plain input");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testGroupedInputsEqual(GameTestHelper helper) {
        // A recipe that requires the same ingredient in multiple slots, like a redstone-based recipe
        List<List<IPrototypedIngredient<ItemStack, Integer>>> inputs = Lists.newArrayList();
        inputs.add(alternatives(new ItemStack(Items.REDSTONE)));
        inputs.add(alternatives(new ItemStack(Items.REDSTONE)));
        inputs.add(alternatives(new ItemStack(Items.HAY_BLOCK)));
        inputs.add(alternatives(new ItemStack(Items.REDSTONE, 2)));

        List<List<IPrototypedIngredient<?, ?>>> groupedInputs = TerminalCraftingOptionInputs
                .getGroupedInputs(createCraftingOption(inputs));
        helper.assertTrue(groupedInputs.size() == 2, "Expected the equal inputs to be grouped");
        helper.assertTrue(ItemStack.isSameItemSameComponents((ItemStack) groupedInputs.get(0).get(0).getPrototype(), new ItemStack(Items.REDSTONE)),
                "Expected redstone as first grouped input");
        helper.assertTrue(((ItemStack) groupedInputs.get(0).get(0).getPrototype()).getCount() == 4,
                "Expected the redstone quantities to be summed");
        helper.assertTrue(ItemStack.isSameItemSameComponents((ItemStack) groupedInputs.get(1).get(0).getPrototype(), new ItemStack(Items.HAY_BLOCK)),
                "Expected hay as second grouped input");
        helper.assertTrue(((ItemStack) groupedInputs.get(1).get(0).getPrototype()).getCount() == 1,
                "Expected the hay quantity to be unchanged");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testGroupedInputsEqualAlternatives(GameTestHelper helper) {
        // Inputs are only grouped if all their alternatives are equal
        List<List<IPrototypedIngredient<ItemStack, Integer>>> inputs = Lists.newArrayList();
        inputs.add(alternatives(new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.BIRCH_PLANKS)));
        inputs.add(alternatives(new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.BIRCH_PLANKS)));
        inputs.add(alternatives(new ItemStack(Items.OAK_PLANKS)));

        List<List<IPrototypedIngredient<?, ?>>> groupedInputs = TerminalCraftingOptionInputs
                .getGroupedInputs(createCraftingOption(inputs));
        helper.assertTrue(groupedInputs.size() == 2, "Expected only the inputs with equal alternatives to be grouped");
        helper.assertTrue(groupedInputs.get(0).size() == 2, "Expected the grouped input to keep its alternatives");
        for (IPrototypedIngredient<?, ?> alternative : groupedInputs.get(0)) {
            helper.assertTrue(((ItemStack) alternative.getPrototype()).getCount() == 2,
                    "Expected the quantity of every alternative to be summed");
        }
        helper.assertTrue(((ItemStack) groupedInputs.get(1).get(0).getPrototype()).getCount() == 1,
                "Expected the ungrouped input to be unchanged");

        helper.succeed();
    }

}
