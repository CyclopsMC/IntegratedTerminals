# Measuring the cost of opening a storage terminal

Measurement only, for issue #219. Nothing here changes how the terminal behaves.
Every number comes from a run in the environment described below; where something
could not be measured, that is stated rather than estimated.

## Environment

| | |
|---|---|
| Minecraft | 26.1.2 |
| NeoForge | 26.1.2.22-beta |
| IntegratedTerminals | 1.9.0-DEV, branch `measure/terminal-open-cost` |
| IntegratedDynamics | 1.33.2-1847 |
| IntegratedCrafting | 1.6.0-710 |
| CyclopsCore | 1.29.5-1012 |
| JVM | Temurin 25.0.4.1+1, server heap 3416 MB |
| OS | Ubuntu 24.04.4, Linux 6.18.44 x86_64 |
| CPU | Intel Xeon @ 2.10 GHz, 4 logical cores |
| RAM | 15 GB |
| Server | dedicated server (`./gradlew runServer`), `network-compression-threshold=256`, `online-mode=false` |
| Client | dev client (`./gradlew runClient`) driven by clientdevbridge-cli, Xvfb + Mesa llvmpipe software GL |
| Link | client and server on the same host; loopback for P0, through a userspace TCP proxy for P1 to P3 |

Server and client are separate JVMs on one 4-core machine, so they compete for CPU.
On a real server the two sides would not share cores, which makes the client timings
here pessimistic and the server timings slightly so.

## How it was measured

Instrumentation sits behind `GeneralConfig.debugTerminalOpenMetrics`, a plain static
boolean that defaults to false and is set per JVM with
`-Dintegratedterminals.debugTerminalOpenMetrics=true`. With the flag off, none of the
timing or sizing code runs.

Server side, one open spans all tabs of one container, from the first
`ITerminalStorageTabServer.init()` in `ContainerTerminalStorageBase.broadcastChanges()`
until the last chunk has been encoded. Every packet is sized by encoding it through its
`StreamCodec` into a fresh `RegistryFriendlyByteBuf` and reading `readableBytes()`, and
compressed with `Deflater` at the default level as a proxy for the wire size, since a
dedicated server compresses packets over 256 bytes. That sizing pass costs time of its
own, which is measured separately and subtracted from the reported figures; the raw
overhead is in the CSV as `measureOverheadMs`. Batches larger than
`terminalStoragePacketMaxInstances` are encoded on the `packetSerializer` executor, so
the open is only considered finished once those tasks have run: `mainThreadMs` is the
server thread cost and `totalMs` runs to the last encoded chunk.

Client side, deserialization is timed separately from the `Minecraft.getInstance().execute`
body. `firstContentMs` is when the first packet has been applied, which is when the player
first sees anything; `completeMs` is the last applied packet, with an open considered
finished once no further packet has been applied for 500 ms. The sorted and filtered view
is rebuilt lazily on render rather than in `onChange`, so it is timed where it actually
happens, in `getFilteredIngredientsView`. `longestStallMs` is the longest single
render-thread apply body.

Both sides capture their scenario tag when the open starts rather than when the row is
written, because a row written late would otherwise be labelled with the next scenario.

### The test network

A dev-only command builds an IntegratedDynamics network with a cable, a storage terminal
part, and one synthetic storage per channel. The storage is a `ResourceHandler<ItemResource>`
exposed on a marker block, registered on the ingredient network with
`IPositionedAddonsNetworkIngredients.addPosition`. This avoids needing roughly 1850 chests
and their block entities to reach 50k distinct stacks, while still exercising the real
index, the real observer and the real packet path.

Stack composition was specified as roughly 60% plain, 30% small payload, 10% heavy payload.
Plain means no data components at all, and distinctness then can only come from distinct
item types, of which this instance has 1615. The generator therefore fills the plain bucket
up to that limit and spills the remainder into the small bucket:

| distinct stacks | plain | small payload | heavy payload |
|---:|---:|---:|---:|
| 1 000 | 600 | 300 | 100 |
| 10 000 | 1 615 | 7 385 | 1 000 |
| 50 000 | 1 615 | 43 385 | 5 000 |

Small payload is a custom name. Heavy payload is a custom name, three lines of lore, three
enchantments and four contained item stacks. At 10k and 50k the mix is therefore heavier
per stack than the specification intended, since the plain shortfall lands in the
small-payload bucket rather than staying component-free.

The 1615-item cap applies per channel, so the four-channel configurations have a lighter
mix than the one-channel configuration at the same total size: at 10k over four channels
each channel holds 2500 stacks of which 1500 are plain, giving 6000 plain overall against
1615 for the single-channel case. Byte totals are therefore not directly comparable
between the 1ch and 4ch rows at the same stack count.

### Scenarios

