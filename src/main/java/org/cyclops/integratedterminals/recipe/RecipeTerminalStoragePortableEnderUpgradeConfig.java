package org.cyclops.integratedterminals.recipe;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import org.cyclops.cyclopscore.config.extendedconfig.RecipeConfigCommon;
import org.cyclops.cyclopscore.init.ModBaseNeoForge;
import org.cyclops.cyclopscore.recipe.type.RecipeCraftingShapelessCustomOutput;
import org.cyclops.integratedterminals.IntegratedTerminals;
import org.cyclops.integratedterminals.RegistryEntries;
import org.cyclops.integratedterminals.item.ItemTerminalStoragePortable;

/**
 * Config for the recipe that ender-upgrades a portable storage terminal with an eye of ender.
 * @author rubensworks
 */
public class RecipeTerminalStoragePortableEnderUpgradeConfig extends RecipeConfigCommon<RecipeCraftingShapelessCustomOutput, ModBaseNeoForge<?>> {

    public RecipeTerminalStoragePortableEnderUpgradeConfig() {
        super(IntegratedTerminals._instance,
                "crafting_special_terminal_storage_portable_ender_upgrade",
                eConfig -> new RecipeCraftingShapelessCustomOutput.Serializer(
                        RecipeTerminalStoragePortableEnderUpgradeConfig::createStaticOutput,
                        RecipeTerminalStoragePortableEnderUpgradeConfig::transformCraftingOutput).getRecipeSerializer()
        );
    }

    /**
     * @return The output that is shown in recipe listings, such as JEI and the info book.
     */
    protected static ItemStackTemplate createStaticOutput() {
        return new ItemStackTemplate(RegistryEntries.ITEM_TERMINAL_STORAGE_PORTABLE.get(), DataComponentPatch.builder()
                .set(RegistryEntries.COMPONENT_ENDER_UPGRADED.get(), true)
                .build());
    }

    /**
     * Copy the input terminal, so that its network link and stored settings are retained.
     */
    protected static ItemStack transformCraftingOutput(CraftingInput inventory, ItemStack staticOutput) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.getItem() instanceof ItemTerminalStoragePortable) {
                if (ItemTerminalStoragePortable.isEnderUpgraded(itemStack)) {
                    // Don't allow upgrading an already upgraded terminal
                    return ItemStack.EMPTY;
                }
                ItemStack output = itemStack.copyWithCount(1);
                ItemTerminalStoragePortable.setEnderUpgraded(output, true);
                return output;
            }
        }
        return ItemStack.EMPTY;
    }

}
