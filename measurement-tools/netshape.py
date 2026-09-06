#!/usr/bin/env python3
"""
A userspace TCP proxy that adds one-way delay and a bandwidth cap per direction.

Used to measure how a storage terminal open behaves on a real connection rather than loopback.
Minecraft is a single plain TCP connection, so relaying bytes is transparent to the game.

Usage:
  netshape.py --listen 25566 --target 127.0.0.1:25565 \
      --down-delay-ms 75 --down-rate-bps 625000 --up-delay-ms 75 --up-rate-bps 625000

"down" is server to client, "up" is client to server. A rate of 0 means unlimited.
Byte counters are printed on exit, and to --stats-file if given.
"""
import argparse
import asyncio
import json
import signal
import sys
import time

CHUNK = 16384


class TokenBucket:
    """
    Byte-rate limiter with a small burst allowance (50 ms of traffic, at least one chunk).

    A full second of burst would let the first ~1 s of a terminal open through unshaped,
    which is exactly the part we are trying to measure.
    """

    def __init__(self, rate_bps):
        self.rate = rate_bps
        self.capacity = max(CHUNK, rate_bps * 0.05) if rate_bps > 0 else 0
        self.tokens = float(self.capacity)
        self.updated = time.monotonic()

    async def consume(self, amount):
        if self.rate <= 0:
            return
        while True:
            now = time.monotonic()
            self.tokens = min(float(self.capacity), self.tokens + (now - self.updated) * self.rate)
            self.updated = now
            if self.tokens >= amount:
                self.tokens -= amount
                return
            await asyncio.sleep((amount - self.tokens) / self.rate)


class Direction:
    def __init__(self, name, delay_ms, rate_bps):
        self.name = name
        self.delay = delay_ms / 1000.0
        self.bucket = TokenBucket(rate_bps)
        self.bytes = 0


async def relay(reader, writer, direction):
    """Shape then delay, preserving order: a queue holds each chunk until its send time."""
    queue = asyncio.Queue()

    async def pump():
        while True:
            item = await queue.get()
            if item is None:
                break
            send_at, data = item
            wait = send_at - time.monotonic()
            if wait > 0:
                await asyncio.sleep(wait)
            try:
                writer.write(data)
                await writer.drain()
            except (ConnectionResetError, BrokenPipeError):
                break

    pump_task = asyncio.create_task(pump())
    try:
        while True:
            data = await reader.read(CHUNK)
            if not data:
                break
            direction.bytes += len(data)
            # Bandwidth first, then propagation delay.
            await direction.bucket.consume(len(data))
            await queue.put((time.monotonic() + direction.delay, data))
    except (ConnectionResetError, BrokenPipeError):
        pass
    finally:
        await queue.put(None)
        await pump_task
        try:
            writer.close()
        except Exception:
            pass


async def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--listen', type=int, required=True)
    parser.add_argument('--target', required=True)
    parser.add_argument('--down-delay-ms', type=float, default=0)
    parser.add_argument('--down-rate-bps', type=int, default=0)
    parser.add_argument('--up-delay-ms', type=float, default=0)
    parser.add_argument('--up-rate-bps', type=int, default=0)
    parser.add_argument('--stats-file')
    args = parser.parse_args()

    host, port = args.target.rsplit(':', 1)
    totals = {'down_bytes': 0, 'up_bytes': 0, 'connections': 0}

    async def handle(client_reader, client_writer):
        totals['connections'] += 1
        try:
            server_reader, server_writer = await asyncio.open_connection(host, int(port))
        except OSError as e:
            print(f'connect to target failed: {e}', file=sys.stderr, flush=True)
            client_writer.close()
            return
        down = Direction('down', args.down_delay_ms, args.down_rate_bps)
        up = Direction('up', args.up_delay_ms, args.up_rate_bps)
        print(f'connection {totals["connections"]} established', flush=True)
        await asyncio.gather(
            relay(server_reader, client_writer, down),
            relay(client_reader, server_writer, up),
        )
        totals['down_bytes'] += down.bytes
        totals['up_bytes'] += up.bytes
        print(f'connection closed: down={down.bytes} up={up.bytes}', flush=True)

    server = await asyncio.start_server(handle, '127.0.0.1', args.listen)
    print(f'listening on 127.0.0.1:{args.listen} -> {args.target} '
          f'(down {args.down_delay_ms}ms/{args.down_rate_bps}Bps, '
          f'up {args.up_delay_ms}ms/{args.up_rate_bps}Bps)', flush=True)

    stop = asyncio.Event()

    def shutdown():
        stop.set()

    loop = asyncio.get_running_loop()
    for sig in (signal.SIGINT, signal.SIGTERM):
        loop.add_signal_handler(sig, shutdown)

    async with server:
        await stop.wait()

    if args.stats_file:
        with open(args.stats_file, 'w') as f:
            json.dump(totals, f)
    print(f'totals: {totals}', flush=True)


if __name__ == '__main__':
    asyncio.run(main())
