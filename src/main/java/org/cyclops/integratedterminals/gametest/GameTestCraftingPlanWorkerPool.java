package org.cyclops.integratedterminals.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageCraftingPlanBase;

import java.util.concurrent.ExecutionException;

/**
 * Game tests for the crafting planner worker pool.
 * @author rubensworks
 */
public class GameTestCraftingPlanWorkerPool {

    @GameTest(template = "cyclopscore:empty")
    public void testWorkerThreadsAreDaemon(GameTestHelper helper) throws ExecutionException, InterruptedException {
        // Non-daemon worker threads would keep the client JVM alive after quitting the game.
        boolean daemon = ContainerTerminalStorageCraftingPlanBase.getWorkerPool().submit(() -> Thread.currentThread().isDaemon()).get();
        helper.assertTrue(daemon, "Crafting planner worker threads must be daemon threads");
        helper.succeed();
    }

}
