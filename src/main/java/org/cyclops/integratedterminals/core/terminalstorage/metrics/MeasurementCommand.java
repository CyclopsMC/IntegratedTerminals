package org.cyclops.integratedterminals.core.terminalstorage.metrics;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integratedterminals.part.PartTypeTerminalStorage;
import org.cyclops.integratedterminals.part.PartTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Debug-only command that builds a storage terminal network of a chosen size and drives opens on it.
 *
 * Registered only when {@link org.cyclops.integratedterminals.GeneralConfig#debugTerminalOpenMetrics} is set.
 * The network storage is synthetic: one {@link MeasurementStorage} per channel is exposed as an item
 * handler on a marker block, and that position is registered on the ingredient network directly.
 * This avoids needing thousands of block entities to reach 50k distinct stacks.
 */
public final class MeasurementCommand {

    /**
     * The block that carries a synthetic storage. Chosen because nothing else provides an item handler for it.
     */
    private static final net.minecraft.world.level.block.Block MARKER_BLOCK = Blocks.LODESTONE;

    private static final BlockPos CABLE_POS = new BlockPos(0, 5, 0);
    private static final BlockPos MARKER_ORIGIN = new BlockPos(4, 5, 0);
    private static final Direction PART_SIDE = Direction.NORTH;

    private static final Map<BlockPos, MeasurementStorage> STORAGES = new HashMap<>();
    private static final Map<Integer, MeasurementStorage> BY_CHANNEL = new HashMap<>();

    private MeasurementCommand() {
    }

    /**
     * Expose the synthetic storages as item handlers on the marker block.
     */
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.Item.BLOCK,
                (level, pos, state, blockEntity, side) -> STORAGES.get(pos), MARKER_BLOCK);
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("itmetrics")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));

        root.then(Commands.literal("setup")
                .then(Commands.argument("stacks", IntegerArgumentType.integer(1))
                        .then(Commands.argument("channels", IntegerArgumentType.integer(1, 16))
                                .executes(context -> setup(context.getSource(),
                                        IntegerArgumentType.getInteger(context, "stacks"),
                                        IntegerArgumentType.getInteger(context, "channels"))))));
        root.then(Commands.literal("change")
                .then(Commands.argument("fraction", DoubleArgumentType.doubleArg(0, 1))
                        .executes(context -> change(context.getSource(),
                                DoubleArgumentType.getDouble(context, "fraction")))));
        root.then(Commands.literal("open").executes(context -> open(context.getSource())));
        root.then(Commands.literal("close").executes(context -> close(context.getSource())));
        root.then(Commands.literal("tag")
                .then(Commands.argument("value", StringArgumentType.word())
                        .executes(context -> tag(context.getSource(),
                                StringArgumentType.getString(context, "value")))));
        root.then(Commands.literal("status").executes(context -> status(context.getSource())));
        root.then(Commands.literal("index").executes(context -> index(context.getSource())));
        root.then(Commands.literal("cluster")
                .then(Commands.argument("enabled", com.mojang.brigadier.arguments.BoolArgumentType.bool())
                        .executes(context -> cluster(context.getSource(),
                                com.mojang.brigadier.arguments.BoolArgumentType.getBool(context, "enabled")))));
        root.then(Commands.literal("crafting")
                .then(Commands.argument("recipes", IntegerArgumentType.integer(0))
                        .executes(context -> crafting(context.getSource(),
                                IntegerArgumentType.getInteger(context, "recipes")))));

        event.getDispatcher().register(root);
    }

    private static int setup(CommandSourceStack source, int stacks, int channels) {
        ServerLevel level = source.getLevel();

        // Clear anything a previous setup left behind
        STORAGES.clear();
        BY_CHANNEL.clear();

        // A cable with a storage terminal part on it
        level.setBlockAndUpdate(CABLE_POS, org.cyclops.integrateddynamics.RegistryEntries.BLOCK_CABLE.value().defaultBlockState());
        NetworkHelpers.initNetwork(level, CABLE_POS, PART_SIDE);
        PartHelpers.addPart(level, CABLE_POS, PART_SIDE, PartTypes.TERMINAL_STORAGE,
                new ItemStack(PartTypes.TERMINAL_STORAGE.getItem()));

        INetwork network = NetworkHelpers.getNetworkChecked(level, CABLE_POS, PART_SIDE);
        IPositionedAddonsNetworkIngredients<ItemStack, Integer> ingredientNetwork = NetworkHelpers
                .<ItemStack, Integer>getIngredientNetwork(Optional.of(network), IngredientComponents.ITEMSTACK)
                .orElseThrow(() -> new IllegalStateException("No item ingredient network"));

        // One synthetic storage per channel, spread evenly
        int perChannel = stacks / channels;
        int remainder = stacks % channels;
        StringBuilder summary = new StringBuilder();
        for (int channel = 0; channel < channels; channel++) {
            BlockPos markerPos = MARKER_ORIGIN.offset(channel * 2, 0, 0);
            level.setBlockAndUpdate(markerPos, MARKER_BLOCK.defaultBlockState());

            MeasurementStorage storage = new MeasurementStorage();
            int count = perChannel + (channel < remainder ? 1 : 0);
            storage.generate(count, 1000L + channel, level.registryAccess());
            STORAGES.put(markerPos, storage);
            BY_CHANNEL.put(channel, storage);

            PartPos partPos = PartPos.of(level, markerPos, Direction.UP);
            ingredientNetwork.addPosition(partPos, 0, channel);
            summary.append("\n  channel ").append(channel).append(": ").append(storage.composition());
        }

        ingredientNetwork.scheduleObservation();
        source.sendSuccess(() -> Component.literal("Set up " + stacks + " stacks over " + channels
                + " channel(s)." + summary), false);
        return 1;
    }

    private static int change(CommandSourceStack source, double fraction) {
        int changed = 0;
        for (MeasurementStorage storage : BY_CHANNEL.values()) {
            changed += storage.mutate(fraction, System.nanoTime());
        }
        ServerLevel level = source.getLevel();
        // The network only re-indexes while something observes it, and one observation pass does not
        // always settle, so drive the observer until the index stops moving. Without this, a change
        // made while the terminal is closed only shows up one open later.
        IPositionedAddonsNetworkIngredients<ItemStack, Integer> ingredientNetwork = NetworkHelpers
                .<ItemStack, Integer>getIngredientNetwork(
                        NetworkHelpers.getNetwork(level, CABLE_POS, PART_SIDE), IngredientComponents.ITEMSTACK)
                .orElseThrow(() -> new IllegalStateException("No item ingredient network"));
        String sizes = "";
        for (int pass = 0; pass < 10; pass++) {
            ingredientNetwork.scheduleObservation();
            ingredientNetwork.runObserverSync();
            StringBuilder current = new StringBuilder();
            for (int channel : BY_CHANNEL.keySet()) {
                current.append(" ").append(ingredientNetwork.getChannelIndex(channel).size());
            }
            if (current.toString().equals(sizes) && pass > 0) {
                break;
            }
            sizes = current.toString();
        }
        int finalChanged = changed;
        String finalSizes = sizes;
        source.sendSuccess(() -> Component.literal("Changed " + finalChanged
                + " stacks, index now" + finalSizes), false);
        return 1;
    }

    private static int open(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = source.getLevel();
        PartTypeTerminalStorage.State state = (PartTypeTerminalStorage.State) PartHelpers
                .getPart(PartPos.of(level, CABLE_POS, PART_SIDE))
                .getState();
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(CABLE_POS), PART_SIDE, CABLE_POS, false);
        PartTypes.TERMINAL_STORAGE.onPartActivated(state, CABLE_POS, level, player,
                InteractionHand.MAIN_HAND, new ItemStack(Items.STICK), hit);
        source.sendSuccess(() -> Component.literal("Opened terminal"), false);
        return 1;
    }

    private static int close(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        source.getPlayerOrException().closeContainer();
        source.sendSuccess(() -> Component.literal("Closed terminal"), false);
        return 1;
    }

    /**
     * Add up to the given number of vanilla crafting recipes to the network, so crafting options
     * are present in the terminal. They are advertised by a synthetic crafting interface per channel.
     */
    private static int crafting(CommandSourceStack source, int recipes) {
        ServerLevel level = source.getLevel();
        INetwork network = NetworkHelpers.getNetworkChecked(level, CABLE_POS, PART_SIDE);
        org.cyclops.integratedcrafting.api.network.ICraftingNetwork craftingNetwork =
                org.cyclops.integratedcrafting.core.CraftingHelpers.getCraftingNetworkChecked(network);

        List<net.minecraft.resources.ResourceKey<net.minecraft.world.item.crafting.Recipe<?>>> recipeKeys =
                new java.util.ArrayList<>();
        for (net.minecraft.world.item.crafting.RecipeHolder<?> holder : level.getServer().getRecipeManager().getRecipes()) {
            if (holder.value().getType() == net.minecraft.world.item.crafting.RecipeType.CRAFTING) {
                recipeKeys.add(holder.id());
            }
        }

        int added = 0;
        int skipped = 0;
        for (int channel : BY_CHANNEL.keySet()) {
            BlockPos markerPos = MARKER_ORIGIN.offset(channel * 2, 0, 0);
            MeasurementCraftingInterface craftingInterface = new MeasurementCraftingInterface(
                    org.cyclops.integrateddynamics.api.part.PrioritizedPartPos.of(
                            PartPos.of(level, markerPos, Direction.DOWN), 0));
            craftingNetwork.addCraftingInterface(channel, craftingInterface);
            for (int i = 0; i < Math.min(recipes, recipeKeys.size()); i++) {
                try {
                    org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition definition =
                            org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeDefinition
                                    .fromRecipeId(level, recipeKeys.get(i));
                    craftingInterface.addRecipe(definition);
                    craftingNetwork.addCraftingInterfaceRecipe(channel, craftingInterface, definition);
                    added++;
                } catch (RuntimeException e) {
                    skipped++;
                }
            }
        }
        int finalAdded = added;
        int finalSkipped = skipped;
        source.sendSuccess(() -> Component.literal("Added " + finalAdded + " crafting recipes ("
                + finalSkipped + " skipped, " + recipeKeys.size() + " available)"), false);
        return 1;
    }

    /**
     * Choose whether stacks added by a change all share one item type.
     */
    private static int cluster(CommandSourceStack source, boolean enabled) {
        MeasurementStorage.clusterNewStacks = enabled;
        source.sendSuccess(() -> Component.literal("New stacks from a change now use "
                + (enabled ? "a single item type" : "varied item types")), false);
        return 1;
    }

    private static int tag(CommandSourceStack source, String value) {
        System.setProperty("integratedterminals.debugTerminalOpenMetricsTag", value);
        source.sendSuccess(() -> Component.literal("Metrics tag: " + value), false);
        return 1;
    }

    /**
     * Report how many instances the network has indexed per channel, so a caller can wait
     * until a storage change has actually been observed.
     */
    private static int index(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        IPositionedAddonsNetworkIngredients<ItemStack, Integer> ingredientNetwork = NetworkHelpers
                .<ItemStack, Integer>getIngredientNetwork(
                        NetworkHelpers.getNetwork(level, CABLE_POS, PART_SIDE), IngredientComponents.ITEMSTACK)
                .orElseThrow(() -> new IllegalStateException("No item ingredient network"));
        StringBuilder builder = new StringBuilder("index");
        for (int channel : BY_CHANNEL.keySet()) {
            builder.append(" ").append(channel).append("=")
                    .append(ingredientNetwork.getChannelIndex(channel).size());
        }
        source.sendSuccess(() -> Component.literal(builder.toString()), false);
        return 1;
    }

    private static int status(CommandSourceStack source) {
        StringBuilder builder = new StringBuilder("Measurement storages:");
        BY_CHANNEL.forEach((channel, storage) ->
                builder.append("\n  channel ").append(channel).append(": ").append(storage.composition()));
        builder.append("\n  tag: ").append(MetricsCsv.tag());
        source.sendSuccess(() -> Component.literal(builder.toString()), false);
        return 1;
    }
}
