package org.cyclops.integratedterminals.network.packet;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
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
public abstract class TerminalStorageIngredientCraftingOptionDataPacketAbstract<T, M, L, P extends TerminalStorageIngredientCraftingOptionDataPacketAbstract<T, M, L, P>> extends PacketCodec<P> {

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

    public TerminalStorageIngredientCraftingOptionDataPacketAbstract(Type<P> type) {
        super(type);
    }

    public TerminalStorageIngredientCraftingOptionDataPacketAbstract(Type<P> type, HolderLookup.Provider lookupProvider, CraftingOptionGuiData<T, M, L> craftingOptionData) {
        super(type);
        this.ingredientComponent = craftingOptionData.getComponent().getName().toString();
        this.location = craftingOptionData.getLocation();
        this.locationInstance = craftingOptionData.getLocationInstance();
        this.tabName = craftingOptionData.getTabName();
        this.channel = craftingOptionData.getChannel();
        this.craftingOption = craftingOptionData.getCraftingOption() != null
                ? HandlerWrappedTerminalCraftingOption.serialize(lookupProvider, craftingOptionData.getCraftingOption())
                : new CompoundTag();
        this.amount = craftingOptionData.getAmount();
        this.craftingPlan = craftingOptionData.getCraftingPlan() != null
                ? HandlerWrappedTerminalCraftingPlan.serialize(lookupProvider, craftingOptionData.getCraftingPlan())
                : new CompoundTag();
    }

    @Override
    public void encode(RegistryFriendlyByteBuf output) {
        super.encode(output);
        output.writeResourceLocation(location.getName());
        location.writeToPacketBuffer(output, locationInstance);
    }

    @Override
    public void decode(RegistryFriendlyByteBuf input) {
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
    protected HandlerWrappedTerminalCraftingOption<T> getCraftingOption(HolderLookup.Provider lookupProvider, IngredientComponent<T, M> ingredientComponent) {
        try {
            return HandlerWrappedTerminalCraftingOption.deserialize(lookupProvider, ingredientComponent, this.craftingOption);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    protected HandlerWrappedTerminalCraftingPlan getCraftingPlan(HolderLookup.Provider lookupProvider) {
        try {
            return HandlerWrappedTerminalCraftingPlan.deserialize(lookupProvider, this.craftingPlan);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public IngredientComponent<T, M> getIngredientComponent() {
        IngredientComponent<?, ?> component = IngredientComponent.REGISTRY.get(ResourceLocation.parse(ingredientComponent));
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

    public CraftingOptionGuiData<T, M, L> getCraftingOptionData(HolderLookup.Provider lookupProvider) {
        IngredientComponent<T, M> ingredientComponent = getIngredientComponent();
        return new CraftingOptionGuiData<>(ingredientComponent, tabName, channel,
                getCraftingOption(lookupProvider, ingredientComponent), amount, getCraftingPlan(lookupProvider), location, locationInstance);
    }
}
