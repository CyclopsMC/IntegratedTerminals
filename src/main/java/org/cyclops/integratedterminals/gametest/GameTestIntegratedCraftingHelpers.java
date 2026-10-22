package org.cyclops.integratedterminals.gametest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import org.cyclops.integratedcrafting.api.crafting.CraftingJob;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.modcompat.integratedcrafting.IntegratedCraftingHelpers;

import java.util.List;

/**
 * Game tests for {@link IntegratedCraftingHelpers}.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestIntegratedCraftingHelpers {

    /**
     * The buffer of a crafting job that received ingredients contains an empty instance,
     * which must not be shown as an ingredient.
     */
    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testPrototypesFromCraftingJobBufferSkipEmpty(GameTestHelper helper) {
        List<List<IPrototypedIngredient<ItemStack, Integer>>> inputs = Lists.newArrayList();
        inputs.add(Lists.newArrayList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK,
                new ItemStack(Items.OAK_PLANKS), IngredientComponent.ITEMSTACK.getMatcher().getExactMatchNoQuantityCondition())));
        IRecipeDefinition recipe = RecipeDefinition.ofIngredients(IngredientComponent.ITEMSTACK, inputs,
                MixedIngredients.ofInstance(IngredientComponent.ITEMSTACK, new ItemStack(Items.STICK, 4)));
        CraftingJob craftingJob = new CraftingJob(0, 0, recipe, 1, new MixedIngredients(Maps.newIdentityHashMap()));
        craftingJob.addToIngredientsStorageBuffer(IngredientComponent.ITEMSTACK, new ItemStack(Items.OAK_PLANKS, 2));
        craftingJob.addToIngredientsStorageBuffer(IngredientComponent.ITEMSTACK, new ItemStack(Items.OAK_PLANKS, 3));
        helper.assertTrue(craftingJob.getIngredientsStorageBuffer().getInstances(IngredientComponent.ITEMSTACK).stream().anyMatch(ItemStack::isEmpty),
                "The crafting job buffer is expected to contain an empty instance");

        List<IPrototypedIngredient<?, ?>> prototypes = IntegratedCraftingHelpers.getPrototypesFromIngredients(craftingJob.getIngredientsStorageBuffer());
        helper.assertValueEqual(prototypes.size(), 1, "Number of buffered ingredients");
        ItemStack planks = (ItemStack) prototypes.get(0).getPrototype();
        helper.assertValueEqual(planks.getItem(), Items.OAK_PLANKS, "Buffered ingredient");
        helper.assertValueEqual(planks.getCount(), 5, "Buffered quantity");
        helper.succeed();
    }

}
