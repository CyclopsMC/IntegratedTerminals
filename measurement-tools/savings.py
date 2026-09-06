#!/usr/bin/env python3
"""Bytes a delta-on-reopen design would avoid, and the transfer time that represents."""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import report

P2, P3 = 625000, 250000

server = report.group(report.load('server_opens.csv'))

def med_comp(profile, size, ch, sc):
    return report.med(server.get((profile, size, ch, sc), []), 'totalCompressedBytes')

# Scenario B sends everything again for no change at all, so the whole open is the saving.
print('| case | compressed bytes sent | bytes a delta design would send | saved | at P2 (5 Mbit/s) | at P3 (2 Mbit/s) |')
print('|---|---:|---:|---:|---:|---:|')
for size in (1000, 10000, 50000):
    full = med_comp('P0', size, '1ch', 'B')
    if full is None:
        continue
    print('| B, %d stacks | %.1f KB | 0 KB | 100%% | %.2f s | %.2f s |'
          % (size, full / 1024, full / P2, full / P3))

# C and D: the delta is what the server actually sent for the same change on a live terminal.
import csv, statistics
from collections import defaultdict
opens = {}
for r in report.load('server_opens.csv'):
    if r['tag'].startswith('delta_'):
        opens[(r['tag'], r['openId'])] = r
totals = defaultdict(lambda: [0, 0])
for r in report.load('server_packets.csv'):
    if r['tag'].startswith('delta_'):
        k = (r['tag'], r['openId'])
        totals[k][0] += int(r['compressedBytes'])
        totals[k][1] += 1
per = defaultdict(list)
for k, row in opens.items():
    comp, pkts = totals.get(k, [0, 0])
    d = comp - int(row['totalCompressedBytes'])
    if d < 0 or pkts - int(row['totalPackets']) <= 0:
        continue
    per['_'.join(k[0].split('_')[:-1])].append((int(row['totalCompressedBytes']), d))

for tag in ('delta_P0_1000_1ch_C', 'delta_P0_1000_1ch_D',
            'delta_P0_10000_1ch_C', 'delta_P0_10000_1ch_D',
            'delta_P0_50000_1ch_C', 'delta_P0_50000_1ch_D'):
    if tag not in per:
        continue
    vals = per[tag]
    full = statistics.median(v[0] for v in vals)
    delta = statistics.median(v[1] for v in vals)
    saved = full - delta
    label = tag.replace('delta_P0_', '').replace('_1ch_', ' stacks, scenario ')
    print('| %s | %.1f KB | %.2f KB | %.1f%% | %.2f s | %.2f s |'
          % (label, full / 1024, delta / 1024, saved / full * 100, saved / P2, saved / P3))
