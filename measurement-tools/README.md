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
