#!/usr/bin/env python3
"""Emit the MEASUREMENT.md tables from the raw metric rows, so every number comes from a run."""
import csv
import os
import statistics
import sys
from collections import defaultdict

METRICS = '/home/user/IntegratedTerminals/run/metrics'


def load(name):
    path = os.path.join(METRICS, name)
    if not os.path.exists(path):
        return []
    with open(path) as f:
        return list(csv.DictReader(f))


def parse_tag(tag):
    """profile_size_channels[suffix]_scenario_rep"""
    if tag.startswith('delta_') or tag in ('flush', 'untagged'):
        return None
    parts = tag.split('_')
    if len(parts) < 5:
        return None
    try:
        return (parts[0], int(parts[1]), '_'.join(parts[2:-2]), parts[-2],
                int(parts[-1].lstrip('r')))
    except ValueError:
        return None


def is_orphaned(row):
    """
    True for a client row whose packets arrived with no terminal container to apply them to.

    That happens when the harness gave up on an open and closed the screen while the burst
    was still in flight: the packets are deserialized and the apply body runs, but it does
    nothing, so the recorded apply time is essentially zero. Such a row measures nothing.
    """
    try:
        return int(row['packets']) > 4 and float(row['applyMsTotal']) < 0.5
    except (KeyError, ValueError):
        return False


def group(rows, drop_warmup=True):
    out = defaultdict(list)
    for r in rows:
        parsed = parse_tag(r['tag'])
        if not parsed:
            continue
        profile, size, channels, scenario, rep = parsed
        if drop_warmup and rep == 1:
            continue
        if is_orphaned(r):
            continue
        out[(profile, size, channels, scenario)].append(r)
    return out


def med(rows, key, scale=1.0):
    vals = []
    for r in rows:
        try:
            vals.append(float(r[key]))
        except (ValueError, KeyError, TypeError):
            pass
    return statistics.median(vals) / scale if vals else None


def fmt(value, digits=1):
    return '-' if value is None else ('%.*f' % (digits, value))


def main_table(profiles=None):
    server = group(load('server_opens.csv'))
    client = group(load('client_opens.csv'))
    keys = sorted(set(server) | set(client), key=lambda k: (k[0], k[1], k[2], k[3]))
    if profiles:
        keys = [k for k in keys if k[0] in profiles]

    print('| profile | stacks | channels | scenario | n | instances sent | packets | raw KB '
          '| compressed KB | server main ms | server total ms | client first ms '
          '| client complete ms | fill gap ms | deserialize ms | apply ms | sort/filter ms '
          '| longest stall ms |')
    print('|---|---:|---:|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|')
    for k in keys:
        s, c = server.get(k, []), client.get(k, [])
        print('| %s | %d | %s | %s | %d | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s |' % (
            k[0], k[1], k[2], k[3], max(len(s), len(c)),
            fmt(med(s, 'ingredientInstances'), 0),
            fmt(med(s, 'totalPackets'), 0), fmt(med(s, 'totalRawBytes', 1024)),
            fmt(med(s, 'totalCompressedBytes', 1024)), fmt(med(s, 'mainThreadMs')),
            fmt(med(s, 'totalMs')), fmt(med(c, 'firstContentMs')), fmt(med(c, 'completeMs')),
            fmt(med(c, 'fillGapMs')), fmt(med(c, 'deserializeMsTotal')),
            fmt(med(c, 'applyMsTotal')), fmt(med(c, 'viewRebuildMsTotal')),
            fmt(med(c, 'longestStallMs'))))


def scenario_b_table():
    """Client complete time for scenario B across profiles: the decision-relevant view."""
    client = group(load('client_opens.csv'))
    profiles, sizes = set(), set()
    for (profile, size, channels, scenario) in client:
        if scenario == 'B' and channels == '1ch':
            profiles.add(profile)
            sizes.add(size)
    profiles = sorted(profiles)
    print('| stacks | ' + ' | '.join(profiles) + ' |')
    print('|---:|' + '---:|' * len(profiles))
    for size in sorted(sizes):
        cells = []
        for profile in profiles:
            cells.append(fmt(med(client.get((profile, size, '1ch', 'B'), []), 'completeMs'), 0))
        print('| %d | %s |' % (size, ' | '.join(cells)))


def backpressure_table():
    server = group(load('server_opens.csv'))
    print('| profile | stacks | channels | scenario | unwritable transitions '
          '| min bytesBeforeUnwritable | max outbound buffered |')
    print('|---|---:|---:|---|---:|---:|---:|')
    for k in sorted(server, key=lambda k: (k[0], k[1], k[2], k[3])):
        rows = server[k]
        print('| %s | %d | %s | %s | %s | %s | %s |' % (
            k[0], k[1], k[2], k[3],
            fmt(med(rows, 'unwritableTransitions'), 0),
            fmt(med(rows, 'minBytesBeforeUnwritable'), 0),
            fmt(med(rows, 'maxOutboundBuffered'), 0)))


def delta_table():
    """What a delta-on-reopen design would have sent, measured from live change packets."""
    opens = {}
    for r in load('server_opens.csv'):
        if r['tag'].startswith('delta_'):
            opens[(r['tag'], r['openId'])] = r
    totals = defaultdict(lambda: [0, 0, 0])
    for r in load('server_packets.csv'):
        if r['tag'].startswith('delta_'):
            key = (r['tag'], r['openId'])
            totals[key][0] += int(r['rawBytes'])
            totals[key][1] += int(r['compressedBytes'])
            totals[key][2] += 1
    per_tag = defaultdict(list)
    for key, row in opens.items():
        allraw, allcomp, allpkts = totals.get(key, [0, 0, 0])
        delta_raw = allraw - int(row['totalRawBytes'])
        delta_comp = allcomp - int(row['totalCompressedBytes'])
        delta_pkts = allpkts - int(row['totalPackets'])
        # No delta packets means the change never reached the client on this open,
        # which happens when the open itself failed. Such a row says nothing.
        if delta_raw < 0 or delta_pkts <= 0:
            continue
        per_tag['_'.join(key[0].split('_')[:-1])].append(
            (int(row['totalRawBytes']), int(row['totalCompressedBytes']),
             delta_raw, delta_comp, delta_pkts))
    print('| case | n | full open raw KB | full open compressed KB | delta raw KB '
          '| delta compressed KB | delta packets | compressed bytes saved |')
    print('|---|---:|---:|---:|---:|---:|---:|---:|')
    for tag in sorted(per_tag):
        vals = per_tag[tag]
        full_c = statistics.median(v[1] for v in vals)
        delta_c = statistics.median(v[3] for v in vals)
        saved = (1 - delta_c / full_c) * 100 if full_c else float('nan')
        print('| %s | %d | %.1f | %.1f | %.2f | %.2f | %.0f | %.1f%% |' % (
            tag.replace('delta_', ''), len(vals),
            statistics.median(v[0] for v in vals) / 1024, full_c / 1024,
            statistics.median(v[2] for v in vals) / 1024, delta_c / 1024,
            statistics.median(v[4] for v in vals), saved))


if __name__ == '__main__':
    what = sys.argv[1] if len(sys.argv) > 1 else 'main'
    {'main': main_table, 'b': scenario_b_table,
     'backpressure': backpressure_table, 'delta': delta_table}[what]()
