package org.cyclops.integratedterminals.gametest;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.commoncapabilities.api.ingredient.IPrototypedIngredient;
import org.cyclops.integratedcrafting.api.crafting.CraftingJob;
import org.cyclops.integratedcrafting.core.CraftingHelpers;
import org.cyclops.integratedcrafting.gametest.GameTestHelpersIntegratedCrafting;
import org.cyclops.integratedcrafting.part.PartTypeInterfaceCrafting;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.event.TerminalCraftingJobFinishedEvent;
import org.cyclops.integratedterminals.modcompat.integratedcrafting.TerminalCraftingOptionRecipeDefinition;
import org.cyclops.integratedterminals.modcompat.integratedcrafting.TerminalStorageTabIngredientCraftingHandlerCraftingNetwork;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Game tests for {@link TerminalCraftingJobFinishedEvent}.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestTerminalCraftingJobFinishedEvent {

    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);

    /**
     * A completed job is forwarded as a handler-agnostic event that carries the job's details.
     */
    @GameTest(template = "empty10", templateNamespace = Reference.MOD_ID, timeoutTicks = 4000)
    public void testCompletedJobIsForwarded(GameTestHelper helper) {
        GameTestHelpersIntegratedCrafting.INetworkPositions<PartTypeInterfaceCrafting.State> positions =
                GameTestHelpersIntegratedCrafting.createBasicNetwork(helper, POS);

        ChestBlockEntity chest = helper.getBlockEntity(POS.east());
        chest.setItem(0, new ItemStack(Items.OAK_PLANKS, 64));
        positions.interfaceRecipeAdders().get(0).accept(Triple.of(0, RecipeType.CRAFTING,
                ResourceLocation.fromNamespaceAndPath("minecraft", "chest")));

        UUID initiator = UUID.randomUUID();
        EventCollector collector = EventCollector.start(initiator);
        int[] startedJobId = new int[1];
        helper.startSequence()
                .thenIdle(20)
                .thenExecute(() -> startedJobId[0] = startJob(helper, initiator, new ItemStack(Items.CHEST, 1)))
                .thenWaitUntil(() -> helper.assertTrue(!hasRunningJobs(helper),
                        "The crafting jobs did not finish"))
                .thenExecute(() -> {
                    helper.assertTrue(collector.rootEvents.size() == 1,
                            "Expected exactly one event for the requested job, but got "
                                    + collector.rootEvents.size());

                    TerminalCraftingJobFinishedEvent event = collector.rootEvents.get(0);
                    helper.assertTrue(event.getCraftingJobId().equals(startedJobId[0]),
                            "The event did not carry the id of the started job");
                    helper.assertTrue(event.getChannel() == IPositionedAddonsNetworkIngredients.DEFAULT_CHANNEL,
                            "The event did not carry the channel the job ran in");
                    helper.assertTrue(event.getHandler().getId().equals(
                                    new TerminalStorageTabIngredientCraftingHandlerCraftingNetwork().getId()),
                            "The event did not carry the crafting network handler");
                    helper.assertTrue(hasOutput(event.getOutputs(), Items.CHEST),
                            "The event did not carry the crafted chest as output");

                    collector.stop();
                })
                .thenSucceed();
    }

    /**
     * Dependencies of a job are forwarded too, but are distinguishable from the requested job.
     */
    @GameTest(template = "empty10", templateNamespace = Reference.MOD_ID, timeoutTicks = 4000)
    public void testDependenciesAreForwardedAsNonRoot(GameTestHelper helper) {
        GameTestHelpersIntegratedCrafting.INetworkPositions<PartTypeInterfaceCrafting.State> positions =
                GameTestHelpersIntegratedCrafting.createBasicNetwork(helper, POS);

        // Only logs are stored, so the planks needed for the chest have to be crafted first
        ChestBlockEntity chest = helper.getBlockEntity(POS.east());
        chest.setItem(0, new ItemStack(Items.OAK_LOG, 64));
        positions.interfaceRecipeAdders().get(0).accept(Triple.of(0, RecipeType.CRAFTING,
                ResourceLocation.fromNamespaceAndPath("minecraft", "chest")));
        positions.interfaceRecipeAdders().get(0).accept(Triple.of(1, RecipeType.CRAFTING,
                ResourceLocation.fromNamespaceAndPath("minecraft", "oak_planks")));

        UUID initiator = UUID.randomUUID();
        EventCollector collector = EventCollector.start(initiator);
        helper.startSequence()
                .thenIdle(20)
                .thenExecute(() -> startJob(helper, initiator, new ItemStack(Items.CHEST, 1)))
                .thenWaitUntil(() -> helper.assertTrue(!hasRunningJobs(helper),
                        "The crafting jobs did not finish"))
                .thenExecute(() -> {
                    helper.assertTrue(collector.rootEvents.size() == 1,
                            "Expected exactly one event for the requested job, but got "
                                    + collector.rootEvents.size());
                    helper.assertTrue(collector.dependencyEvents > 0,
                            "Expected the crafted plank dependency to be forwarded as a non-root event");
                    collector.stop();
                })
                .thenSucceed();
    }

    private static boolean hasOutput(List<IPrototypedIngredient<?, ?>> outputs, Item item) {
        for (IPrototypedIngredient<?, ?> output : outputs) {
            if (output.getPrototype() instanceof ItemStack itemStack && itemStack.is(item)) {
                return true;
            }
        }
        return false;
    }

    private static int startJob(GameTestHelper helper, UUID initiator, ItemStack output) {
        INetwork network = getNetwork(helper);
        int channel = IPositionedAddonsNetworkIngredients.DEFAULT_CHANNEL;
        TerminalStorageTabIngredientCraftingHandlerCraftingNetwork handler =
                new TerminalStorageTabIngredientCraftingHandlerCraftingNetwork();
        Iterator<IRecipeDefinition> recipes = CraftingHelpers.getCraftingNetworkChecked(network)
                .getRecipeIndex(channel).getRecipes(IngredientComponents.ITEMSTACK, output, ItemMatch.ITEM);
        if (!recipes.hasNext()) {
            helper.fail("No recipe was available in the network for " + output);
        }
        ITerminalCraftingPlan<Integer> craftingPlan = handler.calculateCraftingPlan(network, channel,
                new TerminalCraftingOptionRecipeDefinition<>(IngredientComponents.ITEMSTACK, recipes.next()),
                output.getCount());
        try {
            handler.startCraftingJob(network, channel, craftingPlan, mockPlayer(helper, initiator), true);
        } catch (Exception e) {
            helper.fail("The crafting job could not be started: " + e.getMessage());
        }
        return craftingPlan.getId();
    }

    private static ServerPlayer mockPlayer(GameTestHelper helper, UUID initiator) {
        // Deliberately not added to the player list, so that no notification packet is sent
        return new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                new GameProfile(initiator, "test-mock-player"), ClientInformation.createDefault());
    }

    private static boolean hasRunningJobs(GameTestHelper helper) {
        Iterator<CraftingJob> craftingJobs = CraftingHelpers.getCraftingNetworkChecked(getNetwork(helper))
                .getCraftingJobs(IPositionedAddonsNetworkIngredients.WILDCARD_CHANNEL);
        return craftingJobs.hasNext();
    }

    private static INetwork getNetwork(GameTestHelper helper) {
        return NetworkHelpers.getNetwork(helper.getLevel(), helper.absolutePos(POS), null)
                .orElseThrow(() -> new IllegalStateException("Could not find a network"));
    }

    /**
     * Collects the forwarded events for one initiator.
     */
    public static class EventCollector {

        private final UUID initiator;
        private final List<TerminalCraftingJobFinishedEvent> rootEvents = Lists.newArrayList();
        private int dependencyEvents = 0;

        public EventCollector(UUID initiator) {
            this.initiator = initiator;
        }

        public static EventCollector start(UUID initiator) {
            EventCollector collector = new EventCollector(initiator);
            NeoForge.EVENT_BUS.register(collector);
            return collector;
        }

        public void stop() {
            NeoForge.EVENT_BUS.unregister(this);
        }

        @SubscribeEvent
        public void onCraftingJobFinished(TerminalCraftingJobFinishedEvent event) {
            if (!this.initiator.equals(event.getInitiator())) {
                return;
            }
            if (event.isRootJob()) {
                this.rootEvents.add(event);
            } else {
                this.dependencyEvents++;
            }
        }
    }

}
