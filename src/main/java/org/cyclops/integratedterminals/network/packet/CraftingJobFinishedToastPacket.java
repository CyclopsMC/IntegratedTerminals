package org.cyclops.integratedterminals.network.packet;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientMatcher;
import org.cyclops.commoncapabilities.api.ingredient.IIngredientSerializer;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratedterminals.Capabilities;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.Reference;
import org.cyclops.integratedterminals.client.gui.toast.CraftingJobToast;
import org.cyclops.integrateddynamics.api.item.TagPathElement;
import org.slf4j.Logger;

/**
 * Packet for showing a toast when a crafting job that the player requested has been completed.
 * @param <T> The instance type.
 * @param <M> The matching condition parameter.
 * @author rubensworks
 */
public class CraftingJobFinishedToastPacket<T, M> extends PacketCodec<CraftingJobFinishedToastPacket<T, M>> {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Type<CraftingJobFinishedToastPacket<?, ?>> ID = new Type<>(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "crafting_job_finished_toast"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingJobFinishedToastPacket<?, ?>> CODEC = (StreamCodec) getCodec(CraftingJobFinishedToastPacket::new);

    @CodecField
    private String ingredientName;
    @CodecField
    private CompoundTag instanceData;

    public CraftingJobFinishedToastPacket() {
        super((Type) ID);
    }

    /**
     * @param lookupProvider A lookup provider.
     * @param ingredientComponent The component of the crafted output.
     * @param instance The crafted output, where the quantity is the total that was crafted.
     */
    public CraftingJobFinishedToastPacket(HolderLookup.Provider lookupProvider,
                                          IngredientComponent<T, M> ingredientComponent, T instance) {
        super((Type) ID);
        this.ingredientName = ingredientComponent.getName().toString();
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new TagPathElement(new CompoundTag()), LOGGER)) {
            TagValueOutput valueOutput = TagValueOutput.createWithContext(scopedCollector, lookupProvider);
            ingredientComponent.getSerializer().serializeInstance(valueOutput, instance);
            this.instanceData = valueOutput.buildResult();
        }
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(Level world, Player player) {
        if (!GeneralConfig.craftingJobFinishedToast) {
            return;
        }

        IngredientComponent<T, M> ingredientComponent = getComponent();
        if (ingredientComponent == null) {
            return;
        }
        IIngredientMatcher<T, M> matcher = ingredientComponent.getMatcher();
        IIngredientSerializer<T, M> serializer = ingredientComponent.getSerializer();
        T instance;
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new TagPathElement(this.instanceData), LOGGER)) {
            ValueInput valueInput = TagValueInput.create(scopedCollector, world.registryAccess(), this.instanceData);
            instance = serializer.deserializeInstance(valueInput);
        }

        // Group by output, so that repeated crafts of the same thing don't pile up.
        // A job that was distributed over multiple crafting interfaces completes as several jobs,
        // so their quantities are summed into a single toast.
        Object token = this.ingredientName + "|" + matcher.getDisplayName(instance).getString();
        var toasts = Minecraft.getInstance().getToastManager();
        CraftingJobToast<T, M> existing = (CraftingJobToast<T, M>) toasts.getToast(CraftingJobToast.class, token);
        if (existing != null) {
            instance = matcher.withQuantity(instance, addQuantities(matcher,
                    matcher.getQuantity(existing.getInstance()), matcher.getQuantity(instance)));
        }

        // The quantity is formatted by the component's own handler, so that fluids, energy,
        // and ingredient components from other mods all read naturally.
        T shownInstance = instance;
        String quantity = ingredientComponent
                .getCapability(Capabilities.IngredientComponentTerminalStorageHandler.INGREDIENT)
                .map(handler -> handler.formatQuantity(shownInstance))
                .orElseGet(() -> String.valueOf(matcher.getQuantity(shownInstance)));
        Component title = Component.translatable("gui.integratedterminals.crafting_job.finished.title")
                .withStyle(ChatFormatting.GREEN);
        Component subtitle = Component.translatable("gui.integratedterminals.crafting_job.finished",
                quantity, matcher.getDisplayName(shownInstance));

        if (existing != null) {
            existing.reset(shownInstance, title, subtitle);
        } else {
            toasts.addToast(new CraftingJobToast<>(token, ingredientComponent, shownInstance, title, subtitle));
        }
    }

    protected static <T, M> long addQuantities(IIngredientMatcher<T, M> matcher, long quantity, long quantityToAdd) {
        try {
            return Math.min(matcher.getMaximumQuantity(), Math.addExact(quantity, quantityToAdd));
        } catch (ArithmeticException e) {
            return matcher.getMaximumQuantity();
        }
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
        // Server-to-client only packet
    }

    protected IngredientComponent<T, M> getComponent() {
        return (IngredientComponent<T, M>) IngredientComponent.REGISTRY.getValue(Identifier.parse(this.ingredientName));
    }

}
