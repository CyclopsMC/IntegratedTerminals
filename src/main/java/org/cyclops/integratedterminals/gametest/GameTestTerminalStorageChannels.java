package org.cyclops.integratedterminals.gametest;

import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientCollapsedCollectionMutable;
import org.cyclops.cyclopscore.ingredient.collection.IIngredientCollection;
import org.cyclops.cyclopscore.ingredient.collection.IngredientCollectionEmpty;
import org.cyclops.cyclopscore.ingredient.collection.IngredientCollectionHelpers;
import org.cyclops.integratedterminals.Capabilities;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.ingredient.IIngredientComponentTerminalStorageHandler;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageChannels;

import java.util.List;

/**
 * Game tests for determining the channels in which ingredients are available in the storage terminal.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestTerminalStorageChannels {

    /**
     * The ingredients that are stored in each channel, which are set up by each test.
     */
    private final Int2ObjectMap<IIngredientCollapsedCollectionMutable<ItemStack, Integer>> channelIngredients =
            new Int2ObjectOpenHashMap<>();

    private static IIngredientComponentTerminalStorageHandler<ItemStack, Integer> getViewHandler() {
        return IngredientComponents.ITEMSTACK
                .getCapability(Capabilities.IngredientComponentTerminalStorageHandler.INGREDIENT)
                .orElseThrow(() -> new IllegalStateException("Could not find an ingredient terminal storage handler"));
    }

    private void store(int channel, ItemStack instance) {
        channelIngredients.computeIfAbsent(channel, (c) -> IngredientCollectionHelpers
                .createCollapsedCollection(IngredientComponents.ITEMSTACK)).add(instance);
    }

    private IIngredientCollection<ItemStack, Integer> getChannelIngredients(int channel) {
        IIngredientCollection<ItemStack, Integer> ingredients = channelIngredients.get(channel);
        return ingredients == null ? new IngredientCollectionEmpty<>(IngredientComponents.ITEMSTACK) : ingredients;
    }

    private Int2LongMap getQuantitiesPerChannel(int[] channels, ItemStack instance) {
        return TerminalStorageChannels.getInstanceQuantitiesPerChannel(IngredientComponents.ITEMSTACK, channels,
                this::getChannelIngredients, instance);
    }

    private static String getTranslationKey(Component component) {
        return ((TranslatableContents) component.getContents()).getKey();
    }

    private static Object[] getTranslationArgs(Component component) {
        return ((TranslatableContents) component.getContents()).getArgs();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testQuantitiesWithoutChannels(GameTestHelper helper) {
        helper.assertTrue(getQuantitiesPerChannel(new int[]{}, new ItemStack(Items.STONE)).isEmpty(),
                "Nothing should be found without channels");

        store(0, new ItemStack(Items.STONE, 5));
        helper.assertTrue(getQuantitiesPerChannel(new int[]{}, new ItemStack(Items.STONE)).isEmpty(),
                "Nothing should be found without channels");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testQuantitiesInSingleChannel(GameTestHelper helper) {
        store(0, new ItemStack(Items.STONE, 5));
        store(3, new ItemStack(Items.DIRT, 7));

        Int2LongMap quantities = getQuantitiesPerChannel(new int[]{0, 3}, new ItemStack(Items.STONE, 64));
        helper.assertTrue(quantities.size() == 1, "Stone should be found in exactly one channel");
        helper.assertTrue(quantities.get(0) == 5, "5 stone should be found in channel 0, but got " + quantities.get(0));
        helper.assertTrue(!quantities.containsKey(3), "No stone should be found in channel 3");

        helper.assertTrue(getQuantitiesPerChannel(new int[]{0, 3}, new ItemStack(Items.DIAMOND)).isEmpty(),
                "An unstored item should not be found in any channel");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testQuantitiesInMultipleChannels(GameTestHelper helper) {
        store(0, new ItemStack(Items.STONE, 5));
        store(0, new ItemStack(Items.STONE, 3));
        store(1, new ItemStack(Items.DIRT, 7));
        store(2, new ItemStack(Items.STONE, 64));

        Int2LongMap quantities = getQuantitiesPerChannel(new int[]{0, 1, 2}, new ItemStack(Items.STONE));
        helper.assertTrue(quantities.size() == 2, "Stone should be found in exactly two channels");
        helper.assertTrue(quantities.get(0) == 8, "8 stone should be found in channel 0, but got " + quantities.get(0));
        helper.assertTrue(quantities.get(2) == 64, "64 stone should be found in channel 2, but got " + quantities.get(2));
        helper.assertTrue(quantities.keySet().toIntArray()[0] == 0,
                "Channels should be ordered as they were given");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testTooltipLinesWithoutChannels(GameTestHelper helper) {
        helper.assertTrue(TerminalStorageChannels.createChannelTooltipLines(getViewHandler(),
                        new ItemStack(Items.STONE), getQuantitiesPerChannel(new int[]{0}, new ItemStack(Items.STONE)))
                .isEmpty(), "No tooltip lines should be shown for an unstored item");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testTooltipLinesForSingleChannel(GameTestHelper helper) {
        store(3, new ItemStack(Items.STONE, 5));

        List<Component> lines = TerminalStorageChannels.createChannelTooltipLines(getViewHandler(),
                new ItemStack(Items.STONE), getQuantitiesPerChannel(new int[]{0, 3}, new ItemStack(Items.STONE)));
        helper.assertTrue(lines.size() == 1, "A single tooltip line should be shown");
        helper.assertTrue(getTranslationKey(lines.get(0))
                        .equals("gui.integratedterminals.terminal_storage.tooltip.channel"),
                "The tooltip line should indicate the channel");
        helper.assertTrue(getTranslationArgs(lines.get(0))[0].equals("3"),
                "The tooltip line should indicate channel 3");

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testTooltipLinesForMultipleChannels(GameTestHelper helper) {
        store(0, new ItemStack(Items.STONE, 5));
        store(3, new ItemStack(Items.STONE, 64));

        List<Component> lines = TerminalStorageChannels.createChannelTooltipLines(getViewHandler(),
                new ItemStack(Items.STONE), getQuantitiesPerChannel(new int[]{0, 3}, new ItemStack(Items.STONE)));
        helper.assertTrue(lines.size() == 3, "A tooltip line should be shown for each channel, with a header");
        helper.assertTrue(getTranslationKey(lines.get(0))
                        .equals("gui.integratedterminals.terminal_storage.tooltip.channels"),
                "The first tooltip line should be the header");
        helper.assertTrue(getTranslationArgs(lines.get(1))[0].equals("0")
                        && getTranslationArgs(lines.get(1))[1].equals("5"),
                "The second tooltip line should indicate 5 in channel 0");
        helper.assertTrue(getTranslationArgs(lines.get(2))[0].equals("3")
                        && getTranslationArgs(lines.get(2))[1].equals("64"),
                "The third tooltip line should indicate 64 in channel 3");

        helper.succeed();
    }

}
