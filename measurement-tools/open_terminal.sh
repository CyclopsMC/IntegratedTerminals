#!/bin/bash
# Aim at the storage terminal part and right-click it. Prints the resulting screen.
python3 "$(dirname "$0")/rcon.py" "tp ClientDevBridge 0.5 5 -2.5 0 20" > /dev/null
sleep 0.5
clientdevbridge look --at 0.5,5.5,0.4 > /dev/null
clientdevbridge use-item 2>&1 | head -1
