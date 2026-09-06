#!/bin/bash
# Start the dedicated server and wait until it accepts RCON.
cd /home/user/IntegratedTerminals
source /tmp/claude-0/-home-user-IntegratedTerminals/2efbb763-3878-5cba-8ea3-c90bdfeacf0e/scratchpad/tools/env.sh
LOG=/tmp/claude-0/-home-user-IntegratedTerminals/2efbb763-3878-5cba-8ea3-c90bdfeacf0e/scratchpad/server.log
: > "$LOG"
nohup ./gradlew runServer > "$LOG" 2>&1 &
for i in $(seq 1 120); do
  sleep 2
  grep -q 'For help, type' "$LOG" && { echo "server ready"; exit 0; }
  grep -q 'BUILD FAILED' "$LOG" && { echo "server failed"; tail -20 "$LOG"; exit 1; }
done
echo "server timeout"; exit 1