- **A** first open
- **B** close, re-open immediately, no storage changes
- **C** close, change 1% of stacks, re-open
- **D** close, change 10% of stacks, re-open
- **E** four channels, open while viewing only channel 0

Three repetitions each, the first discarded as warm-up; tables report medians of the
remaining two. Changes for C and D are applied while a terminal is open, for a reason
that is itself a finding: see the observations below.

### Shaping the network

`tc` is not present in this environment and the kernel offers no netem, so shaping is done by
a userspace TCP proxy (`measurement-tools/netshape.py`) rather than at the kernel level. It
listens on one port, connects to the server on another, and relays bytes both ways, applying a
token bucket for bandwidth and then a fixed hold for propagation delay, with ordering preserved
by a queue. Minecraft is a single plain TCP connection, so the relay is transparent to the game.
The bucket allows a 50 ms burst rather than a full second, because a one-second allowance would
let the entire first second of an open through unshaped, which is the part being measured.

The obvious caveat of a userspace relay is that it terminates TCP, so the client's congestion
window is with the proxy rather than the real server, and slow start over a high-latency link
is not modelled faithfully. That matters less here than it normally would, because the results
show the link is not the constraint. Only one shaping implementation was available, so the
spot-check comparing netem against the relay could not be done.

The proxy was verified against a sink server before use: 2 MB through a 250 000 B/s cap took
8.045 s (249 KB/s), through a 625 000 B/s cap 3.229 s (619 KB/s), a 150 ms delay put the first
byte at 0.153 s, and with no shaping 2 MB crossed in 6 ms.

Profiles, delay being one way so the round trip is double:

| profile | one-way delay | server to client bandwidth | represents |
|---|---:|---:|---|
| P0 | 0 ms | unlimited | loopback control, no proxy in the path |
| P0ctl | 0 ms | unlimited | same matrix through the proxy, to price the proxy itself |
| P1 | 25 ms | 20 Mbit/s | same-continent public server, good connection |
| P2 | 75 ms | 5 Mbit/s | cross-continent server, ordinary home connection |
| P3 | 150 ms | 2 Mbit/s | poor connection, or a busy server sharing upload |

The P0ctl control confirms the proxy costs nothing measurable. First open, one channel:

| | compressed KB | server main thread ms | client complete ms |
|---|---:|---:|---:|
| 1 000 stacks, direct | 18.3 | 5.1 | 11.5 |
| 1 000 stacks, through proxy | 18.3 | 4.9 | 13.0 |
| 10 000 stacks, direct | 171.4 | 287.7 | 560.5 |
| 10 000 stacks, through proxy | 171.4 | 265.3 | 499.5 |

Byte counts are identical and the timings differ by less than the run-to-run spread, in both
directions, so the proxy is not adding cost.

### One change that was not measurement

The dedicated server will not boot on this branch's parent, for a reason unrelated to terminals
and described under the observations below. To get a server at all, the registration of
`CraftingJobFinishedToastPacket` is skipped, and only when the debug flag is set and only on a
dedicated server. Nothing on the measured path is touched: packet sizes, chunk sizes, the
serializer thread pool and every code path an open takes are exactly as they are on master.

## Results

### Every scenario and profile

Medians over the repetitions after the warm-up. `n` is how many rows contributed.
Byte and packet counts are server-side, measured at the point of sending; the client's own
byte counter omits the small max-quantity packets, which are under 0.1% of the total.

