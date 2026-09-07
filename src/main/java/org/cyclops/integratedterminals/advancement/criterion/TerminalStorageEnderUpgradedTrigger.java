package org.cyclops.integratedterminals.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Triggers when a storage terminal is ender-upgraded.
 * @author rubensworks
 */
public class TerminalStorageEnderUpgradedTrigger extends SimpleCriterionTrigger<TerminalStorageEnderUpgradedTrigger.Instance> {

    @Nullable
    private static TerminalStorageEnderUpgradedTrigger INSTANCE = null;

    public static final Codec<TerminalStorageEnderUpgradedTrigger.Instance> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(
                            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TerminalStorageEnderUpgradedTrigger.Instance::player)
                    )
                    .apply(builder, TerminalStorageEnderUpgradedTrigger.Instance::new)
    );

    public TerminalStorageEnderUpgradedTrigger() {
        INSTANCE = this;
    }

    @Override
    public Codec<TerminalStorageEnderUpgradedTrigger.Instance> codec() {
        return CODEC;
    }

    /**
     * Trigger this criterion for the given player.
     * @param player The player that ender-upgraded a storage terminal.
     */
    public static void onEnderUpgraded(ServerPlayer player) {
        if (INSTANCE != null) {
            INSTANCE.trigger(player, instance -> true);
        }
    }

    public static record Instance(
            Optional<ContextAwarePredicate> player
    ) implements SimpleCriterionTrigger.SimpleInstance {
    }
}
