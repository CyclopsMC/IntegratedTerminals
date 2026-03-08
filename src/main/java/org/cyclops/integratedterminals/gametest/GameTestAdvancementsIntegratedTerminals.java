package org.cyclops.integratedterminals.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariableInvalidateListener;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStoragePart;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageState;
import org.cyclops.integratedterminals.part.PartTypes;

import java.util.Optional;
import java.util.UUID;

/**
 * Game tests for all advancements in IntegratedTerminals.
 * @author rubensworks
 */
public class GameTestAdvancementsIntegratedTerminals {

    public static final String TEMPLATE_EMPTY = Reference.MOD_ID + ":empty10";

    private static boolean hasAdvancementUnlocked(ServerPlayer player, String advancementId) {
        Identifier id = Identifier.parse(advancementId);
        ServerAdvancementManager manager = ServerLifecycleHooks.getCurrentServer().getAdvancements();
        AdvancementHolder holder = manager.get(id);
        if (holder == null) {
            return false;
        }
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerAdvancements(player).getOrStartProgress(holder).isDone();
    }

    private static void assertAdvancementNotUnlocked(GameTestHelper helper, ServerPlayer player, String advancementId) {
        helper.assertTrue(!hasAdvancementUnlocked(player, advancementId),
                "Advancement should NOT have been obtained: " + advancementId);
    }

    /**
     * Creates a {@link ContainerTerminalStoragePart} that suppresses {@code cyclopscore:value_notify}
     * packet sends. This is needed in game tests where the embedded network channel does not have
     * custom NeoForge channels negotiated, causing {@link UnsupportedOperationException} whenever
     * {@link org.cyclops.cyclopscore.inventory.container.ContainerExtended#setValue} tries to send
     * a packet to the client.
     *
     * <p>Because Java uses virtual dispatch even during superclass construction, overriding
     * {@code setValue} here also prevents the packet sends that happen inside the
     * {@link ContainerTerminalStoragePart} constructor (e.g. from {@code setSelectedChannel}).
     */
    private static ContainerTerminalStoragePart createSilentContainer(ServerPlayer player, PartTarget partTarget) {
        return new ContainerTerminalStoragePart(
                1, player.getInventory(), partTarget, PartTypes.TERMINAL_STORAGE,
                Optional.empty(), new TerminalStorageState(() -> {})) {
            @Override
            public void setValue(int valueId, CompoundTag value) {
                // Suppress cyclopscore:value_notify packet sends in the game test environment.
            }
        };
    }

