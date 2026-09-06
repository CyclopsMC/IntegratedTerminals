#!/usr/bin/env python3
"""
Drives the storage terminal open scenarios against the dedicated server and the dev client.

Scenarios:
  A first open
  B close and re-open with no storage changes
  C close, change 1% of stacks, re-open
  D close, change 10% of stacks, re-open
  E four channels, open while viewing only channel 0

For C and D the change is applied while a terminal is open, under a 'delta_' tag. That is
required because the network only re-indexes while something observes it, and it also
records what a delta-on-reopen design would have had to send.
"""
import argparse
import csv
import os
import subprocess
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import rcon

PROJECT = '/home/user/IntegratedTerminals'
METRICS = os.path.join(PROJECT, 'run', 'metrics')
CLIENT_OPENS = os.path.join(METRICS, 'client_opens.csv')
SERVER_OPENS = os.path.join(METRICS, 'server_opens.csv')
TERMINAL_SCREEN = 'ContainerScreenTerminalStorage'


def cdb(*args, timeout=240):
    try:
        result = subprocess.run(['clientdevbridge'] + list(args), cwd=PROJECT,
                                capture_output=True, text=True, timeout=timeout)
        return (result.stdout + result.stderr).strip()
    except subprocess.TimeoutExpired:
        return 'TIMEOUT'


def set_tag(tag):
    rcon.run(['itmetrics tag ' + tag])
    cdb('eval', 'System.setProperty("integratedterminals.debugTerminalOpenMetricsTag", "'
        + tag + '"); "' + tag + '"')


def rows(path):
    if not os.path.exists(path):
        return []
    with open(path) as f:
        return list(csv.DictReader(f))


def close_terminal():
    cdb('close-screen')
    time.sleep(0.8)


SERVER_PORT = 25565


def reconnect():
    """A large open can stall the server enough for the client to time out; rejoin if so."""
    print('    client is not in a world, reconnecting', flush=True)
    subprocess.run([os.path.join(os.path.dirname(os.path.abspath(__file__)), 'connect.sh'),
                    str(SERVER_PORT)], cwd=PROJECT, capture_output=True, text=True, timeout=300)
    time.sleep(3)


def open_terminal(attempts=3):
    """Right-click the terminal part, making sure no screen is in the way first."""
    out = ''
    for _ in range(attempts):
        if 'in world    false' in cdb('status'):
            reconnect()
        close_terminal()
        rcon.run(['tp ClientDevBridge 0.5 5 -2.5 0 20'])
        time.sleep(0.5)
        cdb('look', '--at', '0.5,5.5,0.4')
        out = cdb('use-item')
        if TERMINAL_SCREEN in out:
            return True
        # On a high-latency link the screen has not arrived by the time use-item returns,
        # so give the open packet time to make the round trip before calling it a failure.
        deadline = time.time() + 30
        while time.time() < deadline:
            time.sleep(1)
            if TERMINAL_SCREEN in cdb('status'):
                return True
        time.sleep(1.5)
    print('    open failed after %d attempts: %s' % (attempts, out), flush=True)
    return False


def wait_for_rows(path, previous, timeout):
    deadline = time.time() + timeout
    while time.time() < deadline:
        if len(rows(path)) > previous:
            time.sleep(1.0)
            return True
        time.sleep(0.3)
    return False


def index_sizes():
    return rcon.run(['itmetrics index'])[0].strip()


def do_open(tag, settle, select_tab=True, channel=None, attempts=2):
    """Perform one measured open. Retries once if no client row is produced."""
    ok_client, ok_server = False, False
    for attempt in range(attempts):
        set_tag(tag)
        before_client = len(rows(CLIENT_OPENS))
        before_server = len(rows(SERVER_OPENS))
        if not open_terminal():
            continue
        if select_tab:
            time.sleep(0.3)
            cdb('click', '--at', '139,13')
        if channel is not None:
            cdb('eval', 'mc.player.containerMenu.setSelectedChannel(%d); "ok"' % channel)
        ok_client = wait_for_rows(CLIENT_OPENS, before_client, settle)
        ok_server = wait_for_rows(SERVER_OPENS, before_server, 15)
        time.sleep(1.0)
        close_terminal()
        if ok_client:
            return ok_client, ok_server
        print('    retrying %s (attempt %d produced no client row)' % (tag, attempt + 1), flush=True)
    return ok_client, ok_server


def apply_change(fraction, tag, settle):
    """
    Apply a storage change while a terminal is open.

    The network only re-indexes while it is observed, so the change would otherwise not be
    visible on the next open. The packets sent after the initial burst of this open are the
    delta, and are recorded under the same tag.
    """
    before_index = index_sizes()
    set_tag(tag)
    before_client = len(rows(CLIENT_OPENS))
    if not open_terminal():
        return False
    cdb('click', '--at', '139,13')
    wait_for_rows(CLIENT_OPENS, before_client, settle)
    time.sleep(1.0)
    rcon.run(['itmetrics change ' + str(fraction)])
    deadline = time.time() + 60
    changed = False
    while time.time() < deadline:
        time.sleep(1)
        if index_sizes() != before_index:
            changed = True
            break
    time.sleep(2)
    close_terminal()
    time.sleep(1)
    if not changed:
        print('    note: index size did not move after the change', flush=True)
    return changed


def setup(size, channels):
    print(rcon.run(['itmetrics setup %d %d' % (size, channels)])[0].strip(), flush=True)
    time.sleep(4)
    previous = ''
    for _ in range(150):
        now = index_sizes()
        if now == previous and '=0' not in now:
            break
        previous = now
        time.sleep(2)
    print('   ', previous, flush=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--sizes', default='1000,10000,50000')
    parser.add_argument('--channels', default='1,4')
    parser.add_argument('--reps', type=int, default=3)
    parser.add_argument('--profile', default='P0')
    parser.add_argument('--scenarios', default='A,B,C,D,E')
    parser.add_argument('--settle', type=float, default=40)
    parser.add_argument('--crafting', type=int, default=0)
    parser.add_argument('--suffix', default='')
    parser.add_argument('--skip-setup', action='store_true',
                        help='the network is already built; do not rebuild and re-index it')
    parser.add_argument('--port', type=int, default=25565,
                        help='port to rejoin on, the proxy port when running a shaped profile')
    args = parser.parse_args()

    global SERVER_PORT
    SERVER_PORT = args.port

    sizes = [int(x) for x in args.sizes.split(',')]
    channel_counts = [int(x) for x in args.channels.split(',')]
    scenarios = args.scenarios.split(',')

    for size in sizes:
        for channels in channel_counts:
            if not args.skip_setup:
                setup(size, channels)
            if args.crafting:
                print('   ', rcon.run(['itmetrics crafting %d' % args.crafting])[0].strip(), flush=True)
            for scenario in scenarios:
                if scenario == 'E' and channels != 4:
                    continue
                for rep in range(1, args.reps + 1):
                    base = '%s_%d_%dch%s_%s_r%d' % (
                        args.profile, size, channels, args.suffix, scenario, rep)
                    print('--- ' + base, flush=True)
                    if scenario in ('C', 'D'):
                        fraction = 0.01 if scenario == 'C' else 0.10
                        apply_change(fraction, 'delta_' + base, args.settle)
                    ok = do_open(base, args.settle, channel=0 if scenario == 'E' else None)
                    print('    client_row=%s server_row=%s' % ok, flush=True)
    print('done', flush=True)


if __name__ == '__main__':
    main()
