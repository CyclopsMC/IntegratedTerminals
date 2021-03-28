package org.cyclops.integratedterminals.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.api.terminalstorage.location.ITerminalStorageLocation;
import org.cyclops.integratedterminals.core.client.gui.CraftingOptionGuiData;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingPlan;
import org.cyclops.integratedterminals.core.terminalstorage.location.TerminalStorageLocations;

import javax.annotation.Nullable;

/**
 * Packet for sending a storage slot click event from client to server.
 * @author rubensworks
 *
 */
public abstract class TerminalStorageIngredientCraftingOptionDataPacketAbstract<T, M, L> extends PacketCodec {

    @CodecField
    private String ingredientComponent;
    private ITerminalStorageLocation<L> location;
    private L locationInstance;
    @CodecField
    private String tabName;
    @CodecField
    private int channel;
    @CodecField
    private CompoundNBT craftingOption;
    @CodecField
    private int amount;
    @CodecField
    private CompoundNBT craftingPlan;

    public TerminalStorageIngredientCraftingOptionDataPacketAbstract() {

    }

    public TerminalStorageIngredientCraftingOptionDataPacketAbstract(CraftingOptionGuiData<T, M, L> craftingOptionData) {
        this.ingredientComponent = craftingOptionData.getComponent().getName().toString();
        this.location = craftingOptionData.getLocation();
        this.locationInstance = craftingOptionData.getLocationInstance();
        this.tabName = craftingOptionData.getTabName();
        this.channel = craftingOptionData.getChannel();
        this.craftingOption = craftingOptionData.getCraftingOption() != null
                ? HandlerWrappedTerminalCraftingOption.serialize(craftingOptionData.getCraftingOption())
                : new CompoundNBT();
        this.amount = craftingOptionData.getAmount();
        this.craftingPlan = craftingOptionData.getCraftingPlan() != null
                ? HandlerWrappedTerminalCraftingPlan.serialize(craftingOptionData.getCraftingPlan())
                : new CompoundNBT();
    }

    @Override
    public void encode(PacketBuffer output) {
        super.encode(output);
        output.writeResourceLocation(location.getName());
        location.writeToPacketBuffer(output, locationInstance);
    }

    @Override
    public void decode(PacketBuffer input) {
        super.decode(input);
        this.location = (ITerminalStorageLocation<L>) TerminalStorageLocations.REGISTRY.getLocation(input.readResourceLocation());
        this.locationInstance = this.location.readFromPacketBuffer(input);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(World world, PlayerEntity player) {

    }

    @Nullable
    protected HandlerWrappedTerminalCraftingOption<T> getCraftingOption(IngredientComponent<T, M> ingredientComponent) {
        try {
            return HandlerWrappedTerminalCraftingOption.deserialize(ingredientComponent, this.craftingOption);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    protected HandlerWrappedTerminalCraftingPlan getCraftingPlan() {
        try {
            return HandlerWrappedTerminalCraftingPlan.deserialize(this.craftingPlan);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public IngredientComponent<T, M> getIngredientComponent() {
        IngredientComponent<?, ?> component = IngredientComponent.REGISTRY.getValue(new ResourceLocation(ingredientComponent));
        if (component == null) {
            throw new IllegalArgumentException("Could not find the ingredient component type " + ingredientComponent);
        }
        return (IngredientComponent<T, M>) component;
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

    public CraftingOptionGuiData<T, M, L> getCraftingOptionData() {
        IngredientComponent<T, M> ingredientComponent = getIngredientComponent();
        return new CraftingOptionGuiData<>(ingredientComponent, tabName, channel,
                getCraftingOption(ingredientComponent), amount, getCraftingPlan(), location, locationInstance);
    }
}