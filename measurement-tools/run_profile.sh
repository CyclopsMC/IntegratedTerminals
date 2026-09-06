#!/bin/bash
# Run the scenario matrix through the shaping proxy for one network profile.
# Usage: run_profile.sh <name> <one-way-delay-ms> <down-bytes-per-s> [measure.py args...]
set -e
NAME=$1; DELAY=$2; RATE=$3; shift 3
S=/tmp/claude-0/-home-user-IntegratedTerminals/2efbb763-3878-5cba-8ea3-c90bdfeacf0e/scratchpad
source $S/tools/env.sh
cd /home/user/IntegratedTerminals

pkill -f "netshape.py" 2>/dev/null || true
sleep 1
nohup python3 -u $S/tools/netshape.py --listen 25566 --target 127.0.0.1:25565 \
  --down-delay-ms "$DELAY" --down-rate-bps "$RATE" \
  --up-delay-ms "$DELAY" --up-rate-bps 0 \
  --stats-file "$S/proxy_${NAME}.json" > "$S/proxy_${NAME}.log" 2>&1 &
sleep 2
grep -q listening "$S/proxy_${NAME}.log" || { echo "proxy failed"; cat "$S/proxy_${NAME}.log"; exit 1; }

$S/tools/connect.sh 25566
python3 -u $S/tools/measure.py --profile "$NAME" --port 25566 "$@"
