package org.cyclops.integratedterminals.part;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartStateEmpty;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integratedterminals.GeneralConfig;
import org.cyclops.integratedterminals.core.part.PartTypeTerminal;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.TerminalStorageTabIngredientCraftingHandlers;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalCraftingJobs;

import java.util.List;
import java.util.Optional;

/**
 * A part that exposes a gui using which players can view and manage the active crafting jobs in the network.
 * @author rubensworks
 */
public class PartTypeTerminalCraftingJob extends PartTypeTerminal<PartTypeTerminalCraftingJob, PartStateEmpty<PartTypeTerminalCraftingJob>> {

    public PartTypeTerminalCraftingJob(String name) {
        super(name);
    }

    @Override
    public int getConsumptionRate(PartStateEmpty<PartTypeTerminalCraftingJob> state) {
        return GeneralConfig.terminalCraftingBaseConsumption;
    }

    @Override
    protected PartStateEmpty<PartTypeTerminalCraftingJob> constructDefaultState() {
        return new PartStateEmpty<PartTypeTerminalCraftingJob>() {
            @Override
            public int getUpdateInterval() {
                return 1; // For enabling energy consumption
            }
        };
    }

    @Override
    public Optional<MenuProvider> getContainerProvider(PartPos pos) {
        return Optional.of(new MenuProvider() {

            @Override
            public Component getDisplayName() {
                return Component.translatable(getTranslationKey());
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(pos);
                return new ContainerTerminalCraftingJobs(id, playerInventory,
                        data.getRight(), Optional.of(data.getLeft()), (PartTypeTerminalCraftingJob) data.getMiddle());
            }
        });
    }

    @Override
    public void writeExtraGuiData(FriendlyByteBuf packetBuffer, PartPos pos, ServerPlayer player) {
        PacketCodec.write(packetBuffer, pos);
        super.writeExtraGuiData(packetBuffer, pos, player);
    }

    @Override
    public void loadTooltip(ItemStack itemStack, List<Component> lines) {
        super.loadTooltip(itemStack, lines);
        if (TerminalStorageTabIngredientCraftingHandlers.REGISTRY.getHandlers().isEmpty()) {
            lines.add(Component.translatable(
                    "parttype.integratedterminals.terminal_crafting_job.tooltip.nohandlers")
                    .withStyle(ChatFormatting.GOLD));
        }
    }
}