| profile | stacks | channels | scenario | n | instances sent | packets | raw KB | compressed KB | server main ms | server total ms | client first ms | client complete ms | fill gap ms | deserialize ms | apply ms | sort/filter ms | longest stall ms |
|---|---:|---:|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| P0 | 1000 | 1ch | A | 2 | 1000 | 4 | 120.1 | 18.3 | 5.1 | 8.3 | 7.1 | 11.5 | 4.4 | 3.5 | 3.9 | 2.0 | 2.2 |
| P0 | 1000 | 1ch | B | 2 | 1000 | 4 | 120.1 | 18.3 | 4.5 | 8.5 | 7.0 | 15.5 | 8.5 | 4.1 | 6.3 | 1.9 | 4.3 |
| P0 | 1000 | 1ch | C | 2 | 992 | 4 | 118.0 | 18.1 | 4.3 | 7.8 | 7.7 | 12.5 | 4.8 | 3.6 | 3.5 | 2.1 | 2.0 |
| P0 | 1000 | 1ch | D | 2 | 918 | 4 | 106.1 | 16.5 | 4.1 | 5.6 | 7.7 | 11.0 | 3.3 | 3.5 | 3.6 | 1.9 | 2.3 |
| P0 | 1000 | 4ch | A | 2 | 1000 | 8 | 120.4 | 20.3 | 7.0 | 7.0 | 3.5 | 10.5 | 7.0 | 3.9 | 2.1 | 1.0 | 0.6 |
| P0 | 1000 | 4ch | B | 2 | 1000 | 8 | 120.4 | 20.3 | 7.0 | 7.0 | 3.9 | 10.0 | 6.1 | 3.8 | 2.0 | 1.0 | 0.6 |
| P0 | 1000 | 4ch | C | 2 | 990 | 12 | 120.1 | 20.4 | 6.7 | 6.7 | 4.3 | 12.0 | 7.7 | 3.8 | 2.2 | 1.1 | 0.6 |
| P0 | 1000 | 4ch | D | 2 | 914 | 12 | 111.1 | 19.4 | 5.4 | 5.4 | 3.8 | 11.0 | 7.2 | 3.8 | 2.2 | 1.1 | 0.6 |
| P0 | 1000 | 4ch | E | 2 | 1000 | 12 | 120.7 | 20.5 | 7.8 | 7.8 | 4.9 | 11.5 | 6.6 | 3.8 | 2.5 | 1.0 | 0.6 |
| P0 | 10000 | 1ch | A | 2 | 10000 | 40 | 1407.3 | 171.4 | 287.7 | 287.7 | 179.6 | 560.5 | 380.9 | 35.9 | 341.7 | 56.9 | 33.6 |
| P0 | 10000 | 1ch | B | 2 | 10000 | 40 | 1407.3 | 171.4 | 285.4 | 285.4 | 220.4 | 611.5 | 391.1 | 35.7 | 342.1 | 56.7 | 33.5 |
| P0 | 10000 | 1ch | C | 2 | 9918 | 40 | 1394.9 | 170.8 | 328.5 | 328.5 | 221.9 | 603.5 | 381.6 | 34.5 | 336.9 | 59.4 | 43.3 |
| P0 | 10000 | 1ch | D | 2 | 9139 | 37 | 1273.0 | 156.6 | 224.8 | 224.8 | 119.2 | 440.0 | 320.8 | 31.2 | 279.2 | 47.3 | 28.6 |
| P0 | 10000 | 1chcraft | A | 2 | 10000 | 47 | 1447.8 | 175.0 | 276.6 | 276.6 | 220.4 | 582.5 | 362.1 | 34.7 | 314.7 | 72.2 | 22.9 |
| P0 | 10000 | 1chcraft | B | 2 | 10000 | 47 | 1447.8 | 175.0 | 260.9 | 260.9 | 120.2 | 486.5 | 366.3 | 34.6 | 319.3 | 56.2 | 23.8 |
| P0 | 10000 | 4ch | A | 2 | 10000 | 40 | 1207.6 | 178.2 | 79.6 | 79.6 | 9.0 | 177.0 | 168.0 | 31.2 | 113.4 | 26.3 | 11.5 |
| P0 | 10000 | 4ch | B | 2 | 10000 | 40 | 1207.6 | 178.2 | 91.0 | 91.0 | 10.5 | 180.5 | 170.0 | 35.8 | 110.3 | 27.8 | 7.6 |
| P0 | 10000 | 4ch | C | 2 | 9920 | 44 | 1194.6 | 177.4 | 81.1 | 81.1 | 16.4 | 181.5 | 165.1 | 34.7 | 111.2 | 28.5 | 7.8 |
| P0 | 10000 | 4ch | D | 2 | 9160 | 44 | 1087.2 | 163.5 | 65.3 | 65.3 | 10.7 | 164.5 | 153.8 | 26.8 | 108.2 | 23.1 | 12.6 |
| P0 | 10000 | 4ch | E | 2 | 10000 | 44 | 1207.9 | 178.4 | 77.9 | 77.9 | 10.4 | 169.0 | 158.6 | 29.3 | 107.1 | 27.8 | 12.1 |
| P0 | 50000 | 1ch | A | 2 | 50000 | 196 | 7373.6 | 844.9 | 5535.9 | 5535.9 | 3230.4 | 10482.0 | 7251.6 | 178.9 | 6981.6 | 0.0 | 135.7 |
| P0 | 50000 | 1ch | B | 2 | 50000 | 196 | 7373.6 | 844.9 | 5524.3 | 5524.3 | 3130.1 | 10356.0 | 7225.9 | 177.7 | 6956.9 | 0.0 | 112.0 |
| P0 | 50000 | 1ch | C | 2 | 49588 | 195 | 7297.2 | 838.4 | 5491.6 | 5491.6 | 3036.9 | 10201.0 | 7164.1 | 179.4 | 6899.4 | 0.0 | 119.0 |
| P0 | 50000 | 1ch | D | 2 | 45780 | 180 | 6603.8 | 761.0 | 4505.3 | 4505.3 | 2616.7 | 8474.5 | 5857.8 | 154.5 | 5620.1 | 0.0 | 105.7 |
| P0 | 50000 | 4ch | A | 2 | 50000 | 200 | 7115.5 | 855.8 | 1628.2 | 1628.2 | 227.4 | 4607.0 | 4379.6 | 175.7 | 4065.4 | 0.0 | 93.1 |
| P0 | 50000 | 4ch | B | 2 | 50000 | 200 | 7115.5 | 855.8 | 1664.0 | 1664.0 | 272.2 | 4748.5 | 4476.3 | 177.5 | 4156.5 | 0.0 | 108.0 |
| P0 | 50000 | 4ch | C | 2 | 49584 | 204 | 7042.6 | 850.0 | 1640.2 | 1640.2 | 221.9 | 4675.0 | 4453.1 | 195.4 | 4115.2 | 0.0 | 100.5 |
| P0 | 50000 | 4ch | D | 2 | 45754 | 187 | 6384.0 | 774.5 | 1404.4 | 1404.4 | 219.2 | 4043.5 | 3824.3 | 159.6 | 3536.2 | 0.0 | 90.8 |
| P0 | 50000 | 4ch | E | 2 | 50000 | 204 | 7115.8 | 856.0 | 1603.0 | 1603.0 | 222.5 | 4576.0 | 4353.5 | 173.0 | 4041.1 | 0.0 | 97.2 |
| P0ctl | 1000 | 1ch | A | 2 | 1000 | 4 | 120.1 | 18.3 | 4.9 | 6.7 | 7.8 | 13.0 | 5.2 | 4.9 | 3.6 | 2.3 | 2.0 |
| P0ctl | 1000 | 1ch | B | 2 | 1000 | 4 | 120.1 | 18.3 | 4.8 | 6.6 | 7.3 | 12.0 | 4.7 | 4.4 | 3.7 | 2.1 | 1.9 |
| P0ctl | 10000 | 1ch | A | 2 | 10000 | 40 | 1407.3 | 171.4 | 265.3 | 265.3 | 124.1 | 499.5 | 375.4 | 37.8 | 329.4 | 57.4 | 29.9 |
| P0ctl | 10000 | 1ch | B | 2 | 10000 | 40 | 1407.3 | 171.4 | 265.4 | 265.4 | 175.1 | 549.0 | 373.9 | 34.6 | 329.5 | 57.4 | 26.1 |
| P1 | 1000 | 1ch | A | 2 | 1000 | 4 | 120.1 | 18.3 | 4.8 | 6.4 | 105.1 | 110.0 | 4.9 | 3.5 | 3.4 | 2.0 | 1.9 |
| P1 | 1000 | 1ch | B | 2 | 1000 | 4 | 120.1 | 18.3 | 4.0 | 5.7 | 57.5 | 63.5 | 6.0 | 4.6 | 4.8 | 2.5 | 2.6 |
| P1 | 10000 | 1ch | A | 2 | 10000 | 40 | 1407.3 | 171.4 | 266.6 | 266.6 | 221.3 | 603.0 | 381.7 | 36.0 | 334.4 | 56.2 | 35.2 |
| P1 | 10000 | 1ch | B | 2 | 10000 | 40 | 1407.3 | 171.4 | 271.8 | 271.8 | 219.9 | 573.0 | 353.1 | 34.1 | 306.9 | 64.2 | 18.4 |
| P1 | 10000 | 1ch | C | 2 | 9919 | 40 | 1393.1 | 170.5 | 272.8 | 272.8 | 168.8 | 534.5 | 365.7 | 55.2 | 297.6 | 55.7 | 17.1 |
| P1 | 10000 | 1ch | D | 2 | 9165 | 37 | 1262.2 | 155.1 | 233.7 | 233.7 | 217.3 | 541.0 | 323.7 | 42.4 | 258.6 | 48.1 | 18.6 |
| P1 | 10000 | 4ch | A | 2 | 10000 | 40 | 1207.6 | 178.2 | 71.5 | 71.5 | 9.6 | 175.0 | 165.4 | 32.4 | 109.4 | 26.8 | 7.3 |
| P1 | 10000 | 4ch | B | 2 | 10000 | 40 | 1207.6 | 178.2 | 86.1 | 86.1 | 58.9 | 226.0 | 167.1 | 31.1 | 113.2 | 24.5 | 14.0 |
| P1 | 50000 | 1ch | A | 2 | 50000 | 196 | 7373.6 | 844.6 | 5649.7 | 5649.7 | 3380.3 | 10633.0 | 7252.7 | 182.1 | 6982.0 | 0.0 | 113.3 |
| P1 | 50000 | 1ch | B | 2 | 50000 | 196 | 7373.6 | 844.6 | 5752.7 | 5752.7 | 3329.6 | 10530.0 | 7200.4 | 182.7 | 6928.7 | 0.0 | 116.9 |
| P2 | 1000 | 1ch | A | 2 | 1000 | 4 | 120.1 | 18.3 | 4.4 | 6.0 | 8.5 | 13.0 | 4.5 | 4.6 | 4.2 | 2.1 | 2.6 |
| P2 | 1000 | 1ch | B | 2 | 1000 | 4 | 120.1 | 18.3 | 3.9 | 8.1 | 7.7 | 13.0 | 5.3 | 3.7 | 3.8 | 2.3 | 2.1 |
| P2 | 10000 | 1ch | A | 3 | 10000 | 40 | 1407.3 | 171.4 | 271.6 | 271.6 | 219.7 | 571.0 | 364.7 | 34.3 | 315.6 | 56.9 | 18.7 |
| P2 | 10000 | 1ch | B | 2 | 10000 | 40 | 1407.3 | 171.4 | 268.0 | 268.0 | 218.3 | 574.0 | 355.7 | 34.6 | 308.2 | 57.6 | 19.7 |
| P2 | 10000 | 1ch | C | 4 | 9934 | 40 | 1392.2 | 170.2 | 261.3 | 261.3 | 219.1 | 569.0 | 354.9 | 33.2 | 310.1 | 50.7 | 31.0 |
| P2 | 10000 | 1ch | D | 3 | 9287 | 38 | 1280.7 | 157.3 | 231.0 | 231.0 | 217.1 | 499.0 | 281.9 | 30.0 | 242.4 | 44.1 | 15.3 |
| P2 | 10000 | 4ch | A | 2 | 10000 | 40 | 1207.6 | 178.2 | 83.6 | 83.6 | 107.1 | 299.5 | 192.4 | 30.6 | 102.2 | 41.4 | 6.8 |
| P2 | 10000 | 4ch | B | 3 | 10000 | 40 | 1207.6 | 178.2 | 71.5 | 71.5 | 9.7 | 263.0 | 254.6 | 32.9 | 109.9 | 47.8 | 7.0 |
| P2 | 50000 | 1ch | A | 2 | 50000 | 196 | 7373.6 | 844.9 | 5724.3 | 5724.3 | 3424.9 | 10867.5 | 7442.6 | 197.6 | 7147.1 | 0.0 | 122.5 |
| P2 | 50000 | 1ch | B | 5 | 50000 | 196 | 7373.6 | 844.9 | 5624.3 | 5624.3 | 3276.0 | 10594.0 | 7318.0 | 202.0 | 7003.3 | 0.0 | 129.9 |
| P3 | 1000 | 1ch | A | 14 | 1000 | 4 | 120.1 | 18.3 | 4.4 | 6.0 | 6.7 | 11.5 | 4.9 | 3.7 | 3.6 | 2.0 | 1.9 |
| P3 | 1000 | 1ch | B | 14 | 1000 | 4 | 120.1 | 18.3 | 4.2 | 5.8 | 6.7 | 11.0 | 4.6 | 3.7 | 3.6 | 2.1 | 1.9 |
| P3 | 10000 | 1ch | A | 14 | 10000 | 40 | 1407.3 | 171.4 | 275.0 | 275.0 | 119.9 | 819.0 | 686.3 | 36.1 | 321.9 | 182.1 | 20.2 |
| P3 | 10000 | 1ch | B | 14 | 10000 | 40 | 1407.3 | 171.4 | 262.9 | 262.9 | 121.3 | 812.0 | 651.7 | 35.4 | 310.6 | 184.8 | 21.6 |
| P3 | 10000 | 1ch | C | 14 | 10000 | 40 | 1407.3 | 171.4 | 268.0 | 268.0 | 120.5 | 815.0 | 661.3 | 34.2 | 310.0 | 182.1 | 29.9 |
| P3 | 10000 | 1ch | D | 14 | 10000 | 40 | 1407.3 | 171.4 | 263.5 | 263.5 | 119.1 | 821.0 | 674.1 | 35.4 | 314.3 | 192.7 | 26.0 |
| P3 | 10000 | 4ch | A | 14 | 10000 | 40 | 1207.6 | 178.2 | 73.4 | 73.4 | 8.6 | 728.0 | 719.2 | 32.7 | 105.5 | 109.7 | 7.2 |
| P3 | 10000 | 4ch | B | 14 | 10000 | 40 | 1207.6 | 178.2 | 72.8 | 72.8 | 8.4 | 727.0 | 718.6 | 32.3 | 104.2 | 105.6 | 8.0 |
| P3 | 50000 | 1ch | A | 14 | 50000 | 196 | 7373.6 | 844.7 | 5521.2 | 5521.2 | 3223.2 | 10477.5 | 7304.6 | 179.9 | 7034.7 | 0.0 | 141.5 |
| P3 | 50000 | 1ch | B | 14 | 50000 | 196 | 7373.6 | 844.7 | 5619.5 | 5619.5 | 3272.2 | 10852.0 | 7579.8 | 218.3 | 7233.3 | 0.0 | 118.7 |

