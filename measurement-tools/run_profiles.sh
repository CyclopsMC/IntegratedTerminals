#!/bin/bash
# Run the network profiles in sequence, through the userspace shaping proxy.
#
# Profiles (one-way delay, server-to-client bandwidth):
#   P0ctl  0 ms   unlimited      control, must match the direct loopback run
#   P1    25 ms   20 Mbit/s      same-continent public server, good connection
#   P2    75 ms    5 Mbit/s      cross-continent server, ordinary home connection
#   P3   150 ms    2 Mbit/s      poor connection, or a busy server sharing upload
set -e
S=/tmp/claude-0/-home-user-IntegratedTerminals/2efbb763-3878-5cba-8ea3-c90bdfeacf0e/scratchpad
LOG=$S/profiles_run.log
: > "$LOG"

run() {
  local name=$1 delay=$2 rate=$3
  shift 3
  echo "===== $name (delay ${delay}ms, ${rate} B/s) =====" | tee -a "$LOG"
  "$S/tools/run_profile.sh" "$name" "$delay" "$rate" "$@" 2>&1 | tee -a "$LOG"
}

# Control: same matrix shape as the direct run, to show the proxy adds nothing.
run P0ctl 0 0 --sizes 1000,10000 --channels 1 --reps 3 --scenarios A,B --settle 60

run P1 25 2500000 --sizes 1000,10000,50000 --channels 1 --reps 3 --scenarios A,B --settle 90
run P1 25 2500000 --sizes 10000 --channels 4 --reps 3 --scenarios A,B --settle 90
run P1 25 2500000 --sizes 10000 --channels 1 --reps 3 --scenarios C,D --settle 90

run P2 75 625000 --sizes 1000,10000,50000 --channels 1 --reps 3 --scenarios A,B --settle 120
run P2 75 625000 --sizes 10000 --channels 4 --reps 3 --scenarios A,B --settle 120
run P2 75 625000 --sizes 10000 --channels 1 --reps 3 --scenarios C,D --settle 120

run P3 150 250000 --sizes 1000,10000,50000 --channels 1 --reps 3 --scenarios A,B --settle 180
run P3 150 250000 --sizes 10000 --channels 4 --reps 3 --scenarios A,B --settle 180
run P3 150 250000 --sizes 10000 --channels 1 --reps 3 --scenarios C,D --settle 180

echo "all profiles done" | tee -a "$LOG"
