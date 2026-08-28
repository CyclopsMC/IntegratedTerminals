package org.cyclops.integratedterminals.client.gui.tooltip;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import org.cyclops.integratedterminals.Reference;

/**
 * Registration of the client-side renderers for this mod's tooltip components.
 * @author rubensworks
 */
@EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class TerminalTooltipComponents {

    @SubscribeEvent
    public static void onRegisterClientTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(CraftingOptionIngredientsTooltip.class, ClientCraftingOptionIngredientsTooltip::new);
    }

}