### How it scales with the number of distinct stacks

First open, one channel, loopback:

| distinct stacks | compressed KB | packets | server main thread ms | client complete ms |
|---:|---:|---:|---:|---:|
| 1 000 | 18.3 | 4 | 5.1 | 11.5 |
| 10 000 | 171.4 | 40 | 287.7 | 560.5 |
| 50 000 | 844.9 | 196 | 5 535.9 | 10 482.0 |

Bytes and packets scale linearly with the stack count. Time does not. Ten times the stacks
costs 56 times the server time, and fifty times the stacks costs 1085 times the server time.
The same shape appears on the client.

Splitting the same contents over more channels makes it markedly cheaper, which points at the
per-channel index rather than the total as the thing that hurts. 50 000 stacks over four
channels of 12 500 carries the same bytes as 50 000 over one channel, but costs 1 628 ms of
server thread instead of 5 536 ms, and 4 607 ms on the client instead of 10 482 ms.

### The re-open is entirely redundant

Scenario B closes the terminal and re-opens it with nothing changed. At every size it sends
byte-for-byte what the first open sent: 18.3 KB at 1k, 171.4 KB at 10k, 844.9 KB at 50k, with
the same packet counts and the same server and client cost. A fresh server tab is built on
each open, its view starts empty, and the whole index is sent again as one ADDITION.

