#!/bin/bash
# Start the dedicated server with JFR enabled and wait for RCON.
cd /home/user/IntegratedTerminals
S=/tmp/claude-0/-home-user-IntegratedTerminals/2efbb763-3878-5cba-8ea3-c90bdfeacf0e/scratchpad
source $S/tools/env_jfr.sh
LOG=$S/server.log
: > "$LOG"
nohup ./gradlew runServer > "$LOG" 2>&1 &
for i in $(seq 1 150); do
  sleep 2
  grep -q 'For help, type' "$LOG" && { echo "server ready (JFR)"; exit 0; }
  grep -q 'BUILD FAILED' "$LOG" && { echo "server failed"; tail -20 "$LOG"; exit 1; }
done
echo "server timeout"; exit 1
