#!/usr/bin/env python3
"""Probe MCP STDIO server: initialize + list_connections."""

import json
import os
import subprocess
import sys
import threading
import time

JAR = os.path.join(os.path.dirname(__file__), "..", "server", "build", "libs", "1c-data-mcp-server.jar")


def read_lines(proc, out_lines):
    for line in proc.stdout:
        line = line.rstrip("\r\n")
        if line:
            out_lines.append(line)


def main():
    env = os.environ.copy()
    env.setdefault("ONEC_USER", "datamcp")
    env.setdefault("ONEC_PASSWORD", "1")

    proc = subprocess.Popen(
        ["java", "-jar", os.path.abspath(JAR)],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        bufsize=1,
        env=env,
    )

    out_lines = []
    reader = threading.Thread(target=read_lines, args=(proc, out_lines), daemon=True)
    reader.start()

    messages = [
        {
            "jsonrpc": "2.0",
            "id": 1,
            "method": "initialize",
            "params": {
                "protocolVersion": "2025-11-25",
                "capabilities": {},
                "clientInfo": {"name": "probe", "version": "1.0"},
            },
        },
        {"jsonrpc": "2.0", "method": "notifications/initialized", "params": {}},
    ]

    for msg in messages:
        proc.stdin.write(json.dumps(msg) + "\n")
    proc.stdin.flush()

    time.sleep(3)

    list_tools = {"jsonrpc": "2.0", "id": 2, "method": "tools/list", "params": {}}
    proc.stdin.write(json.dumps(list_tools) + "\n")
    proc.stdin.flush()
    time.sleep(2)

    call = {
        "jsonrpc": "2.0",
        "id": 3,
        "method": "tools/call",
        "params": {"name": "list_connections", "arguments": {}},
    }
    proc.stdin.write(json.dumps(call) + "\n")
    proc.stdin.flush()

    deadline = time.time() + 45
    while time.time() < deadline:
        for line in out_lines:
            if '"id":3' in line or '"id": 3' in line:
                print(line)
                proc.kill()
                return 0
        time.sleep(0.2)

    print("Timeout waiting for tools/call response", file=sys.stderr)
    print("Captured stdout lines:", file=sys.stderr)
    for line in out_lines:
        if line.startswith("{"):
            print(line, file=sys.stderr)
    proc.kill()
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
