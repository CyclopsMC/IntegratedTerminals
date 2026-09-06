package org.cyclops.integratedterminals.core.terminalstorage.metrics;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A synthetic inventory of distinct item stacks, used as network storage while measuring
 * the cost of opening a storage terminal.
 *
 * Debug-only. Not registered unless
 * {@link org.cyclops.integratedterminals.GeneralConfig#debugTerminalOpenMetrics} is set.
 */
public class MeasurementStorage implements ResourceHandler<ItemResource> {

    /**
     * Share of stacks with no components at all. Capped by the number of registered items,
     * since distinctness without components can only come from distinct item types.
     */
    private static final double PLAIN_SHARE = 0.60;
    /**
     * Share of stacks carrying a small component payload (a custom name).
     */
    private static final double SMALL_SHARE = 0.30;

    /**
     * If stacks that differ only by components should all share one item type, which is the slow case.
     * Applies both to generated heavy stacks and to stacks added by a change.
     */
    public static boolean clusterNewStacks = false;

    private final List<ItemStack> stacks = new ArrayList<>();
    private final List<Item> itemPool = new ArrayList<>();
    private int plainCount;
    private int smallCount;
    private int heavyCount;

    /**
     * Fill this storage with the given number of distinct stacks.
     * @param count The number of distinct collapsed stacks to generate.
     * @param seed A seed, so runs are reproducible and comparable.
     * @param registryAccess Registry access, needed for the enchantments of heavy stacks.
     */
    public void generate(int count, long seed, RegistryAccess registryAccess) {
        this.stacks.clear();
        this.plainCount = 0;
        this.smallCount = 0;
        this.heavyCount = 0;

        List<Item> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR) {
                items.add(item);
            }
        }
        this.itemPool.clear();
        this.itemPool.addAll(items);
        List<Holder<Enchantment>> enchantments = new ArrayList<>();
        HolderLookup.RegistryLookup<Enchantment> lookup = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
        lookup.listElements().forEach(enchantments::add);

        Random random = new Random(seed);

        // Plain stacks can only be as many as there are item types.
        int plainTarget = Math.min((int) (count * PLAIN_SHARE), items.size());
        int heavyTarget = (int) (count * (1 - PLAIN_SHARE - SMALL_SHARE));
        int smallTarget = count - plainTarget - heavyTarget;

        for (int i = 0; i < plainTarget; i++) {
            ItemStack stack = new ItemStack(items.get(i % items.size()));
            stack.setCount(1 + random.nextInt(64));
            this.stacks.add(stack);
            this.plainCount++;
        }
        for (int i = 0; i < smallTarget; i++) {
            ItemStack stack = new ItemStack(items.get(random.nextInt(items.size())));
            stack.setCount(1 + random.nextInt(64));
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("s" + i));
            this.stacks.add(stack);
            this.smallCount++;
        }
        for (int i = 0; i < heavyTarget; i++) {
            // Spreading heavy stacks over item types is the normal case. Putting them all on one
            // item is the pathological case, kept selectable so it can be measured.
            ItemStack stack = new ItemStack(clusterNewStacks
                    ? Items.DIAMOND_SWORD
                    : items.get(random.nextInt(items.size())));
            stack.setCount(1);
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("heavy-" + i));
            stack.set(DataComponents.LORE, new ItemLore(List.of(
                    Component.literal("Measurement payload line one for entry " + i),
                    Component.literal("Measurement payload line two for entry " + i),
                    Component.literal("Measurement payload line three for entry " + i))));
            if (!enchantments.isEmpty()) {
                ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                for (int e = 0; e < 3; e++) {
                    mutable.set(enchantments.get((i * 3 + e) % enchantments.size()), 1 + ((i + e) % 5));
                }
                stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
            }
            List<ItemStack> contained = new ArrayList<>();
            for (int c = 0; c < 4; c++) {
                ItemStack inner = new ItemStack(items.get(random.nextInt(items.size())));
                inner.setCount(1 + random.nextInt(16));
                contained.add(inner);
            }
            stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contained));
            this.stacks.add(stack);
            this.heavyCount++;
        }
    }

    /**
     * Change a fraction of the stacks, as a re-open scenario does.
     * Changes are split evenly between quantity changes, removals and freshly added stacks.
     * @param fraction The fraction of stacks to change, between 0 and 1.
     * @param seed A seed.
     * @return The number of stacks changed.
     */
    public int mutate(double fraction, long seed) {
        Random random = new Random(seed);
        int target = (int) (this.stacks.size() * fraction);
        int changed = 0;
        for (int i = 0; i < target; i++) {
            int index = random.nextInt(this.stacks.size());
            ItemStack stack = this.stacks.get(index);
            switch (i % 3) {
                case 0 -> {
                    // Quantity change
                    if (!stack.isEmpty()) {
                        stack.setCount(1 + random.nextInt(64));
                        changed++;
                    }
                }
                case 1 -> {
                    // Removal
                    if (!stack.isEmpty()) {
                        this.stacks.set(index, ItemStack.EMPTY);
                        changed++;
                    }
                }
                default -> {
                    // New distinct stack. Whether these all share one item type matters a lot:
                    // stacks of the same item differing only in components cluster in the
                    // network's ingredient index and make it far slower.
                    ItemStack fresh = new ItemStack(clusterNewStacks
                            ? Items.STONE
                            : this.itemPool.get(random.nextInt(this.itemPool.size())));
                    fresh.setCount(1 + random.nextInt(64));
                    fresh.set(DataComponents.CUSTOM_NAME, Component.literal("new-" + seed + "-" + i));
                    this.stacks.set(index, fresh);
                    changed++;
                }
            }
        }
        return changed;
    }

    /**
     * @return A description of the generated composition, for the report.
     */
    public String composition() {
        return this.stacks.size() + " stacks (" + this.plainCount + " plain, "
                + this.smallCount + " small payload, " + this.heavyCount + " heavy payload)";
    }

    public void clear() {
        this.stacks.clear();
    }

    @Override
    public int size() {
        return this.stacks.size();
    }

    @Override
    public ItemResource getResource(int index) {
        return ItemResource.of(getStack(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return getStack(index).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return 64;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return false;
    }

    // Read-only: opens never write, and keeping it inert avoids perturbing what we measure.
    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return 0;
    }

    private ItemStack getStack(int index) {
        return index >= 0 && index < this.stacks.size() ? this.stacks.get(index) : ItemStack.EMPTY;
    }
}
