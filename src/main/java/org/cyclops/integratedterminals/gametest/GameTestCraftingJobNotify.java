package org.cyclops.integratedterminals.gametest;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.commoncapabilities.IngredientComponents;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ItemMatch;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeDefinition;
import org.cyclops.integratedcrafting.api.crafting.CraftingJob;
import org.cyclops.integratedcrafting.api.network.ICraftingNetwork;
import org.cyclops.integratedcrafting.core.CraftingHelpers;
import org.cyclops.integratedcrafting.gametest.GameTestHelpersIntegratedCrafting;
import org.cyclops.integratedcrafting.part.PartTypeInterfaceCrafting;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetworkIngredients;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.modcompat.integratedcrafting.TerminalCraftingOptionRecipeDefinition;
import org.cyclops.integratedterminals.modcompat.integratedcrafting.TerminalStorageTabIngredientCraftingHandlerCraftingNetwork;

import com.google.common.collect.Lists;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.cyclops.integratedcrafting.api.event.CraftingJobFinishedEvent;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Game tests for requesting a notification when a crafting job started from a terminal is completed.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestCraftingJobNotify {

    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);

    /**
     * A job started with the notify option enabled carries the initiator and the notify flag.
     */
    @GameTest(template = "empty10", templateNamespace = Reference.MOD_ID, timeoutTicks = 2000)
    public void testStartCraftingJobWithNotify(GameTestHelper helper) {
        testStartCraftingJob(helper, true);
    }

    /**
     * A job started with the notify option disabled carries the initiator, but not the notify flag.
     */
    @GameTest(template = "empty10", templateNamespace = Reference.MOD_ID, timeoutTicks = 2000)
    public void testStartCraftingJobWithoutNotify(GameTestHelper helper) {
        testStartCraftingJob(helper, false);
    }

    private void testStartCraftingJob(GameTestHelper helper, boolean notifyOnCompletion) {
        prepareNetwork(helper);

        // This player is deliberately not added to the player list,
        // so that no notification packet is sent for the completed job.
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "test-mock-player"), ClientInformation.createDefault());

        helper.startSequence()
                .thenIdle(20)
                .thenExecute(() -> {
                    INetwork network = getNetwork(helper);
                    int channel = IPositionedAddonsNetworkIngredients.DEFAULT_CHANNEL;
                    TerminalStorageTabIngredientCraftingHandlerCraftingNetwork handler =
                            new TerminalStorageTabIngredientCraftingHandlerCraftingNetwork();

                    ITerminalCraftingPlan<Integer> craftingPlan = handler.calculateCraftingPlan(network, channel,
                            new TerminalCraftingOptionRecipeDefinition<>(IngredientComponents.ITEMSTACK,
                                    getChestRecipe(helper, network, channel)), 1);
                    try {
                        handler.startCraftingJob(network, channel, craftingPlan, player, notifyOnCompletion);
                    } catch (Exception e) {
                        helper.fail("The crafting job could not be started: " + e.getMessage());
                    }

                    CraftingJob craftingJob = getSingleCraftingJob(helper, network, channel);
                    helper.assertTrue(player.getUUID().toString().equals(craftingJob.getInitiatorUuid()),
                            "The started job did not carry the initiator");
                    helper.assertTrue(craftingJob.isNotifyInitiator() == notifyOnCompletion,
                            "The started job did not carry the expected notify flag");
                })
                .thenSucceed();
    }

    private static void prepareNetwork(GameTestHelper helper) {
        GameTestHelpersIntegratedCrafting.INetworkPositions<PartTypeInterfaceCrafting.State> positions =
                GameTestHelpersIntegratedCrafting.createBasicNetwork(helper, POS);

        ChestBlockEntity chest = helper.getBlockEntity(POS.east());
        chest.setItem(0, new ItemStack(Items.OAK_PLANKS, 64));

        positions.interfaceRecipeAdders().get(0).accept(Triple.of(0, RecipeType.CRAFTING,
                ResourceLocation.fromNamespaceAndPath("minecraft", "chest")));
    }

    private static IRecipeDefinition getChestRecipe(GameTestHelper helper, INetwork network, int channel) {
        Iterator<IRecipeDefinition> recipes = CraftingHelpers.getCraftingNetworkChecked(network)
                .getRecipeIndex(channel)
                .getRecipes(IngredientComponents.ITEMSTACK, new ItemStack(Items.CHEST), ItemMatch.ITEM);
        if (!recipes.hasNext()) {
            helper.fail("No chest recipe was available in the network");
        }
        return recipes.next();
    }

    private static CraftingJob getSingleCraftingJob(GameTestHelper helper, INetwork network, int channel) {
        ICraftingNetwork craftingNetwork = CraftingHelpers.getCraftingNetworkChecked(network);
        Iterator<CraftingJob> craftingJobs = craftingNetwork.getCraftingJobs(channel);
        if (!craftingJobs.hasNext()) {
            helper.fail("No crafting job was scheduled");
        }
        return craftingJobs.next();
    }

    /**
     * A job whose dependencies must be crafted first emits exactly one notification,
     * for the requested job rather than for each of its dependencies.
     */
    @GameTest(template = "empty10", templateNamespace = Reference.MOD_ID, timeoutTicks = 4000)
    public void testNestedJobNotifiesOnce(GameTestHelper helper) {
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
        NotifyCollector collector = NotifyCollector.start(initiator);
        helper.startSequence()
                .thenIdle(20)
                .thenExecute(() -> startJob(helper, initiator, new ItemStack(Items.CHEST, 1)))
                .thenWaitUntil(() -> helper.assertTrue(!hasRunningJobs(helper),
                        "The crafting jobs did not finish"))
                .thenExecute(() -> {
                    helper.assertTrue(collector.dependencies > 0,
                            "Expected the plank dependency to have been crafted, so that the job was nested");
                    helper.assertTrue(collector.notified.size() == 1,
                            "Expected exactly one notification for a nested job, but got "
                                    + collector.notified.size());
                    collector.stop();
                })
                .thenSucceed();
    }

    private static void startJob(GameTestHelper helper, UUID initiator, ItemStack output) {
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
     * Counts the completions that the toast listener would act on, for one initiator.
     */
    public static class NotifyCollector {

        private final UUID initiator;
        private final List<CraftingJob> notified = Lists.newArrayList();
        private int dependencies = 0;

        public NotifyCollector(UUID initiator) {
            this.initiator = initiator;
        }

        public static NotifyCollector start(UUID initiator) {
            NotifyCollector collector = new NotifyCollector(initiator);
            NeoForge.EVENT_BUS.register(collector);
            return collector;
        }

        public void stop() {
            NeoForge.EVENT_BUS.unregister(this);
        }

        @SubscribeEvent
        public void onCraftingJobFinished(CraftingJobFinishedEvent event) {
            CraftingJob craftingJob = event.getCraftingJob();
            if (!this.initiator.toString().equals(craftingJob.getInitiatorUuid())) {
                return;
            }
            // The same filter that CraftingJobFinishedToastListener applies
            if (event.isRootJob() && craftingJob.isNotifyInitiator()) {
                this.notified.add(craftingJob);
            } else {
                this.dependencies++;
            }
        }
    }

}
