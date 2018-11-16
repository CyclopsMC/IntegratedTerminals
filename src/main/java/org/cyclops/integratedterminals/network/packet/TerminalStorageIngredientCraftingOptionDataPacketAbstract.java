package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;

/**
 * Packet for sending a storage slot click event from client to server.
 * @author rubensworks
 *
 */
public abstract class TerminalStorageIngredientCraftingOptionDataPacketAbstract<T, M> extends PacketCodec {

    @CodecField
    private BlockPos pos;
    @CodecField
    private EnumFacing side;
    @CodecField
    private String tabName;
    @CodecField
    private int channel;
    @CodecField
    private NBTTagCompound craftingOption;
    @CodecField
    private int amount;

    public TerminalStorageIngredientCraftingOptionDataPacketAbstract() {

    }

    public TerminalStorageIngredientCraftingOptionDataPacketAbstract(CraftingOptionGuiData<T, M> craftingOptionData) {
        this.pos = craftingOptionData.getPos();
        this.side = craftingOptionData.getSide();
        this.tabName = craftingOptionData.getTabName();
        this.channel = craftingOptionData.getChannel();
        this.craftingOption = HandlerWrappedTerminalCraftingOption.serialize(craftingOptionData.getCraftingOption());
        this.amount = craftingOptionData.getAmount();
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void actionClient(World world, EntityPlayer player) {

    }

    protected HandlerWrappedTerminalCraftingOption<T> getCraftingOption(IngredientComponent<T, M> ingredientComponent) {
        return HandlerWrappedTerminalCraftingOption.deserialize(ingredientComponent, this.craftingOption);
    }

    public int getChannel() {
        return channel;
    }

    public String getTabName() {
        return tabName;
    }

    public int getAmount() {
        return amount;
    }

    public CraftingOptionGuiData<T, M> getCraftingOptionData(IngredientComponent<T, M> ingredientComponent) {
        return new CraftingOptionGuiData<>(pos, side, ingredientComponent, tabName, channel, getCraftingOption(ingredientComponent), amount);
    }
}