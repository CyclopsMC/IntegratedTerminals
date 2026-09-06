#!/usr/bin/env python3
"""Minimal Minecraft RCON client, so scenarios can be driven from the shell."""
import socket
import struct
import sys

HOST, PORT, PASSWORD = '127.0.0.1', 25575, 'metrics'


def send(sock, req_id, req_type, body):
    payload = struct.pack('<ii', req_id, req_type) + body.encode('utf8') + b'\x00\x00'
    sock.sendall(struct.pack('<i', len(payload)) + payload)


def recv(sock):
    raw = sock.recv(4)
    if len(raw) < 4:
        raise ConnectionError('short read')
    length = struct.unpack('<i', raw)[0]
    data = b''
    while len(data) < length:
        chunk = sock.recv(length - len(data))
        if not chunk:
            raise ConnectionError('closed')
        data += chunk
    req_id, req_type = struct.unpack('<ii', data[:8])
    return req_id, req_type, data[8:-2].decode('utf8', 'replace')


def run(commands):
    with socket.create_connection((HOST, PORT), timeout=120) as sock:
        send(sock, 1, 3, PASSWORD)
        req_id, _, _ = recv(sock)
        if req_id == -1:
            raise SystemExit('rcon auth failed')
        out = []
        for i, command in enumerate(commands):
            send(sock, 10 + i, 2, command)
            _, _, body = recv(sock)
            out.append(body)
        return out


if __name__ == '__main__':
    for line in run(sys.argv[1:]):
        print(line)
