#!/usr/bin/env python3
"""Median each benchmark across the runs of each CyclopsCore build."""
import re, sys, glob, statistics, collections
runs = collections.defaultdict(lambda: collections.defaultdict(list))
for path in sorted(glob.glob(sys.argv[1] + '/bench_*.log')):
    m = re.search(r'bench_(1\.30\.4[^/]*?)(?:_r\d+)?\.log$', path)
    if not m:
        m = re.search(r'bench_(plainfix)\.log$', path)
        if not m:
            continue
    build = m.group(1).replace('-DEV', '').replace('1.30.4', 'baseline').replace('baseline-', '')
    for name, value in re.findall(r'(index_[a-z_]+): ([0-9.]+)', open(path, errors='replace').read()):
        runs[name][build].append(float(value))
order = ['baseline', 'hashfix', 'classfix', 'plainfix']
names = list(runs.keys())
print(f"{'benchmark':38s} " + ' '.join(f"{b:>12s}" for b in order) + "   n")
for name in names:
    cells = []
    ns = []
    for b in order:
        vals = runs[name].get(b, [])
        ns.append(len(vals))
        cells.append(f"{statistics.median(vals):12.6f}" if vals else f"{'-':>12s}")
    print(f"{name:38s} " + ' '.join(cells) + "   " + ','.join(map(str, ns)))
