package org.cyclops.integratedterminals.core.client.gui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integratedterminals.api.terminalstorage.location.ITerminalStorageLocation;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingPlan;
import org.cyclops.integratedterminals.core.terminalstorage.location.TerminalStorageLocations;

import javax.annotation.Nullable;

/**
 * @author rubensworks
 */
public class CraftingOptionGuiData<T, M, L> {

    private final IngredientComponent<T, M> component;
    private final String tabName;
    private final int channel;
    @Nullable
    private final HandlerWrappedTerminalCraftingOption<T> craftingOption;
    private final int amount;
    @Nullable
    private final HandlerWrappedTerminalCraftingPlan craftingPlan;
    private final ITerminalStorageLocation<L> location;
    private final L locationInstance;

    public CraftingOptionGuiData(IngredientComponent<T, M> component, String tabName,
                                 int channel, @Nullable HandlerWrappedTerminalCraftingOption<T> craftingOption,
                                 int amount, HandlerWrappedTerminalCraftingPlan craftingPlan,
                                 ITerminalStorageLocation<L> location, L locationInstance) {
        this.component = component;
        this.tabName = tabName;
        this.channel = channel;
        this.craftingOption = craftingOption;
        this.amount = amount;
        this.craftingPlan = craftingPlan;
        this.location = location;
        this.locationInstance = locationInstance;
    }

    public IngredientComponent<T, M> getComponent() {
        return component;
    }

    public String getTabName() {
        return tabName;
    }

    public int getChannel() {
        return channel;
    }

    @Nullable
    public HandlerWrappedTerminalCraftingOption<T> getCraftingOption() {
        return craftingOption;
    }

    public int getAmount() {
        return amount;
    }

    @Nullable
    public HandlerWrappedTerminalCraftingPlan getCraftingPlan() {
        return craftingPlan;
    }

    public ITerminalStorageLocation<L> getLocation() {
        return location;
    }

    public L getLocationInstance() {
        return locationInstance;
    }

    public CraftingOptionGuiData<T, M, L> copyWithAmount(int amount) {
        return new CraftingOptionGuiData<>(
                this.getComponent(),
                this.getTabName(),
                this.getChannel(),
                this.getCraftingOption(),
                amount,
                this.getCraftingPlan(),
                getLocation(),
                getLocationInstance()
        );
    }

    public void writeToPacketBuffer(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeUtf(component.getName().toString());
        packetBuffer.writeUtf(tabName);
        packetBuffer.writeInt(channel);
        packetBuffer.writeInt(amount);
        packetBuffer.writeBoolean(craftingOption != null);
        if (craftingOption != null) {
            packetBuffer.writeNbt(HandlerWrappedTerminalCraftingOption.serialize(craftingOption));
        }
        packetBuffer.writeBoolean(craftingPlan != null);
        if (craftingPlan != null) {
            packetBuffer.writeNbt(HandlerWrappedTerminalCraftingPlan.serialize(craftingPlan));
        }
        packetBuffer.writeResourceLocation(location.getName());
        location.writeToPacketBuffer(packetBuffer, locationInstance);
    }

    public static CraftingOptionGuiData readFromPacketBuffer(FriendlyByteBuf packetBuffer) {
        IngredientComponent component = IngredientComponent.REGISTRY.get(new ResourceLocation(packetBuffer.readUtf(32767)));
        String tabName = packetBuffer.readUtf(32767);
        int channel = packetBuffer.readInt();
        int amount = packetBuffer.readInt();
        HandlerWrappedTerminalCraftingOption craftingOption = null;
        if (packetBuffer.readBoolean()) {
            craftingOption = HandlerWrappedTerminalCraftingOption.deserialize(component, packetBuffer.readNbt());
        }
        HandlerWrappedTerminalCraftingPlan craftingPlan = null;
        if (packetBuffer.readBoolean()) {
            craftingPlan = HandlerWrappedTerminalCraftingPlan.deserialize(packetBuffer.readNbt());
        }
        ITerminalStorageLocation<?> location = TerminalStorageLocations.REGISTRY.getLocation(packetBuffer.readResourceLocation());
        Object locationInstance = location.readFromPacketBuffer(packetBuffer);
        return new CraftingOptionGuiData(component, tabName, channel, craftingOption, amount, craftingPlan, location, locationInstance);
    }
}
