#!/bin/bash
# Does the scenario D slowdown come from the change size, or from the added stacks all
# sharing one item type? Same 10k network, same 10% change, only the item variety differs.
set -e
S=/tmp/claude-0/-home-user-IntegratedTerminals/2efbb763-3878-5cba-8ea3-c90bdfeacf0e/scratchpad
source "$S/tools/env.sh"
cd /home/user/IntegratedTerminals

for MODE in varied same; do
  echo "=== new stacks: $MODE ==="
  python3 "$S/tools/rcon.py" "itmetrics setup 10000 1" > /dev/null
  if [ "$MODE" = "same" ]; then
    python3 "$S/tools/rcon.py" "itmetrics cluster true" | head -1
  else
    python3 "$S/tools/rcon.py" "itmetrics cluster false" | head -1
  fi
  # Let the initial index settle.
  for _ in $(seq 1 30); do
    sleep 2
    R=$(python3 "$S/tools/rcon.py" "itmetrics index" | head -1)
    [ "$R" = "index 0=10000" ] && break
  done
  echo "  after setup: $R"

  # Baseline open on the freshly built network.
  python3 -u "$S/tools/measure.py" --sizes 10000 --channels 1 --reps 2 \
    --scenarios A --settle 60 --profile "CL${MODE}base" --skip-setup > /dev/null

  # Three 10% changes, each applied with a terminal open so the index picks them up.
  python3 -u "$S/tools/measure.py" --sizes 10000 --channels 1 --reps 3 \
    --scenarios D --settle 60 --profile "CL${MODE}" --skip-setup > /dev/null
  echo "  after changes: $(python3 "$S/tools/rcon.py" "itmetrics index" | head -1)"
done
echo "cluster test done"
