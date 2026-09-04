package org.cyclops.integratedterminals.gametest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.MixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.PrototypedIngredient;
import org.cyclops.integratedcrafting.api.crafting.ICraftingInterface;
import org.cyclops.integratedcrafting.core.CraftingHelpers;
import org.cyclops.integratedcrafting.core.part.PartTypeInterfaceCraftingBase;
import org.cyclops.integratedcrafting.gametest.GameTestHelpersIntegratedCrafting;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.modcompat.integratedcrafting.TerminalCraftingOptionRecipeDefinition;
import org.cyclops.integratedterminals.modcompat.integratedcrafting.TerminalStorageTabIngredientCraftingHandlerCraftingNetwork;

import java.util.Collection;
import java.util.List;

/**
 * Game tests for the crafting machines that are exposed by crafting options.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestTerminalCraftingOptionMachines {

    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);
    public static final int TIMEOUT = 2000;

    private static IRecipeDefinition createRecipe() {
        List<List<IPrototypedIngredient<ItemStack, Integer>>> inputs = Lists.newArrayList();
        inputs.add(Lists.newArrayList(new PrototypedIngredient<>(IngredientComponent.ITEMSTACK,
                new ItemStack(Items.OAK_PLANKS),
                IngredientComponent.ITEMSTACK.getMatcher().getExactMatchNoQuantityCondition())));
        return RecipeDefinition.ofIngredients(IngredientComponent.ITEMSTACK, inputs,
                MixedIngredients.ofInstance(IngredientComponent.ITEMSTACK, new ItemStack(Items.STICK, 4)));
    }

    /**
     * The machines of a crafting option must survive being sent to the client.
     */
    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testCraftingMachinesSerialization(GameTestHelper helper) {
        TerminalStorageTabIngredientCraftingHandlerCraftingNetwork handler =
                new TerminalStorageTabIngredientCraftingHandlerCraftingNetwork();
        TerminalCraftingOptionRecipeDefinition<ItemStack, Integer> craftingOption =
                new TerminalCraftingOptionRecipeDefinition<>(IngredientComponent.ITEMSTACK, createRecipe(), -1,
                        Lists.newArrayList(new ItemStack(Items.FURNACE), new ItemStack(Items.CRAFTING_TABLE)));

        CompoundTag tag = handler.serializeCraftingOption(helper.getLevel().registryAccess(), craftingOption);
        TerminalCraftingOptionRecipeDefinition<?, ?> deserialized = handler.deserializeCraftingOption(
                helper.getLevel().registryAccess(), IngredientComponent.ITEMSTACK, tag);

        List<ItemStack> machines = deserialized.getCraftingMachines();
        helper.assertValueEqual(machines.size(), 2, "Crafting machine count is incorrect");
        helper.assertValueEqual(machines.get(0).getItem(), Items.FURNACE, "First crafting machine is incorrect");
        helper.assertValueEqual(machines.get(1).getItem(), Items.CRAFTING_TABLE, "Second crafting machine is incorrect");

        helper.succeed();
    }

    /**
     * A crafting option without machines must not write them,
     * so that nothing is sent for handlers that can not determine them.
     */
    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testCraftingMachinesSerializationEmpty(GameTestHelper helper) {
        TerminalStorageTabIngredientCraftingHandlerCraftingNetwork handler =
                new TerminalStorageTabIngredientCraftingHandlerCraftingNetwork();
        TerminalCraftingOptionRecipeDefinition<ItemStack, Integer> craftingOption =
                new TerminalCraftingOptionRecipeDefinition<>(IngredientComponent.ITEMSTACK, createRecipe());

        CompoundTag tag = handler.serializeCraftingOption(helper.getLevel().registryAccess(), craftingOption);
        helper.assertFalse(tag.contains("craftingMachines"), "Expected no crafting machines to be serialized");

        TerminalCraftingOptionRecipeDefinition<?, ?> deserialized = handler.deserializeCraftingOption(
                helper.getLevel().registryAccess(), IngredientComponent.ITEMSTACK, tag);
        helper.assertTrue(deserialized.getCraftingMachines().isEmpty(), "Expected no crafting machines");

        helper.succeed();
    }

    /**
     * Check the machines that are determined for the first recipe that the network exposes.
     *
     * @param helper The game test helper.
     * @param attuned If an attuned crafting interface should be used.
     * @param crafters The machines to place before the crafting interfaces.
     * @param expectedInterfaces The expected number of interfaces that expose the recipe.
     * @param expectedMachine The expected machine item.
     */
    protected void testCraftingMachinesInNetwork(GameTestHelper helper, boolean attuned, Block[] crafters,
                                                 int expectedInterfaces, Item expectedMachine,
                                                 RecipeType<?> recipeType, ResourceLocation recipeName) {
        GameTestHelpersIntegratedCrafting.INetworkPositions<PartTypeInterfaceCraftingBase.State<?, ?>> positions =
                GameTestHelpersIntegratedCrafting.createBasicNetwork(helper, POS, attuned, crafters);

        // Add the recipe to every crafting interface.
        // Attuned interfaces derive their recipes from their machine, so this is a no-op for those.
        for (int i = 0; i < crafters.length; i++) {
            positions.interfaceRecipeAdders().get(i).accept(Triple.of(0, recipeType, recipeName));
        }

        helper.succeedWhen(() -> {
            INetwork network = NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null)
                    .orElseThrow(() -> new IllegalStateException("Could not find a network"));
            Multimap<IRecipeDefinition, ICraftingInterface> recipeCraftingInterfaces = CraftingHelpers
                    .getCraftingNetworkChecked(network)
                    .getRecipeCraftingInterfaces(IPositionedAddonsNetworkIngredients.DEFAULT_CHANNEL);
            helper.assertFalse(recipeCraftingInterfaces.isEmpty(), "The network exposes no recipes yet");

            IRecipeDefinition recipe = recipeCraftingInterfaces.keySet().iterator().next();
            Collection<ICraftingInterface> craftingInterfaces = recipeCraftingInterfaces.get(recipe);
            helper.assertValueEqual(craftingInterfaces.size(), expectedInterfaces,
                    "Crafting interface count is incorrect");

            List<ItemStack> machines = TerminalStorageTabIngredientCraftingHandlerCraftingNetwork
                    .getCraftingMachines(craftingInterfaces, Maps.newIdentityHashMap());
            helper.assertValueEqual(machines.size(), 1, "Crafting machine count is incorrect");
            helper.assertValueEqual(machines.get(0).getItem(), expectedMachine, "Crafting machine is incorrect");
        });
    }

    @GameTest(template = "empty10", templateNamespace = Reference.MOD_ID, timeoutTicks = TIMEOUT)
    public void testCraftingMachinesInNetworkCraftingTable(GameTestHelper helper) {
        testCraftingMachinesInNetwork(helper, false, new Block[]{Blocks.CRAFTING_TABLE}, 1, Items.CRAFTING_TABLE,
                RecipeType.CRAFTING, ResourceLocation.fromNamespaceAndPath("minecraft", "chest"));
    }

    @GameTest(template = "empty10", templateNamespace = Reference.MOD_ID, timeoutTicks = TIMEOUT)
    public void testCraftingMachinesInNetworkFurnace(GameTestHelper helper) {
        testCraftingMachinesInNetwork(helper, false, new Block[]{Blocks.FURNACE}, 1, Items.FURNACE,
                RecipeType.SMELTING, ResourceLocation.fromNamespaceAndPath("minecraft", "iron_ingot_from_smelting_raw_iron"));
    }

    @GameTest(template = "empty10", templateNamespace = Reference.MOD_ID, timeoutTicks = TIMEOUT)
    public void testCraftingMachinesInNetworkAttunedCraftingTable(GameTestHelper helper) {
        testCraftingMachinesInNetwork(helper, true, new Block[]{Blocks.CRAFTING_TABLE}, 1, Items.CRAFTING_TABLE,
                RecipeType.CRAFTING, ResourceLocation.fromNamespaceAndPath("minecraft", "chest"));
    }

    @GameTest(template = "empty10", templateNamespace = Reference.MOD_ID, timeoutTicks = TIMEOUT)
    public void testCraftingMachinesInNetworkAttunedFurnace(GameTestHelper helper) {
        testCraftingMachinesInNetwork(helper, true, new Block[]{Blocks.FURNACE}, 1, Items.FURNACE,
                RecipeType.SMELTING, ResourceLocation.fromNamespaceAndPath("minecraft", "iron_ingot_from_smelting_raw_iron"));
    }

    /**
     * Two interfaces that target the same machine type must only show that machine once.
     */
    @GameTest(template = "empty10", templateNamespace = Reference.MOD_ID, timeoutTicks = TIMEOUT)
    public void testCraftingMachinesInNetworkDeduplicated(GameTestHelper helper) {
        testCraftingMachinesInNetwork(helper, false, new Block[]{Blocks.CRAFTING_TABLE, Blocks.CRAFTING_TABLE},
                2, Items.CRAFTING_TABLE,
                RecipeType.CRAFTING, ResourceLocation.fromNamespaceAndPath("minecraft", "chest"));
    }

}
