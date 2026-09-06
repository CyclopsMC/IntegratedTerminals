#!/usr/bin/env python3
"""Rank the frames appearing in jdk.ExecutionSample stacks of one thread."""
import sys, re, collections
path, thread = sys.argv[1], sys.argv[2]
needle = sys.argv[3] if len(sys.argv) > 3 else None
total = 0
frame_counts = collections.Counter()
leaf_counts = collections.Counter()
cur_thread = None
stack = None
for line in open(path, errors='replace'):
    s = line.strip()
    if s.startswith('jdk.ExecutionSample'):
        cur_thread = None; stack = None
    elif s.startswith('sampledThread ='):
        m = re.search(r'"([^"]*)"', s)
        cur_thread = m.group(1) if m else None
    elif s.startswith('stackTrace = ['):
        stack = []
    elif s == ']' and stack is not None:
        if cur_thread == thread and stack:
            if needle is None or any(needle in f for f in stack):
                total += 1
                for f in set(stack):
                    frame_counts[f] += 1
                leaf_counts[stack[0]] += 1
        stack = None
    elif stack is not None and s:
        stack.append(s.split('(')[0].split(' line:')[0])
print(f"thread={thread} samples={total}" + (f" filtered by '{needle}'" if needle else ""))
print("--- frames anywhere on the stack ---")
for f, c in frame_counts.most_common(28):
    print(f"{100.0*c/total:6.1f}%  {f}")
print("--- leaf frames ---")
for f, c in leaf_counts.most_common(14):
    print(f"{100.0*c/total:6.1f}%  {f}")