### Client complete time for scenario B, by profile

This is the single most decision-relevant view: a re-open with nothing changed, which is
exactly the case the feature would turn into a near-zero-byte handshake. Milliseconds,
one channel, median after warm-up.

| distinct stacks | P0 loopback | P0 via proxy | P1 25 ms / 20 Mbit | P2 75 ms / 5 Mbit | P3 150 ms / 2 Mbit |
|---:|---:|---:|---:|---:|---:|
| 1 000 | 16 | 12 | 64 | 13 | 11 |
| 10 000 | 612 | 549 | 573 | 574 | 812 |
| 50 000 | 10 356 | not run | 10 530 | 10 594 | 10 852 |

The striking result is how flat these rows are. Going from loopback to a 150 ms, 2 Mbit/s link
adds 200 ms at 10 000 stacks and 500 ms at 50 000. The link is not what the player is waiting
for. At 50 000 stacks the client spends around 7 seconds of the 10.5 applying the diff on the
render thread, and the transfer hides behind that.

The 64 ms in the P1 1 000-stack cell is noise from a two-sample median, not an effect of the
profile; the 1 000-stack case is 11 to 16 ms everywhere else.

### What a delta-on-reopen design would save

For scenarios C and D the change was applied while a terminal was open, so the server sent
exactly the delta. Those packets are what a delta design would have to send instead of the
full contents. Measured, not modelled:

