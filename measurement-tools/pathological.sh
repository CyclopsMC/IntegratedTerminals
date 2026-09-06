#!/bin/bash
# The two shapes that regressed worst before the hash fix:
#   1. all heavy stacks on one item, distinct only by components (the diamond sword case)
#   2. a 10% change whose new stacks all share one item type
# Pass a label (BEFORE or AFTER) so the rows are distinguishable.
set -e
LABEL=${1:-AFTER}
S=/tmp/claude-0/-home-user-IntegratedTerminals/2efbb763-3878-5cba-8ea3-c90bdfeacf0e/scratchpad
source "$S/tools/env.sh"
cd /home/user/IntegratedTerminals

echo "=== ${LABEL}: 50k with all heavy stacks on one item ==="
python3 "$S/tools/rcon.py" "itmetrics cluster true" | head -1
python3 "$S/tools/rcon.py" "itmetrics setup 50000 1" | head -2
for _ in $(seq 1 40); do
  sleep 5
  R=$(python3 "$S/tools/rcon.py" "itmetrics index" | head -1)
  [ "$R" = "index 0=50000" ] && break
done
echo "  index after setup: $R"
python3 -u "$S/tools/measure.py" --sizes 50000 --channels 1 --reps 3 \
  --scenarios A --settle 180 --skip-setup --profile "${LABEL}SWORD" 2>&1 | grep -E "client_row|open failed"

echo "=== ${LABEL}: 10k, 10% change adding stacks of one item type ==="
python3 "$S/tools/rcon.py" "itmetrics setup 10000 1" | head -2
for _ in $(seq 1 40); do
  sleep 3
  R=$(python3 "$S/tools/rcon.py" "itmetrics index" | head -1)
  [ "$R" = "index 0=10000" ] && break
done
python3 -u "$S/tools/measure.py" --sizes 10000 --channels 1 --reps 3 \
  --scenarios D --settle 120 --skip-setup --profile "${LABEL}CLUST" 2>&1 | grep -E "client_row|open failed"

python3 "$S/tools/rcon.py" "itmetrics cluster false" | head -1
echo "pathological ${LABEL} done"
