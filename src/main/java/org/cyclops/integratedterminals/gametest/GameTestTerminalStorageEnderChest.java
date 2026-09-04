package org.cyclops.integratedterminals.gametest;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabEnderChest;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabEnderChestCommon;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStoragePart;
import org.cyclops.integratedterminals.item.ItemTerminalStoragePortable;
import org.cyclops.integratedterminals.part.PartTypeTerminalStorage;
import org.cyclops.integratedterminals.part.PartTypes;

import java.util.List;
import java.util.Optional;

/**
 * Game tests for the ender-upgraded storage terminal and its ender chest tab.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestTerminalStorageEnderChest {

    private static final BlockPos POS = new BlockPos(1, 2, 1);

    /**
     * Places a cable with a storage terminal part on it.
     */
    private static PartTarget placeTerminal(GameTestHelper helper) {
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        NetworkHelpers.initNetwork(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH,
                PartTypes.TERMINAL_STORAGE, new ItemStack(PartTypes.TERMINAL_STORAGE.getItem()));
        return PartTarget.fromCenter(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH));
    }

    private static PartTypeTerminalStorage.State getPartState(GameTestHelper helper, BlockPos pos) {
        return (PartTypeTerminalStorage.State) PartHelpers
                .getPart(PartPos.of(helper.getLevel(), helper.absolutePos(pos), Direction.NORTH))
                .getState();
    }

    /**
     * Simulates a player right-clicking the storage terminal part with the given item.
     */
    static void enderUpgradePart(GameTestHelper helper, BlockPos pos, ServerPlayer player, ItemStack heldItem) {
        BlockPos absolutePos = helper.absolutePos(pos);
        PartTypeTerminalStorage.State state = getPartState(helper, pos);
        BlockHitResult hitResult = new BlockHitResult(
                Vec3.atCenterOf(absolutePos), Direction.NORTH, absolutePos, false);
        try {
            PartTypes.TERMINAL_STORAGE.onPartActivated(state, absolutePos, helper.getLevel(), player,
                    InteractionHand.MAIN_HAND, heldItem, hitResult);
        } catch (UnsupportedOperationException e) {
            // When the terminal is not ender-upgraded, the terminal gui is opened instead,
            // which sends a cyclopscore:value_notify packet that fails in the game test environment,
            // because the embedded channel does not have custom NeoForge channels negotiated.
        }
    }

    /**
     * Tests that right-clicking a storage terminal with an eye of ender ender-upgrades it.
     */
    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testEnderUpgradeWithEyeOfEnder(GameTestHelper helper) {
        ServerPlayer player = GameTestAdvancementsIntegratedTerminals.createMockServerPlayer(helper);
        placeTerminal(helper);

        helper.assertTrue(!getPartState(helper, POS).isEnderUpgraded(),
                "Terminal should not be ender-upgraded before right-clicking it with an eye of ender");

        // Creative players don't consume the eye of ender, so make sure we're not in creative mode
        player.getAbilities().instabuild = false;

        ItemStack heldItem = new ItemStack(Items.ENDER_EYE, 2);
        enderUpgradePart(helper, POS, player, heldItem);

        helper.assertTrue(getPartState(helper, POS).isEnderUpgraded(),
                "Terminal should be ender-upgraded after right-clicking it with an eye of ender");
        helper.assertTrue(heldItem.getCount() == 1,
                "One eye of ender should have been consumed, but the stack size is " + heldItem.getCount());

        helper.succeed();
    }

    /**
     * Tests that right-clicking a storage terminal with another item does not ender-upgrade it.
     */
    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testEnderUpgradeWithOtherItem(GameTestHelper helper) {
        ServerPlayer player = GameTestAdvancementsIntegratedTerminals.createMockServerPlayer(helper);
        placeTerminal(helper);

        player.getAbilities().instabuild = false;

        ItemStack heldItem = new ItemStack(Items.ENDER_PEARL, 2);
        enderUpgradePart(helper, POS, player, heldItem);

        helper.assertTrue(!getPartState(helper, POS).isEnderUpgraded(),
                "Terminal should not be ender-upgraded by an ender pearl");
        helper.assertTrue(heldItem.getCount() == 2, "No ender pearl should have been consumed");

        helper.succeed();
    }

    /**
     * Tests that breaking an ender-upgraded storage terminal gives back the eye of ender.
     */
    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testEnderUpgradedTerminalDropsEyeOfEnder(GameTestHelper helper) {
        ServerPlayer player = GameTestAdvancementsIntegratedTerminals.createMockServerPlayer(helper);
        PartTarget partTarget = placeTerminal(helper);
        enderUpgradePart(helper, POS, player, new ItemStack(Items.ENDER_EYE));

        PartTypeTerminalStorage.State state = getPartState(helper, POS);
        List<ItemStack> drops = Lists.newArrayList();
        PartTypes.TERMINAL_STORAGE.addDrops(partTarget, state, drops, true, false);

        helper.assertTrue(drops.stream().anyMatch(drop -> drop.is(Items.ENDER_EYE)),
                "An eye of ender should have been dropped");
        helper.assertTrue(!state.isEnderUpgraded(),
                "The ender upgrade should have been removed after dropping the eye of ender");

        helper.succeed();
    }

    /**
     * Tests that the ender chest tab is only interactable after the terminal has been ender-upgraded.
     */
    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testEnderChestTabAvailability(GameTestHelper helper) {
        ServerPlayer player = GameTestAdvancementsIntegratedTerminals.createMockServerPlayer(helper);
        PartTarget partTarget = placeTerminal(helper);

        ContainerTerminalStoragePart container = GameTestAdvancementsIntegratedTerminals
                .createSilentContainer(player, partTarget);
        TerminalStorageTabEnderChestCommon tabCommon = (TerminalStorageTabEnderChestCommon)
                container.getTabCommon(TerminalStorageTabEnderChest.NAME.toString());

        helper.assertTrue(tabCommon != null, "The ender chest tab should always be present in the container");
        helper.assertTrue(!container.isEnderUpgraded(), "The terminal should not be ender-upgraded yet");
        helper.assertTrue(!tabCommon.isAvailable(), "The ender chest tab should not be available yet");

        Slot enderSlot = container.getSlot(tabCommon.getFirstSlotIndex());
        helper.assertTrue(!enderSlot.mayPlace(new ItemStack(Items.STICK)),
                "Ender chest slots should not accept items before the terminal is ender-upgraded");

        enderUpgradePart(helper, POS, player, new ItemStack(Items.ENDER_EYE));

        helper.assertTrue(container.isEnderUpgraded(), "The terminal should be ender-upgraded");
        helper.assertTrue(tabCommon.isAvailable(), "The ender chest tab should be available");
        helper.assertTrue(enderSlot.mayPlace(new ItemStack(Items.STICK)),
                "Ender chest slots should accept items after the terminal is ender-upgraded");

        helper.succeed();
    }

    /**
     * Tests shift-clicking items from the player inventory into the ender chest, and back.
     */
    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testEnderChestQuickMove(GameTestHelper helper) {
        ServerPlayer player = GameTestAdvancementsIntegratedTerminals.createMockServerPlayer(helper);
        PartTarget partTarget = placeTerminal(helper);

        ContainerTerminalStoragePart container = GameTestAdvancementsIntegratedTerminals
                .createSilentContainer(player, partTarget);
        enderUpgradePart(helper, POS, player, new ItemStack(Items.ENDER_EYE));
        container.setSelectedTab(TerminalStorageTabEnderChest.NAME.toString());

        TerminalStorageTabEnderChestCommon tabCommon = (TerminalStorageTabEnderChestCommon)
                container.getTabCommon(TerminalStorageTabEnderChest.NAME.toString());
        int firstEnderSlot = tabCommon.getFirstSlotIndex();

        // Shift-click a stack from the player inventory into the ender chest
        container.getSlot(0).set(new ItemStack(Items.STICK, 16));
        container.quickMoveStack(player, 0);

        helper.assertTrue(container.getSlot(0).getItem().isEmpty(),
                "The player inventory slot should be empty after shift-clicking into the ender chest");
        helper.assertTrue(player.getEnderChestInventory().getItem(0).is(Items.STICK)
                        && player.getEnderChestInventory().getItem(0).getCount() == 16,
                "The ender chest should contain the shift-clicked items, but it contains "
                        + player.getEnderChestInventory().getItem(0));

        // Shift-click the stack back out of the ender chest
        container.quickMoveStack(player, firstEnderSlot);

        helper.assertTrue(player.getEnderChestInventory().getItem(0).isEmpty(),
                "The ender chest slot should be empty after shift-clicking out of it");
        helper.assertTrue(player.getInventory().countItem(Items.STICK) == 16,
                "The player inventory should contain the shift-clicked items again");

        helper.succeed();
    }

    /**
     * Tests that a portable storage terminal can be ender-upgraded via crafting,
     * while retaining the network it was linked to.
     */
    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testPortableEnderUpgradeRecipe(GameTestHelper helper) {
        Optional<RecipeHolder<?>> recipeHolder = helper.getLevel().getRecipeManager()
                .byKey(ResourceLocation.parse("integratedterminals:crafting/terminal_storage_portable_ender_upgrade"));
        helper.assertTrue(recipeHolder.isPresent(), "The portable terminal ender upgrade recipe should be registered");
        CraftingRecipe recipe = (CraftingRecipe) recipeHolder.get().value();

        ItemStack terminal = new ItemStack(org.cyclops.integratedterminals.RegistryEntries.ITEM_TERMINAL_STORAGE_PORTABLE.get());
        ItemTerminalStoragePortable.setGroupId(terminal, 42);
        CraftingInput input = CraftingInput.of(2, 1, List.of(terminal, new ItemStack(Items.ENDER_EYE)));

        helper.assertTrue(recipe.matches(input, helper.getLevel()), "The ender upgrade recipe should match");
        ItemStack output = recipe.assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(ItemTerminalStoragePortable.isEnderUpgraded(output),
                "The crafted portable terminal should be ender-upgraded");
        helper.assertTrue(ItemTerminalStoragePortable.getGroupId(output) == 42,
                "The crafted portable terminal should retain its network link");

        // Upgrading an already upgraded terminal should not be possible
        CraftingInput upgradedInput = CraftingInput.of(2, 1, List.of(output, new ItemStack(Items.ENDER_EYE)));
        helper.assertTrue(recipe.assemble(upgradedInput, helper.getLevel().registryAccess()).isEmpty(),
                "An already ender-upgraded portable terminal should not be upgradable again");

        helper.succeed();
    }

}