| case | n | full open raw KB | full open compressed KB | delta raw KB | delta compressed KB | delta packets | compressed bytes saved |
|---|---:|---:|---:|---:|---:|---:|---:|
| P0_10000_1ch_C | 3 | 1402.3 | 171.1 | 16.77 | 3.57 | 4 | 97.9% |
| P0_10000_1ch_D | 3 | 1341.9 | 164.5 | 159.94 | 23.87 | 6 | 85.5% |
| P0_10000_4ch_C | 3 | 1204.5 | 178.3 | 16.82 | 5.33 | 16 | 97.0% |
| P0_10000_4ch_D | 3 | 1148.2 | 171.5 | 144.90 | 27.45 | 16 | 84.0% |
| P0_1000_1ch_C | 3 | 118.9 | 18.2 | 2.39 | 0.99 | 4 | 94.5% |
| P0_1000_1ch_D | 3 | 114.0 | 17.5 | 16.29 | 3.60 | 4 | 79.5% |
| P0_1000_4ch_C | 3 | 120.4 | 20.5 | 1.45 | 1.08 | 14 | 94.7% |
| P0_1000_4ch_D | 3 | 115.9 | 19.9 | 14.49 | 4.65 | 16 | 76.7% |
| P0_50000_1ch_C | 3 | 7344.5 | 842.0 | 91.93 | 14.37 | 4 | 98.3% |
| P0_50000_1ch_D | 3 | 6996.7 | 805.0 | 825.63 | 110.58 | 26 | 86.3% |
| P0_50000_4ch_C | 3 | 7088.3 | 854.0 | 84.60 | 17.41 | 16 | 98.0% |
| P0_50000_4ch_D | 3 | 6748.7 | 817.0 | 810.28 | 120.65 | 32 | 85.2% |
| P1_10000_1ch_C | 3 | 1400.7 | 170.9 | 18.80 | 3.98 | 4 | 97.7% |
| P1_10000_1ch_D | 3 | 1335.4 | 163.3 | 165.51 | 24.69 | 6 | 84.9% |
| P2_10000_1ch_C | 3 | 1400.3 | 170.8 | 18.74 | 3.97 | 4 | 97.7% |
| P2_10000_1ch_D | 3 | 1330.5 | 162.9 | 161.79 | 24.31 | 6 | 85.1% |
| P3_10000_1ch_C | 3 | 1400.2 | 170.9 | 18.66 | 3.93 | 4 | 97.7% |
| P3_10000_1ch_D | 3 | 1340.9 | 164.0 | 162.52 | 24.35 | 6 | 85.2% |

