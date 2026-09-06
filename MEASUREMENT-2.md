# Storage terminal open cost, after the fix

A follow-up to [MEASUREMENT.md](MEASUREMENT.md). That report measured the cost of opening a
storage terminal and found it superlinear in the number of stored stacks. This one measures the
same thing again after the cause was found and fixed, and re-decides
[#219](https://github.com/CyclopsMC/IntegratedTerminals/issues/219) on the new numbers.

Every number below comes from a run made for this report. Where something could not be run,
it says so.

## What changed

Four changes, in two repositories.

**CyclopsCore, [PR 238](https://github.com/CyclopsMC/CyclopsCore/pull/238).** Three changes. The
first is the fix; the other two exist because measuring the first exposed them.

*The ItemStack hash ignored data components.* `IItemStackHelpers.getItemStackHashCode` hashed
only the count and the item, while equality compares components in full. Every stack of the same
item therefore hashed alike, so the hash-based ingredient collections that IntegratedDynamics
builds its storage index from collapsed into one bucket per item type. Each lookup in such a
bucket became a linear scan doing full component comparisons, which is where the superlinear
scaling came from. The hash now includes the component map.

*Classified lookups scanned everything when the classifier was empty.* A single-classified
collection partitions instances by a category type, and ItemStack is classified by its item. When
a query's match condition covers that category, every match has to share the query's classifier,
so an absent classifier means an empty result. `contains` and `iterator` returned one directly,
but `getAll`, `keySet`, `containsKey`, `countKey` and `count` fell through to the unclassified
path, which scans every instance to produce a result already known to be empty. A storage index
keeps one classified map per priority level, so an item lookup visits every level and every level
not holding that item scanned all of its entries. This is a pre-existing bug, independent of the
hash. It is why item-only lookups were slow before any of this, and it is what made them slower
still once hashing stopped being nearly free.

*Plain stacks hashed their item's default components.* A component map hashes its prototype
alongside its patch, and the prototype is the item's defaults, which the item already in the hash
stands for. For a stack carrying no patch that walk was the entire cost of the hash, and plain
stacks are what most of a storage network consists of. It is skipped now. This stays consistent
with equality because `PatchedDataComponentMap` keeps its patch sanitized: setting a component to
its default removes it from the patch rather than storing it, so two stacks of one item have
equal components exactly when they have equal patches.

**IntegratedTerminals, branch `perf/terminal-open`.** The client tab kept one collapsed
ingredients collection per channel plus one for the wildcard channel, and applied every change
to both. On a single-channel network those two collections hold exactly the same thing, so the
same collection was being built twice. With one channel the wildcard view is now the very same
object as that channel's view; the alias is broken and an independent copy taken as soon as a
second channel appears.

## Confirming the cause

The hypothesis was that the component-blind hash caused pathological collisions. It was tested
by profiling before changing anything.

JFR profile of the unmodified 50 000 stack first open, 3651 execution samples on the server
thread:

* 65.3% inside `IngredientInstanceWrapper.equals` to `IIngredientMatcher.matchesExactly` to
  `ItemMatch.areItemStacksEqual` to `DataComparator.compare`
* 61.3% inside treeified `HashMap` buckets (`HashMap$TreeNode.find` 57.1%,
  `getTreeNode` 43.3%)

Java converts a bucket to a red-black tree only at eight or more collisions, so a treeified
bucket is direct evidence of pathological collisions rather than ordinary hashing cost. The
mechanism is fully accounted for: `IngredientCollectionPrototypeMap.getPrototype` normalizes the
count to 1, which removes the only other varying term and leaves the hash a function of the item
alone.

Hypothesis confirmed.

## Before and after

Loopback, one channel, no shaping. Times in milliseconds.

The before column is the P0 loopback matrix from MEASUREMENT.md, medians of two runs after a
discarded warm-up. The after column is a fresh run of the same scenarios against the fixed
build carrying all four changes, medians of seven runs after a discarded warm-up. The sample
sizes differ, which matters for the 1000 stack rows where the effect is smaller than the spread,
and not for the rest.

| stacks | scenario | server before | server after | client before | client after |
|---:|---|---:|---:|---:|---:|
| 1 000 | A first open | 5.1 | 4.4 | 11.5 | 10.0 |
| 1 000 | B re-open | 4.5 | 4.1 | 15.5 | 10.0 |
| 1 000 | C 1% changed | 4.3 | 4.0 | 12.5 | 11.0 |
| 1 000 | D 10% changed | 4.1 | 4.0 | 11.0 | 9.0 |
| 10 000 | A first open | 287.7 | 33.1 | 560.5 | 84.0 |
| 10 000 | B re-open | 285.4 | 33.0 | 611.5 | 78.0 |
| 10 000 | C 1% changed | 328.5 | 31.9 | 603.5 | 81.0 |
| 10 000 | D 10% changed | 224.8 | 29.4 | 440.0 | 68.0 |
| 50 000 | A first open | 5535.9 | 180.6 | 10482.0 | 567.0 |
| 50 000 | B re-open | 5524.3 | 173.5 | 10356.0 | 525.0 |

At 1 000 stacks nothing meaningful changes either way. At 10 000 the server is 8.7 times faster
and the client 6.7 times faster on a first open. At 50 000 the server is 30.7 times faster and
the client 18.5 times faster.

Byte counts are unchanged: 7373.6 KB raw and 843.3 KB compressed over 196 packets at 50 000
stacks, the same as before. Nothing about what is sent changed, only what it costs to build and
apply.

### Scaling

The point of MEASUREMENT.md was that the cost grew faster than the network did. It no longer
does.

| stacks | server before | ratio | server after | ratio |
|---:|---:|---:|---:|---:|
| 1 000 | 5.1 | 1x | 4.4 | 1x |
| 10 000 | 287.7 | 56x | 33.1 | 7.5x |
| 50 000 | 5535.9 | 1085x | 180.6 | 41x |

| stacks | client before | ratio | client after | ratio |
|---:|---:|---:|---:|---:|
| 1 000 | 11.5 | 1x | 10.0 | 1x |
| 10 000 | 560.5 | 49x | 84.0 | 8.4x |
| 50 000 | 10482.0 | 911x | 567.0 | 57x |

Fifty times the stacks now costs 41 times the server work and 57 times the client work. That is
linear within the noise of these runs. Before, it cost a thousand times as much.

### The shapes that were worst

Two networks were built specifically to make collisions as bad as possible, by putting every
component-bearing stack on a single item so they differ only by their components.

| case | before | after |
|---|---|---|
| 50 000 stacks, all heavy stacks on `diamond_sword` | tripped the 60 s single-tick watchdog | 335.7 ms server, 933.5 ms client |
| 10 000 stacks, 10% change adding stacks of one item type | 1503.4 ms server | 31.8 ms server, 116.5 ms client |

The first of those is the case a player actually hits: a storage full of enchanted books,
damaged tools or written books. It used to hang the server hard enough to trip the watchdog.
It now costs about a third of a second of server time.

## Was the wildcard change worth it

Measured on its own, at 10 000 stacks, scenario A, medians of six and nine runs after a
discarded warm-up, both against the fixed CyclopsCore. Only the client is affected; this is a
client-side change.

| | without the alias | with the alias |
|---|---:|---:|
| client complete | 180.5 | 177.0 |
| of which apply | 32.8 | 21.1 |
| of which sort and filter | 34.5 | 25.3 |
| server main | 37.9 | 33.9 |

The duplicated work is real and it goes away: applying the incoming changes costs a third less,
and rebuilding the sorted view a quarter less. End to end it is not visible. Client completion
moves by 2%, which is inside the run-to-run spread of these measurements, and the server
difference is noise since the change is client-only. Deserialization moved by more than the
whole saving between the two runs in the opposite direction.

So the change removes a genuine duplicate, halves the client-side memory the terminal holds on a
single-channel network, and does not measurably speed up an open. It is worth keeping on those
grounds, not on a speed claim.

## Does channel count itself cost anything

MEASUREMENT.md reported four-channel networks opening much faster than single-channel ones at
the same total size, and flagged the comparison as confounded: the generator caps the plain
component-free stacks at the number of registered item types per channel, so a four-channel
network of the same total size held far more plain stacks and far fewer component-bearing ones.

Settling it needs a size at which the cap binds in neither configuration. At 2 000 stacks the
compositions come out identical: 1 200 plain, 600 small payload and 200 heavy payload in both
cases, confirmed by the generator's own output and by the byte counts (240.1 KB against 239.1 KB
raw). Medians of six runs after a discarded warm-up, against the fixed build.

| channels | server main | client complete | of which apply | of which sort and filter |
|---:|---:|---:|---:|---:|
| 1 | 9.7 | 22.0 | 4.7 | 4.9 |
| 2 | 9.7 | 24.5 | 9.3 | 3.0 |
| 4 | 14.1 | 25.0 | 8.9 | 2.8 |

Client apply steps up between one and two channels and then stays flat. That step is the
wildcard view: with one channel it is aliased, with two or more it is maintained separately, so
the work doubles once and does not grow after that. Client completion behaves the same way,
3 ms then flat. Server main is 9.7, 9.7, 14.1, with no trend that survives the spread seen
elsewhere in these runs.

So channel count itself is not a meaningful cost, and the earlier appearance of four channels
being faster was the composition confound plus the hash bug. Lazy per-channel initialization
would not buy anything and was not implemented.

## Where the time goes now

JFR, same environment, 50 000 stacks, scenarios A and B. The recording covers the whole run, so
the percentages below are within the samples that touch the open path, not within the whole
recording.

**Server**, 297 samples inside `ContainerTerminalStorageBase.broadcastChanges`:

| share | frame |
|---:|---|
| 75.1% | `TerminalStorageTabIngredientComponentServer.reApplyFilter` |
| 66.3% | `ItemStackHelpersCommon.getItemStackHashCode` |
| 66.0% | `PatchedDataComponentMap.hashCode` |
| 58.6% | `TerminalStorageTabIngredientComponentServer.initChannel` |

Leaf frames: `Reference2ObjectMaps.fastIterator` 16.5%, `Reference2ObjectOpenHashMap.hashCode`
12.8%, `Objects.hashCode` 11.4%, `PatchedDataComponentMap.hashPatch` 9.8%.

**Client**, 534 samples on the main thread that touch IntegratedTerminals code:

| share | frame |
|---:|---|
| 48.1% | `TerminalStorageIngredientChangeEventPacket.actionClient` |
| 46.6% | `ContainerScreenTerminalStorage.extractBackground` |
| 40.4% | `ContainerScreenTerminalStorage.drawTabContents` |
| 30.7% | `TerminalStorageTabIngredientComponentClient.getSlots` |
| 30.0% | `TerminalStorageTabIngredientComponentClient.getFilteredIngredientsView` |
| 28.8% | `IngredientInstanceWrapper.hashCode` |

Leaf frames: `Objects.hashCode` 8.2%, `HashMap.getNode` 6.6%,
`Reference2ObjectArrayMap.reference2ObjectEntrySet` 4.7%, `PatchedDataComponentMap.hashPatch`
4.3%.

The cost has moved. It used to be comparing components inside oversized hash buckets; it is now
hashing components. That is a far better place for it to be, since hashing is done once per
lookup rather than once per collision, but it is still the single largest identifiable cost on
both sides.

These profiles were taken before the plain-stack change, which removes the prototype walk that
`Reference2ObjectOpenHashMap.hashCode` and `Objects.hashCode` above represent, so the hashing
share is smaller now than they show. They were not retaken.

## What the index benchmarks say

`PERFORMANCE_BENCHMARK_ENABLED=true ./gradlew runGameTestServer` in IntegratedDynamics, ms per
operation, medians of three whole runs each. The middle column separates the hash change from the
two that followed it. The `plain`, `single_item`, `few_items` and `heavy_components` shapes were
added for this work.

| benchmark | before | hash only | all three | vs before |
|---|---:|---:|---:|---|
| index_lookup_exact | 0.004513 | 0.001614 | 0.001837 | 2.5x faster |
| index_lookup_item | 0.277345 | 0.820625 | 0.001127 | 246x faster |
| index_modification | 0.000650 | 0.001789 | 0.002346 | ~3x slower |
| index_lookup_nonempty_first | 0.000215 | 0.000346 | 0.000269 | unchanged |
| index_lookup_nonempty_all | 0.012464 | 0.013368 | 0.011223 | unchanged |
| index_lookup_exact_plain | 0.001096 | 0.002014 | 0.000867 | 1.3x faster |
| index_lookup_item_plain | 0.296732 | 0.717556 | 0.000956 | 310x faster |
| index_modification_plain | 0.000574 | 0.001276 | 0.000582 | unchanged |
| index_lookup_exact_single_item | 3.692786 | 0.001340 | 0.001257 | 2938x faster |
| index_modification_single_item | 3.190534 | 0.001113 | 0.001214 | 2628x faster |
| index_lookup_item_single_item | 0.431085 | 0.558893 | 0.559649 | 1.3x slower |
| index_lookup_exact_few_items | 0.070948 | 0.000883 | 0.000804 | 88x faster |
| index_modification_few_items | 0.159795 | 0.001145 | 0.001208 | 132x faster |
| index_lookup_exact_heavy_components | 0.003313 | 0.003172 | 0.002793 | 1.2x faster |
| index_modification_heavy_components | 0.000421 | 0.002611 | 0.002580 | 6x slower |

The `plain` shape is 5000 stacks distinct by item and count with no components at all. It is
faster or unchanged on every benchmark.

### What a realistic composition costs

`plain` and `spread` bracket a real storage network rather than describing one. A `mixed` shape
gives one instance in ten a component and leaves the rest plain, which is closer to a modpack:
bulk material with a tail of enchanted, damaged or named items. Medians of three runs each.

| benchmark | before | after | factor |
|---|---:|---:|---|
| index_lookup_exact_mixed | 0.001618 | 0.001022 | 1.6x faster |
| index_lookup_item_mixed | 0.220683 | 0.000955 | 231x faster |
| index_modification_mixed | 0.000501 | 0.000710 | 1.4x slower |

The modification cost tracks the fraction of instances carrying components, since only those pay
for the hash:

| component-bearing share | modification, after vs before |
|---|---|
| 0% (`plain`) | 1.05x |
| 10% (`mixed`) | 1.42x |
| 74% (`spread`) | 1.70x |
| 100% with a large payload (`heavy_components`) | 6.53x |

In absolute terms a modification on the mixed shape costs 209 ns more than it did. An item-only
lookup on the same shape costs 220 microseconds less. One avoided lookup pays for roughly a
thousand modifications, so on any network where item-only lookups happen at all, which is every
extraction that ignores components, the trade is heavily one-sided.

The hash-only column is the reason the other two changes exist. Taken alone the hash makes
item-only lookups three times slower and modifications two to three times slower, which would be
a bad trade for a mod pack full of unique items. Neither was caused by the hash being wrong: the
item-only cost was the classifier fall-through described above, and the modification cost was
hashing item defaults that the item already stood for.

Three regressions survive.

**`index_modification` on the spread shape, about 3x.** That fixture is 74% component-bearing, so
most modifications hash a component map on add and on remove. Run to run spread here is large,
0.000530 to 0.001029 before and 0.001258 to 0.003469 after across three runs each, so the factor
is not well determined; the direction is. On the plain shape, where a modification hashes no
components, it is unchanged.

**`index_modification_heavy_components`, about 6x.** Every stack there carries lore plus four
contained stacks, so hashing them is genuinely more work than not hashing them. This is the case
the hash exists for, and 2.6 microseconds per operation buys the 2628x on
`index_modification_single_item`.

**`index_lookup_item_single_item`, about 1.3x.** Asking for every variant of an item that has
5000 of them has to return 5000 results, so the classifier cannot help and each result costs a
slightly dearer hash.

## Remaining hot spots, ranked

Not implemented. Listed in the order I would take them.

1. **Hash the prototype once per index operation instead of two to four times.**
   `IngredientMapWrappedAdapter` builds a fresh wrapper inside each of `get`, `put` and `remove`,
   so the get-then-put pairs in `IngredientCollectionPrototypeMap.add` and
   `IngredientPositionsIndex.addPosition` hash the same prototype twice. Every storage change is
   then applied twice over, once to its own channel and once to the wildcard channel, in
   `PositionedAddonsNetworkIngredients.applyChangesToChannel`. A compute or merge style method on
   `IIngredientMapMutable`, overridden in the wrapped and classified maps, would collapse each
   pair into one hash. Hashing is 63.6% of index modification samples, so this is worth roughly a
   third of what remains of the modification regression.

   An earlier version of this report claimed that caching the hash inside
   `IngredientInstanceWrapper` was the biggest win here. That was wrong. Each `wrap` call
   produces a fresh wrapper that is hashed exactly once, and `HashMap` stores the hash in its
   nodes so it never recomputes on resize. Caching in the wrapper helps only where a stored
   wrapper is hashed again, which is the `iterator()` case in item 3 below.
2. **Coalesce the client view rebuild during the initial burst.** Every applied change packet
   calls `resetFilteredIngredientsViews`, and the next rendered frame rebuilds the filtered and
   sorted list from scratch. A 50 000 stack open sends 196 packets, so the list can be rebuilt
   many times while the terminal is still filling. Sorting and filtering is 73 to 80 ms of the
   525 to 567 ms open at 50 000 stacks. Deferring the rebuild while packets are still arriving
   would recover most of it.
3. **`IngredientMapWrappedAdapter.iterator()`** calls `collection.get(key)` for every key while
   iterating the key set, hashing each entry a second time. I tried the `entrySet()` version and
   measured no improvement on `index_lookup_item` (0.797 against 0.801 ms per operation), so the
   theory that it explained that benchmark was wrong; the classifier fall-through did. It is
   still redundant work and may pay off elsewhere.

## Decision on #219

[#219](https://github.com/CyclopsMC/IntegratedTerminals/issues/219) proposes keeping terminal
contents on the client across sessions, so that re-opening a terminal does not resend everything.
Its premise is that re-opening is expensive. Scenario B is exactly that case.

Scenario B at 50 000 stacks now costs **173.5 ms of server main thread time and 525.0 ms of
client wall time**. The decision rule set for this task was to recommend closing or parking the
issue if that came in under roughly 500 ms server and 1 s client. It does, on both.

For scale, 50 000 distinct stacks is a very large network. At 10 000 stacks, which is already a
big storage, a re-open costs 33.0 ms of server time and 78.0 ms on the client. At 1 000 stacks
it is 4.1 ms and 10.0 ms.

The recommendation is to park #219. The cost it was written to avoid has fallen by a factor of
about thirty, and what remains is not a cost worth the complexity of client-side persistence,
delta packets after close, or proximity prefetching, each of which brings cache invalidation
problems of its own. If it is picked up again later, it should be on fresh numbers rather than
these, and the two hot spots at the top of the list above are cheaper ways to buy the same time.

A comment drafted for the issue, not posted:

> Measured this after fixing the underlying cause. `getItemStackHashCode` in CyclopsCore left
> data components out of the hash while equality compared them in full, so every stack of the
> same item landed in one hash bucket and every storage lookup degenerated into a scan. Fixing
> that also turned up two more things in the same path: classified lookups fell back on a full
> scan whenever the queried classifier held nothing, and every hash walked the item's default
> components even for stacks carrying none.
>
> With all three fixed (CyclopsMC/CyclopsCore#238), re-opening a terminal on a 50 000 stack
> network costs 174 ms of server main thread time and 525 ms on the client, down from 5.5 s and
> 10.4 s. At 10 000 stacks it is 33 ms and 78 ms. Item-only storage lookups, which every
> extraction that ignores components performs, went from 0.28 ms to 0.001 ms per operation.
>
> That removes the reason this issue was opened. Suggest parking it: the remaining cost is not
> worth client-side persistence and its invalidation problems. If it comes back, the two cheaper
> wins are collapsing the repeated prototype hashing in the index update path, and coalescing the
> client's sorted-view rebuild while the initial packet burst is still arriving.
>
> Full numbers and profiles in MEASUREMENT-2.md on the measurement branch.

## What was not verified

* The before column comes from a two-sample matrix and the after column from a seven-sample one.
  The effect sizes at 10 000 and 50 000 stacks are far larger than the spread, so this does not
  affect the conclusion; at 1 000 stacks it means those rows say nothing.
* All timings are loopback on one machine. MEASUREMENT.md established that the link was not the
  constraint before the fix, and the byte counts are unchanged, so shaped profiles were not
  re-run.
* The client was profiled this time, unlike in the first report, but the client percentages are
  over samples that touch IntegratedTerminals code, which excludes rendering and network frames
  that do not.
* The IntegratedDynamics benchmark numbers come from `runGameTestServer` with
  `PERFORMANCE_BENCHMARK_ENABLED=true`, not from a terminal open. No terminal scenario was built
  that makes item-only lookups or index modifications dominant, so the practical weight of the
  surviving modification regressions is unquantified.
* The profiles in the section above were taken before the last two changes and were not retaken,
  so they overstate how much of the remaining time is component hashing.
* Fabric and Forge keep the common `hasComponentPatch` implementation, which builds a patch
  instance for stacks that carry one. Only NeoForge was benchmarked.
* The dedicated server still does not boot on `master-26-lts` in userdev without the debug-flag
  bypass described in MEASUREMENT.md. That bug is unrelated and untouched.
