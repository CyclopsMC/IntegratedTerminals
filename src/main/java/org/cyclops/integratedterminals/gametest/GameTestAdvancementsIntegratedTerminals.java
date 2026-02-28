package org.cyclops.integratedterminals.gametest;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentCommon;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStoragePart;
import org.cyclops.integratedterminals.part.PartTypes;

/**
 * Game tests for all advancements in IntegratedTerminals.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestAdvancementsIntegratedTerminals {

    private static boolean hasAdvancementUnlocked(ServerPlayer player, String advancementId) {
        ResourceLocation id = ResourceLocation.parse(advancementId);
        ServerAdvancementManager manager = player.server.getAdvancements();
        AdvancementHolder holder = manager.get(id);
        if (holder == null) {
            return false;
        }
        return player.server.getPlayerList().getPlayerAdvancements(player).getOrStartProgress(holder).isDone();
    }

    /**
     * Tests the root advancement, triggered by having a part_display_panel in the inventory.
     */
    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testAdvancementRoot(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Give player a part_display_panel to trigger the inventory_changed criterion
        player.getInventory().setItem(0, new ItemStack(
                BuiltInRegistries.ITEM.get(ResourceLocation.parse("integrateddynamics:part_display_panel"))));
        player.inventoryMenu.broadcastChanges();
        helper.succeedWhen(() -> helper.assertTrue(
                hasAdvancementUnlocked(player, "integratedterminals:root"),
                "root advancement not unlocked"));
    }

    /**
     * Tests the menril_glass advancement, triggered by having menril_glass in the inventory.
     */
    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testAdvancementMenrilGlass(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Give player menril_glass to trigger the inventory_changed criterion
        player.getInventory().setItem(0, new ItemStack(
                BuiltInRegistries.ITEM.get(ResourceLocation.parse("integratedterminals:menril_glass"))));
        player.inventoryMenu.broadcastChanges();
        helper.succeedWhen(() -> helper.assertTrue(
                hasAdvancementUnlocked(player, "integratedterminals:storage_terminal/menril_glass"),
                "menril_glass advancement not unlocked"));
    }

    /**
     * Tests the craft_storage_terminal advancement, triggered by crafting a part_terminal_storage.
     */
    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testAdvancementCraftStorageTerminal(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // Simulate crafting a part_terminal_storage by posting the ItemCraftedEvent
        ItemStack craftedItem = new ItemStack(
                BuiltInRegistries.ITEM.get(ResourceLocation.parse("integratedterminals:part_terminal_storage")));
        NeoForge.EVENT_BUS.post(new PlayerEvent.ItemCraftedEvent(player, craftedItem, new SimpleContainer(9)));
        helper.succeedWhen(() -> helper.assertTrue(
                hasAdvancementUnlocked(player, "integratedterminals:storage_terminal/craft_storage_terminal"),
                "craft_storage_terminal advancement not unlocked"));
    }

    /**
     * Tests the gui_storage_terminal advancement, triggered by opening the terminal storage GUI.
     */
    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testAdvancementGuiStorageTerminal(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();

        // Place cable block and add terminal_storage part
        helper.setBlock(pos, RegistryEntries.BLOCK_CABLE.value());
        NetworkHelpers.initNetwork(helper.getLevel(), helper.absolutePos(pos), Direction.NORTH);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(pos), Direction.NORTH,
                PartTypes.TERMINAL_STORAGE, new ItemStack(PartTypes.TERMINAL_STORAGE.getItem()));

        // Open the terminal storage container (fires PlayerContainerEvent.Open which triggers the advancement)
        PartPos partPos = PartPos.of(helper.getLevel(), helper.absolutePos(pos), Direction.NORTH);
        PartHelpers.openContainerPart(player, partPos, PartTypes.TERMINAL_STORAGE);

        helper.succeedWhen(() -> helper.assertTrue(
                hasAdvancementUnlocked(player, "integratedterminals:storage_terminal/gui_storage_terminal"),
                "gui_storage_terminal advancement not unlocked"));
    }

    /**
     * Tests the filter_enchantable advancement, triggered by setting an itemstack_enchantable operator variable
     * in the terminal storage filter slots.
     */
    @GameTest(template = "empty", templateNamespace = "cyclopscore")
    public void testAdvancementFilterEnchantable(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();

        // Place cable block and add terminal_storage part
        helper.setBlock(pos, RegistryEntries.BLOCK_CABLE.value());
        NetworkHelpers.initNetwork(helper.getLevel(), helper.absolutePos(pos), Direction.NORTH);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(pos), Direction.NORTH,
                PartTypes.TERMINAL_STORAGE, new ItemStack(PartTypes.TERMINAL_STORAGE.getItem()));

        // Open the terminal storage container
        PartPos partPos = PartPos.of(helper.getLevel(), helper.absolutePos(pos), Direction.NORTH);
        PartHelpers.openContainerPart(player, partPos, PartTypes.TERMINAL_STORAGE);

        // Place the itemstack_enchantable operator variable in the filter slot of the itemstack tab
        if (player.containerMenu instanceof ContainerTerminalStoragePart container) {
            // Get the item tab (minecraft:itemstack)
            Object tabObject = container.getTabCommon("minecraft:itemstack");
            if (tabObject instanceof TerminalStorageTabIngredientComponentCommon<?, ?> tab) {
                int slotIdx = tab.getVariableSlotNumberStart();
                // Create a variable that holds the itemstack_enchantable operator as a value
                ItemStack operatorVariable = GameTestHelpersIntegratedDynamics.createVariableForValue(
                        helper.getLevel(), ValueTypes.OPERATOR,
                        ValueTypeOperator.ValueOperator.of(Operators.OBJECT_ITEMSTACK_ISENCHANTABLE));
                // Place the variable in the first filter slot to trigger dirtyInv = true
                container.getSlot(slotIdx).set(operatorVariable);
                // Broadcast changes to trigger the tab update which fires the advancement event
                container.broadcastChanges();
            }
        }

        helper.succeedWhen(() -> helper.assertTrue(
                hasAdvancementUnlocked(player, "integratedterminals:storage_terminal_filtering/filter_enchantable"),
                "filter_enchantable advancement not unlocked"));
    }

}
