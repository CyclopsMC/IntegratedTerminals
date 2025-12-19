package org.cyclops.integratedterminals.core.client.gui;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integratedterminals.api.terminalstorage.location.ITerminalStorageLocation;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingPlan;
import org.cyclops.integratedterminals.core.terminalstorage.location.TerminalStorageLocations;

import javax.annotation.Nullable;
import java.util.function.Function;

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

    public void writeToPacketBuffer(RegistryFriendlyByteBuf packetBuffer) {
        packetBuffer.writeUtf(component.getName().toString());
        packetBuffer.writeUtf(tabName);
        packetBuffer.writeInt(channel);
        packetBuffer.writeInt(amount);
        packetBuffer.writeBoolean(craftingOption != null);
        if (craftingOption != null) {
            packetBuffer.writeNbt(IModHelpers.get().getMinecraftHelpers().valueOutputToNbt(o -> HandlerWrappedTerminalCraftingOption.serialize(o, craftingOption), packetBuffer.registryAccess()));
        }
        packetBuffer.writeBoolean(craftingPlan != null);
        if (craftingPlan != null) {
            packetBuffer.writeNbt(HandlerWrappedTerminalCraftingPlan.serialize(packetBuffer.registryAccess(), craftingPlan));
        }
        packetBuffer.writeIdentifier(location.getName());
        location.writeToPacketBuffer(packetBuffer, locationInstance);
    }

    public static CraftingOptionGuiData readFromPacketBuffer(RegistryFriendlyByteBuf packetBuffer) {
        IngredientComponent component = IngredientComponent.REGISTRY.getValue(Identifier.parse(packetBuffer.readUtf(32767)));
        String tabName = packetBuffer.readUtf(32767);
        int channel = packetBuffer.readInt();
        int amount = packetBuffer.readInt();
        HandlerWrappedTerminalCraftingOption craftingOption = null;
        if (packetBuffer.readBoolean()) {
            craftingOption = IModHelpers.get().getMinecraftHelpers().valueInputFromNbt(packetBuffer.readNbt(), packetBuffer.registryAccess(), (Function<ValueInput, HandlerWrappedTerminalCraftingOption>) i -> HandlerWrappedTerminalCraftingOption.deserialize(i, component));
        }
        HandlerWrappedTerminalCraftingPlan craftingPlan = null;
        if (packetBuffer.readBoolean()) {
            craftingPlan = HandlerWrappedTerminalCraftingPlan.deserialize(packetBuffer.registryAccess(), packetBuffer.readNbt());
        }
        ITerminalStorageLocation<?> location = TerminalStorageLocations.REGISTRY.getLocation(packetBuffer.readIdentifier());
        Object locationInstance = location.readFromPacketBuffer(packetBuffer);
        return new CraftingOptionGuiData(component, tabName, channel, craftingOption, amount, craftingPlan, location, locationInstance);
    }
}
