# Storage terminal open cost, after the fix

A follow-up to [MEASUREMENT.md](MEASUREMENT.md). That report measured the cost of opening a
storage terminal and found it superlinear in the number of stored stacks. This one measures the
same thing again after the cause was found and fixed, and re-decides
[#219](https://github.com/CyclopsMC/IntegratedTerminals/issues/219) on the new numbers.

Every number below comes from a run made for this report. Where something could not be run,
it says so.

## What changed

Two changes, in two repositories.

**CyclopsCore, [PR 238](https://github.com/CyclopsMC/CyclopsCore/pull/238).**
`IItemStackHelpers.getItemStackHashCode` hashed only the count and the item, leaving data
components out, while equality compares components in full. Every stack of the same item
therefore hashed alike, so the hash-based ingredient collections that IntegratedDynamics builds
its storage index from collapsed into one bucket per item type. Each lookup in such a bucket
became a linear scan doing full component comparisons, which is where the superlinear scaling
came from. The hash now includes the component map.

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
build, medians of four runs after a discarded warm-up. The sample sizes differ, which matters
for the 1000 stack rows where the effect is smaller than the spread, and not for the rest.

| stacks | scenario | server before | server after | client before | client after |
|---:|---|---:|---:|---:|---:|
| 1 000 | A first open | 5.1 | 4.6 | 11.5 | 10.5 |
| 1 000 | B re-open | 4.5 | 4.5 | 15.5 | 10.5 |
| 1 000 | C 1% changed | 4.3 | 5.7 | 12.5 | 11.5 |
| 1 000 | D 10% changed | 4.1 | 4.7 | 11.0 | 10.0 |
| 10 000 | A first open | 287.7 | 33.0 | 560.5 | 93.0 |
| 10 000 | B re-open | 285.4 | 34.5 | 611.5 | 92.5 |
| 10 000 | C 1% changed | 328.5 | 31.5 | 603.5 | 82.5 |
| 10 000 | D 10% changed | 224.8 | 29.8 | 440.0 | 70.5 |
| 50 000 | A first open | 5535.9 | 163.7 | 10482.0 | 533.0 |
| 50 000 | B re-open | 5524.3 | 186.6 | 10356.0 | 671.5 |

At 1 000 stacks nothing meaningful changes either way. At 10 000 the server is 8.7 times faster
and the client 6.0 times faster on a first open. At 50 000 the server is 33.8 times faster and
the client 19.7 times faster.

Byte counts are unchanged: 7373.6 KB raw and 843.3 KB compressed over 196 packets at 50 000
stacks, the same as before. Nothing about what is sent changed, only what it costs to build and
apply.

### Scaling

The point of MEASUREMENT.md was that the cost grew faster than the network did. It no longer
does.

| stacks | server before | ratio | server after | ratio |
|---:|---:|---:|---:|---:|
| 1 000 | 5.1 | 1x | 4.6 | 1x |
| 10 000 | 287.7 | 56x | 33.0 | 7.2x |
| 50 000 | 5535.9 | 1085x | 163.7 | 36x |

| stacks | client before | ratio | client after | ratio |
|---:|---:|---:|---:|---:|
| 1 000 | 11.5 | 1x | 10.5 | 1x |
| 10 000 | 560.5 | 49x | 93.0 | 8.9x |
| 50 000 | 10482.0 | 911x | 533.0 | 51x |

Fifty times the stacks now costs 36 times the server work and 51 times the client work. That is
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
lookup rather than once per collision, but it is now the single largest identifiable cost on
both sides.

## Remaining hot spots, ranked

Not implemented. Listed in the order I would take them.

1. **Cache the ItemStack component hash.** `IngredientInstanceWrapper.hashCode` recomputes
   `getComponent().getMatcher().hash(getInstance())` on every call, and that walks the whole
   component map. The wrapper holds an immutable instance and stores nothing, so a lazily
   computed final field would remove most of the hashing seen in both profiles above. This is
   the largest single item on either side and the cheapest to do. It belongs in
   CommonCapabilitiesAPI, which is a separate repository.
2. **Coalesce the client view rebuild during the initial burst.** Every applied change packet
   calls `resetFilteredIngredientsViews`, and the next rendered frame rebuilds the filtered and
   sorted list from scratch. A 50 000 stack open sends 196 packets, so the list can be rebuilt
   many times while the terminal is still filling. Sorting and filtering is 71 to 105 ms of the
   533 to 671 ms open at 50 000 stacks. Deferring the rebuild while packets are still arriving
   would recover most of it.
3. **`IngredientMapWrappedAdapter.iterator()`** calls `collection.get(key)` for every key while
   iterating the key set, hashing each entry a second time. I tried the `entrySet()` version and
   measured no improvement on the IntegratedDynamics `index_lookup_item` benchmark (0.797 against
   0.801 ms per operation), so the theory that it explains that benchmark's regression is wrong.
   It is still redundant work and may pay off elsewhere.
4. **Item-only lookups regressed.** With components in the hash, a match on item alone has to
   visit every component variant of that item rather than a single bucket. The
   IntegratedDynamics `index_lookup_item` benchmark went from 0.241 to 0.798 ms per operation.
   Nothing on the terminal open path showed this, but a secondary index keyed on item alone
   would remove it if some other path turns out to care.

## Decision on #219

[#219](https://github.com/CyclopsMC/IntegratedTerminals/issues/219) proposes keeping terminal
contents on the client across sessions, so that re-opening a terminal does not resend everything.
Its premise is that re-opening is expensive. Scenario B is exactly that case.

Scenario B at 50 000 stacks now costs **186.6 ms of server main thread time and 671.5 ms of
client wall time**. The decision rule set for this task was to recommend closing or parking the
issue if that came in under roughly 500 ms server and 1 s client. It does, on both.

For scale, 50 000 distinct stacks is a very large network. At 10 000 stacks, which is already a
big storage, a re-open costs 34.5 ms of server time and 92.5 ms on the client. At 1 000 stacks
it is 4.5 ms and 10.5 ms.

The recommendation is to park #219. The cost it was written to avoid has fallen by a factor of
about thirty, and what remains is not a cost worth the complexity of client-side persistence,
delta packets after close, or proximity prefetching, each of which brings cache invalidation
problems of its own. If it is picked up again later, it should be on fresh numbers rather than
these, and the two hot spots at the top of the list above are cheaper ways to buy the same time.

A comment drafted for the issue, not posted:

> Measured this after fixing the underlying cause. `getItemStackHashCode` in CyclopsCore left
> data components out of the hash while equality compared them in full, so every stack of the
> same item landed in one hash bucket and every storage lookup degenerated into a scan. With
> that fixed (CyclopsMC/CyclopsCore#238), re-opening a terminal on a 50 000 stack network costs
> 186 ms of server main thread time and 672 ms on the client, down from 5.5 s and 10.4 s. At
> 10 000 stacks it is 35 ms and 93 ms.
>
> That removes the reason this issue was opened. Suggest parking it: the remaining cost is not
> worth client-side persistence and its invalidation problems. If it comes back, the two cheaper
> wins are caching the ItemStack component hash in `IngredientInstanceWrapper`, which is now the
> largest single frame in both server and client profiles, and coalescing the client's
> sorted-view rebuild while the initial packet burst is still arriving.
>
> Full numbers and profiles in MEASUREMENT-2.md on the measurement branch.

## What was not verified

* The before column comes from a two-sample matrix and the after column from a four-sample one.
  The effect sizes at 10 000 and 50 000 stacks are far larger than the spread, so this does not
  affect the conclusion; at 1 000 stacks it means those rows say nothing.
* All timings are loopback on one machine. MEASUREMENT.md established that the link was not the
  constraint before the fix, and the byte counts are unchanged, so shaped profiles were not
  re-run.
* The client was profiled this time, unlike in the first report, but the client percentages are
  over samples that touch IntegratedTerminals code, which excludes rendering and network frames
  that do not.
* The IntegratedDynamics benchmark numbers quoted for the item-only regression come from
  `runGameTestServer` with `PERFORMANCE_BENCHMARK_ENABLED=true`, not from a terminal open. No
  terminal scenario was built that makes item-only lookups dominant, so the practical cost of
  that regression is unquantified.
* The dedicated server still does not boot on `master-26-lts` in userdev without the debug-flag
  bypass described in MEASUREMENT.md. That bug is unrelated and untouched.
