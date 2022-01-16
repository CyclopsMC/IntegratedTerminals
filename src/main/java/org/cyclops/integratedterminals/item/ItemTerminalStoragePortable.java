package org.cyclops.integratedterminals.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
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

import net.minecraft.item.Item.Properties;

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
    public void openGuiForItemIndex(World world, ServerPlayerEntity player, int itemIndex, Hand hand) {
        if (world.isClientSide()) {
            super.openGuiForItemIndex(world, player, itemIndex, hand);
        } else {
            ItemStack itemStack = InventoryHelpers.getItemFromIndex(player, itemIndex, hand);
            int groupId = getGroupId(itemStack);
            if (groupId >= 0) {
                Optional<INetwork> network = ContainerTerminalStorageItem.getNetworkFromItem(itemStack);
                if (network.isPresent()) {
                    super.openGuiForItemIndex(world, player, itemIndex, hand);
                } else {
                    player.displayClientMessage(new TranslationTextComponent("item.integratedterminals.terminal_storage_portable.status.invalid_network"), true);
                }
            } else {
                player.displayClientMessage(new TranslationTextComponent("item.integratedterminals.terminal_storage_portable.status.no_network"), true);
            }
        }
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if (!context.getLevel().isClientSide()) {
            PartPos partPos = PartPos.of(context.getLevel(), context.getClickedPos(), context.getClickedFace());
            PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(partPos);
            if (partStateHolder != null && partStateHolder.getPart() == PartTypes.CONNECTOR_OMNI) {
                PartTypeConnectorOmniDirectional.State state = (PartTypeConnectorOmniDirectional.State) partStateHolder.getState();
                setGroupId(stack, state.getGroupId());
                context.getPlayer().displayClientMessage(new TranslationTextComponent("item.integratedterminals.terminal_storage_portable.status.linked"), true);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Nullable
    @Override
    public INamedContainerProvider getContainer(World world, PlayerEntity playerEntity, int itemIndex, Hand hand, ItemStack itemStack) {
        return new NamedContainerProviderItem(itemIndex, hand, itemStack.getHoverName(),
                (id, playerInventory, slot, hand1) -> new ContainerTerminalStorageItem(id, playerInventory, slot, hand1, Optional.empty(),
                        getTerminalStorageState(InventoryHelpers.getItemFromIndex(playerEntity, itemIndex, hand), playerEntity, itemIndex, hand)));
    }

    @Override
    public Class<? extends Container> getContainerClass(World world, PlayerEntity playerEntity, ItemStack itemStack) {
        return ContainerTerminalStorageItem.class;
    }

    @Override
    public void writeExtraGuiData(PacketBuffer packetBuffer, World world, ServerPlayerEntity player, int itemIndex, Hand hand) {
        super.writeExtraGuiData(packetBuffer, world, player, itemIndex, hand);
        packetBuffer.writeBoolean(false);
        getTerminalStorageState(InventoryHelpers.getItemFromIndex(player, itemIndex, hand), player, itemIndex, hand).writeToPacketBuffer(packetBuffer);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack == null || newStack == null || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        int groupId = getGroupId(stack);
        if (groupId >= 0) {
            tooltip.add(new TranslationTextComponent(L10NValues.PART_TOOLTIP_MONODIRECTIONALCONNECTOR_GROUP, groupId));
        }
    }

    public static int getGroupId(ItemStack itemStack) {
        CompoundNBT tag = itemStack.getTag();
        if (tag == null || !tag.contains(NBT_KEY_GROUP, Constants.NBT.TAG_INT)) {
            return -1;
        } else {
            return tag.getInt(NBT_KEY_GROUP);
        }
    }

    public static void setGroupId(ItemStack itemStack, int groupId) {
        CompoundNBT tag = itemStack.getOrCreateTag();
        tag.putInt(NBT_KEY_GROUP, groupId);
    }

    public static ITerminalStorageTabCommon.IVariableInventory getVariableInventory(ItemStack itemStack) {
        // Navigate to relevant tag in item
        CompoundNBT tagRoot = itemStack.getOrCreateTag();
        if (!tagRoot.contains(NBT_KEY_NAMED_INVENTORIES, Constants.NBT.TAG_COMPOUND)) {
            tagRoot.put(NBT_KEY_NAMED_INVENTORIES, new CompoundNBT());
        }
        CompoundNBT tagInventories = tagRoot.getCompound(NBT_KEY_NAMED_INVENTORIES);

        return new ITerminalStorageTabCommon.IVariableInventory() {
            @Override
            public NonNullList<ItemStack> getNamedInventory(String name) {
                CompoundNBT tag = tagInventories.getCompound(name);
                NonNullList<ItemStack> list = NonNullList.withSize(tag.getInt("itemCount"), ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(tag, list);
                return list;
            }

            @Override
            public void setNamedInventory(String name, NonNullList<ItemStack> inventory) {
                CompoundNBT tag = new CompoundNBT();
                tag.putString("tabName", name);
                tag.putInt("itemCount", inventory.size());
                ItemStackHelper.saveAllItems(tag, inventory);
                tagInventories.put(name, tag);
            }
        };
    }

    public static TerminalStorageState getTerminalStorageState(ItemStack itemStack, PlayerEntity player, int slot, Hand hand) {
        // Navigate to relevant tag in item
        CompoundNBT tagRoot = itemStack.getOrCreateTag();
        if (!tagRoot.contains(NBT_KEY_STATES, Constants.NBT.TAG_COMPOUND)) {
            tagRoot.put(NBT_KEY_STATES, new CompoundNBT());
        }
        CompoundNBT tagStates = tagRoot.getCompound(NBT_KEY_STATES);
        String playerKey = player.getUUID().toString();

        // Construct item dirty mark listener
        Wrapper<TerminalStorageState> stateWrapped = new Wrapper<>();
        IDirtyMarkListener dirtyMarkListener = () -> {
            // The tag may be updated or newly set, so we set it again in the item's tag
            tagStates.put(playerKey, stateWrapped.get().getTag());
        };

        // Instantiate storage state from NBT
        if (!tagStates.contains(playerKey, Constants.NBT.TAG_COMPOUND)) {
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
