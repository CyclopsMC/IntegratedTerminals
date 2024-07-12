package org.cyclops.integratedterminals.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.core.terminalstorage.button.TerminalButtonItemStackCraftingGridAutoRefill;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

/**
 * Packet for telling the server the new autoRefill value.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientItemStackCraftingGridSetAutoRefill extends PacketCodec<TerminalStorageIngredientItemStackCraftingGridSetAutoRefill> {

    public static final Type<TerminalStorageIngredientItemStackCraftingGridSetAutoRefill> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "terminal_storage_ingredient_itemstack_crafting_grid_set_auto_refill"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalStorageIngredientItemStackCraftingGridSetAutoRefill> CODEC = getCodec(TerminalStorageIngredientItemStackCraftingGridSetAutoRefill::new);

    @CodecField
    private String tabId;
    @CodecField
    private int autoRefillType;

    public TerminalStorageIngredientItemStackCraftingGridSetAutoRefill() {
        super(ID);
    }

    public TerminalStorageIngredientItemStackCraftingGridSetAutoRefill(String tabId,
                                                                       TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType autoRefill) {
        super(ID);
        this.tabId = tabId;
        this.autoRefillType = autoRefill.ordinal();
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(Level world, Player player) {

    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
        if(player.containerMenu instanceof ContainerTerminalStorageBase) {
            ContainerTerminalStorageBase container = ((ContainerTerminalStorageBase) player.containerMenu);
            ITerminalStorageTabCommon tabCommon = container.getTabCommon(tabId);
            if (tabCommon instanceof TerminalStorageTabIngredientComponentItemStackCraftingCommon) {
                ((TerminalStorageTabIngredientComponentItemStackCraftingCommon) tabCommon)
                        .setAutoRefill(TerminalButtonItemStackCraftingGridAutoRefill.AutoRefillType.values()[this.autoRefillType]);
            }
        }
    }

}
