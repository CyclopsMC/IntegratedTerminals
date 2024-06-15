package org.cyclops.integratedterminals.network.packet;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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
    private CompoundTag craftingOption;
    @CodecField
    private int amount;
    @CodecField
    private CompoundTag craftingPlan;

    public TerminalStorageIngredientCraftingOptionDataPacketAbstract(ResourceLocation id) {
        super(id);
    }

    public TerminalStorageIngredientCraftingOptionDataPacketAbstract(ResourceLocation id, CraftingOptionGuiData<T, M, L> craftingOptionData) {
        super(id);
        this.ingredientComponent = craftingOptionData.getComponent().getName().toString();
        this.location = craftingOptionData.getLocation();
        this.locationInstance = craftingOptionData.getLocationInstance();
        this.tabName = craftingOptionData.getTabName();
        this.channel = craftingOptionData.getChannel();
        this.craftingOption = craftingOptionData.getCraftingOption() != null
                ? HandlerWrappedTerminalCraftingOption.serialize(craftingOptionData.getCraftingOption())
                : new CompoundTag();
        this.amount = craftingOptionData.getAmount();
        this.craftingPlan = craftingOptionData.getCraftingPlan() != null
                ? HandlerWrappedTerminalCraftingPlan.serialize(craftingOptionData.getCraftingPlan())
                : new CompoundTag();
    }

    @Override
    public void encode(FriendlyByteBuf output) {
        super.encode(output);
        output.writeResourceLocation(location.getName());
        location.writeToPacketBuffer(output, locationInstance);
    }

    @Override
    public void decode(FriendlyByteBuf input) {
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
    public void actionClient(Level world, Player player) {

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
        IngredientComponent<?, ?> component = IngredientComponent.REGISTRY.get(new ResourceLocation(ingredientComponent));
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