The projected time saved is bytes saved divided by the profile's bandwidth. It is a
first-order estimate of the transfer alone, and the measurements above show transfer is not
the binding constraint, so it understates what the feature would actually save:

| case | compressed bytes sent | bytes a delta design would send | saved | at P2 (5 Mbit/s) | at P3 (2 Mbit/s) |
|---|---:|---:|---:|---:|---:|
| B, 1000 stacks | 18.3 KB | 0 KB | 100% | 0.03 s | 0.07 s |
| B, 10000 stacks | 171.4 KB | 0 KB | 100% | 0.28 s | 0.70 s |
| B, 50000 stacks | 844.9 KB | 0 KB | 100% | 1.38 s | 3.46 s |
| 1000 stacks, scenario C | 18.2 KB | 0.99 KB | 94.5% | 0.03 s | 0.07 s |
| 1000 stacks, scenario D | 17.5 KB | 3.60 KB | 79.5% | 0.02 s | 0.06 s |
| 10000 stacks, scenario C | 171.1 KB | 3.57 KB | 97.9% | 0.27 s | 0.69 s |
| 10000 stacks, scenario D | 164.5 KB | 23.87 KB | 85.5% | 0.23 s | 0.58 s |
| 50000 stacks, scenario C | 842.0 KB | 14.37 KB | 98.3% | 1.36 s | 3.39 s |
| 50000 stacks, scenario D | 805.0 KB | 110.58 KB | 86.3% | 1.14 s | 2.84 s |

### Crafting options

With 296 vanilla crafting recipes registered on the network, at 10 000 stacks on one channel:

| | packets | raw KB | compressed KB | share of raw | share of compressed |
|---|---:|---:|---:|---:|---:|
| ingredient contents | 44 | 1 407.5 | 171.6 | 97.2% | 98.1% |
| crafting options | 3 | 40.3 | 3.4 | 2.8% | 1.9% |

Server main-thread time was 269.0 ms with the recipes against 287.7 ms without, so at this
recipe count crafting options cost nothing measurable in time. They work out at roughly 139
raw bytes per recipe, so a network with several thousand recipes would still be a minor share
next to a large ingredient index. Only 296 of the 300 requested were added: four vanilla
recipes could not be converted to a recipe definition and were skipped.

### Server outbound buffer

Across 451 recorded opens the outbound Netty channel went unwritable exactly once, on one
50 000-stack re-open under P3. That sample reported zero bytes buffered and zero bytes before
unwritable, which is what an inactive channel reports, so it reflects the connection closing
rather than the server filling a buffer. Every other open stayed writable, with the lowest
`bytesBeforeUnwritable` around 55 KB against the 64 KB high-water mark and at most about
10 KB ever queued.

The server is not holding megabytes per opening player, and the reason is the same one that
runs through the rest of these numbers: it cannot produce the data faster than even the P3
link drains it. 845 KB in 5.5 seconds is 154 KB/s, below the 250 KB/s cap.

## Observations that are not numbers

**The dedicated server cannot start.** On `master-26-lts`, `CommonProxy.registerPackets`
registers `CraftingJobFinishedToastPacket`, whose class references `net.minecraft.client.Minecraft`
and carries `@OnlyIn(Dist.CLIENT)` on `actionClient`. NeoForge no longer strips `@OnlyIn`
members at runtime and warns about this exact method at startup. Registering the packet
therefore loads client-only classes and the dedicated server dies during mod setup with
`NoClassDefFoundError: net/minecraft/client/gui/components/toasts/Toast`. Measuring at all
required skipping that one registration, which this branch does only when the debug flag is
set. This is not fixed here, and it is worth attention independently of #219.

**Stacks of one item that differ only in components are very expensive to index.** The first
attempt at 50 000 stacks put all 5 000 heavy-payload stacks on `minecraft:diamond_sword`,
varying only name, lore, enchantments and contents. Indexing took over 60 seconds in a single
tick and tripped the server watchdog, with the stack inside `IngredientPositionsIndex.addPosition`
going through `IngredientInstanceWrapper.equals` into full data-component comparison.
Spreading the same 5 000 heavy stacks across item types indexed the same 50 000 stacks in
under 8 seconds.

A controlled run isolates it. Same 10 000-stack network, same 10% change, same resulting
instance count and byte count, differing only in whether the added stacks share one item type:

| new stacks from the change | instances | raw KB | server main thread ms |
|---|---:|---:|---:|
| all on one item type | 9 233 | 1 264.7 | 1 503.4 |
| spread across item types | 9 236 | 1 278.2 | 273.8 |
| (plain open, no change) | 10 000 | 1 407.3 | 300.8 |

