package org.cyclops.integratedterminals.part;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
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
    public Optional<INamedContainerProvider> getContainerProvider(PartPos pos) {
        return Optional.of(new INamedContainerProvider() {

            @Override
            public ITextComponent getDisplayName() {
                return new TranslationTextComponent(getTranslationKey());
            }

            @Override
            public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(pos);
                return new ContainerTerminalCraftingJobs(id, playerInventory,
                        data.getRight(), Optional.of(data.getLeft()), (PartTypeTerminalCraftingJob) data.getMiddle());
            }
        });
    }

    @Override
    public void writeExtraGuiData(PacketBuffer packetBuffer, PartPos pos, ServerPlayerEntity player) {
        PacketCodec.write(packetBuffer, pos);
        super.writeExtraGuiData(packetBuffer, pos, player);
    }

    @Override
    public void loadTooltip(ItemStack itemStack, List<ITextComponent> lines) {
        super.loadTooltip(itemStack, lines);
        if (TerminalStorageTabIngredientCraftingHandlers.REGISTRY.getHandlers().isEmpty()) {
            lines.add(new TranslationTextComponent(
                    "parttype.integratedterminals.terminal_crafting_job.tooltip.nohandlers")
                    .withStyle(TextFormatting.GOLD));
        }
    }
}
