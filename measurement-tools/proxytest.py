#!/usr/bin/env python3
"""Check the shaping proxy: time to first byte and total transfer time through it."""
import asyncio, subprocess, sys, time

TARGET = 2_000_000

async def main():
    done = asyncio.Event()
    stats = {}

    async def sink(reader, writer):
        total = 0
        while total < TARGET:
            d = await reader.read(65536)
            if not d:
                break
            if total == 0:
                stats['first'] = time.monotonic()
            total += len(d)
        stats['last'] = time.monotonic()
        stats['total'] = total
        done.set()

    server = await asyncio.start_server(sink, '127.0.0.1', 29001)
    proc = subprocess.Popen([sys.executable, '-u', sys.argv[1],
                             '--listen', '29002', '--target', '127.0.0.1:29001',
                             '--up-delay-ms', sys.argv[2], '--up-rate-bps', sys.argv[3]],
                            stdout=subprocess.DEVNULL, stderr=subprocess.STDOUT)
    await asyncio.sleep(2)
    t0 = time.monotonic()
    reader, writer = await asyncio.open_connection('127.0.0.1', 29002)
    writer.write(b'x' * TARGET)
    try:
        await asyncio.wait_for(done.wait(), timeout=90)
    except asyncio.TimeoutError:
        pass
    print('delay=%sms cap=%sB/s: first byte %.3f s, %d bytes complete %.3f s'
          % (sys.argv[2], sys.argv[3], stats.get('first', 0) - t0,
             stats.get('total', 0), stats.get('last', 0) - t0))
    writer.close()
    proc.terminate()
    server.close()

asyncio.run(main())