A 5.5x difference with the same data volume. This matters in practice: a storage full of
enchanted books or damaged tools is exactly this shape. It also had to be corrected in this
measurement, because the first matrix run added every new stack as `minecraft:stone` and so
reported scenario D at 10 000 stacks as 1 892 ms of server time; with varied items it is
225 ms, in line with the other scenarios.

**The network only re-indexes while a terminal is observing it.** With the terminal closed, a
storage change is not picked up: the index stayed at its old value across repeated
`scheduleObservation()` and `runObserverSync()` calls, and only moved once a terminal was
opened. Even then the change lands one observation cycle late. Scenarios C and D had to apply
their change with a terminal open for this reason. For a delta-on-reopen design this is a
direct complication: at the moment the client reconnects, the server's own index may not yet
reflect what the storage holds, so "what changed since your snapshot" is not simply readable
from the index.

**Packet ordering.** No DELETION packet was ever observed arriving before an ADDITION batch it
depended on, at any profile including P3. The reason is structural rather than lucky: on a
fresh open the server tab's view starts empty, so `reApplyFilter` produces additions only, and
deletions only appear on later change events once the initial burst has been applied. Higher
latency did not change this, because the ordering is imposed by a single TCP connection, and
the chunks of one open are submitted to the single-threaded `packetSerializer` in order.

**Thread usage.** With `packetSerializationEnableMultithreading` on, chunks past the first are
encoded on the tab's own single-thread executor. That executor is created per tab per open, so
one terminal open with three tabs creates three threads that live until the container closes.
On the client, deserialization runs off the render thread and the apply body runs on it; the
longest single render-thread stall observed was 531 ms, on a 50 000-stack warm-up open; the
medians stay under 150 ms even at 50 000 stacks, because the work arrives in 512-instance
chunks. The client does not freeze in one long block, it stutters through many short ones.

**Where the client time actually goes.** At 10 000 stacks on one channel: 36 ms deserializing,
342 ms applying the diff, 57 ms rebuilding the sorted and filtered view. The apply dominates,
and it is called twice per change event, because `onChange` recurses to apply the same
instances to the wildcard channel as well.

**A measurement limitation.** At 50 000 stacks the sorted-and-filtered rebuild is reported as
0 ms. The rebuild is lazy, happening on the next render after the view is invalidated, and at
that size it lands outside the window the open is measured over. The 10 000-stack figures do
capture it. Treat the 50 000-stack sort/filter column as unmeasured rather than free.

**The one disconnect, and what caused it.** A client did time out once, during the 50 000-stack
four-channel setup, with the server reporting ticks running up to 40 seconds behind. That
happened while the ingredient network was indexing the freshly built storage, before any
terminal was opened. It is an indexing cost, not an open cost, and it is partly an artifact of
this machine running client and server on four shared cores. No disconnect or timeout occurred
during any measured open, at any profile, including 50 000 stacks under P3.

## Recommendation

Against the thresholds set for this measurement, the answer splits, and the split is the
interesting part. On the network side the feature does not clear the bar: scenario B at 10 000
stacks completes in 574 ms under P2, comfortably inside the "under about 1 s, not worth the
complexity" line, and no disconnect occurred during any measured open, including 50 000 stacks
under P3. On the CPU side it does clear the bar: a 10 000-stack first open is 171 KB compressed,
which is inside the 200 KB line, but costs 288 ms of server main thread against a 100 ms line
and 560 ms of client completion against a 250 ms line. The reason the two halves disagree is
that bytes are not what this operation is made of. Going from loopback to a 150 ms, 2 Mbit/s
link costs 200 ms more at 10 000 stacks and 500 ms more at 50 000, while the open itself costs
560 ms and 10.5 s respectively; the transfer hides almost entirely behind the server serializing
and the client applying. A delta-on-reopen design as framed in #219 is therefore worth doing,
but not for the reason the issue gives: the saving is not the redundant bytes, it is the
redundant work at both ends, and the case rests on large networks. At 1 000 stacks the whole
open is 18 KB and 11 ms and nothing needs fixing; at 50 000 stacks a re-open with nothing
changed spends 5.5 s of server main thread and 10.5 s of client time reproducing byte-for-byte
what the client already had, and that is worth eliminating on its own. Two caveats belong with
that recommendation. First, client and server shared four cores on this machine, so both timings
are pessimistic, though the server main-thread figure is measured on the server thread itself.
Second, the design has a complication these runs surfaced: the network only re-indexes while a
terminal is observing it and applies changes one cycle late, so at the moment a client
reconnects the server's index may not yet reflect the storage, and "what changed since your
snapshot" is not simply readable from it. Before building the cache, the superlinear scaling is
worth a look on its own, since 50 000 stacks over four channels costs a third of what the same
50 000 cost over one, for identical bytes.
