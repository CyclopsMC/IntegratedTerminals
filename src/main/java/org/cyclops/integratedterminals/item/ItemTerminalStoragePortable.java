package org.cyclops.integratedterminals.item;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.cyclopscore.inventory.ItemLocation;
import org.cyclops.cyclopscore.inventory.container.NamedContainerProviderItem;
import org.cyclops.cyclopscore.item.ItemGui;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.PartTypeConnectorOmniDirectional;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabCommon;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageItem;
import org.cyclops.integratedterminals.inventory.container.TerminalStorageState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * A portable storage terminal.
 * @author rubensworks
 */
public class ItemTerminalStoragePortable extends ItemGui {

    public static String NBT_KEY_GROUP = Reference.MOD_ID + ":groupKey";
    public static String NBT_KEY_NAMED_INVENTORIES = Reference.MOD_ID + ":namedInventories";
    public static String NBT_KEY_STATES = Reference.MOD_ID + ":terminalStorageStates";

    public ItemTerminalStoragePortable(Properties properties) {
        super(properties);
    }

    @Override
    public void openGuiForItemIndex(Level world, ServerPlayer player, ItemLocation itemLocation) {
        if (world.isClientSide()) {
            super.openGuiForItemIndex(world, player, itemLocation);
        } else {
            ItemStack itemStack = itemLocation.getItemStack(player);
            int groupId = getGroupId(itemStack);
            if (groupId >= 0) {
                Optional<INetwork> network = ContainerTerminalStorageItem.getNetworkFromItem(itemStack);
                if (network.isPresent()) {
                    super.openGuiForItemIndex(world, player, itemLocation);
                } else {
                    player.displayClientMessage(new TranslatableComponent("item.integratedterminals.terminal_storage_portable.status.invalid_network"), true);
                }
            } else {
                player.displayClientMessage(new TranslatableComponent("item.integratedterminals.terminal_storage_portable.status.no_network"), true);
            }
        }
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (!context.getLevel().isClientSide()) {
            PartPos partPos = PartPos.of(context.getLevel(), context.getClickedPos(), context.getClickedFace());
            PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(partPos);
            if (partStateHolder != null && partStateHolder.getPart() == PartTypes.CONNECTOR_OMNI) {
                PartTypeConnectorOmniDirectional.State state = (PartTypeConnectorOmniDirectional.State) partStateHolder.getState();
                setGroupId(stack, state.getGroupId());
                context.getPlayer().displayClientMessage(new TranslatableComponent("item.integratedterminals.terminal_storage_portable.status.linked"), true);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public MenuProvider getContainer(Level world, Player playerEntity, ItemLocation itemLocation) {
        return new NamedContainerProviderItem(itemLocation, itemLocation.getItemStack(playerEntity).getHoverName(),
                (id, playerInventory, itemLocation1) -> new ContainerTerminalStorageItem(id, playerInventory, itemLocation1, Optional.empty(),
                        getTerminalStorageState(itemLocation.getItemStack(playerEntity), playerEntity, itemLocation1)));
    }

    @Override
    public Class<? extends AbstractContainerMenu> getContainerClass(Level world, Player playerEntity, ItemStack itemStack) {
        return ContainerTerminalStorageItem.class;
    }

    @Override
    public void writeExtraGuiData(FriendlyByteBuf packetBuffer, Level world, ServerPlayer player, ItemLocation itemLocation) {
        super.writeExtraGuiData(packetBuffer, world, player, itemLocation);
        packetBuffer.writeBoolean(false);
        getTerminalStorageState(itemLocation.getItemStack(player), player, itemLocation).writeToPacketBuffer(packetBuffer);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack == null || newStack == null || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        int groupId = getGroupId(stack);
        if (groupId >= 0) {
            tooltip.add(new TranslatableComponent(L10NValues.PART_TOOLTIP_MONODIRECTIONALCONNECTOR_GROUP, groupId));
        }
    }

    public static int getGroupId(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag == null || !tag.contains(NBT_KEY_GROUP, Tag.TAG_INT)) {
            return -1;
        } else {
            return tag.getInt(NBT_KEY_GROUP);
        }
    }

    public static void setGroupId(ItemStack itemStack, int groupId) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putInt(NBT_KEY_GROUP, groupId);
    }

    public static ITerminalStorageTabCommon.IVariableInventory getVariableInventory(ItemStack itemStack) {
        // Navigate to relevant tag in item
        CompoundTag tagRoot = itemStack.getOrCreateTag();
        if (!tagRoot.contains(NBT_KEY_NAMED_INVENTORIES, Tag.TAG_COMPOUND)) {
            tagRoot.put(NBT_KEY_NAMED_INVENTORIES, new CompoundTag());
        }
        CompoundTag tagInventories = tagRoot.getCompound(NBT_KEY_NAMED_INVENTORIES);

        return new ITerminalStorageTabCommon.IVariableInventory() {
            @Override
            public NonNullList<ItemStack> getNamedInventory(String name) {
                CompoundTag tag = tagInventories.getCompound(name);
                NonNullList<ItemStack> list = NonNullList.withSize(tag.getInt("itemCount"), ItemStack.EMPTY);
                ContainerHelper.loadAllItems(tag, list);
                return list;
            }

            @Override
            public void setNamedInventory(String name, NonNullList<ItemStack> inventory) {
                CompoundTag tag = new CompoundTag();
                tag.putString("tabName", name);
                tag.putInt("itemCount", inventory.size());
                ContainerHelper.saveAllItems(tag, inventory);
                tagInventories.put(name, tag);
            }
        };
    }

    public static TerminalStorageState getTerminalStorageState(ItemStack itemStack, Player player, ItemLocation itemLocation) {
        // Navigate to relevant tag in item
        CompoundTag tagRoot = itemStack.getOrCreateTag();
        if (!tagRoot.contains(NBT_KEY_STATES, Tag.TAG_COMPOUND)) {
            tagRoot.put(NBT_KEY_STATES, new CompoundTag());
        }
        CompoundTag tagStates = tagRoot.getCompound(NBT_KEY_STATES);
        String playerKey = player.getUUID().toString();

        // Construct item dirty mark listener
        Wrapper<TerminalStorageState> stateWrapped = new Wrapper<>();
        IDirtyMarkListener dirtyMarkListener = () -> {
            // The tag may be updated or newly set, so we set it again in the item's tag
            tagStates.put(playerKey, stateWrapped.get().getTag());
        };

        // Instantiate storage state from NBT
        if (!tagStates.contains(playerKey, Tag.TAG_COMPOUND)) {
            TerminalStorageState state = TerminalStorageState.getPlayerDefault(player, dirtyMarkListener);
            stateWrapped.set(state);
            tagStates.put(playerKey, state.getTag());
            return state;
        } else {
            TerminalStorageState state = new TerminalStorageState(tagStates.getCompound(playerKey), dirtyMarkListener);
            stateWrapped.set(state);
            return state;
        }
    }
}
