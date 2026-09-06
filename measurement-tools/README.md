# Measurement tooling

Scratch scripts used to produce `MEASUREMENT.md`. Not part of the mod, not wired into the
build. They exist so the numbers can be reproduced or re-analysed.

Everything expects the mod's debug flag on both JVMs:
`-Dintegratedterminals.debugTerminalOpenMetrics=true`.

| file | what it does |
|---|---|
| `start_server.sh` | starts the dedicated server and waits for it to accept RCON |
| `rcon.py` | minimal RCON client, so scenarios can be driven from the shell |
| `connect.sh` | points the running dev client at a port and waits until it is in the world |
| `open_terminal.sh` | aims at the terminal part and right-clicks it |
| `measure.py` | runs the scenario matrix and writes the raw rows |
| `netshape.py` | userspace TCP proxy adding per-direction delay and a bandwidth cap |
| `proxytest.py` | checks the proxy actually shapes, against a sink server |
| `run_profile.sh` | runs one network profile through the proxy |
| `run_profiles.sh` | runs the P0 control and P1 to P3 in sequence |
| `cluster_test.sh` | isolates the item-clustering effect on index cost |
| `report.py` | emits the report tables from the raw rows |
| `savings.py` | computes what a delta-on-reopen design would avoid |

The `*.log` files are the console output of the runs the report is built from.
Raw rows are in `../measurements/`.

`tc` was unavailable, so `netshape.py` was used instead of netem. See MEASUREMENT.md for
the control comparison and the caveats that come with a userspace relay.

## Running against a locally built CyclopsCore or IntegratedDynamics

Both IntegratedTerminals and IntegratedDynamics already support this, so no Gradle edits are
needed. Their `build.gradle` reads `secrets.properties` and prefers a local version when one
is named there.

1. Clone CyclopsCore and initialise its submodule. The CommonCapabilities API is a git
   submodule under `loader-neoforge/src/api/java/org/cyclops/commoncapabilities/api`, and
   without it the build fails with roughly 950 errors about a missing
   `org.cyclops.commoncapabilities.api.ingredient` package:

       git clone https://github.com/CyclopsMC/CyclopsCore /home/user/cyclopscore
       cd /home/user/cyclopscore && git submodule update --init --depth 1

2. Publish it to the local Maven repository. The published version is
   `<mod_version>-<build_number>`, and `build_number` defaults to `DEV`:

       ./gradlew :loader-neoforge:publishToMavenLocal

   To keep a patched and an unpatched build side by side, temporarily suffix `mod_version`
   in `gradle.properties` before publishing, for example `1.30.4-hashfix`, which yields
   `1.30.4-hashfix-DEV`. Revert the file afterwards so the change stays out of any commit.

3. Point IntegratedTerminals at it by creating `secrets.properties` in the repository root
   (it is gitignored):

       cyclopscore_version_local=1.30.4-DEV

   IntegratedDynamics takes the same key in its own `secrets.properties`. Flipping the value
   between two published versions and restarting is enough to A/B the two, since
   `mavenLocal()` is first in the repository list.

## Profiling the server

`tools/env_jfr.sh` is `env.sh` plus a JFR recording on every JVM it starts, and
`tools/start_server_jfr.sh` starts the dedicated server with it. Dump and read a recording
with:

    jcmd <server pid> JFR.dump name=open filename=server.jfr
    jfr print --events jdk.ExecutionSample --stack-depth 40 server.jfr

Filter samples by `sampledThread = "Server thread"`. Frames containing `HashMap$TreeNode`
indicate buckets that have treeified, which is the signature of pathological hash collisions.

## Reproducing the collision shapes

`itmetrics cluster true` makes both the generated heavy stacks and the stacks added by a
change share a single item type, which is the shape that collides. `tools/pathological.sh`
runs the two cases that regressed worst.