    /**
     * Creates a mock server player in the level.
     *
     * <p>This replicates {@link GameTestHelper#makeMockServerPlayerInLevel()} but catches the
     * {@link UnsupportedOperationException} that IntegratedDynamics throws when it tries to send
     * an {@code integrateddynamics:all_labels} packet during the player login event, because the
     * game test environment's embedded channel does not have custom NeoForge channels negotiated.
     * By the time that exception fires, {@code placeNewPlayer} has already fully set up the player:
     * {@link ServerPlayer#connection} is assigned, {@link ServerPlayer#initInventoryMenu()} has
     * been called (enabling the {@code containerListener} that fires advancement criteria), and the
     * player has been added to both the world and the player list.
     */
    private static ServerPlayer createMockServerPlayer(GameTestHelper helper) {
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(
                new GameProfile(UUID.randomUUID(), "test-mock-player"), false);
        ServerPlayer player = new ServerPlayer(
                helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation()) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return true;
            }
        };
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        try {
            helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        } catch (Exception ignored) {
            // IntegratedDynamics sends a custom packet (integrateddynamics:all_labels) during the
            // PlayerLoggedInEvent that fails in game tests because no channel negotiation occurs.
            // Everything needed for advancement tests is set up before that event fires.
        }
        return player;
    }

    /**
     * Tests the root advancement, triggered by having a part_display_panel in the inventory.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRoot(GameTestHelper helper) {
        ServerPlayer player = createMockServerPlayer(helper);
        // Give player a part_display_panel to trigger the inventory_changed criterion
        player.getInventory().setItem(0, new ItemStack(
                BuiltInRegistries.ITEM.getValue(Identifier.parse("integrateddynamics:part_display_panel"))));
        player.inventoryMenu.broadcastChanges();
        helper.succeedWhen(() -> helper.assertTrue(
                hasAdvancementUnlocked(player, "integratedterminals:root"),
                "root advancement not unlocked"));
    }

    /**
     * Tests the menril_glass advancement, triggered by having menril_glass in the inventory.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementMenrilGlass(GameTestHelper helper) {
        ServerPlayer player = createMockServerPlayer(helper);
        // Give player menril_glass to trigger the inventory_changed criterion
        player.getInventory().setItem(0, new ItemStack(
                BuiltInRegistries.ITEM.getValue(Identifier.parse("integratedterminals:menril_glass"))));
        player.inventoryMenu.broadcastChanges();
        helper.succeedWhen(() -> helper.assertTrue(
                hasAdvancementUnlocked(player, "integratedterminals:storage_terminal/menril_glass"),
                "menril_glass advancement not unlocked"));
    }

    /**
     * Tests the craft_storage_terminal advancement, triggered by crafting a part_terminal_storage.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementCraftStorageTerminal(GameTestHelper helper) {
        ServerPlayer player = createMockServerPlayer(helper);
        // Simulate crafting a part_terminal_storage by posting the ItemCraftedEvent
        ItemStack craftedItem = new ItemStack(
                BuiltInRegistries.ITEM.getValue(Identifier.parse("integratedterminals:part_terminal_storage")));
        NeoForge.EVENT_BUS.post(new PlayerEvent.ItemCraftedEvent(player, craftedItem, new SimpleContainer(9)));
        helper.succeedWhen(() -> helper.assertTrue(
                hasAdvancementUnlocked(player, "integratedterminals:storage_terminal/craft_storage_terminal"),
                "craft_storage_terminal advancement not unlocked"));
    }

    /**
     * Tests the gui_storage_terminal advancement, triggered by opening the terminal storage GUI.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementGuiStorageTerminal(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        ServerPlayer player = createMockServerPlayer(helper);

        // Place cable block and add terminal_storage part
        helper.setBlock(pos, RegistryEntries.BLOCK_CABLE.value());
        NetworkHelpers.initNetwork(helper.getLevel(), helper.absolutePos(pos), Direction.NORTH);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(pos), Direction.NORTH,
                PartTypes.TERMINAL_STORAGE, new ItemStack(PartTypes.TERMINAL_STORAGE.getItem()));

        // Create the container directly (not via openMenu, which triggers custom-packet sends that
        // fail in the game test environment's embedded channel). Fire PlayerContainerEvent.Open to
        // trigger the cyclopscore:container_gui_open advancement criterion.
        PartPos partPos = PartPos.of(helper.getLevel(), helper.absolutePos(pos), Direction.NORTH);
        PartTarget partTarget = PartTarget.fromCenter(partPos);
        ContainerTerminalStoragePart container = createSilentContainer(player, partTarget);
        NeoForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, container));

        helper.succeedWhen(() -> helper.assertTrue(
                hasAdvancementUnlocked(player, "integratedterminals:storage_terminal/gui_storage_terminal"),
                "gui_storage_terminal advancement not unlocked"));
    }

    /**
     * Tests the filter_enchantable advancement, triggered by setting an itemstack_enchantable operator variable
     * in the terminal storage filter slots.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementFilterEnchantable(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        ServerPlayer player = createMockServerPlayer(helper);

        // Place cable block and add terminal_storage part
        helper.setBlock(pos, RegistryEntries.BLOCK_CABLE.value());
        NetworkHelpers.initNetwork(helper.getLevel(), helper.absolutePos(pos), Direction.NORTH);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(pos), Direction.NORTH,
                PartTypes.TERMINAL_STORAGE, new ItemStack(PartTypes.TERMINAL_STORAGE.getItem()));

        // Create the container directly (not via openMenu, which triggers custom-packet sends that
        // fail in the game test environment's embedded channel).
        PartPos partPos = PartPos.of(helper.getLevel(), helper.absolutePos(pos), Direction.NORTH);
        PartTarget partTarget = PartTarget.fromCenter(partPos);
        ContainerTerminalStoragePart container = createSilentContainer(player, partTarget);

        // Directly trigger the advancement by calling onVariableContentsUpdated with a mock
        // IVariable that returns the itemstack_enchantable operator value. This simulates what
        // happens when a valid operator variable is placed in the filter slot and the network
        // evaluates it — bypassing the InventoryVariableEvaluator which returns null in game
        // tests because the variable item is not registered in the network's variable store.
        INetwork network = container.getNetwork().get();
        ValueTypeOperator.ValueOperator operatorValue =
                ValueTypeOperator.ValueOperator.of(Operators.OBJECT_ITEMSTACK_ISENCHANTABLE);
        IVariable<ValueTypeOperator.ValueOperator> mockVariable = new IVariable<>() {
            @Override
            public IValueType<ValueTypeOperator.ValueOperator> getType() {
                return ValueTypes.OPERATOR;
            }

            @Override
            public ValueTypeOperator.ValueOperator getValue() throws EvaluationException {
                return operatorValue;
            }

            @Override
            public void addInvalidationListener(IVariableInvalidateListener listener) {}

            @Override
            public void removeInvalidationListener(IVariableInvalidateListener listener) {}

            @Override
            public void invalidate() {}
        };
        container.onVariableContentsUpdated(network, mockVariable);

        helper.succeedWhen(() -> helper.assertTrue(
                hasAdvancementUnlocked(player, "integratedterminals:storage_terminal_filtering/filter_enchantable"),
                "filter_enchantable advancement not unlocked"));
    }

    /**
     * Negative test: giving a non-matching item should NOT trigger the root advancement.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementRootNegative(GameTestHelper helper) {
        ServerPlayer player = createMockServerPlayer(helper);
        // Give player a stick (not part_display_panel) - should NOT trigger the root advancement
        player.getInventory().setItem(0, new ItemStack(
                BuiltInRegistries.ITEM.getValue(Identifier.parse("minecraft:stick"))));
        player.inventoryMenu.broadcastChanges();
        helper.succeedWhen(() -> assertAdvancementNotUnlocked(helper, player, "integratedterminals:root"));
    }

    /**
     * Negative test: giving a non-matching item should NOT trigger the menril_glass advancement.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementMenrilGlassNegative(GameTestHelper helper) {
        ServerPlayer player = createMockServerPlayer(helper);
        // Give player glass (not menril_glass) - should NOT trigger the menril_glass advancement
        player.getInventory().setItem(0, new ItemStack(
                BuiltInRegistries.ITEM.getValue(Identifier.parse("minecraft:glass"))));
        player.inventoryMenu.broadcastChanges();
        helper.succeedWhen(() -> assertAdvancementNotUnlocked(helper, player,
                "integratedterminals:storage_terminal/menril_glass"));
    }

    /**
     * Negative test: crafting a non-matching item should NOT trigger the craft_storage_terminal advancement.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementCraftStorageTerminalNegative(GameTestHelper helper) {
        ServerPlayer player = createMockServerPlayer(helper);
        // Simulate crafting a stick (not part_terminal_storage) - should NOT trigger the advancement
        ItemStack craftedItem = new ItemStack(
                BuiltInRegistries.ITEM.getValue(Identifier.parse("minecraft:stick")));
        NeoForge.EVENT_BUS.post(new PlayerEvent.ItemCraftedEvent(player, craftedItem, new SimpleContainer(9)));
        helper.succeedWhen(() -> assertAdvancementNotUnlocked(helper, player,
                "integratedterminals:storage_terminal/craft_storage_terminal"));
    }

    /**
     * Negative test: opening a non-matching container should NOT trigger the gui_storage_terminal advancement.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementGuiStorageTerminalNegative(GameTestHelper helper) {
        ServerPlayer player = createMockServerPlayer(helper);
        // Fire PlayerContainerEvent.Open with the player's inventory menu (not ContainerTerminalStorageBase)
        NeoForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.inventoryMenu));
        helper.succeedWhen(() -> assertAdvancementNotUnlocked(helper, player,
                "integratedterminals:storage_terminal/gui_storage_terminal"));
    }

    /**
     * Negative test: setting a non-matching operator should NOT trigger the filter_enchantable advancement.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void testAdvancementFilterEnchantableNegative(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        ServerPlayer player = createMockServerPlayer(helper);

        // Place cable block and add terminal_storage part
        helper.setBlock(pos, RegistryEntries.BLOCK_CABLE.value());
        NetworkHelpers.initNetwork(helper.getLevel(), helper.absolutePos(pos), Direction.NORTH);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(pos), Direction.NORTH,
                PartTypes.TERMINAL_STORAGE, new ItemStack(PartTypes.TERMINAL_STORAGE.getItem()));

        // Create the container directly
        PartPos partPos = PartPos.of(helper.getLevel(), helper.absolutePos(pos), Direction.NORTH);
        PartTarget partTarget = PartTarget.fromCenter(partPos);
        ContainerTerminalStoragePart container = createSilentContainer(player, partTarget);

        // Use OBJECT_ITEMSTACK_ISENCHANTED (not OBJECT_ITEMSTACK_ISENCHANTABLE) - should NOT trigger the advancement
        INetwork network = container.getNetwork().get();
        ValueTypeOperator.ValueOperator operatorValue =
                ValueTypeOperator.ValueOperator.of(Operators.OBJECT_ITEMSTACK_ISENCHANTED);
        IVariable<ValueTypeOperator.ValueOperator> mockVariable = new IVariable<>() {
            @Override
            public IValueType<ValueTypeOperator.ValueOperator> getType() {
                return ValueTypes.OPERATOR;
            }

            @Override
            public ValueTypeOperator.ValueOperator getValue() throws EvaluationException {
                return operatorValue;
            }

            @Override
            public void addInvalidationListener(IVariableInvalidateListener listener) {}

            @Override
            public void removeInvalidationListener(IVariableInvalidateListener listener) {}

            @Override
            public void invalidate() {}
        };
        container.onVariableContentsUpdated(network, mockVariable);

        helper.succeedWhen(() -> assertAdvancementNotUnlocked(helper, player,
                "integratedterminals:storage_terminal_filtering/filter_enchantable"));
    }

}
