package org.cyclops.integratedterminals.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentItemStackCraftingCommon;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;

/**
 * Packet for sending a storage slot click event from client to server.
 * @author rubensworks
 *
 */
public class TerminalStorageIngredientItemStackCraftingGridShiftClickOutput extends PacketCodec<TerminalStorageIngredientItemStackCraftingGridShiftClickOutput> {

    public static final Type<TerminalStorageIngredientItemStackCraftingGridShiftClickOutput> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "terminal_storage_ingredient_itemstack_crafting_grid_shift_click_output"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalStorageIngredientItemStackCraftingGridShiftClickOutput> CODEC = getCodec(TerminalStorageIngredientItemStackCraftingGridShiftClickOutput::new);

    @CodecField
    private String tabId;
    @CodecField
    private int channel;
    @CodecField
    private boolean craftOnce;

    public TerminalStorageIngredientItemStackCraftingGridShiftClickOutput() {
        super(ID);
    }

    public TerminalStorageIngredientItemStackCraftingGridShiftClickOutput(String tabId, int channel, boolean craftOnce) {
        super(ID);
        this.tabId = tabId;
        this.channel = channel;
        this.craftOnce = craftOnce;
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
            ContainerTerminalStorageBase<?> container = ((ContainerTerminalStorageBase) player.containerMenu);
            ITerminalStorageTabCommon tabCommon = container.getTabCommon(tabId);
            if (tabCommon instanceof TerminalStorageTabIngredientComponentItemStackCraftingCommon) {
                TerminalStorageTabIngredientComponentItemStackCraftingCommon tabCommonCrafting =
                        (TerminalStorageTabIngredientComponentItemStackCraftingCommon) tabCommon;
                ITerminalStorageTabCommon.IVariableInventory variableInventory = container.getVariableInventory().get();

                // Loop until the result slot is empty
                ResultSlot slotCrafting = tabCommonCrafting.getSlotCrafting();
                ItemStack currentCraftingItem = slotCrafting.getItem().copy();
                ItemStack resultStack;
                int craftedAmount = 0;
                do {
                    // Break the loop once we can not add the result into the player inventory anymore
                    if (!ItemHandlerHelper.insertItem(new PlayerMainInvWrapper(player.getInventory()),
                            slotCrafting.getItem(), true).isEmpty()) {
                        break;
                    }

                    // Break the loop if we are crafting something else
                    if (!ItemStack.isSameItemSameComponents(currentCraftingItem, slotCrafting.getItem())) {
                        break;
                    }

                    // Remove the current result stack and properly call all events
                    resultStack = slotCrafting.remove(64);
                    slotCrafting.onTake(player, resultStack);
                    craftedAmount += resultStack.getCount();

                    if (!resultStack.isEmpty()) {
                        // Move result into player inventory
                        player.getInventory().placeItemBackInInventory(resultStack.copy(), true);

                        // Re-calculate recipe
                        tabCommonCrafting.updateCraftingResult(player, player.containerMenu, variableInventory);
                    }
                } while(!this.craftOnce && !resultStack.isEmpty() && craftedAmount < resultStack.getMaxStackSize());
            }
        }
    }

}
